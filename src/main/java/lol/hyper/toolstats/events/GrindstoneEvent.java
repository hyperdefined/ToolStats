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

package lol.hyper.toolstats.events;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GrindstoneEvent implements Listener {

    private final ToolStats toolStats;

    public GrindstoneEvent(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onGrindstone(PrepareGrindstoneEvent event) {
        if (!toolStats.config.getBoolean("tokens.enabled")) {
            return;
        }

        GrindstoneInventory inventory = event.getInventory();
        ItemStack upperSlotItem = inventory.getUpperItem();
        ItemStack lowerSlotItem = inventory.getLowerItem();

        // make sure both slots have an item
        if (upperSlotItem == null || lowerSlotItem == null) {
            return;
        }

        // make sure the first item is a valid item
        if (!toolStats.itemChecker.isValidItem(upperSlotItem.getType())) {
            return;
        }

        PersistentDataContainer lowerSlotContainer = lowerSlotItem.getItemMeta().getPersistentDataContainer();
        // check to see if the lower item is a token
        if (lowerSlotItem.getType() != Material.PAPER || !lowerSlotContainer.has(toolStats.tokenType, PersistentDataType.STRING)) {
            return;
        }

        // check to see if the token is a reset token
        String tokenType = lowerSlotContainer.get(toolStats.tokenType, PersistentDataType.STRING);
        if (tokenType == null) {
            return;
        }
        if (!tokenType.equalsIgnoreCase("reset")) {
            return;
        }

        // make sure the upper slot item has tokens applied
        PersistentDataContainer upperSlotContainer = upperSlotItem.getItemMeta().getPersistentDataContainer();
        if (!upperSlotContainer.has(toolStats.tokenApplied, PersistentDataType.STRING)) {
            return;
        }

        String appliedTokens = upperSlotContainer.get(toolStats.tokenApplied, PersistentDataType.STRING);
        if (appliedTokens == null) {
            return;
        }

        // perform the reset
        ItemStack resetItem = reset(upperSlotItem.clone(), appliedTokens);
        if (resetItem != null) {
            inventory.setResult(resetItem);
        }
    }

    /**
     * Reset a given item's stat and lore to zero.
     *
     * @param inputItem     The input item.
     * @param appliedTokens The tokens this item has.
     * @return The reset item, or null if it broke.
     */
    private ItemStack reset(ItemStack inputItem, String appliedTokens) {
        ItemMeta meta = inputItem.getItemMeta();
        if (meta == null) {
            return null;
        }

        ItemMeta finalMeta = meta.clone();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String[] tokens = appliedTokens.split(",");
        for (String token : tokens) {
            switch (appliedTokens) {
                case "player-kills": {
                    container.set(toolStats.swordPlayerKills, PersistentDataType.INTEGER, 0);
                    finalMeta = toolStats.itemLore.updatePlayerKills(inputItem, 0);
                    break;
                }
                case "mob-kills": {
                    container.set(toolStats.swordMobKills, PersistentDataType.INTEGER, 0);
                    finalMeta = toolStats.itemLore.updateMobKills(inputItem, 0);
                    break;
                }
                case "blocks-mined": {
                    container.set(toolStats.genericMined, PersistentDataType.INTEGER, 0);
                    finalMeta = toolStats.itemLore.updateBlocksMined(inputItem, 0);
                    break;
                }
                case "crops-mined": {
                    container.set(toolStats.cropsHarvested, PersistentDataType.INTEGER, 0);
                    finalMeta = toolStats.itemLore.updateCropsMined(inputItem, 0);
                    break;
                }
                case "fish-caught": {
                    container.set(toolStats.fishingRodCaught, PersistentDataType.INTEGER, 0);
                    finalMeta = toolStats.itemLore.updateFishCaught(inputItem, 0);
                    break;
                }
                case "sheep-sheared": {
                    container.set(toolStats.shearsSheared, PersistentDataType.INTEGER, 0);
                    finalMeta = toolStats.itemLore.updateSheepSheared(inputItem, 0);
                    break;
                }
                case "damage-taken": {
                    container.set(toolStats.armorDamage, PersistentDataType.INTEGER, 0);
                    finalMeta = toolStats.itemLore.updateDamage(inputItem, 0.0);
                    break;
                }
                case "arrows-shot": {
                    container.set(toolStats.arrowsShot, PersistentDataType.INTEGER, 0);
                    finalMeta = toolStats.itemLore.updateArrowsShot(inputItem, 0);
                    break;
                }
                case "flight-time": {
                    container.set(toolStats.flightTime, PersistentDataType.INTEGER, 0);
                    finalMeta = toolStats.itemLore.updateFlightTime(inputItem, 0);
                    break;
                }
            }
        }

        // return the final item
        if (finalMeta != null) {
            inputItem.setItemMeta(finalMeta);
            return inputItem;
        }
        return null;
    }
}
