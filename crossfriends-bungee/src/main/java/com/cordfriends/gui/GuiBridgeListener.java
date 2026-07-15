package com.cordfriends.gui;

import com.cordfriends.CrossFriendsPlugin;
import com.cordfriends.data.DataManager;
import com.cordfriends.data.PlayerProfile;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Pont entre le proxy et le module Spigot d'interface (menu d'amis affiche en jeu) :
 * - recoit la demande "rejoindre cet ami" envoyee par le module Spigot via plugin-messaging ;
 * - bascule le joueur sur le bon serveur (le proxy est seul a pouvoir le faire) ;
 * - une fois la connexion etablie, indique au serveur d'arrivee qui teleporter.
 *
 * Le module Spigot, lui, se contente d'afficher l'inventaire et de relayer les clics :
 * il n'a pas besoin de connaitre la logique d'amitie / blocage, deja verifiee ici.
 */
public class GuiBridgeListener implements Listener {

    private final CrossFriendsPlugin plugin;

    /** Joueur en attente de teleportation -> UUID de l'ami a rejoindre, une fois le changement de serveur effectue. */
    private final Map<UUID, UUID> pendingTeleports = new ConcurrentHashMap<>();

    public GuiBridgeListener(CrossFriendsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!CrossFriendsPlugin.CHANNEL.equals(event.getTag())) {
            return;
        }
        // On ne traite que les messages provenant d'un serveur backend (jamais d'un client) et destines a un joueur
        if (!(event.getSender() instanceof Server) || !(event.getReceiver() instanceof ProxiedPlayer)) {
            return;
        }
        event.setCancelled(true);

        ProxiedPlayer requester = (ProxiedPlayer) event.getReceiver();
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        String action;
        try {
            action = in.readUTF();
        } catch (Exception e) {
            return;
        }

        if ("JOIN_REQUEST".equals(action)) {
            try {
                handleJoinRequest(requester, in.readUTF());
            } catch (Exception e) {
                plugin.getLogger().warning("Message JOIN_REQUEST invalide recu de " + requester.getName());
            }
        } else if ("OPEN_GUI_REQUEST".equals(action)) {
            FriendGuiOpener.open(plugin, requester);
        }
    }

    private void handleJoinRequest(ProxiedPlayer requester, String friendUuidRaw) {
        UUID friendUuid;
        try {
            friendUuid = UUID.fromString(friendUuidRaw);
        } catch (IllegalArgumentException e) {
            return;
        }

        DataManager dm = plugin.getDataManager();
        PlayerProfile requesterProfile = dm.getProfile(requester.getUniqueId());

        if (!requesterProfile.getFriends().contains(friendUuid)) {
            requester.sendMessage(ChatColor.RED + "Vous n'etes plus ami avec ce joueur.");
            return;
        }
        if (dm.isBlocked(requester.getUniqueId(), friendUuid)) {
            requester.sendMessage(ChatColor.RED + "Action impossible avec ce joueur.");
            return;
        }

        ProxiedPlayer friendPlayer = plugin.getProxy().getPlayer(friendUuid);
        if (friendPlayer == null || friendPlayer.getServer() == null) {
            requester.sendMessage(ChatColor.RED + "Ce joueur est hors-ligne pour le moment.");
            return;
        }

        ServerInfo targetServer = friendPlayer.getServer().getInfo();
        String friendName = friendPlayer.getName();

        if (requester.getServer() != null && requester.getServer().getInfo().equals(targetServer)) {
            // Deja sur le meme serveur : pas besoin de changer, on demande directement la teleportation
            sendTeleportTo(requester, friendName);
            return;
        }

        pendingTeleports.put(requester.getUniqueId(), friendUuid);
        requester.sendMessage(ChatColor.AQUA + "Connexion vers le serveur de " + friendName + "...");
        requester.connect(targetServer);
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID friendUuid = pendingTeleports.remove(player.getUniqueId());
        if (friendUuid == null) {
            return;
        }

        ProxiedPlayer friendPlayer = plugin.getProxy().getPlayer(friendUuid);
        if (friendPlayer == null) {
            // L'ami s'est deconnecte pendant le transfert, rien a faire de plus
            return;
        }
        String friendName = friendPlayer.getName();

        // Petit delai de securite pour laisser le canal de plugin-messaging s'initialiser sur le nouveau serveur
        plugin.getProxy().getScheduler().schedule(plugin, () -> sendTeleportTo(player, friendName), 1, TimeUnit.SECONDS);
    }

    private void sendTeleportTo(ProxiedPlayer player, String friendName) {
        Server server = player.getServer();
        if (server == null) {
            return;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("TELEPORT_TO");
        out.writeUTF(friendName);
        server.sendData(CrossFriendsPlugin.CHANNEL, out.toByteArray());
    }
}
