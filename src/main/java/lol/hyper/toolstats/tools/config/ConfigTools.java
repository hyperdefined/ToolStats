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

package lol.hyper.toolstats.tools.config;

import lol.hyper.toolstats.ToolStats;
import lol.hyper.toolstats.tools.ItemLore;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.regex.Matcher;

public class ConfigTools {

    private final ToolStats toolStats;

    public ConfigTools(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Checks the config to see if we want to show lore on certain items.
     *
     * @param material   The item type to check.
     * @param configName The config we are checking under.
     * @return If we want to allow lore or not.
     */
    public boolean checkConfig(Material material, String configName) {
        String itemName = material.toString().toLowerCase();
        String itemType = null;
        // hardcode these
        if (material == Material.BOW || material == Material.CROSSBOW || material == Material.SHEARS || material == Material.TRIDENT || material == Material.FISHING_ROD) {
            switch (material) {
                case CROSSBOW:
                case BOW: {
                    itemType = "bow";
                    break;
                }
                case SHEARS: {
                    itemType = "shears";
                    break;
                }
                case TRIDENT: {
                    itemType = "trident";
                    break;
                }
                case FISHING_ROD: {
                    itemType = "fishing-rod";
                    break;
                }
            }
        } else {
            itemType = itemName.substring(itemName.indexOf('_') + 1);
        }

        switch (itemType) {
            case "pickaxe": {
                return toolStats.config.getBoolean("enabled." + configName + ".pickaxe");
            }
            case "sword": {
                return toolStats.config.getBoolean("enabled." + configName + ".sword");
            }
            case "shovel": {
                return toolStats.config.getBoolean("enabled." + configName + ".shovel");
            }
            case "axe": {
                return toolStats.config.getBoolean("enabled." + configName + ".axe");
            }
            case "hoe": {
                return toolStats.config.getBoolean("enabled." + configName + ".hoe");
            }
            case "shears": {
                return toolStats.config.getBoolean("enabled." + configName + ".shears");
            }
            case "crossbow":
            case "bow": {
                return toolStats.config.getBoolean("enabled." + configName + ".bow");
            }
            case "trident": {
                return toolStats.config.getBoolean("enabled." + configName + ".trident");
            }
            case "fishing-rod": {
                return toolStats.config.getBoolean("enabled." + configName + ".fishing-rod");
            }
            case "helmet":
            case "chestplate":
            case "leggings":
            case "boots": {
                return toolStats.config.getBoolean("enabled." + configName + ".armor");
            }
        }
        return false;
    }

    /**
     * Format a string to be ready for lore usage.
     *
     * @param configName  The message to use.
     * @param placeHolder The placeholder text in the message.
     * @param value       The value to set the placeholder.
     * @return Formatted string, null if the configName doesn't exist.
     */
    public String formatLore(String configName, String placeHolder, Object value) {
        String lore = toolStats.config.getString("messages." + configName);
        if (lore == null) {
            return null;
        }

        // set the placeholder to the value
        lore = lore.replace(placeHolder, String.valueOf(value));

        Matcher hexMatcher = ItemLore.HEX_PATTERN.matcher(lore);
        while (hexMatcher.find()) {
            String hexCode = hexMatcher.group(1);
            lore = lore.replaceAll(hexMatcher.group(), net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString());
        }

        Matcher colorMatcher = ItemLore.COLOR_CODES.matcher(lore);
        while (colorMatcher.find()) {
            String colorCode = colorMatcher.group(1);
            lore = lore.replaceAll("&" + colorCode, ChatColor.getByChar(colorCode).toString());
        }

        return ChatColor.translateAlternateColorCodes('§', lore);
    }

    /**
     * Remove all color codes from a message.
     *
     * @param message The message.
     * @return The message without color codes.
     */
    public String removeColor(String message) {
        message = ItemLore.COLOR_CODES.matcher(message).replaceAll("");
        message = ItemLore.HEX_PATTERN.matcher(message).replaceAll("");
        return message;
    }
}
