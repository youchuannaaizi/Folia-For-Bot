package xyz.ycmc.mc.plugins.bot;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class YCBot extends JavaPlugin {
    private BotManager botManager;
    private static final String PLUGIN_VERSION = "1.46"; // 版本常量

    @Override
    public void onEnable() {
        // 加载阶段日志
        getLogger().info("当前版本: " + PLUGIN_VERSION);
        getLogger().info("插件加载中.........");

        // 初始化机器人管理器
        botManager = new BotManager(this);

        // 注册命令
        PluginCommand cmd = getCommand("bot");
        if (cmd == null) {
            getLogger().severe("命令注册失败，插件即将禁用");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 配置命令处理器
        cmd.setExecutor(new CommandHandler(botManager, this));
        cmd.setTabCompleter(new TabHandler(botManager));

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(new BotEventListener(botManager), this);

        // 完成加载日志
        getLogger().info("插件作者：YouChuan");
        getLogger().info("插件加载完成");
    }

    @Override
    public void onDisable() {
        // 清理所有机器人
        botManager.removeAllBots();
        getLogger().info("插件卸载完成");
    }

    // ------------------ 命令处理器 ------------------
    private static class CommandHandler implements CommandExecutor {
        private final BotManager botManager;
        private final YCBot plugin;

        public CommandHandler(BotManager botManager, YCBot plugin) {
            this.botManager = botManager;
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "spawn":
                    handleSpawn(sender, args);
                    break;
                case "kill":
                    handleKill(sender, args);
                    break;
                case "list":
                    handleList(sender);
                    break;
                default:
                    sender.sendMessage("§c未知命令");
            }
            return true;
        }

        private void sendHelp(CommandSender sender) {
            sender.sendMessage(new String[]{
                    "§6==== YCBot 帮助 ====",
                    "§a/bot spawn <名称> §7- 生成机器人",
                    "§a/bot kill <名称>  §7- 删除机器人",
                    "§a/bot list        §7- 列出你的机器人"
            });
        }

        private void handleSpawn(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§c只有玩家可以生成机器人");
                return;
            }
            if (args.length < 2) {
                sender.sendMessage("§c用法: /bot spawn <名称>");
                return;
            }

            String botName = args[1];
            botManager.spawnBot(player, botName).thenAcceptAsync(success -> {
                String msg = success ?
                        "§a机器人 " + botName + " 已生成" :
                        "§c生成失败";
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(msg));
            });
        }

        private void handleKill(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§c只有玩家可以删除机器人");
                return;
            }
            if (args.length < 2) {
                sender.sendMessage("§c用法: /bot kill <名称>");
                return;
            }

            String botName = args[1];
            boolean result = botManager.removeBot(player, botName);
            sender.sendMessage(result ?
                    "§a机器人 " + botName + " 已删除" :
                    "§c未找到该机器人");
        }

        private void handleList(CommandSender sender) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§c只有玩家可以查看列表");
                return;
            }
            Collection<String> bots = botManager.getBotNames(player.getUniqueId());
            sender.sendMessage("§e你的机器人 (" + bots.size() + "):");
            bots.forEach(name -> sender.sendMessage("§7- " + name));
        }
    }

    // ------------------ Tab补全处理器 ------------------
    private static class TabHandler implements TabCompleter {
        private final BotManager botManager;

        public TabHandler(BotManager botManager) {
            this.botManager = botManager;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
            List<String> suggestions = new ArrayList<>();
            if (args.length == 1) {
                Arrays.asList("spawn", "kill", "list").forEach(cmdName -> {
                    if (cmdName.startsWith(args[0].toLowerCase())) {
                        suggestions.add(cmdName);
                    }
                });
            } else if (args.length == 2 && "kill".equalsIgnoreCase(args[0])) {
                if (sender instanceof Player player) {
                    String input = args[1].toLowerCase();
                    botManager.getBotNames(player.getUniqueId()).stream()
                            .filter(name -> name.toLowerCase().startsWith(input))
                            .forEach(suggestions::add);
                }
            }
            return suggestions;
        }
    }
}