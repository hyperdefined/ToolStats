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
import org.bukkit.ChatColor;
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

import java.util.*;

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
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        // only check certain items
        if (!toolStats.itemChecker.isValidItem(itemStack.getType())) {
            return;
        }

        // if the player shift clicks, send them this warning
        if (event.isShiftClick()) {
            String configMessage = toolStats.config.getString("messages.shift-click-warning.crafting");
            if (configMessage != null) {
                if (configMessage.length() != 0) {
                    event.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', configMessage));
                }
            }
        }
        // test the item before setting it
        ItemStack newItem = addLore(itemStack, player);
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
    private ItemStack addLore(ItemStack itemStack, Player owner) {
        // clone the item
        ItemStack newItem = itemStack.clone();
        ItemMeta meta = newItem.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(itemStack + " does NOT have any meta! Unable to update stats.");
            return null;
        }
        // get the current time
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if the item already has the tag
        // this is to prevent duplicate tags
        if (container.has(toolStats.timeCreated, PersistentDataType.LONG) || container.has(toolStats.genericOwner, PersistentDataType.LONG)) {
            return null;
        }

        // only make the hash if it's enabled
        if (toolStats.config.getBoolean("generate-hash-for-items")) {
            String hash = toolStats.hashMaker.makeHash(newItem.getType(), owner.getUniqueId(), timeCreated);
            container.set(toolStats.hash, PersistentDataType.STRING, hash);
        }

        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.genericOwner, new UUIDDataType(), owner.getUniqueId());
        container.set(toolStats.originType, PersistentDataType.INTEGER, 0);

        List<String> lore;
        // get the current lore the item
        if (meta.hasLore()) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<>();
        }
        // do we add the lore based on the config?
        if (toolStats.checkConfig(itemStack.getType(), "created-date")) {
            String createdOnRaw = toolStats.getLoreFromConfig("created.created-on", true);
            if (createdOnRaw == null) {
                toolStats.logger.warning("There is no lore message for messages.created.created-on!");
                return null;
            }
            lore.add(createdOnRaw.replace("{date}", toolStats.numberFormat.formatDate(finalDate)));
            meta.setLore(lore);
        }
        if (toolStats.checkConfig(itemStack.getType(), "created-by")) {
            String createdByRaw = toolStats.getLoreFromConfig("created.created-by", true);
            if (createdByRaw == null) {
                toolStats.logger.warning("There is no lore message for messages.created.created-by!");
                return null;
            }
            lore.add(createdByRaw.replace("{player}", owner.getName()));
            meta.setLore(lore);
        }
        newItem.setItemMeta(meta);
        return newItem;
    }
}
