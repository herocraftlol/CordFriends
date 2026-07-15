package com.crossfriends.spigot;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Module Spigot/Paper a installer sur CHAQUE serveur backend connecte au proxy.
 * Il ne contient aucune logique d'amitie : il se contente d'afficher le menu
 * et de relayer les clics vers le plugin BungeeCord "CrossFriends", qui reste
 * la seule source de verite (amis, blocages, courrier...).
 */
public class CrossFriendsSpigotPlugin extends JavaPlugin {

    public static final String CHANNEL = "crossfriends:main";

    private static CrossFriendsSpigotPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        if (!getServer().spigot().getConfig().getBoolean("settings.bungeecord", false)) {
            getLogger().warning("Le mode proxy ('bungeecord: true') ne semble pas active dans spigot.yml.");
            getLogger().warning("Le menu d'amis risque de ne pas fonctionner sans cela (voir le README).");
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, new FriendsGuiMessenger(this));
        getServer().getPluginManager().registerEvents(new FriendsMenuListener(this), this);

        getLogger().info("CrossFriends-GUI active.");
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("CrossFriends-GUI desactive.");
    }

    /**
     * @return l'instance active du plugin, ou {@code null} s'il n'est pas (encore) charge.
     *         Utilise en interne et par {@link com.crossfriends.spigot.api.CrossFriendsSpigotAPI}.
     */
    public static CrossFriendsSpigotPlugin getInstance() {
        return instance;
    }
}
