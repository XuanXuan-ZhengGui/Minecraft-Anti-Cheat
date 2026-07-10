package com.ultracheat.manager;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AlertManager {
    private final UltraAntiCheat plugin;
    private final ConfigManager config;
    private final Set<UUID> alertPlayers = new HashSet<>();

    public AlertManager(UltraAntiCheat plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void addAlertPlayer(Player p) { alertPlayers.add(p.getUniqueId()); }
    public void removeAlertPlayer(Player p) { alertPlayers.remove(p.getUniqueId()); }
    public boolean hasAlerts(Player p) { return alertPlayers.contains(p.getUniqueId()); }

    public void broadcast(CheckResult result) {
        if (!config.isAlertsEnabled()) return;
        String prefix = ChatColor.translateAlternateColorCodes('&', config.getAlertPrefix());
        String msg = prefix + String.format("&c%s &7| &f%s &7| %s &7(置信度: &e%.1f%%&7)",
                result.getPlayer().getName(), result.getCheckName(),
                result.getDescription(), result.getConfidence() * 100);
        String formatted = ChatColor.translateAlternateColorCodes('&', msg);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("uac.alerts") && alertPlayers.contains(p.getUniqueId())) {
                p.sendMessage(formatted);
            }
        }
        if (config.isConsoleAlerts()) {
            plugin.getLogger().info(ChatColor.stripColor(formatted));
        }
    }

    public void broadcast(String msg) {
        if (!config.isAlertsEnabled()) return;
        String formatted = ChatColor.translateAlternateColorCodes('&', config.getAlertPrefix() + msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("uac.alerts")) p.sendMessage(formatted);
        }
    }
}
