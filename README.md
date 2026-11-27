# InventarioAdmin

Plugin para **Paper 26.3** que permite a los OP ver y editar el inventario y el cofre de Ender
de los jugadores conectados usando ventanas de inventario reales.

## Comandos (permiso `inventario.admin`, por defecto solo OP)

| Comando | Qué hace |
|---|---|
| `/inventario` o `/inventario menu` | Ventana con la cabeza de cada jugador conectado (clic izquierdo: inventario, clic derecho: cofre de Ender) |
| `/inventario ver <jugador>` | Ventana editable con el inventario, la armadura y la mano secundaria |
| `/inventario ender <jugador>` | Abre el cofre de Ender del jugador (en vivo) |
| `/inventario dar <jugador> <ítem> [cantidad]` | Agrega un ítem |
| `/inventario quitar <jugador> <ítem> [cantidad]` | Quita un ítem |
| `/inventario vaciar <jugador>` | Vacía el inventario |

Alias: `/inv`, `/invsee`.

## Cómo usar la ventana de edición

- Arrastra ítems de tu inventario (parte de abajo) a la ventana para **agregarlos** al jugador.
- Saca ítems de la ventana a tu inventario, o suéltalos fuera, para **quitarlos**.
- Fila superior: casco, peto, pantalones, botas y mano secundaria. Los paneles grises son decoración.
- Botones de la fila inferior: actualizar, cofre de Ender, vaciar (Shift + clic) y cerrar.
- Los cambios se aplican al jugador al instante y la ventana se actualiza si él mueve sus ítems.

## Compilar

Necesitas **JDK 25**.

```
gradle build
```

El `.jar` queda en `build/libs/InventarioAdmin-1.0.0.jar`. Cópialo a la carpeta `plugins` de tu servidor Paper.

Sin instalar nada: sube la carpeta a un repositorio de GitHub; el flujo `.github/workflows/build.yml`
compila el plugin y deja el `.jar` en la pestaña *Actions* > *Artifacts*.

Para fijar una build de Paper concreta, cambia en `build.gradle` la versión de `paper-api`
(por ejemplo `26.3.build.49-alpha`).

## Límites

- Solo funciona con jugadores **conectados**.
- No funciona en Folia.
- Si el jugador y tú editan el mismo ítem a la vez, gana el último cambio.
