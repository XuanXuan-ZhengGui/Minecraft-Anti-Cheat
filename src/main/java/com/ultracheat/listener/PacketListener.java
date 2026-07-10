package com.ultracheat.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.ultracheat.UltraAntiCheat;
import com.ultracheat.checks.impl.combat.VelocityCheck;
import com.ultracheat.player.PlayerData;
import org.bukkit.entity.Player;

public class PacketListener {

    private final UltraAntiCheat plugin;
    private ProtocolManager protocolManager;

    public PacketListener(UltraAntiCheat plugin) {
        this.plugin = plugin;
        if (ProtocolLibrary.getProtocolManager() == null) {
            plugin.getLogger().warning("ProtocolLib 未找到，数据包检测将受限");
            return;
        }
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        register();
    }

    private void register() {
        // 监听所有移动数据包来统计频率
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Client.POSITION, PacketType.Play.Client.POSITION_LOOK,
                PacketType.Play.Client.LOOK, PacketType.Play.Client.FLYING) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player p = event.getPlayer();
                if (p == null) return;
                PlayerData data = plugin.getCheckManager().getPlayerData(p);
                data.setPacketCount(data.getPacketCount() + 1);
                data.setLastPacketTime(System.currentTimeMillis());
            }
        });

        // 监听攻击数据包
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player p = event.getPlayer();
                if (p == null) return;
                PlayerData data = plugin.getCheckManager().getPlayerData(p);
                data.getClickTimes().add(System.currentTimeMillis());
            }
        });

        plugin.getLogger().info("数据包监听器已注册");
    }
}
