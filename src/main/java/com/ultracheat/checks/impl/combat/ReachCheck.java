package com.ultracheat.checks.impl.combat;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.api.CheckResult;
import com.ultracheat.checks.AbstractCheck;
import com.ultracheat.manager.ConfigManager;
import com.ultracheat.util.LocationUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public class ReachCheck extends AbstractCheck {

    public ReachCheck(UltraAntiCheat plugin, ConfigManager config) {
        super(plugin, config, "reach", "COMBAT");
    }

    @Override
    @SuppressWarnings("unchecked")
    public CheckResult runCheck(Player p) {
        // 兼容 1.20.X 和 1.21.X: 使用 getNearbyEntities 替代 getTargetEntity
        Entity target = null;
        double minAngle = Double.MAX_VALUE;

        Vector eyeDir = LocationUtil.getDirection(p.getLocation());
        Location eyeLoc = LocationUtil.getEyeLocation(p.getLocation());

        Collection<Entity> nearby = p.getWorld().getNearbyEntities(
                eyeLoc, 6.0, 6.0, 6.0);

        for (Entity entity : nearby) {
            if (entity.equals(p) || !(entity instanceof LivingEntity)) continue;
            if (entity.isDead()) continue;

            Vector toEntity = entity.getLocation().clone()
                    .add(0, ((LivingEntity) entity).getHeight() / 2, 0)
                    .subtract(eyeLoc).toVector();
            double distance = toEntity.length();
            if (distance > 6.0) continue;

            toEntity.normalize();
            double angle = eyeDir.angle(toEntity);

            // 只考虑玩家视野锥范围内的实体（约 90 度）
            if (angle < Math.toRadians(60) && angle < minAngle) {
                minAngle = angle;
                target = entity;
            }
        }

        if (target == null || !(target instanceof LivingEntity)) return CheckResult.pass();

        Location eye = LocationUtil.getEyeLocation(p.getLocation());
        Location targetCenter = target.getLocation().clone().add(0, ((LivingEntity) target).getHeight() / 2, 0);
        double distance = eye.distance(targetCenter);

        double maxReach = config.getCheckConfig("reach", "max-reach", 3.15);
        if (p.getGameMode() == GameMode.CREATIVE) maxReach = 5.0;

        // Ping补偿
        if (config.getCheckConfig("reach", "ping-compensation", 1.0) > 0) {
            maxReach += p.getPing() / 1000.0 * 0.1;
        }

        if (distance > maxReach) {
            return fail(p, String.format("攻击距离: %.2f (上限: %.2f)", distance, maxReach), 1.5, 0.8);
        }
        return CheckResult.pass();
    }
}
