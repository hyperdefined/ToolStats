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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockBreak implements Listener {

    private final ToolStats toolStats;
    public final List<Block> brokenContainers = new ArrayList<>();

    public BlockBreak(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!toolStats.configTools.checkWorld(player.getWorld().getName())) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItemInMainHand();
        Block block = event.getBlock();

        if (block.getType() == Material.CHEST || block.getType() == Material.BARREL) {
            brokenContainers.add(block);
            Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, scheduledTask -> brokenContainers.remove(block), 20);
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
                ItemMeta newMeta = toolStats.itemLore.updateCropsMined(heldItem, 1);
                if (newMeta != null) {
                    // replace item in main hand
                    heldItem.setItemMeta(newMeta);
                }
            } else {
                // item is a hoe, but not breaking crops
                ItemMeta newMeta = toolStats.itemLore.updateBlocksMined(heldItem, 1);
                if (newMeta != null) {
                    // replace item in main hand
                    heldItem.setItemMeta(newMeta);
                }
            }
        } else {
            // item is not a hoe
            // update the blocks mined
            ItemMeta newMeta = toolStats.itemLore.updateBlocksMined(heldItem, 1);
            if (newMeta != null) {
                // replace item in main hand
                heldItem.setItemMeta(newMeta);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (!toolStats.configTools.checkWorld(player.getWorld().getName())) {
            return;
        }

        Location eventLocation = event.getBlock().getLocation();
        Chunk eventChunk = eventLocation.getChunk();
        Bukkit.getRegionScheduler().runDelayed(toolStats, eventLocation.getWorld(), eventChunk.getX(), eventChunk.getZ(), scheduledTask -> {
            boolean validLootDrops = false;
            for (Location droppedLootLocation : toolStats.generateLoot.droppedLootLocations) {
                if (eventLocation.getWorld() == droppedLootLocation.getWorld()) {
                    double distance = droppedLootLocation.distance(eventLocation);
                    if (distance <= 1.0) {
                        validLootDrops = true;
                    }
                }
            }

            if (validLootDrops) {
                toolStats.generateLoot.droppedLootLocations.remove(eventLocation);
                for (Item droppedItemEntity : event.getItems()) {
                    ItemStack droppedItem = droppedItemEntity.getItemStack();
                    if (!toolStats.itemChecker.isValidItem(droppedItem.getType())) {
                        continue;
                    }

                    ItemStack newItem = toolStats.inventoryClose.addLootedOrigin(droppedItem, player);
                    if (newItem != null) {
                        droppedItemEntity.setItemStack(newItem);
                    }
                }
            }
        }, 1);
    }
}
