package com.cordfriends.spigot;

import java.util.UUID;

/**
 * Represente une ligne du menu d'amis, recue depuis le proxy BungeeCord
 * via le canal de plugin-messaging "crossfriends:main".
 */
public record FriendEntry(UUID uuid, String name, boolean online, String server) {
}
