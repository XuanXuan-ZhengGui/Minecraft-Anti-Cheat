package com.ultracheat.checks;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.Check;
import com.ultracheat.api.CheckResult;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;

public abstract class AbstractCheck implements Check {
    protected final UltraAntiCheat plugin;
    protected final ConfigManager config;
    protected final String name;
    protected final String type;

    public AbstractCheck(UltraAntiCheat plugin, ConfigManager config, String name, String type) {
        this.plugin = plugin;
        this.config = config;
        this.name = name;
        this.type = type;
    }

    @Override public String getName() { return name; }
    @Override public String getType() { return type; }
    @Override public boolean isEnabled() { return config.isCheckEnabled(name); }

    protected PlayerData getData(Player p) {
        return plugin.getCheckManager().getPlayerData(p);
    }

    protected CheckResult fail(Player p, String desc, double vl, double confidence) {
        return CheckResult.fail(p, name, desc, vl, confidence);
    }
}
