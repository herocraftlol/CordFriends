package com.crossfriends.gui;

import com.crossfriends.CrossFriendsPlugin;
import com.crossfriends.data.DataManager;
import com.crossfriends.data.PlayerProfile;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * Construit et envoie le paquet d'ouverture du menu d'amis (OPEN_GUI) vers le serveur backend
 * sur lequel se trouve le joueur.
 * <p>
 * Point d'entree unique et reutilisable : la commande {@code /friend gui} passe par ici,
 * mais n'importe quel autre plugin tournant sur le proxy peut aussi appeler
 * {@link #openFriendsGui(CrossFriendsPlugin, ProxiedPlayer)} directement (par exemple depuis
 * un evenement personnalise, un clic detecte cote backend et relaye au proxy, etc.),
 * sans devoir passer par le chat du joueur.
 */
public final class GuiService {

    public enum Result {
        /** Le paquet OPEN_GUI a bien ete envoye au serveur backend du joueur. */
        OPENED,
        /** Le joueur n'a aucun ami, rien n'a ete envoye. */
        NO_FRIENDS,
        /** Le joueur n'est actuellement connecte a aucun serveur backend. */
        NOT_CONNECTED
    }

    private GuiService() {
    }

    /**
     * Ouvre (demande l'ouverture d)le menu d'amis pour ce joueur, quel que soit
     * l'appelant (commande, autre plugin, pont plugin-messaging...).
     *
     * @param plugin instance du plugin CrossFriends
     * @param player joueur pour lequel ouvrir le menu
     * @return le resultat de l'operation, a utiliser par l'appelant pour informer le joueur si besoin
     */
    public static Result openFriendsGui(CrossFriendsPlugin plugin, ProxiedPlayer player) {
        DataManager dm = plugin.getDataManager();
        PlayerProfile profile = dm.getProfile(player.getUniqueId());

        if (profile.getFriends().isEmpty()) {
            return Result.NO_FRIENDS;
        }
        if (player.getServer() == null) {
            return Result.NOT_CONNECTED;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("OPEN_GUI");
        out.writeInt(profile.getFriends().size());

        for (UUID uuid : profile.getFriends()) {
            PlayerProfile friendProfile = dm.getProfile(uuid);
            String name = friendProfile.getName() != null ? friendProfile.getName() : uuid.toString();
            ProxiedPlayer online = plugin.getProxy().getPlayer(uuid);
            boolean isOnline = online != null;
            String server = (isOnline && online.getServer() != null) ? online.getServer().getInfo().getName() : "";

            out.writeUTF(uuid.toString());
            out.writeUTF(name);
            out.writeBoolean(isOnline);
            out.writeUTF(server);
        }

        player.getServer().sendData(CrossFriendsPlugin.CHANNEL, out.toByteArray());
        return Result.OPENED;
    }
}
