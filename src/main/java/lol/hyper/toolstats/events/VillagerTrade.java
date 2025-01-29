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
import lol.hyper.toolstats.tools.UUIDDataType;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Date;
import java.util.List;

public class VillagerTrade implements Listener {

    private final ToolStats toolStats;

    public VillagerTrade(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTrade(InventoryClickEvent event) {
        if (event.isCancelled() || event.getCurrentItem() == null) {
            return;
        }
        Inventory inventory = event.getClickedInventory();
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        // only check villager inventories
        if (!(inventory instanceof MerchantInventory)) {
            return;
        }
        // only check the result slot (the item you receive)
        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }
        ItemStack tradedItem = event.getCurrentItem();
        Material tradedMaterial = tradedItem.getType();
        // only check items we want
        if (!toolStats.itemChecker.isValidItem(tradedMaterial)) {
            return;
        }
        // if the player shift clicks
        if (event.isShiftClick()) {
            // store the player inventory before they trade the items
            ItemStack[] beforeTrade = player.getInventory().getContents();
            // run a tick after to see the changes
            player.getScheduler().runDelayed(toolStats, scheduledTask -> {
                // get their inventory after the trade
                ItemStack[] afterTrade = player.getInventory().getContents();
                for (int i = 0; i < afterTrade.length; i++) {
                    ItemStack newSlotItem = afterTrade[i];
                    ItemStack oldSlotItem = beforeTrade[i];

                    // if this slot is empty after trading, skip it
                    if (newSlotItem == null) {
                        continue;
                    }

                    // if the item matches what we traded
                    if (newSlotItem.getType() == tradedMaterial) {
                        // if the slot was empty before we traded, this means we just traded it
                        if (oldSlotItem == null) {
                            // add the lore
                            ItemStack newItem = addTradeOrigin(newSlotItem, player);
                            if (newItem != null) {
                                player.getInventory().setItem(i, newItem);
                            }
                        }
                    }
                }
            }, null, 1);
            return;
        }
        ItemStack newItem = addTradeOrigin(tradedItem, player);
        if (newItem != null) {
            // set the new item
            inventory.setItem(event.getSlot(), newItem);
        }
    }

    /**
     * Adds "traded by" tags to item.
     *
     * @param oldItem The item to add lore.
     * @param owner   The player who traded.
     * @return The item with lore.
     */
    private ItemStack addTradeOrigin(ItemStack oldItem, Player owner) {
        ItemStack newItem = oldItem.clone();
        ItemMeta meta = newItem.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(newItem + " does NOT have any meta! Unable to update stats.");
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(toolStats.timeCreated, PersistentDataType.LONG) || container.has(toolStats.itemOwner, PersistentDataType.LONG)) {
            return null;
        }

        if (!toolStats.configTools.checkConfig(newItem.getType(), "traded-tag")) {
            return null;
        }

        // only make the hash if it's enabled
        if (toolStats.config.getBoolean("generate-hash-for-items")) {
            String hash = toolStats.hashMaker.makeHash(newItem.getType(), owner.getUniqueId(), timeCreated);
            container.set(toolStats.hash, PersistentDataType.STRING, hash);
        }

        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.itemOwner, new UUIDDataType(), owner.getUniqueId());
        container.set(toolStats.originType, PersistentDataType.INTEGER, 3);
        String formattedDate = toolStats.numberFormat.formatDate(finalDate);
        List<Component> newLore = toolStats.itemLore.addNewOwner(meta, owner.getName(), formattedDate);
        meta.lore(newLore);
        newItem.setItemMeta(meta);
        return newItem;
    }
}
