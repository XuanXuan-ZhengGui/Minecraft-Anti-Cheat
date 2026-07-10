package com.ultracheat.checks.impl.block;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;

public class ScaffoldCheck extends AbstractCheck {

    public ScaffoldCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "scaffold", "BLOCK");
    }

    @Override
    public CheckResult runCheck(Player p) {
        if (!p.isSneaking()) return CheckResult.pass();
        float pitch = p.getLocation().getPitch();
        PlayerData data = getData(p);
        float yawDiff = data.getYawDiff();

        double threshold = config.getCheckConfig("scaffold", "rotation-threshold", 2.0);
        if (pitch > 70 && yawDiff < threshold) {
            return fail(p, String.format("俯仰角:%.1f 水平旋转:%.2f", pitch, yawDiff), 1.0, 0.65);
        }
        return CheckResult.pass();
    }
}
