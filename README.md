# CrossFriends — Amis, messages prives et courrier inter-serveur

Deux modules Maven independants :

| Dossier | Ou l'installer | Obligatoire ? |
|---|---|---|
| `crossfriends-bungee/` | Sur le **proxy BungeeCord** | Oui — contient toute la logique |
| `crossfriends-spigot/` | Sur **chaque serveur backend** (Spigot/Paper) | Non — ajoute juste l'interface graphique `/friend gui` |

Voir le `README.md` de chaque dossier pour le detail des commandes, de la configuration et des instructions de compilation/installation.

## Demarrage rapide

```bash
cd crossfriends-bungee && mvn clean package
cd ../crossfriends-spigot && mvn clean package
```

Puis :
1. `crossfriends-bungee/target/crossfriends-bungee.jar` → `plugins/` du proxy BungeeCord → redemarrer le proxy.
2. (Optionnel) `crossfriends-spigot/target/crossfriends-spigot.jar` → `plugins/` de chaque serveur backend ou vous voulez l'interface graphique → verifier `bungeecord: true` dans `spigot.yml` → redemarrer.

## Ce que ca apporte

- **Amis** : `/friend add|accept|deny|remove|list|requests`, avec boutons cliquables a la reception d'une demande.
- **Blocage** : `/friend block|unblock|blocked` — un joueur bloque ne peut plus envoyer de demande d'ami, de message prive ni de mail.
- **Messages prives inter-serveur** : `/msg`, `/r` — livres instantanement si le destinataire est en ligne (n'importe quel serveur), sinon sauvegardes comme courrier.
- **Courrier hors-ligne pour tous** : `/mail send|read|clear` — par defaut ouvert a n'importe quel joueur, ami ou non (reglable).
- **Autocompletion (tab)** sur toutes les commandes : sous-commandes puis noms de joueurs pertinents.
- **Interface graphique** (module Spigot) : `/friend gui` ouvre un menu avec vos amis, pour les rejoindre en un clic ou leur ecrire directement.

Tout fonctionne **entre serveurs** car la logique vit sur le proxy BungeeCord, point unique qui voit l'ensemble du reseau.

## Release

Telechargez les fichiers JAR depuis la page des [releases](https://github.com/herocraftlol/Bungeefriends-Message-Mail/releases).
