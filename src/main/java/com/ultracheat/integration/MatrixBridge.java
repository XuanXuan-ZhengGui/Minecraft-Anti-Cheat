package com.ultracheat.integration;

import com.ultracheat.UltraAntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class MatrixBridge {

    public MatrixBridge(UltraAntiCheat plugin) {
        Plugin matrix = Bukkit.getPluginManager().getPlugin("Matrix");
        if (matrix != null && matrix.isEnabled()) {
            plugin.getLogger().info("§a[桥接] Matrix v" + matrix.getDescription().getVersion() + " 已连接");
        } else {
            plugin.getLogger().info("§7[桥接] Matrix 未安装 (可选)");
        }
    }
}
