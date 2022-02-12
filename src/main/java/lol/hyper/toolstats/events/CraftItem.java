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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.*;

public class CraftItem implements Listener {

    private final ToolStats toolStats;
    public final String[] validItems = {
            "pickaxe", "sword", "shovel", "axe", "hoe", "bow", "helmet", "chestplate", "leggings", "boots", "fishing"
    };
    private final SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH);

    public CraftItem(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        String name = itemStack.getType().toString().toLowerCase(Locale.ROOT);
        // only check for items we want
        for (String x : validItems) {
            if (name.contains(x)) {
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
                if (addLore(itemStack, player) == null) {
                    return;
                }
                // set the result
                event.setCurrentItem(addLore(itemStack, player));
            }
        }
    }

    /**
     * Adds crafted tags to item.
     * @param itemStack The item add item to.
     * @param owner The player crafting.
     * @return A copy of the item with the tags + lore.
     */
    private ItemStack addLore(ItemStack itemStack, Player owner) {
        // clone the item
        ItemStack newItem = itemStack.clone();
        ItemMeta meta = newItem.getItemMeta();
        if (meta == null) {
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

        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.genericOwner, new UUIDDataType(), owner.getUniqueId());

        String createdByRaw = toolStats.getLoreFromConfig("created.created-by", true);
        String createdOnRaw = toolStats.getLoreFromConfig("created.created-on", true);

        if (createdOnRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.created.created-on!");
            return null;
        }
        if (createdByRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.created.created-by!");
            return null;
        }

        List<String> lore;
        // get the current lore the item
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
        } else {
            lore = new ArrayList<>();
        }
        // do we add the lore based on the config?
        if (toolStats.checkConfig(itemStack, "created-date")) {
            lore.add(createdOnRaw.replace("{date}", format.format(finalDate)));
        }
        if (toolStats.checkConfig(itemStack, "created-by")) {
            lore.add(createdByRaw.replace("{player}", owner.getName()));
        }
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }
}
