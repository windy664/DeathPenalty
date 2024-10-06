package org.windy.deathPenalty;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class DeathPenalty extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private FileConfiguration playerData;
    private File playerDataFile;
    private Logger logger;
    private String prefix;
    private Double healthmax;
    private boolean respawn;
    @Override
    public void onEnable() {
        // 加载配置文件
        this.saveDefaultConfig();
        config = this.getConfig();
        logger = getLogger();
        prefix = config.getString("prefix", "§b[§b死亡惩罚§b]§f");
        healthmax = config.getDouble("health-max", 40.0);
        respawn = config.getBoolean("mohist", false);

        // 加载玩家数据文件
        loadPlayerData();

        // 打印艺术字符画
        this.getServer().getConsoleSender().sendMessage(Text.logo);

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.getServer().getConsoleSender().sendMessage(Text.logo);
        logger.info("§b死亡惩罚插件关闭！定制插件联系366262436");
        savePlayerData(); // 在插件停用时保存玩家数据
    }

    // 加载玩家数据文件
    private void loadPlayerData() {
        playerDataFile = new File(getDataFolder(), "data.yml");
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();  // 如果文件不存在则创建
            } catch (IOException e) {
                logger.severe("无法创建 data.yml 文件！");
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
    }

    // 保存玩家数据文件
    private void savePlayerData() {
        if (playerData == null) {
            logger.severe("playerData 为空，无法保存玩家数据！");
            return;
        }
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            logger.severe("无法保存玩家数据到 data.yml 文件！");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();

        // 获取当前生命值上限
        double currentMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double decrement = config.getDouble("health-decrease-per-death", 2.0); // 默认下降 2 点

        // 计算新的生命值上限，但最低为 1 点
        double newMaxHealth = Math.max(1.0, currentMaxHealth - decrement);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);

        // 保存新的生命值上限到 data.yml 文件
        playerData.set("players." + playerId + ".max-health", newMaxHealth);
        savePlayerData();

        logger("玩家 " + player.getName() + " 死了，新的生命 " + newMaxHealth);

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if(!respawn){
            logger("非mohist模式，不处理兼容");
            return;
        }
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // 从 data.yml 文件中获取玩家的生命值上限
        double maxHealth = playerData.getDouble("players." + playerId + ".max-health", healthmax);


        // 延迟几秒后设置生命值上限
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // 设置玩家的生命值上限
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            logger(player.getName() + " 重生，新的生命 " + maxHealth);
        }, 40L); // 延迟2秒 (20 ticks = 1 second)
    }

    @EventHandler
    public void onPlayerConsumeGoldenApple(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        UUID playerId = player.getUniqueId();

        if (item.getType() == Material.GOLDEN_APPLE) {
            // 获取当前生命值上限
            double currentMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            double healthRestore = config.getDouble("health-restore-per-golden-apple", 10.0); // 默认恢复 10 点

            // 计算新的生命值上限
            double newMaxHealth = Math.min(healthmax, currentMaxHealth + healthRestore);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);

            // 保存新的生命值上限到 data.yml 文件
            playerData.set("players." + playerId + ".max-health", newMaxHealth);
            savePlayerData();

            logger("玩家 " + player.getName() + " 吃了个金苹果，现在的生命 " + newMaxHealth);

        }
    }

    // 处理命令
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("swcf")) {
            if (args.length == 0) {
                sender.sendMessage("用法: /swcf <reload|debug|setmaxhealth>");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    reloadConfig();
                    loadPlayerData();
                    sender.sendMessage("配置文件和玩家数据已重新加载！");
                    break;

                case "debug":
                    boolean debugMode = !config.getBoolean("debug", false);
                    config.set("debug", debugMode);
                    saveConfig();
                    sender.sendMessage("调试模式已 " + (debugMode ? "开启" : "关闭"));
                    break;

                case "setmaxhealth":
                    if (args.length == 3 && sender.hasPermission("swcf.setmaxhealth")) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null) {
                            try {
                                double newMaxHealth = Double.parseDouble(args[2]);
                                if (newMaxHealth < 1.0 || newMaxHealth > healthmax) {
                                    sender.sendMessage("生命值上限必须在 1 到 "+ healthmax +"之间！");
                                    return true;
                                }
                                target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);

                                // 保存新的生命值上限到 data.yml 文件
                                playerData.set("players." + target.getUniqueId() + ".max-health", newMaxHealth);
                                savePlayerData();

                                sender.sendMessage("已将 " + target.getName() + " 的生命值上限设置为 " + newMaxHealth);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(prefix+"请输入有效的数字作为生命值上限！");
                            }
                        } else {
                            sender.sendMessage(prefix+"玩家 " + args[1] + " 未找到！");
                        }
                    } else {
                        sender.sendMessage(prefix+"用法: /swcf setmaxhealth <玩家名> <新的生命值上限>");
                    }
                    break;

                default:
                    sender.sendMessage(prefix+"未知命令: /swcf " + args[0]);
                    break;
            }
            return true;
        }
        return false;
    }

    public void logger(String message) {
        if (config.getBoolean("debug", false)) {
            this.getServer().getConsoleSender().sendMessage(prefix + message);
        }
    }
}

