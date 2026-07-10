package com.ultracheat.checks.impl.movement;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PhaseCheck extends AbstractCheck {

    public PhaseCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "phase", "MOVEMENT");
    }

    @Override
    public CheckResult runCheck(Player p) {
        if (p.isInsideVehicle() || p.isDead() || p.isFlying()) return CheckResult.pass();
        Location loc = p.getLocation();
        Block block = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();

        if (isSolid(block.getType()) && isSolid(head.getType())) {
            return fail(p, "身体位于方块内部", 2.0, 0.9);
        }
        return CheckResult.pass();
    }

    private boolean isSolid(Material m) {
        return m.isSolid() && m.isOccluding() && m != Material.BARRIER
            && m != Material.CHEST && m != Material.ENDER_CHEST
            && !m.name().contains("DOOR") && !m.name().contains("GATE")
            && !m.name().contains("TRAPDOOR");
    }
}
