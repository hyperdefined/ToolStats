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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ItemLore {

    private final ToolStats toolStats;

    public ItemLore(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Updates existing lore on an item.
     *
     * @param itemMeta The item's meta.
     * @param oldLine  The old line to replace.
     * @param newLine  The new line to replace oldLine.
     * @return The item's new lore.
     */
    public List<Component> updateItemLore(ItemMeta itemMeta, Component oldLine, Component newLine) {
        List<Component> itemLore;
        if (itemMeta.hasLore()) {
            itemLore = itemMeta.lore();
            // keep track of line index
            // this doesn't mess the lore of existing items
            for (int x = 0; x < itemLore.size(); x++) {
                String line = PlainTextComponentSerializer.plainText().serialize(itemLore.get(x));
                // find the old line to update, keeping index
                // this means we update this line only!
                if (line.equals(PlainTextComponentSerializer.plainText().serialize(oldLine))) {
                    itemLore.set(x, newLine);
                    return itemLore;
                }
            }
            // if the item has lore, but we didn't find the line
            itemLore.add(newLine);
        } else {
            // if the item has no lore, create a new list and add the line
            itemLore = new ArrayList<>();
            itemLore.add(newLine);
        }
        return itemLore;
    }

    /**
     * Add lore to a given item.
     *
     * @param itemMeta The item's meta.
     * @param newLine  The new line to add to the lore.
     * @return The new item's lore.
     */
    public List<Component> addItemLore(ItemMeta itemMeta, Component newLine) {
        List<Component> itemLore;
        if (itemMeta.hasLore()) {
            itemLore = itemMeta.lore();
            itemLore.add(newLine);
        } else {
            itemLore = new ArrayList<>();
            itemLore.add(newLine);
        }
        return itemLore;
    }

    /**
     * Remove a given lore from an item.
     *
     * @param inputLore The item's lore.
     * @param toRemove  The line to remove.
     * @return The lore with the line removed.
     */
    public List<Component> removeLore(List<Component> inputLore, Component toRemove) {
        if (inputLore == null) {
            return Collections.emptyList();
        }
        List<Component> newLore = new ArrayList<>(inputLore);
        newLore.removeIf(line -> PlainTextComponentSerializer.plainText().serialize(line).equals(PlainTextComponentSerializer.plainText().serialize(toRemove)));
        return newLore;
    }

    /**
     * Add x to the crops mined stat.
     *
     * @param playerTool The tool to update.
     */
    public ItemMeta updateCropsMined(ItemStack playerTool, int add) {
        ItemStack clone = playerTool.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }
        // read the current stats from the item
        // if they don't exist, then start from 0
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.crops-harvested")) {
            if (container.has(toolStats.cropsHarvested)) {
                Integer cropsMined = container.get(toolStats.cropsHarvested, PersistentDataType.INTEGER);
                if (cropsMined == null) {
                    return null;
                }
                container.remove(toolStats.cropsHarvested);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "crops-mined");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldCropsMinedFormatted = toolStats.numberFormat.formatInt(cropsMined);
                    Component lineToRemove = toolStats.configTools.formatLore("crops-harvested", "{crops}", oldCropsMinedFormatted);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "crops-mined");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.cropsHarvested) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer cropsMined = 0;
        if (container.has(toolStats.cropsHarvested, PersistentDataType.INTEGER)) {
            cropsMined = container.get(toolStats.cropsHarvested, PersistentDataType.INTEGER);
        }

        if (cropsMined == null) {
            cropsMined = 0;
            toolStats.logger.warning(clone + " does not have valid crops-mined set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.cropsHarvested, PersistentDataType.INTEGER, cropsMined + add);
        String oldCropsMinedFormatted = toolStats.numberFormat.formatInt(cropsMined);
        String newCropsMinedFormatted = toolStats.numberFormat.formatInt(cropsMined + add);
        Component oldLine = toolStats.configTools.formatLore("crops-harvested", "{crops}", oldCropsMinedFormatted);
        Component newLine = toolStats.configTools.formatLore("crops-harvested", "{crops}", newCropsMinedFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add x to the blocks mined stat.
     *
     * @param playerTool The tool to update.
     */
    public ItemMeta updateBlocksMined(ItemStack playerTool, int add) {
        ItemStack clone = playerTool.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.configTools.checkConfig(clone.getType(), "blocks-mined")) {
            if (container.has(toolStats.blocksMined)) {
                Integer blocksMined = container.get(toolStats.blocksMined, PersistentDataType.INTEGER);
                if (blocksMined == null) {
                    return null;
                }
                container.remove(toolStats.blocksMined);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "blocks-mined");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldBlocksMinedFormatted = toolStats.numberFormat.formatInt(blocksMined);
                    Component lineToRemove = toolStats.configTools.formatLore("blocks-mined", "{blocks}", oldBlocksMinedFormatted);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        boolean validToken = toolStats.itemChecker.checkTokens(container, "blocks-mined");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.blocksMined) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        // read the current stats from the item
        // if they don't exist, then start from 0
        Integer blocksMined = 0;
        if (container.has(toolStats.blocksMined, PersistentDataType.INTEGER)) {
            blocksMined = container.get(toolStats.blocksMined, PersistentDataType.INTEGER);
        }

        if (blocksMined == null) {
            blocksMined = 0;
            toolStats.logger.warning(clone + " does not have valid generic-mined set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.blocksMined, PersistentDataType.INTEGER, blocksMined + add);
        String oldBlocksMinedFormatted = toolStats.numberFormat.formatInt(blocksMined);
        String newBlocksMinedFormatted = toolStats.numberFormat.formatInt(blocksMined + add);
        Component oldLine = toolStats.configTools.formatLore("blocks-mined", "{blocks}", oldBlocksMinedFormatted);
        Component newLine = toolStats.configTools.formatLore("blocks-mined", "{blocks}", newBlocksMinedFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add +1 to the player kills stat.
     *
     * @param playerWeapon The tool to update.
     */
    public ItemMeta updatePlayerKills(ItemStack playerWeapon, int add) {
        ItemStack clone = playerWeapon.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.configTools.checkConfig(clone.getType(), "player-kills")) {
            if (container.has(toolStats.playerKills)) {
                Integer playerKills = container.get(toolStats.playerKills, PersistentDataType.INTEGER);
                if (playerKills == null) {
                    return null;
                }
                container.remove(toolStats.playerKills);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "player-kills");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldPlayerKillsFormatted = toolStats.numberFormat.formatInt(playerKills);
                    Component lineToRemove = toolStats.configTools.formatLore("player-kills", "{kills}", oldPlayerKillsFormatted);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "player-kills");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.playerKills) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer playerKills = 0;
        if (container.has(toolStats.playerKills, PersistentDataType.INTEGER)) {
            playerKills = container.get(toolStats.playerKills, PersistentDataType.INTEGER);
        }

        if (playerKills == null) {
            playerKills = 0;
            toolStats.logger.warning(clone + " does not have valid player-kills set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.playerKills, PersistentDataType.INTEGER, playerKills + add);
        String oldPlayerKillsFormatted = toolStats.numberFormat.formatInt(playerKills);
        String newPlayerKillsFormatted = toolStats.numberFormat.formatInt(playerKills + add);
        Component oldLine = toolStats.configTools.formatLore("kills.player", "{kills}", oldPlayerKillsFormatted);
        Component newLine = toolStats.configTools.formatLore("kills.player", "{kills}", newPlayerKillsFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add x to the mob kills stat.
     *
     * @param playerWeapon The tool to update.
     */
    public ItemMeta updateMobKills(ItemStack playerWeapon, int add) {
        ItemStack clone = playerWeapon.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.configTools.checkConfig(clone.getType(), "mob-kills")) {
            if (container.has(toolStats.mobKills)) {
                Integer mobKills = container.get(toolStats.mobKills, PersistentDataType.INTEGER);
                if (mobKills == null) {
                    return null;
                }
                container.remove(toolStats.mobKills);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "mob-kills");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldMobKillsFormatted = toolStats.numberFormat.formatInt(mobKills);
                    Component lineToRemove = toolStats.configTools.formatLore("mob-kills", "{kills}", oldMobKillsFormatted);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "mob-kills");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.mobKills) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer mobKills = 0;
        if (container.has(toolStats.mobKills, PersistentDataType.INTEGER)) {
            mobKills = container.get(toolStats.mobKills, PersistentDataType.INTEGER);
        }

        if (mobKills == null) {
            mobKills = 0;
            toolStats.logger.warning(clone + " does not have valid mob-kills set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.mobKills, PersistentDataType.INTEGER, mobKills + add);
        String oldMobKillsFormatted = toolStats.numberFormat.formatInt(mobKills);
        String newMobKillsFormatted = toolStats.numberFormat.formatInt(mobKills + add);
        Component oldLine = toolStats.configTools.formatLore("kills.mob", "{kills}", oldMobKillsFormatted);
        Component newLine = toolStats.configTools.formatLore("kills.mob", "{kills}", newMobKillsFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add damage to an armor piece.
     *
     * @param armorPiece The armor to update.
     * @param damage     The amount of damage to apply.
     * @param bypass     Bypass the negative damage check.
     */
    public ItemMeta updateArmorDamage(ItemStack armorPiece, double damage, boolean bypass) {
        // ignore if the damage is zero or negative
        if (damage < 0) {
            if (!bypass) {
                return null;
            }
        }
        ItemStack clone = armorPiece.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.armor-damage")) {
            if (container.has(toolStats.armorDamage)) {
                Double armorDamage = container.get(toolStats.armorDamage, PersistentDataType.DOUBLE);
                if (armorDamage == null) {
                    return null;
                }
                container.remove(toolStats.armorDamage);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "damage-taken");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldDamageTakenFormatted = toolStats.numberFormat.formatDouble(armorDamage);
                    Component lineToRemove = toolStats.configTools.formatLore("damage-taken", "{damage}", oldDamageTakenFormatted);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "damage-taken");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.armorDamage) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        Double damageTaken = 0.0;
        if (container.has(toolStats.armorDamage, PersistentDataType.DOUBLE)) {
            damageTaken = container.get(toolStats.armorDamage, PersistentDataType.DOUBLE);
        }

        if (damageTaken == null) {
            damageTaken = 0.0;
            toolStats.logger.warning(clone + " does not have valid damage-taken set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.armorDamage, PersistentDataType.DOUBLE, damageTaken + damage);
        String oldDamageFormatted = toolStats.numberFormat.formatDouble(damageTaken);
        String newDamageFormatted = toolStats.numberFormat.formatDouble(damageTaken + damage);
        Component oldLine = toolStats.configTools.formatLore("damage-taken", "{damage}", oldDamageFormatted);
        Component newLine = toolStats.configTools.formatLore("damage-taken", "{damage}", newDamageFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add damage to a weapon.
     *
     * @param weapon The weapon to update.
     * @param damage The amount of damage to apply.
     * @param bypass Bypass the negative damage check.
     */
    public ItemMeta updateWeaponDamage(ItemStack weapon, double damage, boolean bypass) {
        // ignore if the damage is zero or negative
        if (damage < 0) {
            if (!bypass) {
                return null;
            }
        }
        ItemStack clone = weapon.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.configTools.checkConfig(clone.getType(), "damage-done")) {
            if (container.has(toolStats.damageDone)) {
                Double damageDone = container.get(toolStats.damageDone, PersistentDataType.DOUBLE);
                if (damageDone == null) {
                    return null;
                }
                container.remove(toolStats.damageDone);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "damage-done");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldDamageDoneFormatted = toolStats.numberFormat.formatDouble(damageDone);
                    Component lineToRemove = toolStats.configTools.formatLore("damage-done", "{damage}", oldDamageDoneFormatted);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "damage-done");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.damageDone) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        Double damageDone = 0.0;
        if (container.has(toolStats.damageDone, PersistentDataType.DOUBLE)) {
            damageDone = container.get(toolStats.damageDone, PersistentDataType.DOUBLE);
        }

        if (damageDone == null) {
            damageDone = 0.0;
            toolStats.logger.warning(clone + " does not have valid damage-done set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.damageDone, PersistentDataType.DOUBLE, damageDone + damage);
        String oldDamageFormatted = toolStats.numberFormat.formatDouble(damageDone);
        String newDamageFormatted = toolStats.numberFormat.formatDouble(damageDone + damage);
        Component oldLine = toolStats.configTools.formatLore("damage-done", "{damage}", oldDamageFormatted);
        Component newLine = toolStats.configTools.formatLore("damage-done", "{damage}", newDamageFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add flight time to an elytra.
     *
     * @param elytra The player's elytra.
     */
    public ItemMeta updateFlightTime(ItemStack elytra, long duration) {
        ItemStack clone = elytra.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.flight-time")) {
            if (container.has(toolStats.flightTime)) {
                Long flightTime = container.get(toolStats.flightTime, PersistentDataType.LONG);
                if (flightTime == null) {
                    return null;
                }
                container.remove(toolStats.flightTime);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "flight-time");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    // if the old format is in the config, check to see if the old format is on the elytra
                    if (toolStats.config.getString("messages.flight-time-old") != null) {
                        String oldFormatFormatted = toolStats.numberFormat.formatDouble((double) flightTime / 1000);
                        Component oldFormat = toolStats.configTools.formatLore("flight-time-old", "{time}", oldFormatFormatted);
                        List<Component> newLore = removeLore(meta.lore(), oldFormat);
                        meta.lore(newLore);
                    }

                    Map<String, String> oldFlightTimeFormatted = toolStats.numberFormat.formatTime(flightTime);
                    Component lineToRemove = toolStats.configTools.formatLoreMultiplePlaceholders("flight-time", oldFlightTimeFormatted);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "flight-time");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.flightTime) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        // read the current stats from the item
        // if they don't exist, then start from 0
        Long flightTime = 0L;
        if (container.has(toolStats.flightTime, PersistentDataType.LONG)) {
            flightTime = container.get(toolStats.flightTime, PersistentDataType.LONG);
        }

        if (flightTime == null) {
            flightTime = 0L;
            toolStats.logger.warning(flightTime + " does not have valid flight-time set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.flightTime, PersistentDataType.LONG, flightTime + duration);
        Map<String, String> oldFlightFormatted = toolStats.numberFormat.formatTime(flightTime);
        Map<String, String> newFlightFormatted = toolStats.numberFormat.formatTime(flightTime + duration);
        // if the old format is in the config, check to see if the old format is on the elytra
        if (toolStats.config.getString("messages.flight-time-old") != null) {
            if (meta.hasLore()) {
                String oldFormatFormatted = toolStats.numberFormat.formatDouble((double) flightTime / 1000);
                Component oldFormat = toolStats.configTools.formatLore("flight-time-old", "{time}", oldFormatFormatted);
                meta.lore(removeLore(meta.lore(), oldFormat));
            }
        }
        Component oldLine = toolStats.configTools.formatLoreMultiplePlaceholders("flight-time", oldFlightFormatted);
        Component newLine = toolStats.configTools.formatLoreMultiplePlaceholders("flight-time", newFlightFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add x to sheep sheared stat.
     *
     * @param shears The shears.
     */
    public ItemMeta updateSheepSheared(ItemStack shears, int add) {
        ItemStack clone = shears.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.sheep-sheared")) {
            if (container.has(toolStats.sheepSheared)) {
                Integer sheepSheared = container.get(toolStats.sheepSheared, PersistentDataType.INTEGER);
                if (sheepSheared == null) {
                    return null;
                }
                container.remove(toolStats.sheepSheared);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "sheep-sheared");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldSheepShearedFormatted = toolStats.numberFormat.formatDouble(sheepSheared);
                    Component lineToRemove = toolStats.configTools.formatLore("sheep-sheared", "{sheep}", oldSheepShearedFormatted);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "sheep-sheared");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.sheepSheared) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer sheepSheared = 0;
        if (container.has(toolStats.sheepSheared, PersistentDataType.INTEGER)) {
            sheepSheared = container.get(toolStats.sheepSheared, PersistentDataType.INTEGER);
        }

        if (sheepSheared == null) {
            sheepSheared = 0;
            toolStats.logger.warning(clone + " does not have valid sheared set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.sheepSheared, PersistentDataType.INTEGER, sheepSheared + add);
        String oldSheepFormatted = toolStats.numberFormat.formatInt(sheepSheared);
        String newSheepFormatted = toolStats.numberFormat.formatInt(sheepSheared + add);
        Component oldLine = toolStats.configTools.formatLore("sheep-sheared", "{sheep}", oldSheepFormatted);
        Component newLine = toolStats.configTools.formatLore("sheep-sheared", "{sheep}", newSheepFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add x to arrows shot stat.
     *
     * @param bow The bow.
     */
    public ItemMeta updateArrowsShot(ItemStack bow, int add) {
        ItemStack clone = bow.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.arrows-shot")) {
            if (container.has(toolStats.arrowsShot)) {
                Integer arrowsShot = container.get(toolStats.arrowsShot, PersistentDataType.INTEGER);
                if (arrowsShot == null) {
                    return null;
                }
                container.remove(toolStats.arrowsShot);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "arrows-shot");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldArrowsShotFormatted = toolStats.numberFormat.formatDouble(arrowsShot);
                    Component lineToRemove = toolStats.configTools.formatLore("arrows-shot", "{arrows}", oldArrowsShotFormatted);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "arrows-shot");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.arrowsShot) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        // read the current stats from the item
        // if they don't exist, then start from 0
        Integer arrowsShot = 0;
        if (container.has(toolStats.arrowsShot, PersistentDataType.INTEGER)) {
            arrowsShot = container.get(toolStats.arrowsShot, PersistentDataType.INTEGER);
        }

        if (arrowsShot == null) {
            arrowsShot = 0;
            toolStats.logger.warning(arrowsShot + " does not have valid arrows-shot set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.arrowsShot, PersistentDataType.INTEGER, arrowsShot + add);
        String oldArrowsFormatted = toolStats.numberFormat.formatInt(arrowsShot);
        String newArrowsFormatted = toolStats.numberFormat.formatInt(arrowsShot + add);
        Component oldLine = toolStats.configTools.formatLore("arrows-shot", "{arrows}", oldArrowsFormatted);
        Component newLine = toolStats.configTools.formatLore("arrows-shot", "{arrows}", newArrowsFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add x to fish caught stat.
     *
     * @param fishingRod The fishing rod.
     */
    public ItemMeta updateFishCaught(ItemStack fishingRod, int add) {
        ItemStack clone = fishingRod.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(clone + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.fish-caught")) {
            if (container.has(toolStats.fishCaught)) {
                Integer fishCaught = container.get(toolStats.fishCaught, PersistentDataType.INTEGER);
                if (fishCaught == null) {
                    return null;
                }
                container.remove(toolStats.fishCaught);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.tokenApplied)) {
                    String appliedTokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "fish-caught");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.tokenApplied);
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldFishCaught = toolStats.numberFormat.formatDouble(fishCaught);
                    Component lineToRemove = toolStats.configTools.formatLore("fished.fish-caught", "{fish}", oldFishCaught);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "fish-caught");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.fishCaught) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }

            // the item does not have a valid token
            if (!validToken) {
                return null;
            }
        } else {
            if (!validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer fishCaught = 0;
        if (container.has(toolStats.fishCaught, PersistentDataType.INTEGER)) {
            fishCaught = container.get(toolStats.fishCaught, PersistentDataType.INTEGER);
        }

        if (fishCaught == null) {
            fishCaught = 0;
            toolStats.logger.warning(clone + " does not have valid fish-caught set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.fishCaught, PersistentDataType.INTEGER, fishCaught + add);
        String oldFishFormatted = toolStats.numberFormat.formatInt(fishCaught);
        String newFishFormatted = toolStats.numberFormat.formatInt(fishCaught + add);
        Component oldLine = toolStats.configTools.formatLore("fished.fish-caught", "{fish}", oldFishFormatted);
        Component newLine = toolStats.configTools.formatLore("fished.fish-caught", "{fish}", newFishFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Format the item owner lore.
     *
     * @param playerName The player's name who owns the items.
     * @param origin     The origin type.
     * @param item       The item.
     * @return A component with the lore.
     */
    public Component formatOwner(String playerName, int origin, ItemStack item) {
        switch (origin) {
            case 0: {
                if (toolStats.configTools.checkConfig(item.getType(), "crafted-by")) {
                    return toolStats.configTools.formatLore("crafted.crafted-by", "{player}", playerName);
                }
                break;
            }
            case 2: {
                if (toolStats.configTools.checkConfig(item.getType(), "looted-by")) {
                    return toolStats.configTools.formatLore("looted.looted-by", "{player}", playerName);
                }
                break;
            }
            case 3: {
                if (toolStats.configTools.checkConfig(item.getType(), "traded-by")) {
                    return toolStats.configTools.formatLore("traded.traded-by", "{player}", playerName);
                }
                break;
            }
            case 4: {
                if (toolStats.config.getBoolean("enabled.elytra-tag")) {
                    return toolStats.configTools.formatLore("looted.found-by", "{player}", playerName);
                }
                break;
            }
            case 5: {
                if (toolStats.configTools.checkConfig(item.getType(), "fished-by")) {
                    return toolStats.configTools.formatLore("fished.caught-by", "{player}", playerName);
                }
                break;
            }
            case 6: {
                if (toolStats.configTools.checkConfig(item.getType(), "spawned-in-by")) {
                    return toolStats.configTools.formatLore("spawned-in.spawned-by", "{player}", playerName);
                }
                break;
            }
        }
        return null;
    }

    /**
     * Format the item creation time.
     *
     * @param creationDate When the item was created.
     * @param origin       The origin type.
     * @param item         The item.
     * @return A component with the lore.
     */
    public Component formatCreationTime(long creationDate, int origin, ItemStack item) {
        String date = toolStats.numberFormat.formatDate(new Date(creationDate));
        switch (origin) {
            case 0: {
                if (toolStats.configTools.checkConfig(item.getType(), "crafted-on")) {
                    return toolStats.configTools.formatLore("crafted.crafted-on", "{date}", date);
                }
                break;
            }
            case 1: {
                if (toolStats.config.getBoolean("enabled.dropped-on")) {
                    return toolStats.configTools.formatLore("dropped-on", "{date}", date);
                }
                break;
            }
            case 2: {
                if (toolStats.configTools.checkConfig(item.getType(), "looted-on")) {
                    return toolStats.configTools.formatLore("looted.looted-on", "{date}", date);
                }
                break;
            }
            case 3: {
                if (toolStats.configTools.checkConfig(item.getType(), "traded-on")) {
                    return toolStats.configTools.formatLore("traded.traded-on", "{date}", date);
                }
                break;
            }
            case 4: {
                if (toolStats.config.getBoolean("enabled.elytra-tag")) {
                    return toolStats.configTools.formatLore("looted.found-on", "{date}", date);
                }
                break;
            }
            case 5: {
                if (toolStats.configTools.checkConfig(item.getType(), "fished-on")) {
                    return toolStats.configTools.formatLore("fished.caught-on", "{date}", date);
                }
                break;
            }
            case 6: {
                if (toolStats.configTools.checkConfig(item.getType(), "spawned-in-on")) {
                    return toolStats.configTools.formatLore("spawned-in.spawned-on", "{date}", date);
                }
                break;
            }
        }
        return null;
    }

    /**
     * Remove all stats, ownership, and creation time from an item.
     *
     * @param inputItem  The input item to remove stats from.
     * @param removeMeta Remove ownership and creation time?
     */
    public ItemStack removeAll(ItemStack inputItem, boolean removeMeta) {
        ItemStack finalItem = inputItem.clone();
        ItemMeta meta = finalItem.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // remove the applied tokens
        if (container.has(toolStats.tokenApplied)) {
            container.remove(toolStats.tokenApplied);
        }

        if (container.has(toolStats.playerKills)) {
            Integer playerKills = container.get(toolStats.playerKills, PersistentDataType.INTEGER);
            if (playerKills != null) {
                container.remove(toolStats.playerKills);

                String playerKillsFormatted = toolStats.numberFormat.formatInt(playerKills);
                Component lineToRemove = toolStats.configTools.formatLore("kills.player", "{kills}", playerKillsFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.mobKills)) {
            Integer mobKills = container.get(toolStats.mobKills, PersistentDataType.INTEGER);
            if (mobKills != null) {
                container.remove(toolStats.mobKills);
                String mobKillsFormatted = toolStats.numberFormat.formatInt(mobKills);
                Component lineToRemove = toolStats.configTools.formatLore("kills.mob", "{kills}", mobKillsFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.blocksMined)) {
            Integer blocksMined = container.get(toolStats.blocksMined, PersistentDataType.INTEGER);
            if (blocksMined != null) {
                container.remove(toolStats.blocksMined);
                String blocksMinedFormatted = toolStats.numberFormat.formatInt(blocksMined);
                Component lineToRemove = toolStats.configTools.formatLore("blocks-mined", "{blocks}", blocksMinedFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.cropsHarvested)) {
            Integer cropsHarvested = container.get(toolStats.playerKills, PersistentDataType.INTEGER);
            if (cropsHarvested != null) {
                container.remove(toolStats.cropsHarvested);
                String cropsHarvestedFormatted = toolStats.numberFormat.formatInt(cropsHarvested);
                Component lineToRemove = toolStats.configTools.formatLore("crops-harvested", "{crops}", cropsHarvestedFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.fishCaught)) {
            Integer fishCaught = container.get(toolStats.fishCaught, PersistentDataType.INTEGER);
            if (fishCaught != null) {
                container.remove(toolStats.fishCaught);
                String fishCaughtFormatted = toolStats.numberFormat.formatInt(fishCaught);
                Component lineToRemove = toolStats.configTools.formatLore("fished.fish-caught", "{fish}", fishCaughtFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.sheepSheared)) {
            Integer sheepSheared = container.get(toolStats.sheepSheared, PersistentDataType.INTEGER);
            if (sheepSheared != null) {
                container.remove(toolStats.sheepSheared);
                String sheepShearedFormatted = toolStats.numberFormat.formatInt(sheepSheared);
                Component lineToRemove = toolStats.configTools.formatLore("sheep.sheared", "{sheep}", sheepShearedFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.armorDamage)) {
            Double armorDamage = container.get(toolStats.armorDamage, PersistentDataType.DOUBLE);
            if (armorDamage != null) {
                container.remove(toolStats.armorDamage);
                String armorDamageFormatted = toolStats.numberFormat.formatDouble(armorDamage);
                Component lineToRemove = toolStats.configTools.formatLore("damage-taken", "{damage}", armorDamageFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.damageDone)) {
            Double damageDone = container.get(toolStats.damageDone, PersistentDataType.DOUBLE);
            if (damageDone != null) {
                container.remove(toolStats.damageDone);
                String damageDoneFormatted = toolStats.numberFormat.formatDouble(damageDone);
                Component lineToRemove = toolStats.configTools.formatLore("damage-done", "{damage}", damageDoneFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.arrowsShot)) {
            Integer arrowsShot = container.get(toolStats.arrowsShot, PersistentDataType.INTEGER);
            if (arrowsShot != null) {
                container.remove(toolStats.arrowsShot);

                String arrowsShotFormatted = toolStats.numberFormat.formatInt(arrowsShot);
                Component lineToRemove = toolStats.configTools.formatLore("arrows-shot", "{arrows}", arrowsShotFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.flightTime)) {
            Long flightTime = container.get(toolStats.flightTime, PersistentDataType.LONG);
            if (flightTime != null) {
                container.remove(toolStats.flightTime);
                Map<String, String> flightTimeFormatted = toolStats.numberFormat.formatTime(flightTime);
                Component lineToRemove = toolStats.configTools.formatLoreMultiplePlaceholders("flight-time", flightTimeFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (removeMeta) {
            Integer origin = null;
            if (container.has(toolStats.originType)) {
                origin = container.get(toolStats.originType, PersistentDataType.INTEGER);
            }

            if (container.has(toolStats.timeCreated)) {
                Long timeCreated = container.get(toolStats.timeCreated, PersistentDataType.LONG);
                if (timeCreated != null && origin != null) {
                    container.remove(toolStats.timeCreated);
                    Component timeCreatedLore = formatCreationTime(timeCreated, origin, finalItem);
                    meta.lore(removeLore(meta.lore(), timeCreatedLore));
                }
            }
            if (container.has(toolStats.itemOwner)) {
                UUID owner = container.get(toolStats.itemOwner, new UUIDDataType());
                if (owner != null && origin != null) {
                    container.remove(toolStats.itemOwner);
                    String ownerName = Bukkit.getOfflinePlayer(owner).getName();
                    if (ownerName != null) {
                        Component ownerLore = formatOwner(ownerName, origin, finalItem);
                        meta.lore(removeLore(meta.lore(), ownerLore));
                    }
                }
            }

            if (origin != null) {
                container.remove(toolStats.originType);
            }

            finalItem.setItemMeta(meta);
        }

        return finalItem;
    }
}
