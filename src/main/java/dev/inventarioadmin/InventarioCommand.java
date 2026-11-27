package dev.inventarioadmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** /inventario [menu|ver|ender|dar|quitar|vaciar] */
final class InventarioCommand implements TabExecutor {
    private static final List<String> SUBS = List.of("menu", "ver", "ender", "dar", "quitar", "vaciar");

    private final InventarioPlugin plugin;

    InventarioCommand(InventarioPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(Msg.PERM)) {
            Msg.send(sender, "No tienes permiso para usar este comando.", NamedTextColor.RED);
            return true;
        }
        String sub = args.length == 0 ? "menu" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "menu" -> {
                if (sender instanceof Player admin) {
                    Menu.open(admin, 0);
                } else {
                    Msg.send(sender, "Solo un jugador puede abrir ventanas.", NamedTextColor.RED);
                }
            }
            case "ver" -> {
                Player admin = requirePlayer(sender);
                Player target = admin == null ? null : target(sender, args);
                if (target != null) {
                    Editor.open(plugin, admin, target);
                }
            }
            case "ender" -> {
                Player admin = requirePlayer(sender);
                Player target = admin == null ? null : target(sender, args);
                if (target != null) {
                    admin.openInventory(target.getEnderChest());
                }
            }
            case "dar" -> give(sender, args);
            case "quitar" -> take(sender, args);
            case "vaciar" -> {
                Player target = target(sender, args);
                if (target != null) {
                    target.getInventory().clear();
                    target.updateInventory();
                    Msg.send(sender, "Inventario de " + target.getName() + " vaciado.", NamedTextColor.YELLOW);
                }
            }
            default -> usage(sender, label);
        }
        return true;
    }

    private void give(CommandSender sender, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        Material material = material(sender, args);
        int amount = amount(sender, args);
        if (material == null || amount < 1) {
            return;
        }
        Map<Integer, ItemStack> leftover = target.getInventory().addItem(new ItemStack(material, amount));
        int notFit = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
        Msg.send(sender, "Entregado a " + target.getName() + ": " + (amount - notFit) + " × "
                + material.name().toLowerCase(Locale.ROOT)
                + (notFit > 0 ? " (" + notFit + " no cabían)" : ""), NamedTextColor.GREEN);
    }

    private void take(CommandSender sender, String[] args) {
        Player target = target(sender, args);
        if (target == null) {
            return;
        }
        Material material = material(sender, args);
        int amount = amount(sender, args);
        if (material == null || amount < 1) {
            return;
        }
        Map<Integer, ItemStack> leftover = target.getInventory().removeItem(new ItemStack(material, amount));
        int notRemoved = leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
        Msg.send(sender, "Quitado a " + target.getName() + ": " + (amount - notRemoved) + " × "
                + material.name().toLowerCase(Locale.ROOT)
                + (notRemoved > 0 ? " (no tenía " + notRemoved + " más)" : ""), NamedTextColor.YELLOW);
    }

    // ---------------------------------------------------------------- ayudas

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player p) {
            return p;
        }
        Msg.send(sender, "Este subcomando solo lo puede usar un jugador.", NamedTextColor.RED);
        return null;
    }

    private Player target(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Msg.send(sender, "Falta el nombre del jugador.", NamedTextColor.RED);
            return null;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            Msg.send(sender, "Jugador no conectado: " + args[1], NamedTextColor.RED);
        }
        return target;
    }

    private Material material(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Msg.send(sender, "Falta el ítem (ej: diamond_sword).", NamedTextColor.RED);
            return null;
        }
        Material material = Material.matchMaterial(args[2]);
        if (material == null || !material.isItem() || material.isAir()) {
            Msg.send(sender, "Ítem desconocido: " + args[2], NamedTextColor.RED);
            return null;
        }
        return material;
    }

    private int amount(CommandSender sender, String[] args) {
        if (args.length < 4) {
            return 1;
        }
        try {
            int amount = Integer.parseInt(args[3]);
            if (amount < 1 || amount > 2304) {
                throw new NumberFormatException();
            }
            return amount;
        } catch (NumberFormatException ex) {
            Msg.send(sender, "Cantidad inválida (1 a 2304): " + args[3], NamedTextColor.RED);
            return -1;
        }
    }

    private void usage(CommandSender sender, String label) {
        Msg.send(sender, "Uso: /" + label + " [menu | ver <jugador> | ender <jugador> | "
                + "dar <jugador> <ítem> [cantidad] | quitar <jugador> <ítem> [cantidad] | vaciar <jugador>]",
                NamedTextColor.GRAY);
    }

    // ---------------------------------------------------------- autocompletar

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(Msg.PERM)) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(SUBS, args[0]);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && !sub.equals("menu")) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return filter(names, args[1]);
        }
        if (args.length == 3 && (sub.equals("dar") || sub.equals("quitar"))) {
            List<String> items = Arrays.stream(Material.values())
                    .filter(m -> m.isItem() && !m.isAir() && !m.name().startsWith("LEGACY_"))
                    .map(m -> m.name().toLowerCase(Locale.ROOT))
                    .toList();
            return filter(items, args[2]).stream().limit(60).toList();
        }
        if (args.length == 4 && (sub.equals("dar") || sub.equals("quitar"))) {
            return filter(List.of("1", "16", "64"), args[3]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String p = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(p)).toList();
    }
}
