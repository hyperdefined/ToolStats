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

import java.util.Arrays;
import java.util.Locale;

public class ItemChecker {

    private static final String[] validItems = { "pickaxe", "sword", "shovel", "axe", "hoe", "bow", "helmet", "chestplate", "leggings", "boots", "fishing", "elytra" };
    private static final String[] validArmor = { "helmet", "chestplate", "leggings", "boots" };
    private static final String[] validMelee = {"sword", "trident", "axe"};
    private static final String[] validMine = { "pickaxe", "axe", "hoe", "shovel", "shear" };

    /**
     * Check if item is an armor piece.
     *
     * @param itemType The item type, not name.
     * @return If the item is an armor piece.
     */
    public static boolean isArmor(Material itemType) {
        return Arrays.stream(validArmor).anyMatch(type -> itemType.toString().toLowerCase(Locale.ROOT).contains(type));
    }

    /**
     * Check if item is a tool or armor piece we want to track.
     *
     * @param itemType The item type, not name.
     * @return If the item something we want to track.
     */
    public static boolean isValidItem(Material itemType) {
        return Arrays.stream(validItems).anyMatch(type -> itemType.toString().toLowerCase(Locale.ROOT).contains(type));
    }

    /**
     * Check if item is a melee weapon.
     *
     * @param itemType The item type, not name.
     * @return If the item is a melee weapon.
     */
    public static boolean isMeleeWeapon(Material itemType) {
        return Arrays.stream(validMelee).anyMatch(type -> itemType.toString().toLowerCase(Locale.ROOT).contains(type));
    }

    /**
     * Check if item is a mining tool.
     *
     * @param itemType The item type, not name.
     * @return If the item is a mining tool.
     */
    public static boolean isMineTool(Material itemType) {
        return Arrays.stream(validMine).anyMatch(type -> itemType.toString().toLowerCase(Locale.ROOT).contains(type));
    }
}
