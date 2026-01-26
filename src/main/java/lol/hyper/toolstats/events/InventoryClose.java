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

import lol.hyper.hyperlib.datatypes.UUIDDataType;
import lol.hyper.toolstats.ToolStats;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InventoryClose implements Listener {

    private final ToolStats toolStats;

    public InventoryClose(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (toolStats.generateLoot.generatedInventory.isEmpty()) {
            return;
        }

        Player player = (Player) event.getPlayer();
        if (!toolStats.configTools.checkWorld(player.getWorld().getName())) {
            return;
        }
        Inventory closedInventory = event.getInventory();
        InventoryHolder holder = closedInventory.getHolder();
        if (toolStats.generateLoot.generatedInventory.containsKey(closedInventory)) {
            Location chestLocation = toolStats.generateLoot.generatedInventory.get(closedInventory);
            toolStats.generateLoot.generatedInventory.remove(closedInventory);

            player.getScheduler().runDelayed(toolStats, scheduledTask -> {
                PlayerInventory playerInventory = player.getInventory();
                for (int i = 0; i < playerInventory.getContents().length; i++) {
                    ItemStack item = playerInventory.getItem(i);
                    if (item == null) {
                        continue;
                    }

                    if (!toolStats.itemChecker.isValidItem(item.getType())) {
                        continue;
                    }

                    ItemStack newItem = addLootedOrigin(item, player);
                    if (newItem != null) {
                        playerInventory.setItem(i, newItem);
                    }
                }
            }, null, 1);

            if (holder instanceof StorageMinecart mineCart) {
                mineCart.getScheduler().runDelayed(toolStats, scheduledTask -> {
                    Inventory chestInventory = mineCart.getInventory();
                    for (int i = 0; i < chestInventory.getContents().length; i++) {
                        ItemStack item = chestInventory.getItem(i);
                        if (item == null) {
                            continue;
                        }

                        if (!toolStats.itemChecker.isValidItem(item.getType())) {
                            continue;
                        }

                        ItemStack newItem = addLootedOrigin(item, player);
                        if (newItem != null) {
                            chestInventory.setItem(i, newItem);
                        }
                    }
                }, null, 1);
            }

            if (holder instanceof Container container) {
                Chunk chestChunk = chestLocation.getChunk();
                Bukkit.getRegionScheduler().runDelayed(toolStats, chestLocation.getWorld(), chestChunk.getX(), chestChunk.getZ(), scheduledTask -> {
                    BlockState blockState = chestLocation.getWorld().getBlockAt(chestLocation).getState();
                    if (blockState instanceof InventoryHolder chest) {
                        Inventory chestInventory = chest.getInventory();
                        for (int i = 0; i < chestInventory.getContents().length; i++) {
                            ItemStack item = chestInventory.getItem(i);
                            if (item == null) {
                                continue;
                            }

                            if (!toolStats.itemChecker.isValidItem(item.getType())) {
                                continue;
                            }

                            ItemStack newItem = addLootedOrigin(item, player);
                            if (newItem != null) {
                                chestInventory.setItem(i, newItem);
                            }
                        }
                    }
                }, 1);
            }
        }
    }

    /**
     * Adds lore to newly generated items.
     *
     * @param itemStack The item to add lore to.
     * @param owner     The player that found the item.
     * @return The item with the lore.
     */
    private ItemStack addLootedOrigin(ItemStack itemStack, Player owner) {
        ItemStack newItem = itemStack.clone();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate;
        if (toolStats.config.getBoolean("normalize-time-creation")) {
            finalDate = toolStats.numberFormat.normalizeTime(timeCreated);
            timeCreated = finalDate.getTime();
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(toolStats.toolStatsKeys.getOriginType(), PersistentDataType.INTEGER) || container.has(toolStats.toolStatsKeys.getTimeCreated(), PersistentDataType.LONG) || container.has(toolStats.toolStatsKeys.getItemOwner(), new UUIDDataType())) {
            return null;
        }

        // get the current lore the item
        List<Component> lore;
        if (meta.hasLore()) {
            lore = meta.lore();
        } else {
            lore = new ArrayList<>();
        }

        // if creation date is enabled, add it
        Component creationDate = toolStats.itemLore.formatCreationTime(timeCreated, 2, newItem);
        if (creationDate != null) {
            container.set(toolStats.toolStatsKeys.getTimeCreated(), PersistentDataType.LONG, timeCreated);
            container.set(toolStats.toolStatsKeys.getOriginType(), PersistentDataType.INTEGER, 2);
            lore.add(creationDate);
            meta.lore(lore);
        }

        // if ownership is enabled, add it
        Component itemOwner = toolStats.itemLore.formatOwner(owner.getName(), 2, newItem);
        if (itemOwner != null) {
            container.set(toolStats.toolStatsKeys.getItemOwner(), new UUIDDataType(), owner.getUniqueId());
            container.set(toolStats.toolStatsKeys.getOriginType(), PersistentDataType.INTEGER, 2);
            lore.add(itemOwner);
            meta.lore(lore);
        }

        // if hash is enabled, add it
        if (toolStats.config.getBoolean("generate-hash-for-items")) {
            String hash = toolStats.hashMaker.makeHash(newItem.getType(), owner.getUniqueId(), timeCreated);
            container.set(toolStats.toolStatsKeys.getHash(), PersistentDataType.STRING, hash);
        }

        newItem.setItemMeta(meta);
        return newItem;
    }
}
