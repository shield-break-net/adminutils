package me.freezy.plugins.papermc.adminutils.listener;

import me.freezy.plugins.papermc.adminutils.AdminUtils;
import me.freezy.plugins.papermc.adminutils.event.PlayerDisableAdminModeEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;

import static me.freezy.plugins.papermc.adminutils.AdminUtils.PREFIX;

public class PlayerDisableAdminModeListener implements Listener {
    private static final Logger logger = LoggerFactory.getLogger(PlayerDisableAdminModeListener.class);
    private final NamespacedKey INVENTORY_SAVE;
    private final NamespacedKey ADMINMODE;

    public PlayerDisableAdminModeListener() {
        INVENTORY_SAVE = new NamespacedKey(JavaPlugin.getPlugin(AdminUtils.class), "inv_save");
        ADMINMODE = new NamespacedKey(JavaPlugin.getPlugin(AdminUtils.class), "adminmode");
    }

    @EventHandler
    public void onDisableAdminMode(PlayerDisableAdminModeEvent event) {
        Player player = event.getPlayer();
        if (!player.getPersistentDataContainer().has(INVENTORY_SAVE, PersistentDataType.STRING)) {
            player.sendMessage(PREFIX.append(MiniMessage.miniMessage().deserialize("<color:red>No saved data found!</color>")));
            return;
        }
        player.sendMessage(PREFIX.append(MiniMessage.miniMessage().deserialize("<color:green>You've disabled the <color:red>Admin Mode</color>!</color>")));

        String data = player.getPersistentDataContainer().get(INVENTORY_SAVE, PersistentDataType.STRING);
        player.getPersistentDataContainer().set(ADMINMODE, PersistentDataType.BOOLEAN, false);
        if (data == null || data.isEmpty()) {
            player.sendMessage(PREFIX.append(MiniMessage.miniMessage().deserialize("<color:red>No saved data found!</color>")));
            return;
        }

        byte[] decodedObj = Base64.getDecoder().decode(data);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedObj);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {

            float exp = bois.readFloat();
            double health = bois.readDouble();
            float exhaustion = bois.readFloat();
            float saturation = bois.readFloat();
            int foodLevel = bois.readInt();
            int fireTicks = bois.readInt();
            Location location = (Location) bois.readObject();
            ItemStack[] contents = (ItemStack[]) bois.readObject();
            Collection<PotionEffect> effects = (Collection<PotionEffect>) bois.readObject();

            if (location != null && contents != null) {
                player.setExp(exp);
                player.setHealth(health);
                player.setExhaustion(exhaustion);
                player.setSaturation(saturation);
                player.setFoodLevel(foodLevel);
                player.setFireTicks(fireTicks);

                player.teleportAsync(location).exceptionally(throwable -> {
                    logger.warn("Async teleport failed, trying sync teleport.");
                    player.teleport(location);
                    return null;
                });

                player.getInventory().setContents(contents);

                for (PotionEffect effect : effects) {
                    player.addPotionEffect(effect);
                }
            } else {
                logger.warn("Location or inventory was null, skipping player setup.");
            }

            player.getServer().getOnlinePlayers().forEach(p -> {
                if (p.getUniqueId().equals(player.getUniqueId())) return;
                if (p.isOp()) {
                    p.sendMessage(MiniMessage.miniMessage().deserialize(String.format("<color:green>%s disabled the <color:red>Admin Mode</color>!</color>", player.getDisplayName())).decoration(TextDecoration.ITALIC, true));
                }
            });

            //player.sendMessage(PREFIX.append(MiniMessage.miniMessage().deserialize("<color:green>You've successfully loaded your data!</color>")));
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to deserialize player data: {}", e.getMessage());
            logger.error("Exception occurred", e);
            player.sendMessage(PREFIX.append(MiniMessage.miniMessage().deserialize("<color:red>Failed to load your data!</color>")));
        }
    }
}
