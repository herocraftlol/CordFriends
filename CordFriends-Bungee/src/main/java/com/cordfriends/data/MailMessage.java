package com.cordfriends.data;

import java.util.UUID;

/**
 * Represente un message laisse a un joueur pour qu'il le lise plus tard
 * (par exemple lorsqu'il etait hors-ligne au moment de l'envoi).
 */
public class MailMessage {

    private UUID senderUuid;
    private String senderName;
    private String message;
    private long timestamp;
    private boolean read;

    /** Constructeur vide requis par Gson pour la deserialisation. */
    public MailMessage() {
    }

    public MailMessage(UUID senderUuid, String senderName, String message, long timestamp) {
        this.senderUuid = senderUuid;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }

    public UUID getSenderUuid() {
        return senderUuid;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
