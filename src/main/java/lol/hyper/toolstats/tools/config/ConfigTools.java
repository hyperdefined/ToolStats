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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigTools {

    private final ToolStats toolStats;
    public static final Pattern COLOR_CODES = Pattern.compile("[&§]([0-9a-fk-or])");
    public static final Pattern CONFIG_HEX_PATTERN = Pattern.compile("[&§]#([A-Fa-f0-9]{6})");
    public static final Pattern MINECRAFT_HEX_PATTERN = Pattern.compile("§x(?:§[a-fA-F0-9]){6}|§[a-fA-F0-9]");

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

        return switch (itemType) {
            case "pickaxe" -> toolStats.config.getBoolean("enabled." + configName + ".pickaxe");
            case "sword" -> toolStats.config.getBoolean("enabled." + configName + ".sword");
            case "shovel" -> toolStats.config.getBoolean("enabled." + configName + ".shovel");
            case "axe" -> toolStats.config.getBoolean("enabled." + configName + ".axe");
            case "hoe" -> toolStats.config.getBoolean("enabled." + configName + ".hoe");
            case "shears" -> toolStats.config.getBoolean("enabled." + configName + ".shears");
            case "crossbow", "bow" -> toolStats.config.getBoolean("enabled." + configName + ".bow");
            case "trident" -> toolStats.config.getBoolean("enabled." + configName + ".trident");
            case "fishing-rod" -> toolStats.config.getBoolean("enabled." + configName + ".fishing-rod");
            case "mace" -> toolStats.config.getBoolean("enabled." + configName + ".mace");
            case "helmet", "chestplate", "leggings", "boots" ->
                    toolStats.config.getBoolean("enabled." + configName + ".armor");
            default -> false;
        };
    }

    /**
     * Format a string to be ready for lore usage.
     *
     * @param configName  The message to use.
     * @param placeHolder The placeholder text in the message.
     * @param value       The value to set the placeholder.
     * @return Formatted string, null if the configName doesn't exist.
     */
    public Component formatLore(String configName, String placeHolder, Object value) {
        String lore = toolStats.config.getString("messages." + configName);
        if (lore == null) {
            return null;
        }

        // the final component for this lore
        Component component;

        // set the placeholder to the value
        lore = lore.replace(placeHolder, String.valueOf(value));

        // if we match the old color codes, then format them as so
        Matcher hexMatcher = CONFIG_HEX_PATTERN.matcher(lore);
        Matcher colorMatcher = COLOR_CODES.matcher(lore);
        if (hexMatcher.find() || colorMatcher.find()) {
            component = LegacyComponentSerializer.legacyAmpersand().deserialize(lore);
        } else {
            // otherwise format them normally
            component = Component.text(lore);
        }

        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Remove all color codes from a message.
     *
     * @param message The message.
     * @return The message without color codes.
     */
    public String removeColor(String message) {
        message = MINECRAFT_HEX_PATTERN.matcher(message).replaceAll("");
        message = COLOR_CODES.matcher(message).replaceAll("");
        message = CONFIG_HEX_PATTERN.matcher(message).replaceAll("");
        return message;
    }
}
