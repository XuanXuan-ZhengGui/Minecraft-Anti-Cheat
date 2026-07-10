package com.ultracheat.manager;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {
    private final UltraAntiCheat plugin;
    private final ConfigManager config;
    private final Map<UUID, Double> violationScores = new ConcurrentHashMap<>();

    public PunishmentManager(UltraAntiCheat plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void process(CheckResult result) {
        if (!config.isPunishmentEnabled()) return;
        Player p = result.getPlayer();
        UUID uuid = p.getUniqueId();

        double current = violationScores.getOrDefault(uuid, 0.0);
        current += result.getViolationLevel() * result.getConfidence();
        current *= config.getViolationDecay();
        violationScores.put(uuid, current);

        if (current >= config.getPunishmentThreshold()) {
            execute(p, result);
            violationScores.put(uuid, 0.0);
        }
    }

    private void execute(Player p, CheckResult result) {
        String cmd = config.getPunishmentCommand()
                .replace("%player%", p.getName())
                .replace("%check%", result.getCheckName())
                .replace("%reason%", result.getDescription())
                .replace("%confidence%", String.format("%.1f", result.getConfidence() * 100));
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        });
    }

    public double getScore(Player p) { return violationScores.getOrDefault(p.getUniqueId(), 0.0); }
    public void reset(Player p) { violationScores.remove(p.getUniqueId()); }
    public void clear() { violationScores.clear(); }
}
