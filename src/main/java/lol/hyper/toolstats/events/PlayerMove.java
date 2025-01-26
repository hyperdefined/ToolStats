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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
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
                PlayerInventory inventory = player.getInventory();
                ItemStack chest = inventory.getChestplate();
                // make sure the player is wearing an elytra
                if (chest != null && chest.getType() == Material.ELYTRA) {
                    long duration = (System.currentTimeMillis() - playerStartFlight.get(player));
                    ItemMeta newItem = toolStats.itemLore.updateFlightTime(chest, duration);
                    if (newItem != null) {
                        inventory.getChestplate().setItemMeta(newItem);
                    }
                }
                playerStartFlight.remove(player);
            }
        }
    }
}
