package com.cordfriends.spigot;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commande "/friend" cote serveur Spigot/Paper.
 *
 * Elle ne reimplemente PAS le systeme d'amis (qui reste entierement gere par le
 * plugin proxy BungeeCord) : elle ne sert qu'a relayer "/friend gui" vers le
 * proxy via plugin-messaging, exactement comme le ferait un clic dans le menu.
 *
 * Son seul but est de permettre a des outils qui ne connaissent que les
 * commandes du serveur backend de declencher l'ouverture du menu d'amis :
 *  - le plugin ChestCommands (menu cliquable -> "console: friend gui" ou
 *    "player: friend gui") ;
 *  - la commande vanilla "/execute as <joueur ou selecteur> run friend gui".
 *
 * Toutes les autres sous-commandes (add, accept, list, etc.) restent a utiliser
 * directement sur le proxy : elles ne sont pas dupliquees ici.
 */
public class FriendSpigotCommand implements CommandExecutor {

    private final CrossFriendsSpigotPlugin plugin;

    public FriendSpigotCommand(CrossFriendsSpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande doit etre executee par un joueur.");
            return true;
        }

        if (args.length == 0 || !(args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu"))) {
            player.sendMessage(ChatColor.YELLOW + "Seul /friend gui peut etre utilise depuis ce serveur.");
            player.sendMessage(ChatColor.GRAY + "Pour les autres commandes (add, accept, list...), utilisez-les normalement : elles sont gerees par le proxy.");
            return true;
        }

        requestOpenGui(player);
        return true;
    }

    private void requestOpenGui(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("OPEN_GUI_REQUEST");
        player.sendPluginMessage(plugin, CrossFriendsSpigotPlugin.CHANNEL, out.toByteArray());
    }
}
