package com.ultracheat.web;

import com.google.gson.Gson;
import com.ultracheat.UltraAntiCheat;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WebDashboard {

    private final UltraAntiCheat plugin;
    private HttpServer server;

    public WebDashboard(UltraAntiCheat plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            int port = plugin.getConfigManager().getWebDashboardPort();
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext("/", new HtmlHandler());
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/players", new PlayersHandler());
            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("§aWeb仪表盘: http://0.0.0.0:" + port);
        } catch (Exception e) {
            plugin.getLogger().warning("§cWeb仪表盘启动失败: " + e.getMessage());
        }
    }

    public void stop() { if (server != null) server.stop(0); }

    private void send(HttpExchange ex, String body, String type) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }

    class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String html = "<!DOCTYPE html><html lang='zh'><head><meta charset='UTF-8'><title>UAC监控</title>" +
            "<style>body{font-family:sans-serif;background:#0f0f1a;color:#e0e0e0;padding:20px}" +
            ".card{background:#1a1a2e;border-radius:10px;padding:15px;margin:10px 0;border:1px solid #2a2a4a}" +
            "h1{background:linear-gradient(90deg,#00d4ff,#7b2ff7);-webkit-background-clip:text;-webkit-text-fill-color:transparent}" +
            "table{width:100%;border-collapse:collapse;background:#1a1a2e;border-radius:10px;overflow:hidden}" +
            "th{background:#0f3460;padding:10px;text-align:left;color:#88ccff}" +
            "td{padding:8px 10px;border-bottom:1px solid #2a2a4a}" +
            ".v{font-size:24px;font-weight:700;color:#00d4ff}" +
            "</style></head><body><h1>UltraAntiCheat 监控</h1><div class='card'><div id='stats'></div></div>" +
            "<table><thead><tr><th>玩家</th><th>Ping</th><th>违规分</th></tr></thead><tbody id='players'></tbody></table>" +
            "<script>function load(){fetch('/api/players').then(r=>r.json()).then(d=>{" +
            "document.getElementById('stats').innerHTML='<div style=\"display:grid;grid-template-columns:repeat(4,1fr);gap:15px\">'" +
            "+'<div><div style=\"font-size:11px;color:#888\">在线</div><div class=\"v\">'+d.players+'</div></div>'" +
            "+'<div><div style=\"font-size:11px;color:#888\">检测</div><div class=\"v\">'+d.checks+'</div></div>'" +
            "+'<div><div style=\"font-size:11px;color:#888\">状态</div><div class=\"v\" style=\"color:#0f0\">运行中</div></div>'" +
            "+'</div>';" +
            "document.getElementById('players').innerHTML=d.list.length===0?" +
            "'<tr><td colspan=3 style=\"text-align:center;color:#666\">无</td></tr>':" +
            "d.list.map(p=>'<tr><td><b>'+p.name+'</b></td><td>'+p.ping+'ms</td><td>'+p.score+'</td></tr>').join('')" +
            "});}load();setInterval(load,3000);</script></body></html>";
            send(ex, html, "text/html");
        }
    }

    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            Map<String, Object> d = new HashMap<>();
            d.put("players", Bukkit.getOnlinePlayers().size());
            d.put("checks", plugin.getCheckManager().getChecks().size());
            d.put("enabled", plugin.getConfigManager().isEnabled());
            send(ex, new Gson().toJson(d), "application/json");
        }
    }

    class PlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            Map<String, Object> d = new HashMap<>();
            d.put("players", Bukkit.getOnlinePlayers().size());
            d.put("checks", plugin.getCheckManager().getChecks().size());
            List<Map<String, Object>> list = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", p.getName());
                m.put("ping", p.getPing());
                m.put("score", String.format("%.2f", plugin.getCheckManager().getViolationScore(p)));
                list.add(m);
            }
            d.put("list", list);
            send(ex, new Gson().toJson(d), "application/json");
        }
    }
}
