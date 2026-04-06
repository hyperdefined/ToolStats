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
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateLoot implements Listener {

    private final ToolStats toolStats;
    public final Map<Inventory, Location> generatedInventory = new HashMap<>();
    public final List<Location> droppedLootLocations = new ArrayList<>();

    public GenerateLoot(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGenerateLoot(LootGenerateEvent event) {
        InventoryHolder inventoryHolder = event.getInventoryHolder();
        if (inventoryHolder == null) {
            return;
        }
        Location lootLocation = event.getLootContext().getLocation();
        if (!toolStats.configTools.checkWorld(lootLocation.getWorld().getName())) {
            return;
        }
        Chunk lootChunk = lootLocation.getChunk();
        Bukkit.getRegionScheduler().runDelayed(toolStats, lootLocation.getWorld(), lootChunk.getX(), lootChunk.getZ(), scheduledTask -> {
            if (inventoryHolder instanceof Container) {
                Block openedChest = null;
                Location chestLocation = null;
                // look at the current list of opened chest and get the distance
                // between the LootContext location and chest location
                // if the distance is less than 1, it's the same chest
                for (Block chest : toolStats.playerInteract.openedChests) {
                    chestLocation = chest.getLocation();
                    if (chest.getWorld() == lootLocation.getWorld()) {
                        double distance = lootLocation.distance(chestLocation);
                        if (distance <= 1.0) {
                            openedChest = chest;
                        }
                    }
                }
                for (Block brokenChest : toolStats.blockBreak.brokenContainers) {
                    Location brokenChestLocation = brokenChest.getLocation();
                    if (brokenChestLocation.getWorld() == lootLocation.getWorld()) {
                        double distance = lootLocation.distance(brokenChestLocation);
                        if (distance <= 1.0) {
                            droppedLootLocations.add(brokenChestLocation);
                            Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, scheduledTask2 -> droppedLootLocations.remove(brokenChestLocation), 20);
                        }
                    }
                }
                // ignore if the chest is not in the same location
                if (openedChest != null) {
                    generatedInventory.put(inventoryHolder.getInventory(), chestLocation);
                }
            }
            if (inventoryHolder instanceof StorageMinecart mineCart) {
                if (toolStats.playerInteract.openedMineCarts.contains(mineCart)) {
                    Inventory mineCartInventory = mineCart.getInventory();
                    generatedInventory.put(mineCartInventory, mineCart.getLocation());
                }
            }
        }, 1);
    }
}
