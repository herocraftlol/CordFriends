# CrossFriends-GUI — module Spigot/Paper (interface graphique)

Petit plugin **Spigot/Paper** à installer sur **chaque serveur backend** (et non sur le proxy). Il n'a aucune logique propre : il affiche un inventaire et relaie les clics au plugin `crossfriends-bungee`, qui reste seul responsable des données (amis, blocages, courrier...). Ce module est **optionnel** : sans lui, toutes les commandes (`/friend`, `/msg`, `/mail`) du module BungeeCord continuent de fonctionner normalement, seule `/friend gui` n'aura pas d'effet visuel.

## Fonctionnement

1. Le joueur tape `/friend gui` (commande gérée par le proxy).
2. Le proxy envoie la liste de ses amis (nom, statut, serveur) à ce plugin via un canal de plugin-messaging (`crossfriends:main`).
3. Ce plugin ouvre un inventaire avec une tête de joueur par ami :
   - **Clic gauche** sur un ami en ligne → demande au proxy de vous transférer sur son serveur, puis vous téléporte à côté de lui une fois arrivé.
   - **Clic droit** → affiche un message cliquable qui pré-remplit votre chat avec `/msg <ami> ` pour lui écrire directement (aucune saisie de pseudo à refaire).

## Pré-requis important : activer le mode proxy

Sur **chaque serveur backend**, dans `spigot.yml`, la section `settings` doit contenir :

```yaml
settings:
  bungeecord: true
```

Sans cela, le serveur backend ignore les plugin-messages venant du proxy et l'interface ne s'ouvrira pas (un avertissement apparaît dans la console au démarrage si ce réglage est manquant).

## Compilation

```bash
mvn clean package
```

Le `pom.xml` compile contre `paper-api` (compatible Spigot). Si votre serveur est un Spigot/CraftBukkit pur (sans Paper), remplacez la dépendance par `org.spigotmc:spigot-api` correspondant à votre version, construite via BuildTools — voir les commentaires dans `pom.xml`.

## Installation

1. Copiez `crossfriends-spigot.jar` dans `plugins/` de **chaque** serveur backend où vous voulez que `/friend gui` fonctionne.
2. Vérifiez `bungeecord: true` dans `spigot.yml` (voir ci-dessus), redémarrez le serveur si vous venez de le modifier.
3. Redémarrez le serveur.
4. Le plugin `crossfriends-bungee` doit déjà être installé et actif sur le proxy.

## Limites connues

- Pas de pagination : au-delà de 54 amis, seuls les 54 premiers sont affichés (à étendre facilement dans `FriendsGuiMessenger`).
- La téléportation après changement de serveur cible le joueur par son pseudo (`Bukkit.getPlayerExact`) : s'il s'est déconnecté entre-temps, un message d'erreur est affiché à la place.
