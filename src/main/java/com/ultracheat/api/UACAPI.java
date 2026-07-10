package com.ultracheat.api;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.manager.CheckManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;
import java.util.List;

public class UACAPI {
    private static UACAPI instance;
    private final UltraAntiCheat plugin;

    public UACAPI(UltraAntiCheat plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static UACAPI getInstance() { return instance; }
    public UltraAntiCheat getPlugin() { return plugin; }
    public double getViolationScore(Player player) { return plugin.getCheckManager().getViolationScore(player); }
    public List<Check> getChecks() { return plugin.getCheckManager().getChecks(); }
    public PlayerData getPlayerData(Player player) { return plugin.getCheckManager().getPlayerData(player); }
}
