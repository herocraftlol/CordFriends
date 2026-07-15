package com.cordfriends.commands;

import com.cordfriends.CrossFriendsPlugin;
import com.cordfriends.data.DataManager;
import com.cordfriends.data.MailMessage;
import com.cordfriends.data.PlayerProfile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.UUID;

/**
 * /r <message>
 * Repond directement au dernier joueur avec qui une conversation a eu lieu
 * (via /msg ou /r), sans avoir a retaper son nom.
 */
public class ReplyCommand extends Command {

    private final CrossFriendsPlugin plugin;

    public ReplyCommand(CrossFriendsPlugin plugin) {
        super("r", null, "reply");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "Cette commande doit etre executee par un joueur.");
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage : /r <message>");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        DataManager dm = plugin.getDataManager();
        UUID lastUuid = dm.getLastConversant(player.getUniqueId());

        if (lastUuid == null) {
            player.sendMessage(ChatColor.RED + "Vous n'avez personne a qui repondre pour le moment.");
            return;
        }

        String message = String.join(" ", Arrays.asList(args));

        if (dm.isBlocked(player.getUniqueId(), lastUuid)) {
            player.sendMessage(ChatColor.RED + "Impossible de repondre a ce joueur (bloque).");
            return;
        }

        if (plugin.isRequireFriendship() && !player.hasPermission("crossfriends.bypass")) {
            PlayerProfile senderProfile = dm.getProfile(player.getUniqueId());
            if (!senderProfile.getFriends().contains(lastUuid)) {
                player.sendMessage(ChatColor.RED + "Vous devez etre ami avec cette personne pour lui repondre.");
                return;
            }
        }

        ProxiedPlayer targetPlayer = plugin.getProxy().getPlayer(lastUuid);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "[" + player.getName() + " -> Vous] " + ChatColor.WHITE + message);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "[Vous -> " + targetPlayer.getName() + "] " + ChatColor.WHITE + message);
            dm.setLastConversant(targetPlayer.getUniqueId(), player.getUniqueId());
        } else {
            PlayerProfile targetProfile = dm.getProfile(lastUuid);
            targetProfile.getMailbox().add(new MailMessage(player.getUniqueId(), player.getName(), message, System.currentTimeMillis()));
            dm.saveProfile(targetProfile);
            player.sendMessage(ChatColor.YELLOW + "Ce joueur est hors-ligne, votre message a ete sauvegarde.");
        }
    }
}
