package com.ultracheat.checks.impl.movement;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import org.bukkit.entity.Player;

/**
 * 无减速检测 (NoSlow)
 *
 * 检测原理：
 * 玩家在食用食物、饮用药水、拉弓、使用盾牌时，移动速度应该大幅降低。
 * NoSlow 作弊允许玩家在这些动作期间保持正常移动速度。
 */
public class NoSlowCheck extends AbstractCheck {

    public NoSlowCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "noslow", "MOVEMENT");
    }

    @Override
    public CheckResult runCheck(Player p) {
        if (p.isFlying() || p.isInsideVehicle()) return CheckResult.pass();

        // 检查玩家是否正在使用物品（吃食物、喝药水、拉弓等）或格挡
        if (!p.isHandRaised() && !p.isBlocking()) return CheckResult.pass();

        double vx = Math.abs(p.getVelocity().getX());
        double vz = Math.abs(p.getVelocity().getZ());
        double speed = Math.sqrt(vx * vx + vz * vz);

        double limit = config.getCheckConfig("noslow", "speed-limit", 0.2);
        if (speed > limit) {
            return fail(p, String.format("使用物品时速度: %.3f (上限: %.3f)", speed, limit), 1.0, 0.7);
        }
        return CheckResult.pass();
    }
}
