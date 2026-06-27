# CrossFriends — plugin BungeeCord (amis / messages privés / courrier inter-serveur)

Plugin **BungeeCord** (proxy) pour Minecraft 1.21 qui ajoute :

- un **système d'amis** avec demandes, acceptation/refus, suppression et liste ;
- des **messages privés** entre joueurs, livrés instantanément **peu importe le sous-serveur** sur lequel se trouvent les deux joueurs (puisque c'est le proxy qui voit tout le réseau) ;
- un **courrier hors-ligne** : si le destinataire n'est pas connecté, le message est sauvegardé et lui est présenté à sa prochaine connexion, où qu'il se reconnecte.

Comme le plugin tourne **sur le proxy BungeeCord** (et pas sur chaque serveur Spigot/Paper), il fonctionne nativement entre tous vos serveurs sans avoir besoin de canaux de plugin-messaging ou de base de données partagée : un seul processus gère tout le réseau.

## Commandes

| Commande | Description |
|---|---|
| `/friend add <joueur>` | Envoyer une demande d'ami |
| `/friend accept <joueur>` | Accepter une demande |
| `/friend deny <joueur>` | Refuser une demande |
| `/friend remove <joueur>` | Retirer un ami |
| `/friend list` | Voir sa liste d'amis (avec statut en ligne/hors-ligne et serveur) |
| `/friend requests` | Voir les demandes en attente |
| `/msg <joueur> <message>` (alias `/tell`, `/w`, `/pm`) | Message privé inter-serveur ; sauvegardé automatiquement si le joueur est hors-ligne |
| `/r <message>` | Répondre au dernier interlocuteur |
| `/mail send <joueur> <message>` | Laisser explicitement un message à lire plus tard |
| `/mail read` | Lire son courrier en attente |
| `/mail clear` | Vider sa boîte de réception |

Par défaut, **il faut être ami** pour utiliser `/msg`, `/r` et `/mail send` (configurable, voir plus bas). Les demandes d'amis reçues alors que le joueur est en ligne s'accompagnent de boutons cliquables `[Accepter]` / `[Refuser]`.

## Configuration (`config.yml`)

```yaml
require-friendship: true   # false pour autoriser les messages entre n'importe quels joueurs
notify-on-join: true       # recapitulatif (courrier non lu + demandes en attente) a la connexion
```

La permission `crossfriends.bypass` permet de contourner l'obligation d'amitié (utile pour le staff).

## Stockage des données

Chaque joueur a son propre fichier JSON dans `plugins/CrossFriends/playerdata/<uuid>.json` (liste d'amis, demandes, courrier). Un fichier `names.json` conserve la correspondance pseudo → UUID pour pouvoir cibler des joueurs hors-ligne. Une sauvegarde automatique a lieu toutes les 5 minutes et à l'arrêt du proxy.

## Compilation

Ce projet nécessite **Maven** et une connexion internet (pour télécharger `bungeecord-api` depuis Maven Central) :

```bash
mvn clean package
```

Le fichier `target/crossfriends-bungee.jar` est généré.

## Installation

1. Copiez `crossfriends-bungee.jar` dans le dossier `plugins/` de votre proxy **BungeeCord** (pas dans les serveurs Spigot/Paper individuels).
2. Redémarrez le proxy.
3. Vérifiez dans la console que le message `CrossFriends active...` apparaît.

## Pistes d'évolution possibles

- Tab-completion sur les noms de joueurs connectés.
- Migration du stockage JSON vers MySQL si le réseau grossit beaucoup (la classe `DataManager` est le seul endroit à modifier).
- Limite de taille de la boîte de courrier, ou expiration automatique des vieux messages.
- Intégration Discord (webhook) pour notifier d'un message reçu hors-ligne.
