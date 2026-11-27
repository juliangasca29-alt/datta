package dev.inventarioadmin;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Ventana de 54 ranuras que refleja el inventario de un jugador.
 *
 * <pre>
 * Fila 1:  [casco][peto][pantalones][botas][mano sec.] [x][x][x] [cabeza info]
 * Filas 2-4: inventario principal (ranuras 9-35 del jugador)
 * Fila 5:  hotbar (ranuras 0-8 del jugador)
 * Fila 6:  botones
 * </pre>
 */
final class Editor {
    static final int SIZE = 54;
    static final int HEAD_SLOT = 8;
    static final int BTN_REFRESH = 45;
    static final int BTN_ENDER = 47;
    static final int BTN_CLEAR = 51;
    static final int BTN_CLOSE = 53;

    private Editor() {}

    /** Ranuras en las que se puede mover ítems. El resto son decoración o botones. */
    static boolean editable(int slot) {
        return slot <= 4 || (slot >= 9 && slot <= 44);
    }

    static void open(InventarioPlugin plugin, Player admin, Player target) {
        EditorHolder holder = new EditorHolder(target.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                Component.text("Inventario de " + target.getName()));
        holder.setInventory(inv);
        fill(inv, target);
        admin.openInventory(inv);
        plugin.getLogger().info(admin.getName() + " abrió el inventario de " + target.getName());
    }

    /** Copia el estado actual del jugador a la ventana (solo cambia lo que difiere). */
    static void fill(Inventory inv, Player target) {
        PlayerInventory pi = target.getInventory();

        set(inv, 0, pi.getHelmet());
        set(inv, 1, pi.getChestplate());
        set(inv, 2, pi.getLeggings());
        set(inv, 3, pi.getBoots());
        set(inv, 4, pi.getItemInOffHand());
        for (int i = 9; i <= 35; i++) {
            set(inv, i, pi.getItem(i));
        }
        for (int i = 0; i <= 8; i++) {
            set(inv, 36 + i, pi.getItem(i));
        }

        ItemStack filler = Ui.filler();
        for (int s : new int[] {5, 6, 7, 46, 48, 50, 52}) {
            set(inv, s, filler);
        }
        set(inv, HEAD_SLOT, info(target));
        set(inv, BTN_REFRESH, Ui.button(Material.LIME_DYE, "Actualizar", NamedTextColor.GREEN,
                "Vuelve a leer el inventario del jugador"));
        set(inv, BTN_ENDER, Ui.button(Material.ENDER_CHEST, "Cofre de Ender", NamedTextColor.LIGHT_PURPLE,
                "Abre el cofre de Ender del jugador"));
        set(inv, BTN_CLEAR, Ui.button(Material.TNT, "Vaciar inventario", NamedTextColor.RED,
                "Shift + clic para borrar todo"));
        set(inv, BTN_CLOSE, Ui.button(Material.BARRIER, "Cerrar", NamedTextColor.GRAY));
    }

    /** Copia el contenido de la ventana al jugador. */
    static void apply(Inventory inv, Player target) {
        PlayerInventory pi = target.getInventory();

        pi.setHelmet(inv.getItem(0));
        pi.setChestplate(inv.getItem(1));
        pi.setLeggings(inv.getItem(2));
        pi.setBoots(inv.getItem(3));
        pi.setItemInOffHand(orAir(inv.getItem(4)));
        for (int i = 9; i <= 35; i++) {
            pi.setItem(i, inv.getItem(i));
        }
        for (int i = 0; i <= 8; i++) {
            pi.setItem(i, inv.getItem(36 + i));
        }
        target.updateInventory();
    }

    private static ItemStack orAir(ItemStack item) {
        return item == null ? new ItemStack(Material.AIR) : item;
    }

    private static void set(Inventory inv, int slot, ItemStack item) {
        ItemStack current = inv.getItem(slot);
        ItemStack wanted = (item == null || item.getType().isAir()) ? null : item;
        if (!Objects.equals(current, wanted)) {
            inv.setItem(slot, wanted);
        }
    }

    private static ItemStack info(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(SkullMeta.class, meta -> {
            meta.setOwningPlayer(target);
            meta.displayName(Msg.plain(target.getName(), NamedTextColor.YELLOW));
            meta.lore(List.of(
                    Msg.plain("Vida: " + String.format("%.1f", target.getHealth()), NamedTextColor.GRAY),
                    Msg.plain("Comida: " + target.getFoodLevel(), NamedTextColor.GRAY),
                    Msg.plain("Nivel: " + target.getLevel(), NamedTextColor.GRAY),
                    Msg.plain("Modo: " + target.getGameMode().name(), NamedTextColor.GRAY),
                    Msg.plain("Mundo: " + target.getWorld().getName(), NamedTextColor.GRAY)));
        });
        return head;
    }
}
