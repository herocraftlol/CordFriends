package com.cordfriends.gui;

import com.cordfriends.CrossFriendsPlugin;
import com.cordfriends.data.DataManager;
import com.cordfriends.data.PlayerProfile;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * Logique commune d'ouverture du menu d'amis (envoi du plugin-message "OPEN_GUI"
 * vers le module Spigot sur lequel se trouve le joueur).
 *
 * Utilisee a la fois par :
 * - /friend gui execute directement sur le proxy (FriendCommand) ;
 * - une demande d'ouverture recue depuis le serveur backend (GuiBridgeListener),
 *   ce qui permet a un plugin Spigot tiers (ChestCommands, /execute as ... run friend gui, etc.)
 *   de declencher l'ouverture du menu sans connaitre la logique d'amitie.
 */
public final class FriendGuiOpener {

    private FriendGuiOpener() {
    }

    public static void open(CrossFriendsPlugin plugin, ProxiedPlayer player) {
        DataManager dm = plugin.getDataManager();
        PlayerProfile profile = dm.getProfile(player.getUniqueId());

        if (profile.getFriends().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Vous n'avez pas encore d'amis. Utilisez /friend add <joueur>.");
            return;
        }
        if (player.getServer() == null) {
            player.sendMessage(ChatColor.RED + "Impossible d'ouvrir l'interface pour le moment, reessayez dans un instant.");
            return;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("OPEN_GUI");
        out.writeInt(profile.getFriends().size());

        for (UUID uuid : profile.getFriends()) {
            PlayerProfile friendProfile = dm.getProfile(uuid);
            String name = friendProfile.getName() != null ? friendProfile.getName() : uuid.toString();
            ProxiedPlayer online = plugin.getProxy().getPlayer(uuid);
            boolean isOnline = online != null;
            String server = (isOnline && online.getServer() != null) ? online.getServer().getInfo().getName() : "";

            out.writeUTF(uuid.toString());
            out.writeUTF(name);
            out.writeBoolean(isOnline);
            out.writeUTF(server);
        }

        player.getServer().sendData(CrossFriendsPlugin.CHANNEL, out.toByteArray());
        player.sendMessage(ChatColor.GRAY + "Ouverture de l'interface d'amis...");
    }
}
