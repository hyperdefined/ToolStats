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

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemChecker {

    private final List<Material> validItems = new ArrayList<>();
    private final List<Material> armorItems = new ArrayList<>();
    private final List<Material> meleeItems = new ArrayList<>();
    private final List<Material> mineItems = new ArrayList<>();

    /**
     * Creates an item checker and saves all valid items we want.
     */
    public ItemChecker() {
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
        validItems.add(Material.BOW);
        validItems.add(Material.FISHING_ROD);
        validItems.add(Material.CROSSBOW);
        validItems.add(Material.ELYTRA);
        validItems.add(Material.MACE);

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
}
