package dev.inventarioadmin;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class InventarioPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        PluginCommand command = getCommand("inventario");
        if (command == null) {
            getLogger().severe("No se encontró el comando 'inventario' en plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        InventarioCommand handler = new InventarioCommand(this);
        command.setExecutor(handler);
        command.setTabCompleter(handler);

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        // Mantiene la ventana al día si el jugador cambia su propio inventario.
        Bukkit.getScheduler().runTaskTimer(this, this::refreshEditors, 10L, 10L);
    }

    private void refreshEditors() {
        for (Player admin : Bukkit.getOnlinePlayers()) {
            Inventory top = admin.getOpenInventory().getTopInventory();
            if (!(top.getHolder(false) instanceof EditorHolder eh)) {
                continue;
            }
            Player target = Bukkit.getPlayer(eh.target());
            if (target == null) {
                Msg.send(admin, "El jugador se desconectó.", NamedTextColor.RED);
                admin.closeInventory();
                continue;
            }
            // No pisar la ventana mientras hay cambios pendientes o un ítem en el cursor.
            if (eh.isApplyQueued() || !admin.getItemOnCursor().getType().isAir()) {
                continue;
            }
            Editor.fill(top, target);
        }
    }
}
