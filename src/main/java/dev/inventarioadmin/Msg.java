package dev.inventarioadmin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

/** Utilidades de mensajes y textos. */
final class Msg {
    static final String PERM = "inventario.admin";

    private Msg() {}

    static void send(CommandSender to, String text, NamedTextColor color) {
        to.sendMessage(Component.text("[Inventario] ", NamedTextColor.GOLD)
                .append(Component.text(text, color)));
    }

    /** Texto sin cursiva (para nombres y lore de ítems). */
    static Component plain(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }
}
