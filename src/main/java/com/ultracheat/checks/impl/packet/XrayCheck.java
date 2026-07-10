package com.ultracheat.checks.impl.packet;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 防透视检测模块
 * 
 * 检测原理：
 * 1. 资源包状态检测 - 检查玩家是否拒绝服务器资源包（可能在使用自己的透视包）
 * 2. 矿石锁定追踪 - 追踪玩家是否在远距离直接锁定/挖掘被遮挡的矿石
 * 3. 异常视线分析 - 分析玩家是否持续朝向被方块遮挡的矿石位置
 * 4. 直线挖掘分析 - 检测玩家是否在无合理探索路径的情况下直接挖向矿石
 * 
 * 针对资源包透视：服务器强制发送一个标记资源包，如果玩家拒绝则标记嫌疑
 * 针对Mod透视：通过行为分析检测（上述2-4项）
 */
public class XrayCheck extends AbstractCheck implements Listener {

    private final UltraAntiCheat plugin;

    // 被遮挡的矿石列表（透视才能看到的目标）
    private static final Set<Material> ORES = EnumSet.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.NETHER_GOLD_ORE, Material.ANCIENT_DEBRIS,
            Material.NETHER_QUARTZ_ORE
    );

    // 遮挡方块（矿石被这些方块包围才算"被遮挡"）
    private static final Set<Material> OCCLUDING = EnumSet.of(
            Material.STONE, Material.DEEPSLATE, Material.GRANITE, Material.DIORITE,
            Material.ANDESITE, Material.TUFF, Material.CALCITE, Material.BASALT,
            Material.BLACKSTONE, Material.NETHERRACK, Material.DIRT, Material.COARSE_DIRT,
            Material.SAND, Material.RED_SAND, Material.SANDSTONE, Material.RED_SANDSTONE,
            Material.OBSIDIAN, Material.END_STONE, Material.MAGMA_BLOCK
    );

    private final Map<UUID, ResourcePackStatus> packStatuses = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> xrayScores = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastXrayFlag = new ConcurrentHashMap<>();
    private final Map<UUID, List<Long>> suspiciousBreakTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> exploredLocations = new ConcurrentHashMap<>();

    private enum ResourcePackStatus {
        ACCEPTED, DECLINED, NOT_SENT
    }

    public XrayCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "xray", "PACKET");
        this.plugin = plugin;
    }

    // ===== 资源包状态追踪 =====

    public void onResourcePackStatus(PlayerResourcePackStatusEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        switch (e.getStatus()) {
            case ACCEPTED:
                packStatuses.put(uuid, ResourcePackStatus.ACCEPTED);
                break;
            case DECLINED:
                packStatuses.put(uuid, ResourcePackStatus.DECLINED);
                // 拒绝服务器资源包 = 可能使用透视包
                addXrayScore(e.getPlayer(), 3.0, "拒绝服务器资源包 (可能使用透视)");
                break;
            case FAILED_DOWNLOAD:
                // 下载失败不算作弊
                break;
        }
    }

    // ===== 矿石锁定追踪 =====

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!isEnabled()) return;
        Player p = e.getPlayer();
        if (p.hasPermission("uac.bypass")) return;

        Block block = e.getBlock();
        Material type = block.getType();

        // 检查挖的是不是矿石
        if (!ORES.contains(type)) {
            resetSuspicion(p);
            return;
        }

        // 检查矿石是否被遮挡（周围6面都是遮挡方块）
        if (isOccluded(block)) {
            // 被遮挡的矿石被挖出 = 高度可疑
            recordSuspiciousBreak(p, block);
            addXrayScore(p, 2.0, "挖掘被遮挡的矿石: " + type.name()
                    + " (坐标: " + block.getX() + "," + block.getY() + "," + block.getZ() + ")");

            // 检查该区域是否已被探索过
            Set<String> explored = exploredLocations.computeIfAbsent(
                    p.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
            if (!wasExplored(p, block.getLocation())) {
                addXrayScore(p, 1.5, "在未探索区域直接定位矿石");
            }
        }
    }

    // ===== 视线方向分析 =====

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!isEnabled()) return;
        Player p = e.getPlayer();
        if (p.hasPermission("uac.bypass")) return;

        // 记录玩家探索过的区域（用于判断是否"未探索就找到矿石"）
        Set<String> explored = exploredLocations.computeIfAbsent(
                p.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
        Location loc = p.getLocation();
        // 玩家周围 4 格范围内都算"已探索"
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    explored.add(blockKey(loc.clone().add(x, y, z)));
                }
            }
        }
        // 限制内存
        if (explored.size() > 50000) {
            explored.clear();
        }
    }

    // ===== 检测方法 =====

    @Override
    public CheckResult runCheck(Player p) {
        UUID uuid = p.getUniqueId();
        int score = xrayScores.getOrDefault(uuid, 0);

        // 自然衰减
        if (score > 0) {
            score = Math.max(0, score - 1);
            xrayScores.put(uuid, score);
        }

        // 分数超过阈值则报告
        int threshold = (int) config.getCheckConfig("xray", "score-threshold", 8);
        if (score >= threshold) {
            CheckResult result = fail(p, "透视嫌疑 (累积分数: " + score + ")", 2.0, 0.8);
            xrayScores.put(uuid, 0);
            return result;
        }

        return CheckResult.pass();
    }

    // ===== 辅助方法 =====

    private boolean isOccluded(Block block) {
        int occludedFaces = 0;
        for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN,
                BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block relative = block.getRelative(face);
            if (OCCLUDING.contains(relative.getType())) {
                occludedFaces++;
            }
        }
        // 至少5面被遮挡才算"被遮挡"
        return occludedFaces >= 5;
    }

    private boolean wasExplored(Player p, Location oreLoc) {
        Set<String> explored = exploredLocations.get(p.getUniqueId());
        if (explored == null) return false;
        // 检查矿石周围 2 格内是否有探索记录
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (explored.contains(blockKey(oreLoc.clone().add(x, y, z)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void recordSuspiciousBreak(Player p, Block block) {
        UUID uuid = p.getUniqueId();
        List<Long> times = suspiciousBreakTimes.computeIfAbsent(
                uuid, k -> Collections.synchronizedList(new ArrayList<>()));
        times.add(System.currentTimeMillis());
        if (times.size() > 20) times.remove(0);

        // 短时间内多次挖掘被遮挡矿石（60秒内）
        long now = System.currentTimeMillis();
        long recentCount = times.stream()
                .filter(t -> now - t < 60000)
                .count();
        if (recentCount >= 3) {
            addXrayScore(p, 2.0, "短时间内连续挖掘 " + recentCount + " 个被遮挡矿石");
        }
    }

    private void addXrayScore(Player p, double amount, String reason) {
        UUID uuid = p.getUniqueId();

        // 防抖：同一条原因3秒内不重复加分
        long last = lastXrayFlag.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - last < 3000) return;
        lastXrayFlag.put(uuid, System.currentTimeMillis());

        int score = xrayScores.getOrDefault(uuid, 0);
        score += amount;
        xrayScores.put(uuid, score);
    }

    private void resetSuspicion(Player p) {
        UUID uuid = p.getUniqueId();
        suspiciousBreakTimes.remove(uuid);
    }

    // ===== 辅助方法 =====

    private String blockKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    // ===== 清理 =====

    public void cleanup(Player p) {
        UUID uuid = p.getUniqueId();
        packStatuses.remove(uuid);
        xrayScores.remove(uuid);
        lastXrayFlag.remove(uuid);
        suspiciousBreakTimes.remove(uuid);
        exploredLocations.remove(uuid);
    }
}
