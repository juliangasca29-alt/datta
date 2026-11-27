package dev.inventarioadmin;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Gestiona los clics en las ventanas del plugin. */
final class GuiListener implements Listener {
    private final InventarioPlugin plugin;

    GuiListener(InventarioPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player admin)) {
            return;
        }
        InventoryHolder holder = e.getView().getTopInventory().getHolder(false);
        if (holder instanceof EditorHolder eh) {
            editorClick(e, admin, eh);
        } else if (holder instanceof MenuHolder mh) {
            menuClick(e, admin, mh);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        Inventory top = e.getView().getTopInventory();
        InventoryHolder holder = top.getHolder(false);
        if (holder instanceof MenuHolder) {
            e.setCancelled(true);
            return;
        }
        if (!(holder instanceof EditorHolder eh) || !(e.getWhoClicked() instanceof Player admin)) {
            return;
        }
        for (int raw : e.getRawSlots()) {
            if (raw < top.getSize() && !Editor.editable(raw)) {
                e.setCancelled(true);
                return;
            }
        }
        queueApply(admin, eh);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder(false) instanceof EditorHolder eh && eh.isApplyQueued()) {
            Player target = Bukkit.getPlayer(eh.target());
            if (target != null) {
                Editor.apply(e.getInventory(), target);
            }
        }
    }

    // ------------------------------------------------------------------ editor

    private void editorClick(InventoryClickEvent e, Player admin, EditorHolder eh) {
        if (!admin.hasPermission(Msg.PERM)) {
            e.setCancelled(true);
            admin.closeInventory();
            return;
        }
        // Evita sacar los paneles decorativos con doble clic.
        if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            e.setCancelled(true);
            return;
        }
        int raw = e.getRawSlot();
        if (raw >= 0 && raw < e.getView().getTopInventory().getSize() && !Editor.editable(raw)) {
            e.setCancelled(true);
            button(e, admin, eh, raw);
            return;
        }
        queueApply(admin, eh);
    }

    private void button(InventoryClickEvent e, Player admin, EditorHolder eh, int raw) {
        Player target = Bukkit.getPlayer(eh.target());
        if (target == null) {
            Msg.send(admin, "El jugador se desconectó.", NamedTextColor.RED);
            Bukkit.getScheduler().runTask(plugin, () -> admin.closeInventory());
            return;
        }
        Inventory inv = e.getView().getTopInventory();
        switch (raw) {
            case Editor.BTN_REFRESH -> Editor.fill(inv, target);
            case Editor.BTN_ENDER -> Bukkit.getScheduler().runTask(plugin, () -> {
                Player t = Bukkit.getPlayer(eh.target());
                if (t != null) {
                    admin.openInventory(t.getEnderChest());
                }
            });
            case Editor.BTN_CLEAR -> {
                if (e.isShiftClick()) {
                    target.getInventory().clear();
                    target.updateInventory();
                    Editor.fill(inv, target);
                    Msg.send(admin, "Inventario de " + target.getName() + " vaciado.", NamedTextColor.YELLOW);
                } else {
                    Msg.send(admin, "Usa Shift + clic para vaciar el inventario.", NamedTextColor.GRAY);
                }
            }
            case Editor.BTN_CLOSE -> Bukkit.getScheduler().runTask(plugin, () -> admin.closeInventory());
            default -> { }
        }
    }

    /** Aplica los cambios de la ventana al jugador un tick después del clic. */
    private void queueApply(Player admin, EditorHolder eh) {
        if (eh.isApplyQueued()) {
            return;
        }
        eh.setApplyQueued(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            eh.setApplyQueued(false);
            Player target = Bukkit.getPlayer(eh.target());
            if (target == null) {
                Msg.send(admin, "El jugador se desconectó.", NamedTextColor.RED);
                admin.closeInventory();
                return;
            }
            Editor.apply(eh.getInventory(), target);
        });
    }

    // -------------------------------------------------------------------- menú

    private void menuClick(InventoryClickEvent e, Player admin, MenuHolder mh) {
        e.setCancelled(true);
        if (!admin.hasPermission(Msg.PERM)) {
            admin.closeInventory();
            return;
        }
        int raw = e.getRawSlot();
        if (raw < 0 || raw >= e.getView().getTopInventory().getSize()) {
            return;
        }
        if (raw == Menu.BTN_PREV && mh.page() > 0) {
            Bukkit.getScheduler().runTask(plugin, () -> Menu.open(admin, mh.page() - 1));
            return;
        }
        if (raw == Menu.BTN_NEXT) {
            Bukkit.getScheduler().runTask(plugin, () -> Menu.open(admin, mh.page() + 1));
            return;
        }
        if (raw == Menu.BTN_CLOSE) {
            Bukkit.getScheduler().runTask(plugin, () -> admin.closeInventory());
            return;
        }
        java.util.UUID id = mh.players().get(raw);
        if (id == null) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player target = Bukkit.getPlayer(id);
            if (target == null) {
                Msg.send(admin, "Ese jugador ya no está conectado.", NamedTextColor.RED);
                Menu.open(admin, mh.page());
            } else if (e.isRightClick()) {
                admin.openInventory(target.getEnderChest());
            } else {
                Editor.open(plugin, admin, target);
            }
        });
    }
}
