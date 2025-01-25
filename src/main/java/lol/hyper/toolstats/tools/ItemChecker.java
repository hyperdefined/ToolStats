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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ItemChecker {

    private final List<Material> validItems = new ArrayList<>();
    private final List<Material> armorItems = new ArrayList<>();
    private final List<Material> meleeItems = new ArrayList<>();
    private final List<Material> mineItems = new ArrayList<>();
    private final ToolStats toolStats;

    public ItemChecker(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Set up the item checker.
     */
    public void setup() {
        for (Material material : Material.values()) {
            String lowerCase = material.toString().toLowerCase(Locale.ROOT);
            if (lowerCase.contains("_pickaxe") || lowerCase.contains("_axe") || lowerCase.contains("_hoe") || lowerCase.contains("_shovel")) {
                mineItems.add(material);
            }

            if (lowerCase.contains("_sword") || lowerCase.contains("_axe")) {
                meleeItems.add(material);
            }

            if (lowerCase.contains("_helmet") || lowerCase.contains("_chestplate") || lowerCase.contains("_leggings") || lowerCase.contains("_boots")) {
                armorItems.add(material);
            }
        }

        // hardcode these
        mineItems.add(Material.SHEARS);
        meleeItems.add(Material.TRIDENT);
        meleeItems.add(Material.MACE);

        validItems.add(Material.BOW);
        validItems.add(Material.CROSSBOW);
        validItems.add(Material.FISHING_ROD);
        validItems.add(Material.ELYTRA);

        // combine the lists
        validItems.addAll(armorItems);
        validItems.addAll(meleeItems);
        validItems.addAll(mineItems);
    }

    /**
     * Check if item is an armor piece.
     *
     * @param itemType The item type, not name.
     * @return If the item is an armor piece.
     */
    public boolean isArmor(Material itemType) {
        return armorItems.contains(itemType);
    }

    /**
     * Check if item is a tool or armor piece we want to track.
     *
     * @param itemType The item type, not name.
     * @return If the item something we want to track.
     */
    public boolean isValidItem(Material itemType) {
        return validItems.contains(itemType);
    }

    /**
     * Check if item is a melee weapon.
     *
     * @param itemType The item type, not name.
     * @return If the item is a melee weapon.
     */
    public boolean isMeleeWeapon(Material itemType) {
        return meleeItems.contains(itemType);
    }

    /**
     * Check if item is a mining tool.
     *
     * @param itemType The item type, not name.
     * @return If the item is a mining tool.
     */
    public boolean isMineTool(Material itemType) {
        return mineItems.contains(itemType);
    }

    /**
     * Check a given item for a target token.
     *
     * @param container   The PDC of the item.
     * @param targetToken The target to look for.
     * @return True if the item has a given token, false if not.
     */
    public boolean checkTokens(PersistentDataContainer container, String targetToken) {
        // make sure the item has tokens
        if (!container.has(toolStats.tokenApplied, PersistentDataType.STRING)) {
            return false;
        }

        // get the tokens for this item
        String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
        if (tokens == null) {
            return false;
        }

        return tokens.contains(targetToken);
    }

    /**
     * Get the tokens for a given item.
     *
     * @param item The item.
     * @return An array of the tokens, empty if there are none.
     */
    private String[] getTokens(ItemStack item) {
        // make sure the item has tokens
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return new String[0];
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(toolStats.tokenApplied, PersistentDataType.STRING)) {
            return new String[0];
        }

        // get the tokens for this item
        String tokensRaw = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
        if (tokensRaw == null) {
            return new String[0];
        }

        return tokensRaw.split(",");
    }

    /**
     * Add a token to an item.
     *
     * @param item  The item.
     * @param token The token to add.
     * @return The new PDC with the new token. Null if something went wrong.
     */
    public ItemStack addToken(ItemStack item, String token) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String[] tokens = getTokens(item);
        // there are no tokens
        if (tokens.length == 0) {
            container.set(toolStats.tokenApplied, PersistentDataType.STRING, token);
        } else {
            // other tokens exist, so add
            String[] newTokens = Arrays.copyOf(tokens, tokens.length + 1);
            newTokens[tokens.length] = token;
            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get the XP levels required to use token in anvil.
     *
     * @param tokenType The token type.
     * @return The amount of levels to use.
     */
    public int getCost(String tokenType) {
        return toolStats.config.getInt("tokens.data." + tokenType + ".levels");
    }
}
