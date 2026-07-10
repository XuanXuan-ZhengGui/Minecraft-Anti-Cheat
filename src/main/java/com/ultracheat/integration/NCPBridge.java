package com.ultracheat.integration;

import com.ultracheat.UltraAntiCheat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class NCPBridge {

    public NCPBridge(UltraAntiCheat plugin) {
        Plugin ncp = Bukkit.getPluginManager().getPlugin("NoCheatPlus");
        if (ncp != null && ncp.isEnabled()) {
            plugin.getLogger().info("§a[桥接] NoCheatPlus v" + ncp.getDescription().getVersion() + " 已连接");
        } else {
            plugin.getLogger().info("§7[桥接] NoCheatPlus 未安装 (可选)");
        }
    }
}
