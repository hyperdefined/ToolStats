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

package lol.hyper.toolstats.tools;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemLore {

    private final ToolStats toolStats;

    public ItemLore(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Adds new lore to an item.
     *
     * @param placeholder      The placeholder from the config. ex: {kills}
     * @param placeholderValue The value to replace the placeholder.
     * @param configLorePath   The path to the config message.
     * @return The item's new lore.
     */
    public List<String> addItemLore(ItemMeta itemMeta, String placeholder, String placeholderValue, String configLorePath) {
        String configLore = toolStats.getLoreFromConfig(configLorePath, false);
        String configLoreRaw = toolStats.getLoreFromConfig(configLorePath, true);

        if (configLore == null || configLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages." + configLorePath + "!");
            toolStats.logger.warning("Unable to update lore for item.");
            return itemMeta.getLore();
        }

        List<String> newLore;
        // replace the placeholder with the value
        // ex: {kills} -> a number
        String newLine = configLoreRaw.replace(placeholder, placeholderValue);

        if (itemMeta.hasLore()) {
            newLore = itemMeta.getLore();
            // keep track of line index
            // this doesn't mess the lore of existing items
            for (int x = 0; x < newLore.size(); x++) {
                // check to see if the line matches the config value
                // this means we update this line only!
                if (newLore.get(x).contains(configLore)) {
                    newLore.set(x, newLine);
                    return newLore;
                }
            }
            // if the item has lore, but we didn't find the line
            newLore.add(newLine);
        } else {
            // if the item has no lore, create a new list and add the line
            newLore = new ArrayList<>();
            newLore.add(newLine);
        }
        return newLore;
    }

    /**
     * Adds new ownership tag to an item.
     *
     * @param itemMeta      The item meta.
     * @param playerName    The new owner of item.
     * @param formattedDate The date of the ownership.
     * @return The item's new lore.
     */
    public List<String> addNewOwner(ItemMeta itemMeta, String playerName, String formattedDate) {
        String dateCreated = null;
        String itemOwner = null;
        Integer origin = null;
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (container.has(toolStats.originType, PersistentDataType.INTEGER)) {
            origin = container.get(toolStats.originType, PersistentDataType.INTEGER);
        }

        if (origin == null) {
            origin = -1;
        }

        switch (origin) {
            case 2: {
                dateCreated = toolStats.getLoreFromConfig("looted.looted-on", true);
                itemOwner = toolStats.getLoreFromConfig("looted.looted-by", true);
                break;
            }
            case 3: {
                dateCreated = toolStats.getLoreFromConfig("traded.traded-on", true);
                itemOwner = toolStats.getLoreFromConfig("traded.traded-by", true);
                break;
            }
            case 4: {
                dateCreated = toolStats.getLoreFromConfig("looted.found-on", true);
                itemOwner = toolStats.getLoreFromConfig("looted.found-by", true);
                break;
            }
            case 5: {
                dateCreated = toolStats.getLoreFromConfig("fished.caught-on", true);
                itemOwner = toolStats.getLoreFromConfig("fished.caught-by", true);
                break;
            }
        }

        if (dateCreated == null || itemOwner == null) {
            toolStats.logger.info("Unable to determine origin of item for " + itemMeta);
            return itemMeta.getLore();
        }

        List<String> newLore;
        if (itemMeta.hasLore()) {
            newLore = itemMeta.getLore();
        } else {
            newLore = new ArrayList<>();
        }

        newLore.add(dateCreated.replace("{date}", formattedDate));
        newLore.add(itemOwner.replace("{player}", playerName));
        return newLore;
    }

    /**
     * Add origin to already existing items.
     *
     * @param itemStack The item to add origin to.
     * @param origin    The origin type.
     */
    public void addOriginTag(ItemStack itemStack, int origin) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(toolStats.originType, PersistentDataType.INTEGER, origin);
        itemStack.setItemMeta(itemMeta);
    }
}
