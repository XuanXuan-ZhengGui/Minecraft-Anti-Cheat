package com.ultracheat.integration;

import com.ultracheat.UltraAntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class GrimACBridge {

    private final UltraAntiCheat plugin;

    public GrimACBridge(UltraAntiCheat plugin) {
        this.plugin = plugin;
        Plugin grim = Bukkit.getPluginManager().getPlugin("GrimAC");
        if (grim == null) grim = Bukkit.getPluginManager().getPlugin("Grim");
        if (grim != null && grim.isEnabled()) {
            plugin.getLogger().info("§a[桥接] GrimAC v" + grim.getDescription().getVersion() + " 已连接");
        } else {
            plugin.getLogger().info("§7[桥接] GrimAC 未安装 (可选)");
        }
    }
}
