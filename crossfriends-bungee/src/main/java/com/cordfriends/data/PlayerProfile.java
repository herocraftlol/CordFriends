package com.cordfriends.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Profil persistant d'un joueur : sa liste d'amis, ses demandes d'amis
 * (entrantes et sortantes) et sa boite de reception (courrier hors-ligne).
 * Un fichier JSON par joueur est conserve sur le disque par le DataManager.
 */
public class PlayerProfile {

    private UUID uuid;
    private String name;
    private Set<UUID> friends = new HashSet<>();
    private Set<UUID> incomingRequests = new HashSet<>();
    private Set<UUID> outgoingRequests = new HashSet<>();
    private Set<UUID> blocked = new HashSet<>();
    private List<MailMessage> mailbox = new ArrayList<>();

    /** Constructeur vide requis par Gson pour la deserialisation. */
    public PlayerProfile() {
    }

    public PlayerProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<UUID> getFriends() {
        return friends;
    }

    public Set<UUID> getIncomingRequests() {
        return incomingRequests;
    }

    public Set<UUID> getOutgoingRequests() {
        return outgoingRequests;
    }

    public Set<UUID> getBlocked() {
        return blocked;
    }

    public List<MailMessage> getMailbox() {
        return mailbox;
    }
}
