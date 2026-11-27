package dev.inventarioadmin;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Identifica la ventana de edición del inventario de un jugador. */
final class EditorHolder implements InventoryHolder {
    private final UUID target;
    private Inventory inventory;
    private boolean applyQueued;

    EditorHolder(UUID target) {
        this.target = target;
    }

    UUID target() {
        return target;
    }

    boolean isApplyQueued() {
        return applyQueued;
    }

    void setApplyQueued(boolean applyQueued) {
        this.applyQueued = applyQueued;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
