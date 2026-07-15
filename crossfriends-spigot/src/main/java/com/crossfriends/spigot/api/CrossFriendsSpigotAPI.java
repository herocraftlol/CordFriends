package com.crossfriends.spigot.api;

import com.crossfriends.spigot.CrossFriendsSpigotPlugin;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

/**
 * API publique permettant a n'importe quel autre plugin Spigot/Paper d'ouvrir le menu
 * d'amis CrossFriends pour un joueur, sans passer par le chat ni par la commande
 * {@code /friend gui} (par exemple depuis un clic sur un item, un NPC, un menu personnel, etc.).
 * <p>
 * Utilisation depuis un autre plugin :
 * <pre>{@code
 * // Optionnel mais recommande : verifier que CrossFriends est bien installe et actif
 * if (Bukkit.getPluginManager().isPluginEnabled("CrossFriends-GUI")) {
 *     CrossFriendsSpigotAPI.openFriendsGui(player);
 * }
 * }</pre>
 * Ajoutez la dependance a {@code plugin.yml} du plugin appelant :
 * <pre>{@code
 * depend: [CrossFriends-GUI]
 * // ou, si l'integration doit rester optionnelle :
 * softdepend: [CrossFriends-GUI]
 * }</pre>
 * <p>
 * Fonctionnement : comme ce module Spigot ne connait pas la liste d'amis (source de verite
 * cote proxy BungeeCord), cette methode se contente de relayer une demande d'ouverture
 * ("GUI_REQUEST") au proxy via le canal de plugin-messaging "crossfriends:main". C'est le
 * proxy qui construit la liste et renvoie ensuite le paquet "OPEN_GUI" qui ouvre reellement
 * l'inventaire (voir {@link com.crossfriends.spigot.FriendsGuiMessenger}).
 * <p>
 * Necessite que le serveur soit bien en mode proxy (bungeecord: true dans spigot.yml / velocity
 * active si Velocity) et que le joueur soit connecte, sans quoi le message sera silencieusement
 * ignore par le proxy.
 */
public final class CrossFriendsSpigotAPI {

    private CrossFriendsSpigotAPI() {
    }

    /**
     * Demande au proxy d'ouvrir le menu d'amis pour ce joueur.
     * <p>
     * Cette methode est asynchrone au sens reseau : elle envoie simplement la demande et
     * retourne immediatement. Si le joueur a des amis et est bien connecte, le proxy
     * renverra un paquet qui ouvrira l'inventaire sur ce serveur (l'ouverture effective se
     * fera donc quelques dizaines de millisecondes plus tard, sur le thread principal).
     * En cas de probleme (aucun ami, joueur non connecte au proxy...), le joueur recevra
     * directement un message d'erreur envoye par le proxy.
     *
     * @param player joueur pour lequel ouvrir le menu d'amis
     * @throws IllegalStateException si le plugin CrossFriends-GUI n'est pas charge/actif
     */
    public static void openFriendsGui(Player player) {
        CrossFriendsSpigotPlugin plugin = CrossFriendsSpigotPlugin.getInstance();
        if (plugin == null) {
            throw new IllegalStateException("CrossFriends-GUI n'est pas actif sur ce serveur.");
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GUI_REQUEST");
        player.sendPluginMessage(plugin, CrossFriendsSpigotPlugin.CHANNEL, out.toByteArray());
    }
}
