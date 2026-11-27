package dev.inventarioadmin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Identifica la ventana con la lista de jugadores conectados. */
final class MenuHolder implements InventoryHolder {
    private final int page;
    private final Map<Integer, UUID> players = new HashMap<>();
    private Inventory inventory;

    MenuHolder(int page) {
        this.page = page;
    }

    int page() {
        return page;
    }

    Map<Integer, UUID> players() {
        return players;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
