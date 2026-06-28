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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * /mail send <joueur> <message> - laisser un message qu'un joueur verra a sa prochaine connexion
 * /mail read                    - lire son courrier en attente
 * /mail clear                   - vider sa boite de reception
 *
 * Par defaut (mail-require-friendship: false dans config.yml), on peut laisser
 * un mail a n'importe quel joueur deja vu sur le reseau, ami ou non.
 */
public class MailCommand extends Command implements TabExecutor {

    private static final List<String> SUBCOMMANDS = Arrays.asList("send", "read", "clear");

    private final CrossFriendsPlugin plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

    public MailCommand(CrossFriendsPlugin plugin) {
        super("mail", null, "courrier");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "Cette commande doit etre executee par un joueur.");
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            sendUsage(player);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "send":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage : /mail send <joueur> <message>");
                    return;
                }
                sendMail(player, args[1], String.join(" ", Arrays.asList(args).subList(2, args.length)));
                break;

            case "read":
            case "list":
                readMail(player);
                break;

            case "clear":
                clearMail(player);
                break;

            default:
                sendUsage(player);
        }
    }

    private void sendMail(ProxiedPlayer player, String targetName, String message) {
        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous laisser un message a vous-meme.");
            return;
        }

        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Joueur inconnu : " + targetName);
            return;
        }
        UUID targetUuid = targetUuidOpt.get();

        if (dm.isBlocked(player.getUniqueId(), targetUuid)) {
            player.sendMessage(ChatColor.RED + "Impossible de laisser un message a " + targetName + " (joueur bloque).");
            return;
        }

        if (plugin.isMailRequireFriendship() && !player.hasPermission("crossfriends.bypass")) {
            PlayerProfile senderProfile = dm.getProfile(player.getUniqueId());
            if (!senderProfile.getFriends().contains(targetUuid)) {
                player.sendMessage(ChatColor.RED + "Vous devez etre ami avec " + targetName + " pour lui laisser un message.");
                return;
            }
        }

        PlayerProfile targetProfile = dm.getProfile(targetUuid);
        targetProfile.getMailbox().add(new MailMessage(player.getUniqueId(), player.getName(), message, System.currentTimeMillis()));
        dm.saveProfile(targetProfile);

        player.sendMessage(ChatColor.GREEN + "Message laisse a " + targetName + ", il le verra a sa prochaine connexion.");

        ProxiedPlayer targetPlayer = plugin.getProxy().getPlayer(targetUuid);
        if (targetPlayer != null) {
            targetPlayer.sendMessage(ChatColor.AQUA + "Vous avez recu un nouveau message de " + player.getName()
                    + ". Tapez /mail read pour le lire.");
        }
    }

    private void readMail(ProxiedPlayer player) {
        DataManager dm = plugin.getDataManager();
        PlayerProfile profile = dm.getProfile(player.getUniqueId());
        List<MailMessage> mailbox = profile.getMailbox();

        if (mailbox.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Vous n'avez aucun message en attente.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Vos messages (" + mailbox.size() + ") ===");
        for (MailMessage mail : mailbox) {
            String date = dateFormat.format(new Date(mail.getTimestamp()));
            player.sendMessage(ChatColor.AQUA + "[" + date + "] " + ChatColor.GREEN + mail.getSenderName()
                    + ChatColor.GRAY + " : " + ChatColor.WHITE + mail.getMessage());
            mail.setRead(true);
        }
        dm.saveProfile(profile);
        player.sendMessage(ChatColor.GRAY + "Utilisez /mail clear pour vider votre boite de reception.");
    }

    private void clearMail(ProxiedPlayer player) {
        DataManager dm = plugin.getDataManager();
        PlayerProfile profile = dm.getProfile(player.getUniqueId());
        int count = profile.getMailbox().size();
        profile.getMailbox().clear();
        dm.saveProfile(profile);
        player.sendMessage(ChatColor.GREEN + "" + count + " message(s) supprime(s).");
    }

    private void sendUsage(ProxiedPlayer player) {
        player.sendMessage(ChatColor.GOLD + "=== Commandes /mail ===");
        player.sendMessage(ChatColor.AQUA + "/mail send <joueur> <message>" + ChatColor.GRAY + " - Laisser un message a voir plus tard");
        player.sendMessage(ChatColor.AQUA + "/mail read" + ChatColor.GRAY + " - Lire vos messages en attente");
        player.sendMessage(ChatColor.AQUA + "/mail clear" + ChatColor.GRAY + " - Vider votre boite de reception");
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            String partial = args.length == 0 ? "" : args[0];
            return TabCompleteUtil.filterKeywords(SUBCOMMANDS, partial);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            return TabCompleteUtil.onlinePlayerNames(plugin, args[1]);
        }
        return Collections.emptyList();
    }
}
