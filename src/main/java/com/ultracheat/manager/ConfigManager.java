package com.ultracheat.manager;

import com.ultracheat.UltraAntiCheat;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final UltraAntiCheat plugin;
    private FileConfiguration config;

    public ConfigManager(UltraAntiCheat plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public boolean isEnabled() { return config.getBoolean("enabled", true); }
    public boolean isDebug() { return config.getBoolean("debug", false); }
    public double getConfidenceThreshold() { return config.getDouble("confidence-threshold", 0.5); }
    public double getViolationDecay() { return config.getDouble("violation-decay", 0.92); }
    public int getCheckInterval() { return config.getInt("check-interval", 4); }
    public boolean isPunishmentEnabled() { return config.getBoolean("punishment.enabled", true); }
    public double getPunishmentThreshold() { return config.getDouble("punishment.threshold", 5.0); }
    public String getPunishmentCommand() { return config.getString("punishment.command", "kick %player% %reason%"); }
    public boolean isAlertsEnabled() { return config.getBoolean("alerts.enabled", true); }
    public String getAlertPrefix() { return config.getString("alerts.prefix", "&7[&bUAC&7] "); }
    public boolean isConsoleAlerts() { return config.getBoolean("alerts.console", true); }
    public boolean isWebDashboardEnabled() { return config.getBoolean("web-dashboard.enabled", false); }
    public int getWebDashboardPort() { return config.getInt("web-dashboard.port", 25586); }
    public boolean isCheckEnabled(String check) { return config.getBoolean("checks." + check + ".enabled", true); }
    public double getCheckMaxViolation(String check) { return config.getDouble("checks." + check + ".max-violation", 15.0); }
    public double getCheckConfig(String check, String key, double def) { return config.getDouble("checks." + check + "." + key, def); }
}
