package com.ultracheat.checks.impl.combat;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VelocityCheck extends AbstractCheck {

    private final Map<UUID, Vector> expectedVelocities = new ConcurrentHashMap<>();
    private final Map<UUID, Long> hitTimes = new ConcurrentHashMap<>();

    public VelocityCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "velocity", "COMBAT");
    }

    public void onPlayerHit(Player p, Vector velocity) {
        expectedVelocities.put(p.getUniqueId(), velocity);
        hitTimes.put(p.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public CheckResult runCheck(Player p) {
        Long hitTime = hitTimes.get(p.getUniqueId());
        if (hitTime == null || System.currentTimeMillis() - hitTime > 2000) return CheckResult.pass();

        Vector expected = expectedVelocities.get(p.getUniqueId());
        if (expected == null) return CheckResult.pass();

        Vector actual = p.getVelocity();
        double rx = expected.getX() != 0 ? Math.abs(actual.getX() / expected.getX()) : 1;
        double ry = expected.getY() != 0 ? Math.abs(actual.getY() / expected.getY()) : 1;
        double rz = expected.getZ() != 0 ? Math.abs(actual.getZ() / expected.getZ()) : 1;

        double min = Math.min(Math.min(rx, ry), rz);
        double threshold = config.getCheckConfig("velocity", "ratio-threshold", 0.3);
        if (min < threshold) {
            return fail(p, String.format("击退比率: %.0f%%", min * 100), 1.5, 0.85);
        }
        return CheckResult.pass();
    }
}
