package com.cordfriends.spigot;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

/**
 * Gere les clics dans le menu d'amis :
 * - clic gauche sur un ami en ligne -> demande au proxy de nous faire rejoindre son serveur ;
 * - clic droit -> propose un lien cliquable qui pre-remplit le chat avec "/msg <ami> ".
 */
public class FriendsMenuListener implements Listener {

    private final CrossFriendsSpigotPlugin plugin;

    public FriendsMenuListener(CrossFriendsSpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();
        if (clicked == null || !(clicked.getHolder() instanceof FriendsMenuHolder holder)) {
            return;
        }
        event.setCancelled(true);

        FriendEntry entry = holder.get(event.getSlot());
        if (entry == null || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClick() == ClickType.RIGHT) {
            player.closeInventory();
            sendMessageSuggestion(player, entry.name());
            return;
        }

        if (event.getClick() == ClickType.LEFT) {
            if (!entry.online()) {
                player.sendMessage(ChatColor.RED + entry.name() + " est hors-ligne pour le moment.");
                return;
            }
            player.closeInventory();
            requestJoin(player, entry.uuid());
        }
    }

    private void sendMessageSuggestion(Player player, String friendName) {
        TextComponent component = new TextComponent(ChatColor.AQUA + "Cliquez pour ecrire a " + friendName + " : ");
        TextComponent link = new TextComponent(ChatColor.GREEN.toString() + ChatColor.BOLD + "[Ecrire]");
        link.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + friendName + " "));
        component.addExtra(link);
        player.spigot().sendMessage(component);
    }

    private void requestJoin(Player player, UUID friendUuid) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("JOIN_REQUEST");
        out.writeUTF(friendUuid.toString());
        player.sendPluginMessage(plugin, CrossFriendsSpigotPlugin.CHANNEL, out.toByteArray());
    }
}
