package com.cordfriends.listeners;

import com.cordfriends.CrossFriendsPlugin;
import com.cordfriends.data.DataManager;
import com.cordfriends.data.MailMessage;
import com.cordfriends.data.PlayerProfile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

/**
 * PostLoginEvent se declenche une seule fois quand le joueur rejoint le PROXY
 * (et non a chaque changement de sous-serveur), ce qui en fait l'endroit ideal
 * pour charger son profil et le notifier de son courrier / ses demandes d'amis.
 */
public class PlayerListener implements Listener {

    private final CrossFriendsPlugin plugin;

    public PlayerListener(CrossFriendsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        DataManager dm = plugin.getDataManager();

        PlayerProfile profile = dm.getProfile(player.getUniqueId());
        profile.setName(player.getName());
        dm.registerName(player.getUniqueId(), player.getName());

        if (!plugin.isNotifyOnJoin()) {
            return;
        }

        // Petit delai pour laisser le client finir de se connecter avant de lui envoyer un message
        plugin.getProxy().getScheduler().schedule(plugin, () -> {
            int unread = 0;
            for (MailMessage mail : profile.getMailbox()) {
                if (!mail.isRead()) {
                    unread++;
                }
            }
            int pendingRequests = profile.getIncomingRequests().size();

            if (unread > 0) {
                player.sendMessage(ChatColor.AQUA + "Vous avez " + unread + " message(s) en attente. Tapez /mail read.");
            }
            if (pendingRequests > 0) {
                player.sendMessage(ChatColor.GOLD + "Vous avez " + pendingRequests + " demande(s) d'ami en attente. Tapez /friend requests.");
            }
        }, 2, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        plugin.getDataManager().unload(event.getPlayer().getUniqueId());
    }
}
