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
import lol.hyper.toolstats.tools.ItemChecker;
import lol.hyper.toolstats.tools.UUIDDataType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Date;
import java.util.List;

public class GenerateLoot implements Listener {

    private final ToolStats toolStats;

    public GenerateLoot(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGenerateLoot(LootGenerateEvent event) {
        InventoryHolder inventoryHolder = event.getInventoryHolder();
        if (inventoryHolder == null) {
            return;
        }
        Location lootLocation = event.getLootContext().getLocation();
        Inventory chestInv = inventoryHolder.getInventory();

        if (inventoryHolder instanceof Chest) {
            Block openedChest = null;
            // look at the current list of opened chest and get the distance
            // between the lootcontext location and chest location
            // if the distance is less than 1, it's the same chest
            for (Block chest : toolStats.playerInteract.openedChests.keySet()) {
                Location chestLocation = chest.getLocation();
                if (chest.getWorld() == lootLocation.getWorld()) {
                    double distance = lootLocation.distance(chestLocation);
                    if (distance <= 1.0) {
                        openedChest = chest;
                    }
                }
            }
            // ignore if the chest is not in the same location
            if (openedChest == null) {
                return;
            }

            // run task later since if it runs on the same tick it breaks idk
            Block finalOpenedChest = openedChest;
            Bukkit.getScheduler().runTaskLater(toolStats, () -> {
                Player player = toolStats.playerInteract.openedChests.get(finalOpenedChest);
                // do a classic for loop, so we keep track of chest index of item
                for (int i = 0; i < chestInv.getContents().length; i++) {
                    ItemStack itemStack = chestInv.getItem(i);
                    // ignore air
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        continue;
                    }
                    if (ItemChecker.isValidItem(itemStack.getType())) {
                        ItemStack newItem = addLore(itemStack, player);
                        if (newItem != null) {
                            chestInv.setItem(i, newItem);
                        }
                    }
                }

            }, 1);
        }
        if (inventoryHolder instanceof StorageMinecart) {
            StorageMinecart mineCart = (StorageMinecart) inventoryHolder;
            if (toolStats.playerInteract.openedMineCarts.containsKey(mineCart)) {
                Player player = toolStats.playerInteract.openedMineCarts.get(mineCart);
                // player clicked this minecart
                for (int i = 0; i < chestInv.getContents().length; i++) {
                    ItemStack itemStack = chestInv.getItem(i);
                    // ignore air
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        continue;
                    }
                    if (ItemChecker.isValidItem(itemStack.getType())) {
                        ItemStack newItem = addLore(itemStack, player);
                        if (newItem != null) {
                            chestInv.setItem(i, newItem);
                        }
                    }
                }
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
    private ItemStack addLore(ItemStack itemStack, Player owner) {
        ItemStack newItem = itemStack.clone();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(toolStats.timeCreated, PersistentDataType.LONG) || container.has(toolStats.genericOwner, PersistentDataType.LONG)) {
            return null;
        }

        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.genericOwner, new UUIDDataType(), owner.getUniqueId());

        String formattedDate = toolStats.numberFormat.formatDate(finalDate);
        List<String> newLore = toolStats.itemLore.addNewOwner(meta, owner.getName(), formattedDate, "LOOTED");

        if (toolStats.checkConfig(newItem, "looted-tag")) {
            meta.setLore(newLore);
        }
        newItem.setItemMeta(meta);
        return newItem;
    }
}
