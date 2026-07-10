package com.ultracheat.integration;

import com.ultracheat.UltraAntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SpartanBridge {

    public SpartanBridge(UltraAntiCheat plugin) {
        Plugin spartan = Bukkit.getPluginManager().getPlugin("Spartan");
        if (spartan != null && spartan.isEnabled()) {
            plugin.getLogger().info("§a[桥接] Spartan v" + spartan.getDescription().getVersion() + " 已连接");
        } else {
            plugin.getLogger().info("§7[桥接] Spartan 未安装 (可选)");
        }
    }
}
