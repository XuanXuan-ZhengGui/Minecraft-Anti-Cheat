package com.ultracheat.api;

import org.bukkit.entity.Player;

public interface Check {
    String getName();
    String getType();
    boolean isEnabled();
    CheckResult runCheck(Player player);
}
