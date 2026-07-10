package com.ultracheat.checks.impl.combat;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class AutoClickerCheck extends AbstractCheck {

    public AutoClickerCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "autoclicker", "COMBAT");
    }

    @Override
    public CheckResult runCheck(Player p) {
        PlayerData data = getData(p);
        long now = System.currentTimeMillis();
        List<Long> clicks = data.getClickTimes();
        clicks.add(now);
        // 只保留最近1秒的数据
        clicks.removeIf(t -> now - t > 1000);

        int cps = clicks.size();
        int threshold = (int) config.getCheckConfig("autoclicker", "cps-threshold", 18);

        if (cps > threshold) {
            return fail(p, String.format("CPS过高: %d (上限: %d)", cps, threshold), 1.0, 0.75);
        }

        // 点击间隔过于一致（自动化特征）
        if (clicks.size() >= 5) {
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < clicks.size(); i++) {
                intervals.add(clicks.get(i) - clicks.get(i-1));
            }
            double stdDev = stdDev(intervals);
            if (stdDev < 5 && cps > 12) {
                return fail(p, String.format("点击间隔过于一致 (CPS:%d, 偏差:%.1fms)", cps, stdDev), 1.5, 0.7);
            }
        }

        return CheckResult.pass();
    }

    private double stdDev(List<Long> values) {
        if (values.size() < 2) return 0;
        double mean = values.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = values.stream().mapToDouble(v -> (v - mean) * (v - mean)).sum() / values.size();
        return Math.sqrt(variance);
    }
}
