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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Locale;

public class BlocksMined implements Listener {

    private final ToolStats toolStats;

    public BlocksMined(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItemInMainHand();
        Block block = event.getBlock();

        if (block.getType() == Material.CHEST) {
            toolStats.playerInteract.openedChests.put(block, player);
            Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, scheduledTask -> toolStats.playerInteract.openedChests.remove(block), 20);
        }

        // only check certain items
        if (!toolStats.itemChecker.isMineTool(heldItem.getType())) {
            return;
        }

        // if the item is a hoe
        if (heldItem.getType().toString().toLowerCase(Locale.ROOT).contains("hoe")) {
            // player is breaking crops with a hoe
            if (block.getBlockData() instanceof Ageable ageable) {
                // ignore crops that are not fully grown
                if (ageable.getAge() != ageable.getMaximumAge()) {
                    return;
                }
                ItemStack newItem = toolStats.itemLore.updateCropsMined(heldItem, 1);
                if (newItem != null) {
                    // replace item in main hand
                    inventory.setItemInMainHand(newItem);
                }
            }
        } else {
            // item is not a hoe
            // update the blocks mined
            ItemStack newItem = toolStats.itemLore.updateBlocksMined(heldItem, 1);
            if (newItem != null) {
                toolStats.logger.info(newItem.toString());
                // replace item in main hand
                inventory.setItemInMainHand(newItem);
            }
        }
    }
}
