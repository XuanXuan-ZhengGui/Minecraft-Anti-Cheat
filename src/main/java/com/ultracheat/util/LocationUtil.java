package com.ultracheat.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class LocationUtil {

    public static Vector getDirection(Location loc) {
        double yaw = Math.toRadians(loc.getYaw());
        double pitch = Math.toRadians(loc.getPitch());
        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);
        return new Vector(x, y, z);
    }

    public static Location getEyeLocation(Location loc) {
        return loc.clone().add(0, 1.62, 0);
    }

    public static double getReachDistance(Location attacker, Location target) {
        Location eye = getEyeLocation(attacker);
        double dx = Math.abs(eye.getX() - target.getX());
        double dy = Math.abs(eye.getY() - target.getY());
        double dz = Math.abs(eye.getZ() - target.getZ());
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public static boolean isSameBlock(Location a, Location b) {
        return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }
}
