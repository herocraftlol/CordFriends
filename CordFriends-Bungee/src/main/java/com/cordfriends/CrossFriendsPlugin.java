package com.cordfriends;

import com.cordfriends.commands.FriendCommand;
import com.cordfriends.commands.MailCommand;
import com.cordfriends.commands.MsgCommand;
import com.cordfriends.commands.ReplyCommand;
import com.cordfriends.data.DataManager;
import com.cordfriends.gui.GuiBridgeListener;
import com.cordfriends.listeners.PlayerListener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public class CrossFriendsPlugin extends Plugin {

    /** Canal de plugin-messaging utilise pour communiquer avec le module Spigot (interface graphique). */
    public static final String CHANNEL = "crossfriends:main";

    private static CrossFriendsPlugin instance;

    private DataManager dataManager;
    private boolean requireFriendship = true;
    private boolean mailRequireFriendship = false;
    private boolean notifyOnJoin = true;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        loadConfig();

        this.dataManager = new DataManager(this);

        getProxy().registerChannel(CHANNEL);

        getProxy().getPluginManager().registerCommand(this, new FriendCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MsgCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this));
        getProxy().getPluginManager().registerCommand(this, new MailCommand(this));

        getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
        getProxy().getPluginManager().registerListener(this, new GuiBridgeListener(this));

        // Sauvegarde periodique de securite, toutes les 5 minutes
        getProxy().getScheduler().schedule(this, () -> dataManager.saveAll(), 5, 5, TimeUnit.MINUTES);

        getLogger().info("CrossFriends active : amis / messages prives / courrier / interface inter-serveur prets.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("CrossFriends desactive, donnees sauvegardees.");
    }

    private void loadConfig() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile.toPath());
                    }
                }
            }
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            requireFriendship = config.getBoolean("require-friendship", true);
            mailRequireFriendship = config.getBoolean("mail-require-friendship", false);
            notifyOnJoin = config.getBoolean("notify-on-join", true);
        } catch (IOException e) {
            getLogger().warning("Impossible de charger config.yml, valeurs par defaut utilisees : " + e.getMessage());
        }
    }

    public static CrossFriendsPlugin getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public boolean isRequireFriendship() {
        return requireFriendship;
    }

    public boolean isMailRequireFriendship() {
        return mailRequireFriendship;
    }

    public boolean isNotifyOnJoin() {
        return notifyOnJoin;
    }
}
