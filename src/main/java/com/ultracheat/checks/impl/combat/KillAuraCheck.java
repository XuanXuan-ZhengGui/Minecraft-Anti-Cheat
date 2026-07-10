package com.ultracheat.checks.impl.combat;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import com.ultracheat.util.MathUtil;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class KillAuraCheck extends AbstractCheck {

    public KillAuraCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "killaura", "COMBAT");
    }

    @Override
    public CheckResult runCheck(Player p) {
        PlayerData data = getData(p);
        float yaw = p.getLocation().getYaw();
        float pitch = p.getLocation().getPitch();
        float yawDiff = MathUtil.getAngleDifference(yaw, data.getLastYaw());
        float pitchDiff = MathUtil.getAngleDifference(pitch, data.getLastPitch());
        data.setYawDiff(yawDiff);
        data.setPitchDiff(pitchDiff);
        data.setLastYaw(yaw);
        data.setLastPitch(pitch);

        // GCD 分析 - KillAura 常产生固定GCD的旋转
        List<Double> gcdHistory = data.getGcdHistory();
        if (yawDiff > 0.1 && pitchDiff > 0.1) {
            double gcd = MathUtil.getGcdOfDoubles(yawDiff, pitchDiff);
            if (gcd > 0.001) {
                gcdHistory.add(gcd);
                if (gcdHistory.size() > config.getCheckConfig("killaura", "gcd-sample-size", 20)) {
                    gcdHistory.remove(0);
                }
            }
        }

        if (gcdHistory.size() >= 5) {
            // 检测GCD是否过于一致（人类玩家的GCD应该有一定随机性）
            double first = gcdHistory.get(0);
            boolean consistent = true;
            for (int i = 1; i < gcdHistory.size(); i++) {
                if (Math.abs(gcdHistory.get(i) - first) > 0.001) {
                    consistent = false;
                    break;
                }
            }
            if (consistent && yawDiff > 5) {
                return fail(p, "GCD过于一致 (疑似KillAura)", 2.0, 0.85);
            }
        }

        // 旋转模式检测 - KillAura 常水平旋转时俯仰角不变
        if (yawDiff > 15 && pitchDiff < 0.1 && data.getAttackCount() > 0) {
            return fail(p, "水平旋转时俯仰角不变", 1.5, 0.7);
        }

        // 攻击频率异常
        int attacks = data.getAttackCount();
        long window = System.currentTimeMillis() - data.getAttackWindowStart();
        if (window > 0 && window < 1000 && attacks > 15) {
            return fail(p, String.format("攻击频率过高: %d次/%.1fs", attacks, window/1000.0), 1.5, 0.75);
        }

        return CheckResult.pass();
    }
}
