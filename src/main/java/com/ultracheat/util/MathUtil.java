package com.ultracheat.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class MathUtil {

    public static double getHorizontalDistance(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx*dx + dz*dz);
    }

    public static double getVerticalDistance(Location a, Location b) {
        return Math.abs(a.getY() - b.getY());
    }

    public static double getDistance(Location a, Location b) {
        return a.distance(b);
    }

    public static double getGcd(double a, double b) {
        if (b < 0.0001) return a;
        return getGcd(b, a % b);
    }

    public static long getGcd(long a, long b) {
        if (b == 0) return a;
        return getGcd(b, a % b);
    }

    public static double getGcdOfDoubles(double a, double b) {
        long expand = (long) Math.pow(2, 24);
        return getGcd((long)(a * expand), (long)(b * expand)) / (double) expand;
    }

    public static float getAngleDifference(float a, float b) {
        float d = Math.abs(a - b) % 360;
        return d > 180 ? 360 - d : d;
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static double getStandardDeviation(double[] values) {
        if (values.length == 0) return 0;
        double mean = 0;
        for (double v : values) mean += v;
        mean /= values.length;
        double variance = 0;
        for (double v : values) variance += (v - mean) * (v - mean);
        variance /= values.length;
        return Math.sqrt(variance);
    }

    public static double getKurtosis(double[] values) {
        if (values.length < 4) return 0;
        double mean = 0;
        for (double v : values) mean += v;
        mean /= values.length;
        double variance = 0;
        for (double v : values) variance += (v - mean) * (v - mean);
        variance /= values.length;
        if (variance == 0) return 0;
        double m4 = 0;
        for (double v : values) m4 += Math.pow(v - mean, 4);
        m4 /= values.length;
        return m4 / (variance * variance) - 3;
    }
}
