package com.ultracheat.checks.impl.packet;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;

public class GroundSpoofCheck extends AbstractCheck {

    public GroundSpoofCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "groundspoof", "PACKET");
    }

    @Override
    public CheckResult runCheck(Player p) {
        PlayerData data = getData(p);
        boolean claimsGround = p.isOnGround();
        boolean actuallyGround = data.getGroundTicks() > 0;

        if (claimsGround && !actuallyGround && data.getAirTicks() > 5) {
            return fail(p, "伪造地面状态 (空中" + data.getAirTicks() + "ticks)", 1.5, 0.8);
        }
        return CheckResult.pass();
    }
}
