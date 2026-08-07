package com.cordfriends.commands;

import com.cordfriends.CrossFriendsPlugin;
import com.cordfriends.data.DataManager;
import com.cordfriends.data.MailMessage;
import com.cordfriends.data.PlayerProfile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * /msg <joueur> <message>
 * Si le destinataire est en ligne (sur n'importe quel serveur du reseau), le
 * message est livre instantanement via le proxy. S'il est hors-ligne, le
 * message est automatiquement sauvegarde comme courrier et lui sera presente
 * a sa prochaine connexion.
 */
public class MsgCommand extends Command implements TabExecutor {

    private final CrossFriendsPlugin plugin;

    public MsgCommand(CrossFriendsPlugin plugin) {
        super("msg", null, "tell", "whisper", "w", "pm");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "Cette commande doit etre executee par un joueur.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage : /msg <joueur> <message>");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        String targetName = args[0];

        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous envoyer un message a vous-meme.");
            return;
        }

        String message = String.join(" ", Arrays.asList(args).subList(1, args.length));

        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Joueur inconnu : " + targetName);
            return;
        }
        UUID targetUuid = targetUuidOpt.get();

        if (dm.isBlocked(player.getUniqueId(), targetUuid)) {
            player.sendMessage(ChatColor.RED + "Impossible d'envoyer un message a " + targetName + " (joueur bloque).");
            return;
        }

        if (plugin.isRequireFriendship() && !player.hasPermission("crossfriends.bypass")) {
            PlayerProfile senderProfile = dm.getProfile(player.getUniqueId());
            if (!senderProfile.getFriends().contains(targetUuid)) {
                player.sendMessage(ChatColor.RED + "Vous devez etre ami avec " + targetName
                        + " pour lui envoyer un message prive (/friend add " + targetName + ").");
                return;
            }
        }

        ProxiedPlayer targetPlayer = plugin.getProxy().getPlayer(targetUuid);

        if (targetPlayer != null) {
            // Le destinataire est en ligne, quel que soit son serveur : livraison instantanee
            targetPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "[" + player.getName() + " -> Vous] " + ChatColor.WHITE + message);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "[Vous -> " + targetPlayer.getName() + "] " + ChatColor.WHITE + message);

            dm.setLastConversant(player.getUniqueId(), targetUuid);
            dm.setLastConversant(targetUuid, player.getUniqueId());
        } else {
            // Hors-ligne : le message est conserve comme courrier en attente
            PlayerProfile targetProfile = dm.getProfile(targetUuid);
            targetProfile.getMailbox().add(new MailMessage(player.getUniqueId(), player.getName(), message, System.currentTimeMillis()));
            dm.saveProfile(targetProfile);

            player.sendMessage(ChatColor.YELLOW + targetName
                    + " est hors-ligne. Votre message a ete sauvegarde et lui sera remis a sa connexion.");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return TabCompleteUtil.onlinePlayerNames(plugin, args[0]);
        }
        return Collections.emptyList();
    }
}
