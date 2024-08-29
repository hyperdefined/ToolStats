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
import org.bukkit.ChatColor;
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
     * Adds/updates lore for an item.
     *
     * @param placeholder      The placeholder from the config. ex: {kills}
     * @param placeholderValue The value to replace the placeholder.
     * @param configLorePath   The path to the config message.
     * @return The item's new lore.
     */
    public List<String> addItemLore(ItemMeta itemMeta, String placeholder, String placeholderValue, String configLorePath) {
        String configLore = toolStats.configTools.getLoreFromConfig(configLorePath, false);
        String configLoreRaw = toolStats.configTools.getLoreFromConfig(configLorePath, true);

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
                String line = ChatColor.stripColor(newLore.get(x));
                if (line.contains(configLore)) {
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
                dateCreatedLore = toolStats.configTools.getLoreFromConfig("looted.looted-on", true);
                itemOwnerLore = toolStats.configTools.getLoreFromConfig("looted.looted-by", true);

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
                dateCreatedLore = toolStats.configTools.getLoreFromConfig("traded.traded-on", true);
                itemOwnerLore = toolStats.configTools.getLoreFromConfig("traded.traded-by", true);

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
                dateCreatedLore = toolStats.configTools.getLoreFromConfig("looted.found-on", true);
                itemOwnerLore = toolStats.configTools.getLoreFromConfig("looted.found-by", true);

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
                dateCreatedLore = toolStats.configTools.getLoreFromConfig("fished.caught-on", true);
                itemOwnerLore = toolStats.configTools.getLoreFromConfig("fished.caught-by", true);

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
                dateCreatedLore = toolStats.configTools.getLoreFromConfig("spawned-in.spawned-on", true);
                itemOwnerLore = toolStats.configTools.getLoreFromConfig("spawned-in.spawned-by", true);

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

    /**
     * Determine an item's origin based on lore.
     *
     * @param itemMeta The item's meta.
     * @param elytra   If they item is an elytra.
     * @return The new item meta with the new origin tag. Returns null if origin cannot be determined.
     */
    public ItemMeta getOrigin(ItemMeta itemMeta, boolean elytra) {
        List<String> lore;
        if (!itemMeta.hasLore()) {
            return null;
        }
        lore = itemMeta.getLore();
        Integer origin = null;

        String createdBy = toolStats.configTools.getLoreFromConfig("created.created-by", false);
        String createdOn = toolStats.configTools.getLoreFromConfig("created.created-on", false);
        String caughtBy = toolStats.configTools.getLoreFromConfig("fished.caught-by", false);
        String lootedBy = toolStats.configTools.getLoreFromConfig("looted.looted-by", false);
        String foundBy = toolStats.configTools.getLoreFromConfig("looted.found-by", false);
        String tradedBy = toolStats.configTools.getLoreFromConfig("traded.traded-by", false);

        for (String line : lore) {
            // this is the worst code I have ever written
            if (createdBy != null && line.contains(createdBy)) {
                origin = 0;
            }
            if (createdOn != null && line.contains(createdOn)) {
                origin = 0;
            }
            if (caughtBy != null && line.contains(caughtBy)) {
                origin = 5;
            }
            if (lootedBy != null && line.contains(lootedBy)) {
                origin = 2;
            }
            // because the config changed, "found-by" was being used for ALL looted items
            // this includes elytras, so we have to check for this mistake
            if (foundBy != null && line.contains(foundBy)) {
                if (elytra) {
                    origin = 4;
                } else {
                    origin = 5;
                }
            }
            if (tradedBy != null && line.contains(tradedBy)) {
                origin = 3;
            }
        }

        if (origin == null) {
            return null;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(toolStats.originType, PersistentDataType.INTEGER, origin);
        return itemMeta;
    }
}
