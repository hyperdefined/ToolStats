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
     * Updates existing lore on an item.
     *
     * @param itemMeta The item's meta.
     * @param oldLine  The old line to replace.
     * @param newLine  The new line to replace oldLine.
     * @return The item's new lore.
     */
    public List<String> updateItemLore(ItemMeta itemMeta, String oldLine, String newLine) {
        List<String> itemLore;
        oldLine = toolStats.configTools.removeColor(oldLine);
        if (itemMeta.hasLore()) {
            itemLore = itemMeta.getLore();
            // keep track of line index
            // this doesn't mess the lore of existing items
            for (int x = 0; x < itemLore.size(); x++) {
                // check to see if the line matches the config value
                // this means we update this line only!
                String line = toolStats.configTools.removeColor(itemLore.get(x));
                if (line.equals(oldLine)) {
                    itemLore.set(x, newLine);
                    return itemLore;
                }
            }
            // if the item has lore, but we didn't find the line
            itemLore.add(newLine);
        } else {
            // if the item has no lore, create a new list and add the line
            itemLore = new ArrayList<>();
            itemLore.add(newLine);
        }
        return itemLore;
    }

    public List<String> addItemLore(ItemMeta itemMeta, String newLine) {
        List<String> itemLore;
        if (itemMeta.hasLore()) {
            itemLore = itemMeta.getLore();
            itemLore.add(newLine);
        } else {
            itemLore = new ArrayList<>();
            itemLore.add(newLine);
        }
        return itemLore;
    }

    /**
     * Adds new ownership to an item.
     *
     * @param itemMeta      The item meta.
     * @param playerName    The new owner of item.
     * @param formattedDate The date of the ownership.
     * @return The item's new lore.
     */
    public List<String> addNewOwner(ItemMeta itemMeta, String playerName, String formattedDate) {
        String dateCreatedLore;
        String itemOwnerLore;
        Integer origin = null;
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (container.has(toolStats.originType, PersistentDataType.INTEGER)) {
            origin = container.get(toolStats.originType, PersistentDataType.INTEGER);
        }

        // if the origin is broken, don't try to set the lore
        if (origin == null) {
            toolStats.logger.info("Unable to determine origin for item " + itemMeta.getAsString());
            toolStats.logger.info("This IS a bug, please report this to the GitHub.");
            return itemMeta.getLore();
        }

        // set the lore based on the origin
        switch (origin) {
            case 2: {
                dateCreatedLore = toolStats.configTools.formatLore("looted.looted-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("looted.looted-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.looted.looted-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.looted.looted-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                break;
            }
            case 3: {
                dateCreatedLore = toolStats.configTools.formatLore("traded.traded-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("traded.traded-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.traded.traded-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.traded.traded-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                break;
            }
            case 4: {
                dateCreatedLore = toolStats.configTools.formatLore("looted.found-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("looted.found-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.looted.found-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.looted.found-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                break;
            }
            case 5: {
                dateCreatedLore = toolStats.configTools.formatLore("fished.caught-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("fished.caught-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.fished.caught-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.fished.caught-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                break;
            }
            case 6: {
                dateCreatedLore = toolStats.configTools.formatLore("spawned-in.spawned-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("spawned-in.spawned-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.spawned-in.spawned-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.spawned-in.spawned-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.getLore();
                }
                break;
            }
            default: {
                toolStats.logger.warning("Origin " + origin + " was found. Data was modified OR something REALLY broke.");
                toolStats.logger.warning(itemMeta.getAsString());
                return itemMeta.getLore();
            }
        }

        List<String> newLore;
        if (itemMeta.hasLore()) {
            newLore = itemMeta.getLore();
        } else {
            newLore = new ArrayList<>();
        }

        newLore.add(dateCreatedLore.replace("{date}", formattedDate));
        newLore.add(itemOwnerLore.replace("{player}", playerName));
        return newLore;
    }
}
