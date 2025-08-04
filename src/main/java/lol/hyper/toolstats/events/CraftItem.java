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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CraftItem implements Listener {

    private final ToolStats toolStats;

    public CraftItem(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
            return;
        }
        ItemStack craftedItem = event.getCurrentItem();
        if (craftedItem == null || craftedItem.getType() == Material.AIR) {
            return;
        }
        Material craftedMaterial = craftedItem.getType();
        // only check certain items
        if (!toolStats.itemChecker.isValidItem(craftedMaterial)) {
            return;
        }

        // if the player shift clicks
        if (event.isShiftClick()) {
            // store the player inventory before they craft the items
            ItemStack[] beforeCraft = player.getInventory().getContents();
            // run a tick after to see the changes
            player.getScheduler().runDelayed(toolStats, scheduledTask -> {
                // get their inventory after the craft
                ItemStack[] afterCraft = player.getInventory().getContents();
                for (int i = 0; i < afterCraft.length; i++) {
                    ItemStack newSlotItem = afterCraft[i];
                    ItemStack oldSlotItem = beforeCraft[i];

                    // if this slot is empty after crafting, skip it
                    if (newSlotItem == null) {
                        continue;
                    }

                    // if the item matches what we crafted
                    if (newSlotItem.getType() == craftedMaterial) {
                        // if the slot was empty before we crafted, this means we just made it
                        if (oldSlotItem == null) {
                            // add the lore
                            ItemStack newItem = addCraftOrigin(newSlotItem, player);
                            if (newItem != null) {
                                player.getInventory().setItem(i, newItem);
                            }
                        }
                    }
                }
            }, null, 1);
            return;
        }

        // the player did not shift click
        ItemStack newItem = addCraftOrigin(craftedItem, player);
        if (newItem != null) {
            // set the result
            event.setCurrentItem(newItem);
        }
    }

    /**
     * Adds crafted tags to item.
     *
     * @param itemStack The item add item to.
     * @param owner     The player crafting.
     * @return A copy of the item with the tags + lore.
     */
    private ItemStack addCraftOrigin(ItemStack itemStack, Player owner) {
        // clone the item
        ItemStack newItem = itemStack.clone();
        ItemMeta meta = newItem.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(itemStack + " does NOT have any meta! Unable to update stats.");
            return null;
        }
        // get the current time
        long timeCreated = System.currentTimeMillis();
        Date finalDate;
        if (toolStats.config.getBoolean("normalize-time-creation")) {
            finalDate = toolStats.numberFormat.normalizeTime(timeCreated);
            timeCreated = finalDate.getTime();
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if the item already has the tag
        // this is to prevent duplicate tags
        if (container.has(toolStats.timeCreated, PersistentDataType.LONG) || container.has(toolStats.itemOwner, PersistentDataType.LONG)) {
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
        Component creationDate = toolStats.itemLore.formatCreationTime(timeCreated, 0, newItem);
        if (creationDate != null) {
            container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
            container.set(toolStats.originType, PersistentDataType.INTEGER, 0);
            lore.add(creationDate);
            meta.lore(lore);
        }

        // if ownership is enabled, add it
        Component itemOwner = toolStats.itemLore.formatOwner(owner.getName(), 0, newItem);
        if (itemOwner != null) {
            container.set(toolStats.itemOwner, new UUIDDataType(), owner.getUniqueId());
            container.set(toolStats.originType, PersistentDataType.INTEGER, 0);
            lore.add(itemOwner);
            meta.lore(lore);
        }

        // if hash is enabled, add it
        if (toolStats.config.getBoolean("generate-hash-for-items")) {
            String hash = toolStats.hashMaker.makeHash(newItem.getType(), owner.getUniqueId(), timeCreated);
            container.set(toolStats.hash, PersistentDataType.STRING, hash);
        }
        newItem.setItemMeta(meta);
        return newItem;
    }
}
