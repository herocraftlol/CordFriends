package com.cordfriends.data;

import com.cordfriends.CrossFriendsPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gere la persistance des profils joueurs (un fichier JSON par joueur), la table
 * de correspondance nom -> UUID (pour retrouver les joueurs hors-ligne), et le
 * dernier interlocuteur de chaque joueur (pour la commande /r).
 *
 * Comme ce plugin tourne uniquement sur le proxy BungeeCord (un seul processus
 * pour tout le reseau), un simple stockage par fichiers JSON suffit : il n'y a
 * pas de probleme de concurrence entre plusieurs serveurs.
 */
public class DataManager {

    private final CrossFriendsPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final File playerDataFolder;
    private final File namesFile;

    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameToUuid = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> lastConversant = new ConcurrentHashMap<>();

    public DataManager(CrossFriendsPlugin plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
        this.namesFile = new File(plugin.getDataFolder(), "names.json");
        loadNames();
    }

    // ---------------------------------------------------------------
    // Table de correspondance nom <-> UUID
    // ---------------------------------------------------------------

    private synchronized void loadNames() {
        if (!namesFile.exists()) {
            return;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(namesFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> raw = gson.fromJson(reader, type);
            if (raw != null) {
                raw.forEach((name, uuid) -> nameToUuid.put(name, UUID.fromString(uuid)));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de charger names.json : " + e.getMessage());
        }
    }

    public synchronized void saveNames() {
        Map<String, String> raw = new HashMap<>();
        nameToUuid.forEach((name, uuid) -> raw.put(name, uuid.toString()));
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(namesFile), StandardCharsets.UTF_8)) {
            gson.toJson(raw, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de sauvegarder names.json : " + e.getMessage());
        }
    }

    /** Enregistre/actualise la correspondance nom -> UUID pour un joueur (appele a la connexion). */
    public void registerName(UUID uuid, String name) {
        nameToUuid.put(name.toLowerCase(Locale.ROOT), uuid);
        saveNames();
    }

    /**
     * Tente de retrouver l'UUID d'un joueur a partir de son nom : d'abord parmi
     * les joueurs actuellement connectes sur le reseau (n'importe quel serveur),
     * puis dans la table des joueurs deja vus par le passe.
     */
    @SuppressWarnings("deprecation")
    public Optional<UUID> resolveUuid(String name) {
        ProxiedPlayer online = plugin.getProxy().getPlayer(name);
        if (online != null) {
            return Optional.of(online.getUniqueId());
        }
        UUID uuid = nameToUuid.get(name.toLowerCase(Locale.ROOT));
        return Optional.ofNullable(uuid);
    }

    // ---------------------------------------------------------------
    // Profils joueurs
    // ---------------------------------------------------------------

    public PlayerProfile getProfile(UUID uuid) {
        return profiles.computeIfAbsent(uuid, this::loadProfile);
    }

    private PlayerProfile loadProfile(UUID uuid) {
        File file = new File(playerDataFolder, uuid.toString() + ".json");
        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                PlayerProfile loaded = gson.fromJson(reader, PlayerProfile.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Erreur de lecture du profil " + uuid + " : " + e.getMessage());
            }
        }
        return new PlayerProfile(uuid, null);
    }

    public synchronized void saveProfile(PlayerProfile profile) {
        File file = new File(playerDataFolder, profile.getUuid().toString() + ".json");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(profile, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Erreur de sauvegarde du profil " + profile.getUuid() + " : " + e.getMessage());
        }
    }

    /** Sauvegarde tous les profils actuellement en cache (appele a l'arret du plugin et periodiquement). */
    public void saveAll() {
        for (PlayerProfile profile : profiles.values()) {
            saveProfile(profile);
        }
        saveNames();
    }

    /** Sauvegarde puis retire un profil du cache (appele quand un joueur se deconnecte du reseau). */
    public void unload(UUID uuid) {
        PlayerProfile profile = profiles.remove(uuid);
        if (profile != null) {
            saveProfile(profile);
        }
    }

    /** Vrai si a a bloque b OU si b a bloque a (le blocage est toujours bidirectionnel en pratique). */
    public boolean isBlocked(UUID a, UUID b) {
        PlayerProfile profileA = getProfile(a);
        if (profileA.getBlocked().contains(b)) {
            return true;
        }
        PlayerProfile profileB = getProfile(b);
        return profileB.getBlocked().contains(a);
    }

    // ---------------------------------------------------------------
    // Dernier interlocuteur (pour /r), garde uniquement en memoire
    // ---------------------------------------------------------------

    public void setLastConversant(UUID a, UUID b) {
        lastConversant.put(a, b);
    }

    public UUID getLastConversant(UUID uuid) {
        return lastConversant.get(uuid);
    }
}
