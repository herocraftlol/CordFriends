package com.cordfriends.spigot;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Module Spigot/Paper a installer sur CHAQUE serveur backend connecte au proxy.
 * Il ne contient aucune logique d'amitie : il se contente d'afficher le menu
 * et de relayer les clics vers le plugin BungeeCord "CrossFriends", qui reste
 * la seule source de verite (amis, blocages, courrier...).
 */
public class CrossFriendsSpigotPlugin extends JavaPlugin {

    public static final String CHANNEL = "crossfriends:main";

    @Override
    public void onEnable() {
        if (!getServer().spigot().getConfig().getBoolean("settings.bungeecord", false)) {
            getLogger().warning("Le mode proxy ('bungeecord: true') ne semble pas active dans spigot.yml.");
            getLogger().warning("Le menu d'amis risque de ne pas fonctionner sans cela (voir le README).");
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, new FriendsGuiMessenger(this));
        getServer().getPluginManager().registerEvents(new FriendsMenuListener(this), this);

        var friendCommand = getCommand("friend");
        if (friendCommand != null) {
            friendCommand.setExecutor(new FriendSpigotCommand(this));
        } else {
            getLogger().warning("La commande 'friend' n'a pas pu etre enregistree (absente du plugin.yml ?).");
        }

        getLogger().info("CrossFriends-GUI active.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CrossFriends-GUI desactive.");
    }
}
