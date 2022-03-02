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
import lol.hyper.toolstats.UUIDDataType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GenerateLoot implements Listener {

    private final ToolStats toolStats;

    public GenerateLoot(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onGenerateLoot(LootGenerateEvent event) {
        InventoryHolder inventoryHolder = event.getInventoryHolder();
        if (inventoryHolder == null) {
            return;
        }
        Location lootLocation = event.getLootContext().getLocation();
        Inventory chestInv = inventoryHolder.getInventory();
        Block openedChest = null;
        // look at the current list of opened chest and get the distance
        // between the lootcontext location and chest location
        // if the distance is less than 1, it's the same chest
        for (Block chest : toolStats.playerInteract.openedChests.keySet()) {
            Location chestLocation = chest.getLocation();
            double distance = lootLocation.distance(chestLocation);
            if (distance <= 1.0) {
                openedChest = chest;
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
                String name = itemStack.getType().toString().toLowerCase(Locale.ROOT);
                for (String x : toolStats.allValidItems) {
                    if (name.contains(x)) {
                        chestInv.setItem(i, addLore(itemStack, player));
                    }
                }
            }

        }, 1);
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

        String foundByLoreRaw = toolStats.getLoreFromConfig("looted.found-by", true);
        String foundOnLoreRaw = toolStats.getLoreFromConfig("looted.found-on", true);

        if (foundByLoreRaw == null || foundOnLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.looted!");
            return null;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
        } else {
            lore = new ArrayList<>();
        }
        if (toolStats.checkConfig(newItem, "looted-tag")) {
            lore.add(foundOnLoreRaw.replace("{date}", toolStats.dateFormat.format(finalDate)));
            lore.add(foundByLoreRaw.replace("{player}", owner.getName()));
        }
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }
}
