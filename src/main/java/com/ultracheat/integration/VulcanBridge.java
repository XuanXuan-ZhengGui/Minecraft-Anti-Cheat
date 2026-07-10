package com.ultracheat.integration;

import com.ultracheat.UltraAntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class VulcanBridge {

    public VulcanBridge(UltraAntiCheat plugin) {
        Plugin vulcan = Bukkit.getPluginManager().getPlugin("Vulcan");
        if (vulcan != null && vulcan.isEnabled()) {
            plugin.getLogger().info("§a[桥接] Vulcan v" + vulcan.getDescription().getVersion() + " 已连接");
        } else {
            plugin.getLogger().info("§7[桥接] Vulcan 未安装 (可选)");
        }
    }
}
