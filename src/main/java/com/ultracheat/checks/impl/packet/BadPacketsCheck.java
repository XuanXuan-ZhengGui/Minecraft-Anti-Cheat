package com.ultracheat.checks.impl.packet;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;

public class BadPacketsCheck extends AbstractCheck {

    public BadPacketsCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "badpackets", "PACKET");
    }

    @Override
    public CheckResult runCheck(Player p) {
        PlayerData data = getData(p);
        int count = data.getPacketCount();
        data.setPacketCount(0); // 重置计数，由PacketListener累加

        int limit = (int) config.getCheckConfig("badpackets", "packet-rate-limit", 25);
        if (count > limit) {
            return fail(p, String.format("数据包频率: %d/s (上限: %d)", count, limit), 1.0, 0.7);
        }
        return CheckResult.pass();
    }
}
