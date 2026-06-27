package com.crossfriends.commands;

import com.crossfriends.CrossFriendsPlugin;
import com.crossfriends.data.DataManager;
import com.crossfriends.data.PlayerProfile;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Optional;
import java.util.UUID;

/**
 * /friend add|accept|deny|remove|list|requests
 * Fonctionne quel que soit le sous-serveur sur lequel se trouvent les joueurs,
 * car la commande est traitee directement par le proxy BungeeCord.
 */
public class FriendCommand extends Command {

    private final CrossFriendsPlugin plugin;

    public FriendCommand(CrossFriendsPlugin plugin) {
        super("friend", null, "f", "amis", "ami");
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

        DataManager dm = plugin.getDataManager();
        PlayerProfile profile = dm.getProfile(player.getUniqueId());

        switch (args[0].toLowerCase()) {
            case "add":
            case "request":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage : /friend add <joueur>");
                    return;
                }
                handleAdd(player, profile, args[1]);
                break;

            case "accept":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage : /friend accept <joueur>");
                    return;
                }
                handleAccept(player, profile, args[1]);
                break;

            case "deny":
            case "refuse":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage : /friend deny <joueur>");
                    return;
                }
                handleDeny(player, profile, args[1]);
                break;

            case "remove":
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage : /friend remove <joueur>");
                    return;
                }
                handleRemove(player, profile, args[1]);
                break;

            case "list":
                handleList(player, profile);
                break;

            case "requests":
            case "pending":
                handleRequests(player, profile);
                break;

            default:
                sendUsage(player);
        }
    }

    private void handleAdd(ProxiedPlayer sender, PlayerProfile senderProfile, String targetName) {
        if (targetName.equalsIgnoreCase(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous ajouter vous-meme.");
            return;
        }

        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Joueur inconnu : " + targetName
                    + " (il doit s'etre connecte au moins une fois sur le reseau).");
            return;
        }
        UUID targetUuid = targetUuidOpt.get();

        if (senderProfile.getFriends().contains(targetUuid)) {
            sender.sendMessage(ChatColor.YELLOW + targetName + " est deja dans votre liste d'amis.");
            return;
        }
        if (senderProfile.getOutgoingRequests().contains(targetUuid)) {
            sender.sendMessage(ChatColor.YELLOW + "Demande deja envoyee a " + targetName + ".");
            return;
        }

        PlayerProfile targetProfile = dm.getProfile(targetUuid);

        // Si la cible nous avait deja envoye une demande, on l'accepte directement
        if (senderProfile.getIncomingRequests().contains(targetUuid)) {
            acceptFriendship(sender, senderProfile, targetUuid, targetProfile);
            return;
        }

        senderProfile.getOutgoingRequests().add(targetUuid);
        targetProfile.getIncomingRequests().add(sender.getUniqueId());
        dm.saveProfile(senderProfile);
        dm.saveProfile(targetProfile);

        sender.sendMessage(ChatColor.GREEN + "Demande d'ami envoyee a " + targetName + ".");

        ProxiedPlayer targetPlayer = plugin.getProxy().getPlayer(targetUuid);
        if (targetPlayer != null) {
            notifyIncomingRequest(targetPlayer, sender.getName());
        }
    }

    private void notifyIncomingRequest(ProxiedPlayer target, String fromName) {
        TextComponent message = new TextComponent(ChatColor.GOLD + fromName + " vous a envoye une demande d'ami. ");

        TextComponent accept = new TextComponent(ChatColor.GREEN + "[Accepter]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + fromName));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(new ComponentBuilder("Cliquez pour accepter").create())));

        TextComponent deny = new TextComponent(ChatColor.RED + " [Refuser]");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny " + fromName));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(new ComponentBuilder("Cliquez pour refuser").create())));

        message.addExtra(accept);
        message.addExtra(deny);
        target.sendMessage(message);
    }

    private void handleAccept(ProxiedPlayer sender, PlayerProfile senderProfile, String fromName) {
        DataManager dm = plugin.getDataManager();
        Optional<UUID> fromUuidOpt = dm.resolveUuid(fromName);
        if (fromUuidOpt.isEmpty() || !senderProfile.getIncomingRequests().contains(fromUuidOpt.get())) {
            sender.sendMessage(ChatColor.RED + "Aucune demande en attente de " + fromName + ".");
            return;
        }
        UUID fromUuid = fromUuidOpt.get();
        PlayerProfile fromProfile = dm.getProfile(fromUuid);
        acceptFriendship(sender, senderProfile, fromUuid, fromProfile);
    }

    private void acceptFriendship(ProxiedPlayer sender, PlayerProfile senderProfile, UUID otherUuid, PlayerProfile otherProfile) {
        senderProfile.getIncomingRequests().remove(otherUuid);
        senderProfile.getOutgoingRequests().remove(otherUuid);
        otherProfile.getIncomingRequests().remove(sender.getUniqueId());
        otherProfile.getOutgoingRequests().remove(sender.getUniqueId());

        senderProfile.getFriends().add(otherUuid);
        otherProfile.getFriends().add(sender.getUniqueId());

        DataManager dm = plugin.getDataManager();
        dm.saveProfile(senderProfile);
        dm.saveProfile(otherProfile);

        String otherName = otherProfile.getName() != null ? otherProfile.getName() : "?";
        sender.sendMessage(ChatColor.GREEN + "Vous etes maintenant ami avec " + otherName + " !");

        ProxiedPlayer otherPlayer = plugin.getProxy().getPlayer(otherUuid);
        if (otherPlayer != null) {
            otherPlayer.sendMessage(ChatColor.GREEN + sender.getName() + " a accepte votre demande d'ami !");
        }
    }

    private void handleDeny(ProxiedPlayer sender, PlayerProfile senderProfile, String fromName) {
        DataManager dm = plugin.getDataManager();
        Optional<UUID> fromUuidOpt = dm.resolveUuid(fromName);
        if (fromUuidOpt.isEmpty() || !senderProfile.getIncomingRequests().remove(fromUuidOpt.get())) {
            sender.sendMessage(ChatColor.RED + "Aucune demande en attente de " + fromName + ".");
            return;
        }
        UUID fromUuid = fromUuidOpt.get();
        PlayerProfile fromProfile = dm.getProfile(fromUuid);
        fromProfile.getOutgoingRequests().remove(sender.getUniqueId());

        dm.saveProfile(senderProfile);
        dm.saveProfile(fromProfile);

        sender.sendMessage(ChatColor.YELLOW + "Demande de " + fromName + " refusee.");

        ProxiedPlayer fromPlayer = plugin.getProxy().getPlayer(fromUuid);
        if (fromPlayer != null) {
            fromPlayer.sendMessage(ChatColor.YELLOW + sender.getName() + " a refuse votre demande d'ami.");
        }
    }

    private void handleRemove(ProxiedPlayer sender, PlayerProfile senderProfile, String targetName) {
        DataManager dm = plugin.getDataManager();
        Optional<UUID> targetUuidOpt = dm.resolveUuid(targetName);
        if (targetUuidOpt.isEmpty() || !senderProfile.getFriends().remove(targetUuidOpt.get())) {
            sender.sendMessage(ChatColor.RED + targetName + " n'est pas dans votre liste d'amis.");
            return;
        }
        UUID targetUuid = targetUuidOpt.get();
        PlayerProfile targetProfile = dm.getProfile(targetUuid);
        targetProfile.getFriends().remove(sender.getUniqueId());

        dm.saveProfile(senderProfile);
        dm.saveProfile(targetProfile);

        sender.sendMessage(ChatColor.YELLOW + targetName + " a ete retire de votre liste d'amis.");
    }

    private void handleList(ProxiedPlayer sender, PlayerProfile profile) {
        if (profile.getFriends().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Vous n'avez pas encore d'amis. Utilisez /friend add <joueur>.");
            return;
        }
        DataManager dm = plugin.getDataManager();
        sender.sendMessage(ChatColor.GOLD + "=== Votre liste d'amis (" + profile.getFriends().size() + ") ===");
        for (UUID uuid : profile.getFriends()) {
            PlayerProfile friendProfile = dm.getProfile(uuid);
            String name = friendProfile.getName() != null ? friendProfile.getName() : uuid.toString();
            ProxiedPlayer online = plugin.getProxy().getPlayer(uuid);
            if (online != null) {
                String server = online.getServer() != null ? online.getServer().getInfo().getName() : "?";
                sender.sendMessage(ChatColor.GREEN + "- " + name + ChatColor.GRAY + " (en ligne sur " + server + ")");
            } else {
                sender.sendMessage(ChatColor.DARK_GRAY + "- " + name + " (hors ligne)");
            }
        }
    }

    private void handleRequests(ProxiedPlayer sender, PlayerProfile profile) {
        if (profile.getIncomingRequests().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Vous n'avez aucune demande d'ami en attente.");
            return;
        }
        DataManager dm = plugin.getDataManager();
        sender.sendMessage(ChatColor.GOLD + "=== Demandes en attente ===");
        for (UUID uuid : profile.getIncomingRequests()) {
            PlayerProfile fromProfile = dm.getProfile(uuid);
            String name = fromProfile.getName() != null ? fromProfile.getName() : uuid.toString();
            sender.sendMessage(ChatColor.AQUA + "- " + name
                    + ChatColor.GREEN + "  /friend accept " + name
                    + ChatColor.RED + "  /friend deny " + name);
        }
    }

    private void sendUsage(ProxiedPlayer player) {
        player.sendMessage(ChatColor.GOLD + "=== Commandes /friend ===");
        player.sendMessage(ChatColor.AQUA + "/friend add <joueur>" + ChatColor.GRAY + " - Envoyer une demande d'ami");
        player.sendMessage(ChatColor.AQUA + "/friend accept <joueur>" + ChatColor.GRAY + " - Accepter une demande");
        player.sendMessage(ChatColor.AQUA + "/friend deny <joueur>" + ChatColor.GRAY + " - Refuser une demande");
        player.sendMessage(ChatColor.AQUA + "/friend remove <joueur>" + ChatColor.GRAY + " - Retirer un ami");
        player.sendMessage(ChatColor.AQUA + "/friend list" + ChatColor.GRAY + " - Voir votre liste d'amis");
        player.sendMessage(ChatColor.AQUA + "/friend requests" + ChatColor.GRAY + " - Voir les demandes en attente");
    }
}
