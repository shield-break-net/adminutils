package me.freezy.plugins.papermc.adminutils;

import me.freezy.plugins.papermc.adminutils.listener.GameModeSwitchListeners;
import me.freezy.plugins.papermc.adminutils.listener.PlayerDisableAdminModeListener;
import me.freezy.plugins.papermc.adminutils.listener.PlayerEnableAdminModeListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdminUtils extends JavaPlugin {
    public static final Component PREFIX = MiniMessage.miniMessage().deserialize("<gradient:#4d94ff:#36ddff>Shield-Break</gradient> <color:gray>-></color> ");
    private static final Logger logger = LoggerFactory.getLogger(AdminUtils.class);

    @Override
    public void onLoad() {
        // Plugin initialization logic
        logger.info("Loading plugin...");
        logger.info("Plugin loaded!");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger.info("Enabling plugin...");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GameModeSwitchListeners(), this);
        pm.registerEvents(new PlayerEnableAdminModeListener(), this);
        pm.registerEvents(new PlayerDisableAdminModeListener(), this);
        logger.info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("Disabling plugin...");
        logger.info("Plugin disabled!");
    }
}
