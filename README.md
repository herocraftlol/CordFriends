# 🤝 CordFriends v1.1.1 - Friend System for BungeeCord

Transform your BungeeCord network with a complete friend system, cross-server private messages, and an intuitive GUI! 🚀


## 📦 Installation

The installation is simple:


### 1️⃣ BungeeCord Side
📂 Place the plugin in the folder:


```
/plugins
```


Then restart your BungeeCord proxy.


### 2️⃣ Spigot / Paper Side
Install the Spigot/Paper version of the plugin on each server in your network.


After installation, restart each server for all features to work.


⚠️ Both parts (BungeeCord + Spigot/Paper) are required for full functionality.


## ✅ Compatibility

🖥️ Compatible with ChestCommands


To open the GUI directly, use the command:


```
op: execute as @s run friend gui
```


## 👥 Friend System

Manage your friend list easily:


```
/friend add
/friend accept
/friend deny
/friend remove
/friend list
/friend requests
```

✨ When a player sends you a friend request, clickable buttons allow you to accept or refuse instantly.


## 🚫 Blocking System

Prevent certain players from contacting you:


```
/friend block
/friend unblock
/friend blocked
```

A blocked player cannot:


❌ Send you a friend request
❌ Send you a private message
❌ Send you mail


## 💬 Cross-Server Private Messages

Communicate with any player on the network:


```
/msg
/r
```

⚡ Messages are sent instantly, even if the player is on another server.


📬 If the recipient is offline, the message is automatically saved as mail.


## ✉️ Offline Mail

Send messages even when a player is disconnected:


```
/mail send
/mail read
/mail clear
```

By default, all players can send mail, whether they are friends or not.


⚙️ This behavior is fully configurable.


## 🎯 Smart Auto-Complete

All commands have TAB completion:


✅ Subcommands
✅ Player names


A much faster and more enjoyable experience.


## 🖥️ GUI Interface

Open the menu with:


```
/friend gui
```

From this interface, you can:


👥 View your friend list
🎮 Join a friend with one click
💬 Send them a private message directly


## 🧩 Developer API — Open the GUI from another plugin

You don't need a player to type `/friend gui` in chat to open the friends menu. Any other
Spigot/Paper plugin on a backend server (for example a plugin that opens the menu when a
player clicks a custom item) can trigger it directly:

```java
// 1. Add the dependency to your plugin.yml
// depend: [CrossFriends-GUI]      // or softdepend: [CrossFriends-GUI] for an optional integration

// 2. Call the API wherever you need it (e.g. in an item click listener)
import com.crossfriends.spigot.api.CrossFriendsSpigotAPI;

@EventHandler
public void onClick(PlayerInteractEvent event) {
    if (isYourFriendsMenuItem(event.getItem())) {
        CrossFriendsSpigotAPI.openFriendsGui(event.getPlayer());
    }
}
```

This simply relays the request to the BungeeCord proxy (the source of truth for friend
data), which builds the friend list and sends back the packet that actually opens the
inventory — exactly like `/friend gui` does, just without going through chat.

If you're writing a plugin for the **BungeeCord proxy itself** (not a backend server) and
already have a `ProxiedPlayer` reference, you can skip the network hop entirely and call
the same service the command uses:

```java
import com.crossfriends.gui.GuiService;

GuiService.Result result = GuiService.openFriendsGui(crossFriendsPluginInstance, proxiedPlayer);
// result is one of: OPENED, NO_FRIENDS, NOT_CONNECTED
```

## 🌐 Network-Wide Functionality

All plugin logic is handled directly by BungeeCord, which centralizes information across your entire network.


This allows you to enjoy all features between all your servers, completely transparently.


✨ One installation on the proxy is enough to synchronize friends, private messages, and mail across the entire network.


## 📥 Download

Download the JAR files from the page [releases](https://github.com/herocraftlol/CordFriends/releases).


## 🛠️ Compilation

```bash
cd crossfriends-bungee && mvn clean package
cd ../crossfriends-spigot && mvn clean package
```
