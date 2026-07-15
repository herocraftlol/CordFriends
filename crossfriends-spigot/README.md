# CordFriends-GUI — module Spigot/Paper (interface graphique)

Petit plugin **Spigot/Paper** à installer sur **chaque serveur backend** (et non sur le proxy). Il n'a aucune logique propre : il affiche un inventaire et relaie les clics au plugin `CordFriends-Bungee`, qui reste seul responsable des données (amis, blocages, courrier...). Ce module est **optionnel** : sans lui, toutes les commandes (`/friend`, `/msg`, `/mail`) du module BungeeCord continuent de fonctionner normalement, seule `/friend gui` n'aura pas d'effet visuel.

## Fonctionnement

1. Le joueur tape `/friend gui` (commande gérée par le proxy), **ou** déclenche le menu depuis ce serveur Spigot (voir section suivante).
2. Le proxy envoie la liste de ses amis (nom, statut, serveur) à ce plugin via un canal de plugin-messaging (`crossfriends:main`).
3. Ce plugin ouvre un inventaire avec une tête de joueur par ami :
   - **Clic gauche** sur un ami en ligne → demande au proxy de vous transférer sur son serveur, puis vous téléporte à côté de lui une fois arrivé.
   - **Clic droit** → affiche un message cliquable qui pré-remplit votre chat avec `/msg <ami> ` pour lui écrire directement (aucune saisie de pseudo à refaire).

## Ouvrir le menu depuis ce serveur (ChestCommands, `/execute as ... run`)

`/friend` (toutes les sous-commandes : `add`, `accept`, `list`...) reste géré par le **proxy** BungeeCord. Mais certains outils (ChestCommands, la commande vanilla `/execute`) n'exécutent que des commandes connues du **serveur Spigot** sur lequel ils tournent, pas celles du proxy.

Pour cette raison, ce module enregistre **aussi** une commande `/friend` côté Spigot, qui ne gère qu'un seul cas : `/friend gui` (et son alias `/friend menu`). Elle relaie simplement la demande au proxy via plugin-messaging (action `OPEN_GUI_REQUEST`), exactement comme le ferait un clic dans le menu — la logique d'amitié reste entièrement côté proxy.

Cela permet :

- **Avec ChestCommands** : configurez une action de type "joueur" (`player: friend gui`) sur un item de menu pour ouvrir directement la liste d'amis.
- **Avec la commande vanilla** :
  ```
  /execute as <joueur ou sélecteur> run friend gui
  ```
  par exemple `/execute as @a run friend gui` ou `/execute as Pseudo run friend gui`.

Aucune permission spécifique n'est requise par défaut (la commande répond simplement "seul /friend gui est géré ici" pour tout autre sous-commande, afin de rappeler que `add`/`accept`/`list`/etc. doivent être tapés normalement, traités par le proxy).

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
4. Le plugin `CordFriends-Bungee` doit déjà être installé et actif sur le proxy.

## Limites connues

- Pas de pagination : au-delà de 54 amis, seuls les 54 premiers sont affichés (à étendre facilement dans `FriendsGuiMessenger`).
- La téléportation après changement de serveur cible le joueur par son pseudo (`Bukkit.getPlayerExact`) : s'il s'est déconnecté entre-temps, un message d'erreur est affiché à la place.
