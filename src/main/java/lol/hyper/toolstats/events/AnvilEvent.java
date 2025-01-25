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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

public class AnvilEvent implements Listener {

    private final ToolStats toolStats;

    public AnvilEvent(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilEvent(PrepareAnvilEvent event) {
        // only listen if the token system is enabled
        if (!toolStats.config.getBoolean("tokens.enabled")) {
            return;
        }
        AnvilInventory inventory = event.getInventory();

        ItemStack firstSlot = inventory.getItem(0);
        ItemStack secondSlot = inventory.getItem(1);

        // make sure both slots have items
        if (firstSlot == null || secondSlot == null) {
            return;
        }

        Material firstSlotMaterial = firstSlot.getType();
        Material secondSlotMaterial = secondSlot.getType();

        // make sure the first item is a valid item
        if (!toolStats.itemChecker.isValidItem(firstSlotMaterial)) {
            return;
        }

        PersistentDataContainer secondSlotContainer = secondSlot.getItemMeta().getPersistentDataContainer();

        // make sure the 2nd item is one of ours
        if (secondSlotMaterial != Material.PAPER || !secondSlotContainer.has(toolStats.tokenType, PersistentDataType.STRING)) {
            return;
        }

        //get the type from the token
        String tokenType = secondSlotContainer.get(toolStats.tokenType, PersistentDataType.STRING);
        if (tokenType == null) {
            return;
        }

        // clone the item
        ItemStack clone = firstSlot.clone();

        // if the item is a mining tool
        if (toolStats.itemChecker.isMineTool(firstSlotMaterial)) {
            if (firstSlotMaterial.toString().toLowerCase(Locale.ROOT).contains("hoe")) {
                // the item is a hoe
                addToken(event, tokenType, "crops-mined", clone);
            } else {
                // since shears will fall under here, check if the token is for sheep sheared
                if (firstSlotMaterial == Material.SHEARS && tokenType.equals("sheep-sheared")) {
                    addToken(event, tokenType, "sheep-sheared", clone);
                    return;
                }
                addToken(event, tokenType, "blocks-mined", clone);
            }
            return;
        }
        if (toolStats.itemChecker.isArmor(firstSlotMaterial)) {
            addToken(event, tokenType, "damage-taken", clone);
            return;
        }
        if (toolStats.itemChecker.isMeleeWeapon(firstSlotMaterial)) {
            if (tokenType.equalsIgnoreCase("player-kills")) {
                addToken(event, tokenType, "player-kills", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("mobs-kills")) {
                addToken(event, tokenType, "mobs-kills", clone);
                return;
            }
            return;
        }
        if (firstSlotMaterial == Material.BOW || firstSlotMaterial == Material.CROSSBOW) {
            if (tokenType.equalsIgnoreCase("player-kills")) {
                addToken(event, tokenType, "player-kills", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("mobs-kills")) {
                addToken(event, tokenType, "mobs-kills", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("arrows-shot")) {
                addToken(event, tokenType, "arrows-shot", clone);
                return;
            }
            return;
        }
        if (firstSlotMaterial == Material.ELYTRA) {
            addToken(event, tokenType, "flight-time", clone);
            return;
        }
        if (firstSlotMaterial == Material.FISHING_ROD) {
            addToken(event, tokenType, "fish-caught", clone);
        }
    }

    /**
     * Add token to an item.
     *
     * @param event         The anvil event.
     * @param attemptToken  The token in the 2nd slot of the anvil. The one the player wants to add.
     * @param targetToken   The token we are checking for. This should match the tool.
     * @param firstSlotItem The item in the first slot.
     */
    private void addToken(PrepareAnvilEvent event, String attemptToken, String targetToken, ItemStack firstSlotItem) {
        // make sure the token we are using is for this tool
        if (!attemptToken.equalsIgnoreCase(targetToken)) {
            event.setResult(null);
            return;
        }

        // if the item already has the token, ignore
        if (toolStats.itemChecker.checkTokens(firstSlotItem.getItemMeta().getPersistentDataContainer(), targetToken)) {
            event.setResult(null);
            return;
        }

        // apply the token and set the result
        ItemStack newItem = toolStats.itemChecker.addToken(firstSlotItem, targetToken);
        switch (targetToken) {
            case "crops-mined": {
                event.setResult(toolStats.itemLore.updateCropsMined(newItem, 0));
                break;
            }
            case "blocks-mined": {
                event.setResult(toolStats.itemLore.updateBlocksMined(newItem, 0));
                break;
            }
            case "damage-taken": {
                event.setResult(toolStats.itemLore.updateDamage(newItem, 0.0));
                break;
            }
            case "mob-kills": {
                event.setResult(toolStats.itemLore.updateMobKills(newItem, 0));
                break;
            }
            case "player-kills": {
                event.setResult(toolStats.itemLore.updatePlayerKills(newItem, 0));
                break;
            }
            case "arrows-shot": {
                event.setResult(toolStats.itemLore.updateArrowsShot(newItem, 0));
                break;
            }
            case "sheep-sheared": {
                event.setResult(toolStats.itemLore.updateSheepSheared(newItem, 0));
                break;
            }
            case "flight-time": {
                event.setResult(toolStats.itemLore.updateFlightTime(newItem, 0));
                break;
            }
            case "fish-caught": {
                event.setResult(toolStats.itemLore.updateFishCaught(newItem, 0));
                break;
            }
        }
        event.getView().setRepairCost(toolStats.itemChecker.getCost(targetToken));
    }
}
