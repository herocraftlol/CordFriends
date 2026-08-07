# CordFriends — module BungeeCord (proxy)

Plugin **BungeeCord** qui contient toute la logique : amis, blocage, messages privés et courrier hors-ligne, inter-serveur. C'est le seul module obligatoire. Le module `CordFriends-Spigot` (interface graphique) est optionnel et vient en complément.

## Commandes

| Commande | Description |
|---|---|
| `/friend add <joueur>` | Envoyer une demande d'ami |
| `/friend accept <joueur>` | Accepter une demande |
| `/friend deny <joueur>` | Refuser une demande |
| `/friend remove <joueur>` | Retirer un ami |
| `/friend list` | Voir sa liste d'amis (statut en ligne/hors-ligne + serveur) |
| `/friend requests` | Voir les demandes en attente |
| `/friend block <joueur>` | Bloquer un joueur (annule amitie + demandes en cours dans les deux sens) |
| `/friend unblock <joueur>` | Debloquer un joueur |
| `/friend blocked` | Voir les joueurs bloques |
| `/friend gui` (alias `/friend menu`) | Ouvrir l'interface graphique d'amis (necessite le module Spigot) |
| `/msg <joueur> <message>` (alias `/tell`, `/w`, `/pm`) | Message privé inter-serveur ; sauvegardé automatiquement si hors-ligne |
| `/r <message>` | Répondre au dernier interlocuteur |
| `/mail send <joueur> <message>` | Laisser un message a lire plus tard, **a n'importe quel joueur par defaut** |
| `/mail read` | Lire son courrier en attente |
| `/mail clear` | Vider sa boîte de réception |

Toutes les commandes proposent l'**autocompletion (tab)** : sous-commandes, puis noms de joueurs pertinents (amis, demandes en attente, joueurs en ligne...). Par exemple `/friend add pat` + Tab proposera les joueurs en ligne commençant par "pat".

## Qui peut écrire a qui ?

Deux réglages indépendants dans `config.yml` :

```yaml
require-friendship: true        # /msg et /r : il faut etre ami (par defaut oui)
mail-require-friendship: false  # /mail send : ouvert a tous par defaut (changez en true pour restreindre aux amis)
notify-on-join: true
```

- La permission `cordfriends.bypass` contourne ces deux restrictions (utile pour le staff).
- **Le blocage est toujours prioritaire** : un joueur bloqué ne peut envoyer ni message privé, ni mail, ni demande d'ami, même si `mail-require-friendship` est à `false`.

## L'interface graphique (`/friend gui`)

Le proxy seul ne peut pas ouvrir d'inventaire en jeu : c'est le rôle du module **CordFriends-Spigot**, à installer en plus sur chaque serveur backend. `/friend gui` ouvre un inventaire listant vos amis (tête de joueur, statut, serveur). Clic gauche sur un ami en ligne = vous êtes transféré sur son serveur puis téléporté à côté de lui. Clic droit = un lien cliquable pré-remplit votre chat avec `/msg <ami> ` pour lui écrire directement. Voir le README du dossier `CordFriends-Spigot` pour l'installation.

## Stockage des données

Un fichier JSON par joueur dans `plugins/CordFriends/playerdata/<uuid>.json` (amis, demandes, blocages, courrier). Un fichier `names.json` retient la correspondance pseudo → UUID. Sauvegarde automatique toutes les 5 minutes et à l'arrêt du proxy.

## Compilation

Nécessite Maven et une connexion internet (pour télécharger `bungeecord-api` depuis Maven Central) :

```bash
mvn clean package
```

Le fichier `target/crossfriends-bungee.jar` est généré.

## Installation

1. Copiez `crossfriends-bungee.jar` dans `plugins/` de votre **proxy BungeeCord** (pas des serveurs Spigot/Paper).
2. Redémarrez le proxy.
3. Vérifiez dans la console que `CordFriends active...` apparaît.

## Pistes d'évolution possibles

- Pagination du menu graphique au-delà de 54 amis.
- Migration du stockage JSON vers MySQL pour un très gros réseau (tout passe par `DataManager`).
- Expiration automatique des vieux messages de la boîte de réception.
