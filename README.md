# CrossFriends — amis, messages privés et courrier inter-serveur

Deux modules Maven independants :

| Dossier | Ou l'installer | Obligatoire ? |
|---|---|---|
| `crossfriends-bungee/` | Sur le **proxy BungeeCord** | Oui — contient toute la logique |
| `crossfriends-spigot/` | Sur **chaque serveur backend** (Spigot/Paper) | Non — ajoute juste l'interface graphique `/friend gui` |

Voir le `README.md` de chaque dossier pour le détail des commandes, de la configuration et des instructions de compilation/installation.

## Démarrage rapide

```bash
cd crossfriends-bungee && mvn clean package
cd ../crossfriends-spigot && mvn clean package
```

Puis :
1. `crossfriends-bungee/target/crossfriends-bungee.jar` → `plugins/` du proxy BungeeCord → redémarrer le proxy.
2. (Optionnel) `crossfriends-spigot/target/crossfriends-spigot.jar` → `plugins/` de chaque serveur backend où vous voulez l'interface graphique → vérifier `bungeecord: true` dans `spigot.yml` → redémarrer.

## Ce que ça apporte

- **Amis** : `/friend add|accept|deny|remove|list|requests`, avec boutons cliquables à la réception d'une demande.
- **Blocage** : `/friend block|unblock|blocked` — un joueur bloqué ne peut plus envoyer de demande d'ami, de message privé ni de mail.
- **Messages privés inter-serveur** : `/msg`, `/r` — livrés instantanément si le destinataire est en ligne (n'importe quel serveur), sinon sauvegardés comme courrier.
- **Courrier hors-ligne pour tous** : `/mail send|read|clear` — par défaut ouvert à n'importe quel joueur, ami ou non (réglable).
- **Autocomplétion (tab)** sur toutes les commandes : sous-commandes puis noms de joueurs pertinents.
- **Interface graphique** (module Spigot) : `/friend gui` ouvre un menu avec vos amis, pour les rejoindre en un clic ou leur écrire directement.

Tout fonctionne **entre serveurs** car la logique vit sur le proxy BungeeCord, point unique qui voit l'ensemble du réseau.
