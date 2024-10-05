package me.freezy.plugins.papermc.adminutils.listener;

import me.freezy.plugins.papermc.adminutils.AdminUtils;
import me.freezy.plugins.papermc.adminutils.event.PlayerEnableAdminModeEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import static me.freezy.plugins.papermc.adminutils.AdminUtils.PREFIX;

public class PlayerEnableAdminModeListener implements Listener {
    private static final Logger logger = LoggerFactory.getLogger(PlayerEnableAdminModeListener.class);
    private final NamespacedKey INVENTORY_SAVE;
    private final NamespacedKey ADMINMODE;

    public PlayerEnableAdminModeListener() {
        INVENTORY_SAVE = new NamespacedKey(JavaPlugin.getPlugin(AdminUtils.class), "inv_save");
        ADMINMODE = new NamespacedKey(JavaPlugin.getPlugin(AdminUtils.class), "adminmode");
    }


    @EventHandler
    public void onEnableAdminMode(PlayerEnableAdminModeEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(PREFIX.append(MiniMessage.miniMessage().deserialize("<color:green>You've enabled the <color:red>Admin Mode</color>!</color>")));

        String encodedObj;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {

            boos.writeFloat(player.getExp());
            boos.writeDouble(player.getHealth());
            boos.writeFloat(player.getExhaustion());
            boos.writeFloat(player.getSaturation());
            boos.writeInt(player.getFoodLevel());
            boos.writeInt(player.getFireTicks());
            boos.writeObject(player.getLocation());
            boos.writeObject(player.getInventory().getContents());
            boos.writeObject(player.getActivePotionEffects());
            boos.flush();

            byte[] serializedObject = baos.toByteArray();

            encodedObj = Base64.getEncoder().encodeToString(serializedObject);

            player.getPersistentDataContainer().set(INVENTORY_SAVE, PersistentDataType.STRING, encodedObj);
            player.getPersistentDataContainer().set(ADMINMODE, PersistentDataType.BOOLEAN, true);
            player.getInventory().clear();
            player.clearActivePotionEffects();

            player.getServer().getOnlinePlayers().forEach(p -> {
                if (p.getUniqueId().equals(player.getUniqueId())) return;
                if (p.isOp()) {
                    p.sendMessage(MiniMessage.miniMessage().deserialize(String.format("<color:green>%s enabled the <color:red>Admin Mode</color>!</color>", player.getDisplayName())).decoration(TextDecoration.ITALIC, true));
                }
            });

            //player.sendMessage(PREFIX.append(MiniMessage.miniMessage().deserialize("<color:green>You've successfully saved your data!</color>")));
        } catch (IOException e) {
            logger.error("Failed to serialize player data: {}", e.getMessage());
            logger.error("Exception occurred", e);
            player.sendMessage(PREFIX.append(MiniMessage.miniMessage().deserialize("<color:red>Failed to save your data!</color>")));
        }
    }
}
