package com.ultracheat.player;

import com.ultracheat.api.CheckResult;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {
    private final UUID uuid;
    private final String name;
    private final Map<String, Double> violationLevels = new ConcurrentHashMap<>();
    private final Map<String, Long> lastViolationTimes = new ConcurrentHashMap<>();
    private final List<CheckResult> recentViolations = Collections.synchronizedList(new ArrayList<>());

    // Movement tracking
    private Location lastLocation;
    private Location lastGroundLocation;
    private Vector lastVelocity;
    private boolean lastOnGround;
    private long lastMoveTime;
    private int groundTicks;
    private int airTicks;
    private double lastYDiff;

    // Combat tracking
    private long lastAttackTime;
    private int attackCount;
    private long attackWindowStart;
    private float lastYaw;
    private float lastPitch;
    private float yawDiff;
    private float pitchDiff;
    private final List<Double> gcdHistory = new ArrayList<>();
    private final List<Long> clickTimes = new ArrayList<>();

    // Packet tracking
    private final List<Long> packetTimes = new ArrayList<>();
    private long lastPacketTime;
    private int packetCount;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.lastLocation = player.getLocation().clone();
        this.lastGroundLocation = player.getLocation().clone();
        this.lastVelocity = new Vector(0, 0, 0);
        this.lastOnGround = player.isOnGround();
        this.lastYaw = player.getLocation().getYaw();
        this.lastPitch = player.getLocation().getPitch();
    }

    public void update() {
        // Called each check tick
    }

    public void addViolation(CheckResult result) {
        String key = result.getCheckName();
        violationLevels.merge(key, result.getViolationLevel(), Double::sum);
        lastViolationTimes.put(key, System.currentTimeMillis());
        recentViolations.add(result);
        if (recentViolations.size() > 50) recentViolations.remove(0);
    }

    public double getTotalViolationLevel() {
        return violationLevels.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public double getViolationLevel(String check) { return violationLevels.getOrDefault(check, 0.0); }
    public List<CheckResult> getRecentViolations() { return new ArrayList<>(recentViolations); }

    // Movement getters/setters
    public Location getLastLocation() { return lastLocation; }
    public void setLastLocation(Location loc) { this.lastLocation = loc; }
    public Location getLastGroundLocation() { return lastGroundLocation; }
    public void setLastGroundLocation(Location loc) { this.lastGroundLocation = loc; }
    public Vector getLastVelocity() { return lastVelocity; }
    public void setLastVelocity(Vector v) { this.lastVelocity = v; }
    public boolean isLastOnGround() { return lastOnGround; }
    public void setLastOnGround(boolean v) { this.lastOnGround = v; }
    public long getLastMoveTime() { return lastMoveTime; }
    public void setLastMoveTime(long t) { this.lastMoveTime = t; }
    public int getGroundTicks() { return groundTicks; }
    public void setGroundTicks(int t) { this.groundTicks = t; }
    public int getAirTicks() { return airTicks; }
    public void setAirTicks(int t) { this.airTicks = t; }
    public double getLastYDiff() { return lastYDiff; }
    public void setLastYDiff(double d) { this.lastYDiff = d; }

    // Combat getters/setters
    public long getLastAttackTime() { return lastAttackTime; }
    public void setLastAttackTime(long t) { this.lastAttackTime = t; }
    public int getAttackCount() { return attackCount; }
    public void setAttackCount(int c) { this.attackCount = c; }
    public long getAttackWindowStart() { return attackWindowStart; }
    public void setAttackWindowStart(long t) { this.attackWindowStart = t; }
    public float getLastYaw() { return lastYaw; }
    public void setLastYaw(float y) { this.lastYaw = y; }
    public float getLastPitch() { return lastPitch; }
    public void setLastPitch(float p) { this.lastPitch = p; }
    public float getYawDiff() { return yawDiff; }
    public void setYawDiff(float d) { this.yawDiff = d; }
    public float getPitchDiff() { return pitchDiff; }
    public void setPitchDiff(float d) { this.pitchDiff = d; }
    public List<Double> getGcdHistory() { return gcdHistory; }
    public List<Long> getClickTimes() { return clickTimes; }

    // Packet getters/setters
    public List<Long> getPacketTimes() { return packetTimes; }
    public long getLastPacketTime() { return lastPacketTime; }
    public void setLastPacketTime(long t) { this.lastPacketTime = t; }
    public int getPacketCount() { return packetCount; }
    public void setPacketCount(int c) { this.packetCount = c; }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
}
