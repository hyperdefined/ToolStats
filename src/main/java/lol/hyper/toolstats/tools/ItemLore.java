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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
            boolean hasLore = false;
            // keep track of line index
            // this doesn't mess the lore of existing items
            for (int x = 0; x < newLore.size(); x++) {
                // check to see if the line matches the config value
                // this means we update this line only!
                if (newLore.get(x).contains(configLore)) {
                    hasLore = true;
                    newLore.set(x, newLine);
                    break;
                }
            }
            // if the item has lore but doesn't have our line, add it
            if (!hasLore) {
                newLore.add(newLine);
            }
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
     * @param type          The type of new ownership.
     * @return The item's new lore.
     */
    public List<String> addNewOwner(ItemMeta itemMeta, String playerName, String formattedDate, String type) {
        String dateCreated = null;
        String itemOwner = null;
        switch (type) {
            case "LOOTED": {
                dateCreated = toolStats.getLoreFromConfig("looted.found-on", true);
                itemOwner = toolStats.getLoreFromConfig("looted.found-by", true);
                break;
            }
            case "CREATED": {
                dateCreated = toolStats.getLoreFromConfig("created.created-on", true);
                itemOwner = toolStats.getLoreFromConfig("created.created-by", true);
                break;
            }
            case "FISHED": {
                dateCreated = toolStats.getLoreFromConfig("fished.caught-on", true);
                itemOwner = toolStats.getLoreFromConfig("fished.caught-by", true);
                break;
            }
            case "TRADED": {
                dateCreated = toolStats.getLoreFromConfig("traded.traded-on", true);
                itemOwner = toolStats.getLoreFromConfig("traded.traded-by", true);
                break;
            }
        }

        if (dateCreated == null || itemOwner == null) {
            toolStats.logger.warning("There is no lore message for messages." + type.toLowerCase(Locale.ENGLISH) + "!");
            toolStats.logger.warning("Unable to update lore for item.");
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
}
