package com.ultracheat.checks.impl.movement;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class SpeedCheck extends AbstractCheck {

    public SpeedCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "speed", "MOVEMENT");
    }

    @Override
    public CheckResult runCheck(Player p) {
        if (p.isFlying() || p.isInsideVehicle()) return CheckResult.pass();
        PlayerData data = getData(p);

        double vx = Math.abs(p.getVelocity().getX());
        double vz = Math.abs(p.getVelocity().getZ());
        double speed = Math.sqrt(vx*vx + vz*vz) * 20.0;

        double max = 0.28 * 20;
        if (p.isSprinting()) max *= 1.3;
        if (p.hasPotionEffect(PotionEffectType.SPEED)) {
            int amp = p.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1;
            max *= (1 + amp * 0.2);
        }

        double threshold = config.getCheckConfig("speed", "speed-multiplier", 1.5);
        if (speed > max * threshold) {
            return fail(p, String.format("速度: %.2f (上限: %.2f)", speed, max), 1.0, 0.7);
        }
        return CheckResult.pass();
    }
}
