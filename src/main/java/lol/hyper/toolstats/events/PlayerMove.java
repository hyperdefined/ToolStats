/*
 * This file is part of ToolStats.
 *
 * ToolStats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ToolStats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToolStats.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.toolstats.events;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerMove implements Listener {

    private final ToolStats toolStats;
    private final Map<Player, Long> playerStartFlight = new HashMap<>();

    public PlayerMove(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCraft(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // player starts to fly
        if (player.isGliding()) {
            // if they are flying, and we don't have them tracked, add them
            if (!playerStartFlight.containsKey(player)) {
                playerStartFlight.put(player, System.currentTimeMillis());
            }
        } else {
            // player is not flying
            if (playerStartFlight.containsKey(player)) {
                trackFlight(player, playerStartFlight.get(player));
                playerStartFlight.remove(player);
            }
        }
    }

    private void trackFlight(Player player, long startTime) {
        ItemStack chest = player.getInventory().getChestplate();
        // make sure their chest piece is an elytra
        if (chest == null || chest.getType() != Material.ELYTRA) {
            return;
        }
        ItemMeta meta = chest.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(chest + " does NOT have any meta! Unable to update stats.");
            return;
        }

        // read the current stats from the item
        // if they don't exist, then start from 0
        Long flightTime = 0L;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.flightTime, PersistentDataType.LONG)) {
            flightTime = container.get(toolStats.flightTime, PersistentDataType.LONG);
        }

        if (flightTime == null) {
            flightTime = 0L;
            toolStats.logger.warning(flightTime + " does not have valid flight-time set! Resting to zero. This should NEVER happen.");
        }

        // get the duration of the flight
        long duration = (System.currentTimeMillis() - startTime) + flightTime;
        container.set(toolStats.flightTime, PersistentDataType.LONG, flightTime + duration);

        // do we add the lore based on the config?
        if (toolStats.config.getBoolean("enabled.flight-time")) {
            String oldFlightFormatted = toolStats.numberFormat.formatDouble((double) flightTime / 1000);
            String newFlightFormatted = toolStats.numberFormat.formatDouble((double) (flightTime + duration) / 1000);
            String oldLine = toolStats.configTools.formatLore("flight-time", "{time}", oldFlightFormatted);
            String newLine = toolStats.configTools.formatLore("flight-time", "{time}", newFlightFormatted);
            if (oldLine == null || newLine == null) {
                return;
            }
            List<String> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
            meta.setLore(newLore);
        }
        chest.setItemMeta(meta);
    }
}
