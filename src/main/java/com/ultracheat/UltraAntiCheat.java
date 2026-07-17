package com.ultracheat;

import com.ultracheat.api.UACAPI;
import com.ultracheat.checks.impl.*;
import com.ultracheat.checks.impl.block.*;
import com.ultracheat.checks.impl.combat.*;
import com.ultracheat.checks.impl.movement.*;
import com.ultracheat.checks.impl.packet.*;
import com.ultracheat.command.UACCommand;
import com.ultracheat.integration.*;
import com.ultracheat.manager.*;
import com.ultracheat.web.WebDashboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import com.ultracheat.player.PlayerData;

public class UltraAntiCheat extends JavaPlugin implements Listener {

    private static UltraAntiCheat instance;
    private ConfigManager configManager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;
    private CheckManager checkManager;
    private WebDashboard webDashboard;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Init managers
        this.configManager = new ConfigManager(this);
        this.alertManager = new AlertManager(this, configManager);
        this.punishmentManager = new PunishmentManager(this, configManager);
        this.checkManager = new CheckManager(this, configManager, alertManager, punishmentManager);

        // Register checks
        registerChecks();

        // Register bridges
        registerBridges();

        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        new com.ultracheat.listener.PacketListener(this);

        // Register command (防御性检查以避免 NPE)
        if (getCommand("uac") != null) {
            getCommand("uac").setExecutor(new UACCommand(this));
        } else {
            getLogger().warning("Command 'uac' not found in plugin.yml");
        }

        // Web dashboard
        if (configManager.isWebDashboardEnabled()) {
            this.webDashboard = new WebDashboard(this);
            webDashboard.start();
        }

        // API
        new UACAPI(this);

        // Schedule checks
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                checkManager.runChecks(p);
            }
        }, 20L, configManager.getCheckInterval());

        getLogger().info("§b[UltraAntiCheat] §a终极融合反作弊已启动!");
        getLogger().info("§b[UltraAntiCheat] §e已注册 " + checkManager.getChecks().size() + " 个检测模块");
    }

    @Override
    public void onDisable() {
        if (webDashboard != null) webDashboard.stop();
        checkManager.clear();
        punishmentManager.clear();
    }

    private void registerChecks() {
        // Movement
        checkManager.registerCheck(new FlyCheck(this, configManager));
        checkManager.registerCheck(new SpeedCheck(this, configManager));
        checkManager.registerCheck(new TimerCheck(this, configManager));
        checkManager.registerCheck(new PhaseCheck(this, configManager));
        checkManager.registerCheck(new NoSlowCheck(this, configManager));
        // Combat
        checkManager.registerCheck(new KillAuraCheck(this, configManager));
        checkManager.registerCheck(new ReachCheck(this, configManager));
        checkManager.registerCheck(new AutoClickerCheck(this, configManager));
        checkManager.registerCheck(new VelocityCheck(this, configManager));
        // Block
        checkManager.registerCheck(new ScaffoldCheck(this, configManager));
        // Packet
        checkManager.registerCheck(new BadPacketsCheck(this, configManager));
        checkManager.registerCheck(new GroundSpoofCheck(this, configManager));
        checkManager.registerCheck(new XrayCheck(this, configManager));

        // Xray 检测需要注册额外事件（防止传入 null）
        checkManager.getChecks().stream()
                .filter(c -> c instanceof com.ultracheat.checks.impl.packet.XrayCheck)
                .findFirst()
                .ifPresent(c -> getServer().getPluginManager().registerEvents((com.ultracheat.checks.impl.packet.XrayCheck) c, this));
    }

    private void registerBridges() {
        new GrimACBridge(this);
        new NCPBridge(this);
        new VulcanBridge(this);
        new MatrixBridge(this);
        new SpartanBridge(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        checkManager.getPlayerData(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        checkManager.removePlayer(e.getPlayer());
        punishmentManager.reset(e.getPlayer());
        // 清理Xray数据
        checkManager.getChecks().stream()
                .filter(c -> c instanceof com.ultracheat.checks.impl.packet.XrayCheck)
                .findFirst()
                .ifPresent(c -> ((com.ultracheat.checks.impl.packet.XrayCheck) c).cleanup(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        if (!configManager.isEnabled()) return;
        Player p = e.getPlayer();
        if (p.hasPermission("uac.bypass")) return;
        PlayerData data = checkManager.getPlayerData(p);
        data.setLastLocation(e.getFrom().clone());
        data.setLastOnGround(p.isOnGround());
        if (p.isOnGround()) {
            data.setLastGroundLocation(e.getTo().clone());
            data.setGroundTicks(data.getGroundTicks() + 1);
            data.setAirTicks(0);
        } else {
            data.setAirTicks(data.getAirTicks() + 1);
            data.setGroundTicks(0);
        }
        data.setLastYDiff(e.getTo().getY() - e.getFrom().getY());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttack(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
        // 玩家作为攻击者
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            PlayerData data = checkManager.getPlayerData(p);
            long now = System.currentTimeMillis();
            data.setLastAttackTime(now);
            data.setAttackCount(data.getAttackCount() + 1);
            if (data.getAttackWindowStart() == 0) data.setAttackWindowStart(now);
            if (now - data.getAttackWindowStart() > 1000) {
                data.setAttackCount(1);
                data.setAttackWindowStart(now);
            }
        }

        // 玩家作为受害者 → 记录期望击退（用于 VelocityCheck）
        if (e.getEntity() instanceof Player victim && e.getDamager() instanceof LivingEntity) {
            Vector expected = victim.getLocation().toVector()
                    .subtract(e.getDamager().getLocation().toVector());
            expected.setY(0);
            if (expected.lengthSquared() > 0.001) {
                // 避免链式调用导致在某些 Bukkit 版本下编译失败（setY 可能返回 void）
                expected = expected.normalize().multiply(0.4);
                expected.setY(0.4);
            } else {
                expected = new Vector(0, 0.4, 0);
            }
            checkManager.getChecks().stream()
                    .filter(c -> c instanceof VelocityCheck)
                    .findFirst()
                    .ifPresent(c -> ((VelocityCheck) c).onPlayerHit(victim, expected));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onResourcePackStatus(PlayerResourcePackStatusEvent e) {
        checkManager.getChecks().stream()
                .filter(c -> c instanceof com.ultracheat.checks.impl.packet.XrayCheck)
                .findFirst()
                .ifPresent(c -> ((com.ultracheat.checks.impl.packet.XrayCheck) c).onResourcePackStatus(e));
    }

    public static UltraAntiCheat getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public AlertManager getAlertManager() { return alertManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public CheckManager getCheckManager() { return checkManager; }
}
