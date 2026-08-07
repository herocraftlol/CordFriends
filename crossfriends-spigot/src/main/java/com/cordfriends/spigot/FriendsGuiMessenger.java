package com.cordfriends.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Recoit les messages venant du proxy BungeeCord sur le canal "crossfriends:main" :
 * - OPEN_GUI : construit et ouvre l'inventaire listant les amis du joueur ;
 * - TELEPORT_TO : teleporte le joueur (qui vient d'arriver sur ce serveur) vers son ami.
 */
public class FriendsGuiMessenger implements PluginMessageListener {

    private final CrossFriendsSpigotPlugin plugin;

    public FriendsGuiMessenger(CrossFriendsSpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CrossFriendsSpigotPlugin.CHANNEL.equals(channel)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String action;
        try {
            action = in.readUTF();
        } catch (Exception e) {
            return;
        }

        if ("OPEN_GUI".equals(action)) {
            handleOpenGui(player, in);
        } else if ("TELEPORT_TO".equals(action)) {
            handleTeleportTo(player, in);
        }
    }

    private void handleOpenGui(Player player, ByteArrayDataInput in) {
        int count = in.readInt();
        List<FriendEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UUID uuid = UUID.fromString(in.readUTF());
            String name = in.readUTF();
            boolean online = in.readBoolean();
            String server = in.readUTF();
            entries.add(new FriendEntry(uuid, name, online, server));
        }

        // Les inventaires doivent etre crees/ouverts sur le thread principal du serveur
        Bukkit.getScheduler().runTask(plugin, () -> openMenu(player, entries));
    }

    private void openMenu(Player player, List<FriendEntry> entries) {
        int size = Math.max(9, Math.min(54, ((entries.size() - 1) / 9 + 1) * 9));
        FriendsMenuHolder holder = new FriendsMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, size, ChatColor.DARK_PURPLE + "Vos amis");
        holder.setInventory(inventory);

        int slot = 0;
        for (FriendEntry entry : entries) {
            if (slot >= size) {
                break;
            }
            inventory.setItem(slot, buildHead(entry));
            holder.put(slot, entry);
            slot++;
        }

        player.openInventory(inventory);
        loadSkinsAsync(player, inventory, holder);
    }

    /**
     * Recupere le vrai skin de chaque ami en arriere-plan (appel reseau vers Mojang via
     * PlayerProfile#complete) puis met a jour les tetes deja affichees dans le menu.
     *
     * On ne peut pas se contenter de OfflinePlayer#setOwningPlayer : cela n'affiche le bon
     * skin que si le joueur a deja rejoint CE serveur backend. En completant un PlayerProfile,
     * on recupere la texture quel que soit le serveur ou l'ami a ete vu pour la derniere fois.
     * Le tout se fait hors du thread principal car l'appel reseau est bloquant.
     */
    private void loadSkinsAsync(Player player, Inventory inventory, FriendsMenuHolder holder) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Map.Entry<Integer, FriendEntry> mapEntry : holder.getSlots().entrySet()) {
                int slot = mapEntry.getKey();
                FriendEntry entry = mapEntry.getValue();

                PlayerProfile profile = Bukkit.createProfile(entry.uuid(), entry.name());
                boolean fetched;
                try {
                    // Appel bloquant (reseau) : recupere les proprietes de texture (skin) aupres de Mojang.
                    fetched = profile.complete(true);
                } catch (Exception e) {
                    // Mojang injoignable, timeout, rate-limit... on garde la tete par defaut pour cet ami.
                    fetched = false;
                }

                if (!fetched) {
                    continue;
                }

                Bukkit.getScheduler().runTask(plugin, () -> applySkin(player, inventory, slot, profile));
            }
        });
    }

    private void applySkin(Player player, Inventory inventory, int slot, PlayerProfile profile) {
        if (!player.isOnline() || !player.getOpenInventory().getTopInventory().equals(inventory)) {
            return;
        }
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return;
        }
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    private ItemStack buildHead(FriendEntry entry) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.uuid()));
            meta.setDisplayName((entry.online() ? ChatColor.GREEN : ChatColor.DARK_GRAY) + entry.name());

            List<String> lore = new ArrayList<>();
            lore.add(entry.online()
                    ? ChatColor.GREEN + "En ligne sur " + entry.server()
                    : ChatColor.GRAY + "Hors-ligne");
            lore.add("");
            if (entry.online()) {
                lore.add(ChatColor.YELLOW + "Clic gauche" + ChatColor.GRAY + " : rejoindre son serveur");
            } else {
                lore.add(ChatColor.DARK_GRAY + "Indisponible (hors-ligne)");
            }
            lore.add(ChatColor.YELLOW + "Clic droit" + ChatColor.GRAY + " : preparer un message");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }

    private void handleTeleportTo(Player player, ByteArrayDataInput in) {
        String friendName = in.readUTF();
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player friend = Bukkit.getPlayerExact(friendName);
            if (friend != null) {
                player.teleport(friend.getLocation());
                player.sendMessage(ChatColor.GREEN + "Vous avez rejoint " + friendName + " !");
            } else {
                player.sendMessage(ChatColor.RED + "Votre ami n'est plus sur ce serveur.");
            }
        });
    }
}
