package com.ultracheat.manager;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.Check;
import com.ultracheat.api.CheckResult;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CheckManager {
    private final UltraAntiCheat plugin;
    private final ConfigManager config;
    private final AlertManager alertManager;
    private final PunishmentManager punishmentManager;
    private final List<Check> checks = new ArrayList<>();
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public CheckManager(UltraAntiCheat plugin, ConfigManager config,
                        AlertManager alertManager, PunishmentManager punishmentManager) {
        this.plugin = plugin;
        this.config = config;
        this.alertManager = alertManager;
        this.punishmentManager = punishmentManager;
    }

    public void registerCheck(Check check) {
        checks.add(check);
    }

    public List<Check> getChecks() { return new ArrayList<>(checks); }

    public void runChecks(Player player) {
        if (!config.isEnabled()) return;
        if (player.hasPermission("uac.bypass")) return;

        PlayerData data = getPlayerData(player);
        data.update();

        for (Check check : checks) {
            if (!check.isEnabled()) continue;
            try {
                CheckResult result = check.runCheck(player);
                if (result != null && result.isFailed()) {
                    data.addViolation(result);
                    alertManager.broadcast(result);
                    if (result.getConfidence() >= config.getConfidenceThreshold()) {
                        punishmentManager.process(result);
                    }
                }
            } catch (Exception e) {
                if (config.isDebug()) {
                    plugin.getLogger().warning("检测异常 [" + check.getName() + "]: " + e.getMessage());
                }
            }
        }
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData(player));
    }

    public void removePlayer(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public double getViolationScore(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        return data == null ? 0.0 : data.getTotalViolationLevel();
    }

    public void clear() {
        checks.clear();
        playerDataMap.clear();
    }
}
