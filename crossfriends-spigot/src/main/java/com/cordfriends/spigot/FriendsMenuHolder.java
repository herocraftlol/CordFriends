package com.cordfriends.spigot;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Sert uniquement a reconnaitre, dans le listener de clic, qu'un inventaire ouvert
 * est bien notre menu d'amis (et pas un coffre ou un autre menu), et a retrouver
 * quel ami correspond a quel slot clique.
 */
public class FriendsMenuHolder implements InventoryHolder {

    private final Map<Integer, FriendEntry> slots = new HashMap<>();
    private Inventory inventory;

    public void put(int slot, FriendEntry entry) {
        slots.put(slot, entry);
    }

    public FriendEntry get(int slot) {
        return slots.get(slot);
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
