package dev.inventarioadmin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/** Ventana con las cabezas de los jugadores conectados. */
final class Menu {
    static final int PER_PAGE = 45;
    static final int BTN_PREV = 45;
    static final int BTN_CLOSE = 49;
    static final int BTN_NEXT = 53;

    private Menu() {}

    static void open(Player admin, int page) {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

        int pages = Math.max(1, (online.size() + PER_PAGE - 1) / PER_PAGE);
        int current = Math.max(0, Math.min(page, pages - 1));

        MenuHolder holder = new MenuHolder(current);
        Inventory inv = Bukkit.createInventory(holder, 54,
                Component.text("Jugadores (" + (current + 1) + "/" + pages + ")"));
        holder.setInventory(inv);

        for (int i = 0; i < PER_PAGE; i++) {
            int index = current * PER_PAGE + i;
            if (index >= online.size()) {
                break;
            }
            Player p = online.get(index);
            holder.players().put(i, p.getUniqueId());

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            head.editMeta(SkullMeta.class, meta -> {
                meta.setOwningPlayer(p);
                meta.displayName(Msg.plain(p.getName(), NamedTextColor.YELLOW));
                meta.lore(List.of(
                        Msg.plain("Clic izquierdo: ver y editar inventario", NamedTextColor.GREEN),
                        Msg.plain("Clic derecho: cofre de Ender", NamedTextColor.LIGHT_PURPLE)));
            });
            inv.setItem(i, head);
        }

        ItemStack filler = Ui.filler();
        for (int s = 45; s < 54; s++) {
            inv.setItem(s, filler);
        }
        if (current > 0) {
            inv.setItem(BTN_PREV, Ui.button(Material.ARROW, "Página anterior", NamedTextColor.YELLOW));
        }
        if (current < pages - 1) {
            inv.setItem(BTN_NEXT, Ui.button(Material.ARROW, "Página siguiente", NamedTextColor.YELLOW));
        }
        inv.setItem(BTN_CLOSE, Ui.button(Material.BARRIER, "Cerrar", NamedTextColor.GRAY));

        admin.openInventory(inv);
    }
}
