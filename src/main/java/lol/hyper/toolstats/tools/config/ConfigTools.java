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
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * @return If we want to add data or not.
     */
    public boolean checkConfig(Material material, String configName) {
        if (toolStats.config.getConfigurationSection("enabled." + configName) == null) {
            toolStats.logger.warning("Missing config section for enabled" + configName);
            return false;
        }

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
            toolStats.logger.warning("Unable to find config message for: messages." + configName);
            return null;
        }

        // if the config message is empty, don't send it
        if (lore.isEmpty()) {
            return null;
        }

        // the final component for this lore
        Component component;

        // set the placeholder to the value
        if (placeHolder != null && value != null) {
            lore = lore.replace(placeHolder, String.valueOf(value));
        }

        component = toolStats.textUtils.format(lore);
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Format a string with several placeholders to be ready for lore usage.
     *
     * @param configName         The message to use.
     * @param placeHoldersValues Map containing placeholders names as keys and values.
     * @return Formatted string, null if the configName doesn't exist.
     */
    public Component formatLoreMultiplePlaceholders(String configName, Map<String, String> placeHoldersValues) {
        String lore = toolStats.config.getString("messages." + configName);
        if (lore == null) {
            toolStats.logger.warning("Unable to find config message for: messages." + configName);
            return null;
        }

        // if the config message is empty, don't send it
        if (lore.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("\\{([^}]+)\\}(\\S*)\\s*");
        Matcher matcher = pattern.matcher(lore);

        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String unit = matcher.group(2);

            result.append(lore, lastEnd, matcher.start());

            if (placeHoldersValues.containsKey(placeholder)) {
                result.append(placeHoldersValues.get(placeholder)).append(unit).append(" ");
            }

            // Update lastEnd to end of the match
            lastEnd = matcher.end();
        }
        if (lastEnd < lore.length()) {
            result.append(lore.substring(lastEnd));
        }

        Component component;
        // Clean output text
        String outputText = result.toString().replaceAll("\\s+", " ").trim();
        component = toolStats.textUtils.format(outputText);

        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Get the token item's lore from config.
     *
     * @param tokenType The type.
     * @return The lore.
     */
    public List<Component> getTokenLore(String tokenType) {
        List<String> raw = toolStats.config.getStringList("tokens.data." + tokenType + ".lore");
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }

        List<Component> finalLore = new ArrayList<>();
        for (String line : raw) {
            if (line.contains("{levels}")) {
                Integer levels = toolStats.config.getInt("tokens.data." + tokenType + ".levels");
                // will return 0 if it doesn't exist
                if (levels != 0) {
                    line = line.replace("{levels}", String.valueOf(levels));
                }
            }
            Component component = toolStats.textUtils.format(line);
            component = component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
            finalLore.add(component);
        }
        return finalLore;
    }
}
