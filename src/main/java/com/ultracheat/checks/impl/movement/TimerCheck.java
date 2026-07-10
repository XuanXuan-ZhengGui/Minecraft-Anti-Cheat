package com.ultracheat.checks.impl.movement;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;

public class TimerCheck extends AbstractCheck {

    public TimerCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "timer", "MOVEMENT");
    }

    @Override
    public CheckResult runCheck(Player p) {
        PlayerData data = getData(p);
        long now = System.currentTimeMillis();
        long last = data.getLastPacketTime();
        data.setLastPacketTime(now);
        if (last == 0) return CheckResult.pass();

        long diff = now - last;
        if (diff < 40) { // 正常tick间隔50ms，如果小于40ms说明加速
            return fail(p, String.format("数据包间隔过短: %dms", diff), 1.0, 0.65);
        }
        return CheckResult.pass();
    }
}
