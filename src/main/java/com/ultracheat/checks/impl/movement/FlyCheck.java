package com.ultracheat.checks.impl.movement;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FlyCheck extends AbstractCheck {

    public FlyCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "fly", "MOVEMENT");
    }

    @Override
    public CheckResult runCheck(Player p) {
        if (p.isFlying() || p.getAllowFlight() || p.isInsideVehicle() || p.isDead()) return CheckResult.pass();
        PlayerData data = getData(p);
        Location from = data.getLastLocation();
        Location to = p.getLocation();
        if (from == null || from.getWorld() != to.getWorld()) return CheckResult.pass();

        double dy = to.getY() - from.getY();
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double horizontal = Math.sqrt(dx*dx + dz*dz) * 20.0;
        double vertical = Math.abs(dy) * 20.0;
        boolean onGround = p.isOnGround();
        int airTicks = data.getAirTicks();

        // 空中垂直速度异常 (正常下落速度约 0.08-0.98/tick)
        if (!onGround && airTicks > 3) {
            if (dy > 0.0 && dy < 0.5 && vertical > 0.15) {
                return fail(p, String.format("空中上升: %.3f", dy), 1.0, 0.75);
            }
            if (dy == 0.0 && airTicks > 5) {
                return fail(p, String.format("空中悬停 %d ticks", airTicks), 1.5, 0.85);
            }
            if (horizontal > 0.6 && airTicks > 3) {
                return fail(p, String.format("空中水平移动: %.3f", horizontal), 1.0, 0.6);
            }
        }

        // 跳跃高度检测 (正常跳跃约 1.25 格)
        if (onGround && data.getLastYDiff() > 1.5) {
            return fail(p, String.format("跳跃过高: %.3f", data.getLastYDiff()), 2.0, 0.9);
        }

        // 掉落速度异常
        if (!onGround && dy < -1.5) {
            return fail(p, String.format("下落过快: %.3f", dy), 1.5, 0.8);
        }

        return CheckResult.pass();
    }
}
