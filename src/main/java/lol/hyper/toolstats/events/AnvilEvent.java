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
import org.bukkit.inventory.meta.ItemMeta;
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

        // make sure the first item is a valid item
        if (!toolStats.itemChecker.isValidItem(firstSlotMaterial)) {
            return;
        }

        PersistentDataContainer secondSlotContainer = secondSlot.getItemMeta().getPersistentDataContainer();

        // make sure the 2nd item is one of ours
        if (!secondSlotContainer.has(toolStats.toolStatsKeys.getTokenType(), PersistentDataType.STRING)) {
            return;
        }

        // get the type from the token
        String tokenType = secondSlotContainer.get(toolStats.toolStatsKeys.getTokenType(), PersistentDataType.STRING);
        if (tokenType == null) {
            return;
        }

        // don't let the player use more than one
        if (secondSlot.getAmount() > 1) {
            return;
        }

        // clone the item
        ItemStack clone = firstSlot.clone();

        if (tokenType.equalsIgnoreCase("reset")) {
            reset(event, clone);
            return;
        }

        if (tokenType.equalsIgnoreCase("remove")) {
            ItemStack removedStats = toolStats.itemLore.removeAll(clone, false);
            event.setResult(removedStats);
            event.getView().setRepairCost(toolStats.itemChecker.getCost("remove"));
            return;
        }

        // if the item is a mining tool
        if (toolStats.itemChecker.isMineTool(firstSlotMaterial)) {
            if (firstSlotMaterial.toString().toLowerCase(Locale.ROOT).contains("hoe")) {
                // the item is a hoe
                if (tokenType.equalsIgnoreCase("blocks-mined")) {
                    addToken(event, tokenType, "blocks-mined", clone);
                }
                if (tokenType.equalsIgnoreCase("crops-mined")) {
                    addToken(event, tokenType, "crops-mined", clone);
                }
            } else {
                // since shears will fall under here, check if the token is for sheep sheared
                if (firstSlotMaterial == Material.SHEARS && tokenType.equals("sheep-sheared")) {
                    addToken(event, tokenType, "sheep-sheared", clone);
                    return;
                }
                addToken(event, tokenType, "blocks-mined", clone);
            }
            // axes are a mining tool, so double check them here for player/mob kills
            if (firstSlotMaterial.toString().toLowerCase(Locale.ROOT).contains("_axe")) {
                if (tokenType.equalsIgnoreCase("player-kills")) {
                    addToken(event, tokenType, "player-kills", clone);
                    return;
                }
                if (tokenType.equalsIgnoreCase("mob-kills")) {
                    addToken(event, tokenType, "mob-kills", clone);
                    return;
                }
                if (tokenType.equalsIgnoreCase("damage-done")) {
                    addToken(event, tokenType, "damage-done", clone);
                    return;
                }
                if (tokenType.equalsIgnoreCase("wither-kills")) {
                    addToken(event, tokenType, "wither-kills", clone);
                    return;
                }
                if (tokenType.equalsIgnoreCase("enderdragon-kills")) {
                    addToken(event, tokenType, "enderdragon-kills", clone);
                    return;
                }
                if (tokenType.equalsIgnoreCase("critical-strikes")) {
                    addToken(event, tokenType, "critical-strikes", clone);
                    return;
                }
                if (tokenType.equalsIgnoreCase("logs-stripped")) {
                    addToken(event, tokenType, "logs-stripped", clone);
                    return;
                }
            }
            return;
        }
        if (toolStats.itemChecker.canGlide(clone)) {
            addToken(event, tokenType, "flight-time", clone);
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
            if (tokenType.equalsIgnoreCase("mob-kills")) {
                addToken(event, tokenType, "mob-kills", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("damage-done")) {
                addToken(event, tokenType, "damage-done", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("wither-kills")) {
                addToken(event, tokenType, "wither-kills", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("enderdragon-kills")) {
                addToken(event, tokenType, "enderdragon-kills", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("critical-strikes")) {
                addToken(event, tokenType, "critical-strikes", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("trident-throws")) {
                addToken(event, tokenType, "trident-throws", clone);
                return;
            }
            return;
        }
        if (firstSlotMaterial == Material.BOW || firstSlotMaterial == Material.CROSSBOW) {
            if (tokenType.equalsIgnoreCase("player-kills")) {
                addToken(event, tokenType, "player-kills", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("mob-kills")) {
                addToken(event, tokenType, "mob-kills", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("arrows-shot")) {
                addToken(event, tokenType, "arrows-shot", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("damage-done")) {
                addToken(event, tokenType, "damage-done", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("wither-kills")) {
                addToken(event, tokenType, "wither-kills", clone);
                return;
            }
            if (tokenType.equalsIgnoreCase("enderdragon-kills")) {
                addToken(event, tokenType, "enderdragon-kills", clone);
                return;
            }
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
                if (toolStats.config.getBoolean("enabled.crops-harvested")) {
                    newItem.setItemMeta(toolStats.itemLore.updateCropsMined(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "blocks-mined": {
                if (toolStats.configTools.checkConfig(newItem.getType(), "blocks-mined")) {
                    newItem.setItemMeta(toolStats.itemLore.updateBlocksMined(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "damage-taken": {
                if (toolStats.config.getBoolean("enabled.armor-damage")) {
                    newItem.setItemMeta(toolStats.itemLore.updateArmorDamage(newItem, 0.0, false));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "damage-done": {
                if (toolStats.configTools.checkConfig(newItem.getType(), "damage-done")) {
                    newItem.setItemMeta(toolStats.itemLore.updateWeaponDamage(newItem, 0.0, false));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "mob-kills": {
                if (toolStats.configTools.checkConfig(newItem.getType(), "mob-kills")) {
                    newItem.setItemMeta(toolStats.itemLore.updateMobKills(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "player-kills": {
                if (toolStats.configTools.checkConfig(newItem.getType(), "player-kills")) {
                    newItem.setItemMeta(toolStats.itemLore.updatePlayerKills(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "arrows-shot": {
                if (toolStats.config.getBoolean("enabled.arrows-shot")) {
                    newItem.setItemMeta(toolStats.itemLore.updateArrowsShot(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "sheep-sheared": {
                if (toolStats.config.getBoolean("enabled.sheep-sheared")) {
                    newItem.setItemMeta(toolStats.itemLore.updateSheepSheared(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "flight-time": {
                if (toolStats.config.getBoolean("enabled.flight-time")) {
                    newItem.setItemMeta(toolStats.itemLore.updateFlightTime(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "fish-caught": {
                if (toolStats.config.getBoolean("enabled.fish-caught")) {
                    newItem.setItemMeta(toolStats.itemLore.updateFishCaught(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "wither-kills": {
                if (toolStats.config.getBoolean("enabled.bosses-killed.wither")) {
                    newItem.setItemMeta(toolStats.itemLore.updateBossesKilled(newItem, 0, "wither"));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "enderdragon-kills": {
                if (toolStats.config.getBoolean("enabled.bosses-killed.enderdragon")) {
                    newItem.setItemMeta(toolStats.itemLore.updateBossesKilled(newItem, 0, "enderdragon"));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "critical-strikes": {
                if (toolStats.config.getBoolean("enabled.critical-strikes")) {
                    newItem.setItemMeta(toolStats.itemLore.updateCriticalStrikes(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "trident-throws": {
                if (toolStats.config.getBoolean("enabled.trident-throws")) {
                    newItem.setItemMeta(toolStats.itemLore.updateTridentThrows(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
            case "logs-stripped": {
                if (toolStats.config.getBoolean("enabled.logs-stripped")) {
                    newItem.setItemMeta(toolStats.itemLore.updateLogsStripped(newItem, 0));
                } else {
                    event.setResult(null);
                    return;
                }
                break;
            }
        }
        event.setResult(newItem);
        event.getView().setRepairCost(toolStats.itemChecker.getCost(targetToken));
    }

    /**
     * Reset an item's stats. This function is... gross.
     * Because of how the lore system is set up, we have to basically revert the stats.
     * The lore function requires the old value, then adds x to the current stat.
     * This is required, so it can find the old line in the lore and update it.
     * So we simply make the stat negative, and add it to reset it to zero.
     * Gross? Yeah, but I don't want to rewrite the lore system again...
     *
     * @param event     The PrepareAnvilEvent event.
     * @param inputItem The input item to reset.
     */
    private void reset(PrepareAnvilEvent event, ItemStack inputItem) {
        ItemStack finalItem = inputItem.clone();
        ItemMeta meta = finalItem.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(toolStats.toolStatsKeys.getPlayerKills())) {
            Integer playerKills = container.get(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER);
            if (playerKills == null) {
                return;
            }
            meta = toolStats.itemLore.updatePlayerKills(finalItem, -playerKills);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getMobKills())) {
            Integer mobKills = container.get(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER);
            if (mobKills == null) {
                return;
            }
            meta = toolStats.itemLore.updateMobKills(finalItem, -mobKills);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getBlocksMined())) {
            Integer blocksMined = container.get(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER);
            if (blocksMined == null) {
                return;
            }
            meta = toolStats.itemLore.updateBlocksMined(finalItem, -blocksMined);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getCropsHarvested())) {
            Integer cropsHarvested = container.get(toolStats.toolStatsKeys.getCropsHarvested(), PersistentDataType.INTEGER);
            if (cropsHarvested == null) {
                return;
            }
            meta = toolStats.itemLore.updateCropsMined(finalItem, -cropsHarvested);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getFishCaught())) {
            Integer fishCaught = container.get(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER);
            if (fishCaught == null) {
                return;
            }
            meta = toolStats.itemLore.updateFishCaught(finalItem, -fishCaught);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getSheepSheared())) {
            Integer sheepSheared = container.get(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER);
            if (sheepSheared == null) {
                return;
            }
            meta = toolStats.itemLore.updateSheepSheared(finalItem, -sheepSheared);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getArmorDamage())) {
            Double armorDamage = container.get(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE);
            if (armorDamage == null) {
                return;
            }
            meta = toolStats.itemLore.updateArmorDamage(finalItem, -armorDamage, true);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getDamageDone())) {
            Double damageDone = container.get(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE);
            if (damageDone == null) {
                return;
            }
            meta = toolStats.itemLore.updateArmorDamage(finalItem, -damageDone, true);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getArrowsShot())) {
            Integer arrowsShot = container.get(toolStats.toolStatsKeys.getArrowsShot(), PersistentDataType.INTEGER);
            if (arrowsShot == null) {
                return;
            }
            meta = toolStats.itemLore.updateArrowsShot(finalItem, -arrowsShot);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getFlightTime())) {
            Long flightTime = container.get(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG);
            if (flightTime == null) {
                return;
            }
            meta = toolStats.itemLore.updateFlightTime(finalItem, -flightTime);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getWitherKills())) {
            Integer witherKills = container.get(toolStats.toolStatsKeys.getWitherKills(), PersistentDataType.INTEGER);
            if (witherKills == null) {
                return;
            }
            meta = toolStats.itemLore.updateBossesKilled(finalItem, -witherKills, "wither");
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getEnderDragonKills())) {
            Integer enderDragonKills = container.get(toolStats.toolStatsKeys.getEnderDragonKills(), PersistentDataType.INTEGER);
            if (enderDragonKills == null) {
                return;
            }
            meta = toolStats.itemLore.updateBossesKilled(finalItem, -enderDragonKills, "enderdragon");
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getCriticalStrikes())) {
            Integer criticalStrikes = container.get(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER);
            if (criticalStrikes == null) {
                return;
            }
            meta = toolStats.itemLore.updateCriticalStrikes(finalItem, -criticalStrikes);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getTridentThrows())) {
            Integer tridentThrows = container.get(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER);
            if (tridentThrows == null) {
                return;
            }
            meta = toolStats.itemLore.updateTridentThrows(finalItem, -tridentThrows);
            finalItem.setItemMeta(meta);
        }
        if (container.has(toolStats.toolStatsKeys.getLogsStripped())) {
            Integer logsStripped = container.get(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER);
            if (logsStripped == null) {
                return;
            }
            meta = toolStats.itemLore.updateLogsStripped(finalItem, -logsStripped);
            finalItem.setItemMeta(meta);
        }
        event.setResult(finalItem);
        event.getView().setRepairCost(toolStats.itemChecker.getCost("reset"));
    }
}
