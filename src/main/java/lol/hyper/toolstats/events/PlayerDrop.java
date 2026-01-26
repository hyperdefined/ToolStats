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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerDrop implements Listener {

    private final ToolStats toolStats;

    public PlayerDrop(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!toolStats.configTools.checkWorld(player.getWorld().getName())) {
            return;
        }

        if (toolStats.generateLoot.generatedInventory.isEmpty()) {
            return;
        }

        player.getScheduler().runDelayed(toolStats, scheduledTask -> {
            Inventory opened = player.getOpenInventory().getTopInventory();
            if (toolStats.generateLoot.generatedInventory.containsKey(opened)) {
                Item droppedItemEntity = event.getItemDrop();
                ItemStack droppedItem = droppedItemEntity.getItemStack();

                if (!toolStats.itemChecker.isValidItem(droppedItem.getType())) {
                    return;
                }

                ItemStack newItem = toolStats.inventoryClose.addLootedOrigin(droppedItem, player);
                if (newItem != null) {
                    droppedItemEntity.setItemStack(newItem);
                }
            }
        }, null, 1);
    }
}
