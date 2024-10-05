package me.freezy.plugins.papermc.adminutils.listener;

import me.freezy.plugins.papermc.adminutils.event.PlayerDisableAdminModeEvent;
import me.freezy.plugins.papermc.adminutils.event.PlayerEnableAdminModeEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;


public class GameModeSwitchListeners implements Listener {
    @EventHandler
    public void onGameModeSwitch(PlayerGameModeChangeEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        GameMode newGameMode = event.getNewGameMode();
        GameMode currentGameMode = player.getGameMode();

        if ((player.hasPermission("adminutils.use") || player.isOp())) {
            if ((newGameMode.equals(GameMode.CREATIVE) || newGameMode.equals(GameMode.SPECTATOR))
                    && !(currentGameMode.equals(GameMode.CREATIVE) || currentGameMode.equals(GameMode.SPECTATOR))) {
                Bukkit.getServer().getPluginManager().callEvent(new PlayerEnableAdminModeEvent(player));
            } else if (!(newGameMode.equals(GameMode.CREATIVE) || newGameMode.equals(GameMode.SPECTATOR))) {
                Bukkit.getServer().getPluginManager().callEvent(new PlayerDisableAdminModeEvent(player));
            }
        } else {
            Bukkit.getServer().getPluginManager().callEvent(new PlayerDisableAdminModeEvent(player));
        }
    }
}

