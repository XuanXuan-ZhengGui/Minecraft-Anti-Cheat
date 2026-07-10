package com.ultracheat.command;

import com.ultracheat.UltraAntiCheat;
import com.ultracheat.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UACCommand implements CommandExecutor {

    private final UltraAntiCheat plugin;

    public UACCommand(UltraAntiCheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) { showHelp(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "help": showHelp(sender); break;
            case "reload":
                plugin.getConfigManager().reload();
                sender.sendMessage("§a[UAC] 配置已重载");
                break;
            case "alerts":
                if (!(sender instanceof Player)) { sender.sendMessage("§c仅玩家可用"); return true; }
                Player p = (Player) sender;
                if (plugin.getAlertManager().hasAlerts(p)) {
                    plugin.getAlertManager().removeAlertPlayer(p);
                    p.sendMessage("§c[UAC] 告警已关闭");
                } else {
                    plugin.getAlertManager().addAlertPlayer(p);
                    p.sendMessage("§a[UAC] 告警已开启");
                }
                break;
            case "info":
                if (args.length < 2) { sender.sendMessage("§c用法: /uac info <玩家>"); return true; }
                showInfo(sender, args[1]);
                break;
            case "status":
                sender.sendMessage("§8§m+------------------------------+");
                sender.sendMessage("§b§l UltraAntiCheat §7- 系统状态");
                sender.sendMessage("§8§m+------------------------------+");
                sender.sendMessage("§7检测引擎: " + (plugin.getConfigManager().isEnabled() ? "§a运行中" : "§c已暂停"));
                sender.sendMessage("§7检测模块: §f" + plugin.getCheckManager().getChecks().size());
                sender.sendMessage("§7在线玩家: §f" + Bukkit.getOnlinePlayers().size());
                sender.sendMessage("§7配置状态: §f已加载");
                sender.sendMessage("§8§m+------------------------------+");
                break;
            case "reset":
                if (args.length < 2) { sender.sendMessage("§c用法: /uac reset <玩家>"); return true; }
                Player t = Bukkit.getPlayer(args[1]);
                if (t == null) { sender.sendMessage("§c玩家不在线"); return true; }
                plugin.getPunishmentManager().reset(t);
                sender.sendMessage("§a[UAC] 已重置 " + t.getName() + " 的违规记录");
                break;
            default:
                sender.sendMessage("§c未知命令");
                showHelp(sender);
        }
        return true;
    }

    private void showHelp(CommandSender s) {
        s.sendMessage("§8§m+------------------------------+");
        s.sendMessage("§b§l UltraAntiCheat §7- 帮助");
        s.sendMessage("§8§m+------------------------------+");
        s.sendMessage("§e/uac alerts §7- 切换告警");
        s.sendMessage("§e/uac info <玩家> §7- 查看玩家信息");
        s.sendMessage("§e/uac status §7- 系统状态");
        s.sendMessage("§e/uac reset <玩家> §7- 重置违规");
        s.sendMessage("§e/uac reload §7- 重载配置");
        s.sendMessage("§8§m+------------------------------+");
    }

    private void showInfo(CommandSender s, String name) {
        Player p = Bukkit.getPlayer(name);
        if (p == null) { s.sendMessage("§c玩家不在线"); return; }
        PlayerData d = plugin.getCheckManager().getPlayerData(p);
        s.sendMessage("§8§m+------------------------------+");
        s.sendMessage("§b§l 玩家信息: §f" + p.getName());
        s.sendMessage("§8§m+------------------------------+");
        s.sendMessage("§7综合违规分: §f" + String.format("%.2f", plugin.getCheckManager().getViolationScore(p)));
        s.sendMessage("§7Ping: §f" + p.getPing() + "ms");
        s.sendMessage("§e最近违规:");
        d.getRecentViolations().forEach(v ->
            s.sendMessage("  §7" + v.getCheckName() + " §8| §f" + v.getDescription())
        );
        s.sendMessage("§8§m+------------------------------+");
    }
}
