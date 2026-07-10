package com.ultracheat.api;

import org.bukkit.entity.Player;

public class CheckResult {
    private final boolean failed;
    private final String checkName;
    private final String description;
    private final double violationLevel;
    private final double confidence;
    private final Player player;

    public static CheckResult pass() {
        return new CheckResult(false, null, null, 0, 0, null);
    }

    public static CheckResult fail(Player player, String checkName, String description, double vl, double confidence) {
        return new CheckResult(true, checkName, description, vl, confidence, player);
    }

    private CheckResult(boolean failed, String checkName, String description, double vl, double confidence, Player player) {
        this.failed = failed;
        this.checkName = checkName;
        this.description = description;
        this.violationLevel = vl;
        this.confidence = confidence;
        this.player = player;
    }

    public boolean isFailed() { return failed; }
    public String getCheckName() { return checkName; }
    public String getDescription() { return description; }
    public double getViolationLevel() { return violationLevel; }
    public double getConfidence() { return confidence; }
    public Player getPlayer() { return player; }
}
