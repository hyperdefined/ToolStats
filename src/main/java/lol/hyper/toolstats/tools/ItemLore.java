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

import lol.hyper.hyperlib.datatypes.UUIDDataType;
import lol.hyper.toolstats.ToolStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }
        // read the current stats from the item
        // if they don't exist, then start from 0
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.crops-harvested")) {
            if (container.has(toolStats.toolStatsKeys.getCropsHarvested())) {
                Integer cropsMined = container.get(toolStats.toolStatsKeys.getCropsHarvested(), PersistentDataType.INTEGER);
                if (cropsMined == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getCropsHarvested());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "crops-mined");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
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
            if (container.has(toolStats.toolStatsKeys.getCropsHarvested()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer cropsMined = 0;
        if (container.has(toolStats.toolStatsKeys.getCropsHarvested(), PersistentDataType.INTEGER)) {
            cropsMined = container.get(toolStats.toolStatsKeys.getCropsHarvested(), PersistentDataType.INTEGER);
        }

        if (cropsMined == null) {
            cropsMined = 0;
            toolStats.logger.warn("{} does not have valid crops-mined set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getCropsHarvested(), PersistentDataType.INTEGER, cropsMined + add);
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.configTools.checkConfig(clone.getType(), "blocks-mined")) {
            if (container.has(toolStats.toolStatsKeys.getBlocksMined())) {
                Integer blocksMined = container.get(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER);
                if (blocksMined == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getBlocksMined());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "blocks-mined");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
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
            if (container.has(toolStats.toolStatsKeys.getBlocksMined()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        // read the current stats from the item
        // if they don't exist, then start from 0
        Integer blocksMined = 0;
        if (container.has(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER)) {
            blocksMined = container.get(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER);
        }

        if (blocksMined == null) {
            blocksMined = 0;
            toolStats.logger.warn("{} does not have valid generic-mined set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER, blocksMined + add);
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.configTools.checkConfig(clone.getType(), "player-kills")) {
            if (container.has(toolStats.toolStatsKeys.getPlayerKills())) {
                Integer playerKills = container.get(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER);
                if (playerKills == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getPlayerKills());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "player-kills");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
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
            if (container.has(toolStats.toolStatsKeys.getPlayerKills()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer playerKills = 0;
        if (container.has(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER)) {
            playerKills = container.get(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER);
        }

        if (playerKills == null) {
            playerKills = 0;
            toolStats.logger.warn("{} does not have valid player-kills set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER, playerKills + add);
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.configTools.checkConfig(clone.getType(), "mob-kills")) {
            if (container.has(toolStats.toolStatsKeys.getMobKills())) {
                Integer mobKills = container.get(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER);
                if (mobKills == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getMobKills());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "mob-kills");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
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
            if (container.has(toolStats.toolStatsKeys.getMobKills()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer mobKills = 0;
        if (container.has(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER)) {
            mobKills = container.get(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER);
        }

        if (mobKills == null) {
            mobKills = 0;
            toolStats.logger.warn("{} does not have valid mob-kills set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER, mobKills + add);
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.armor-damage")) {
            if (container.has(toolStats.toolStatsKeys.getArmorDamage())) {
                Double armorDamage = container.get(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE);
                if (armorDamage == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getArmorDamage());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "damage-taken");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
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
            if (container.has(toolStats.toolStatsKeys.getArmorDamage()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Double damageTaken = 0.0;
        if (container.has(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE)) {
            damageTaken = container.get(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE);
        }

        if (damageTaken == null) {
            damageTaken = 0.0;
            toolStats.logger.warn("{} does not have valid damage-taken set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE, damageTaken + damage);
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.configTools.checkConfig(clone.getType(), "damage-done")) {
            if (container.has(toolStats.toolStatsKeys.getDamageDone())) {
                Double damageDone = container.get(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE);
                if (damageDone == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getDamageDone());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "damage-done");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
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
            if (container.has(toolStats.toolStatsKeys.getDamageDone()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Double damageDone = 0.0;
        if (container.has(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE)) {
            damageDone = container.get(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE);
        }

        if (damageDone == null) {
            damageDone = 0.0;
            toolStats.logger.warn("{} does not have valid damage-done set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE, damageDone + damage);
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.flight-time")) {
            if (container.has(toolStats.toolStatsKeys.getFlightTime())) {
                Long flightTime = container.get(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG);
                if (flightTime == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getFlightTime());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "flight-time");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
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
            if (container.has(toolStats.toolStatsKeys.getFlightTime()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        // read the current stats from the item
        // if they don't exist, then start from 0
        Long flightTime = 0L;
        if (container.has(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG)) {
            flightTime = container.get(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG);
        }

        if (flightTime == null) {
            flightTime = 0L;
            toolStats.logger.warn("{} does not have valid flight-time set! Resting to zero. This should NEVER happen.", flightTime);
        }

        container.set(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG, flightTime + duration);
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.sheep-sheared")) {
            if (container.has(toolStats.toolStatsKeys.getSheepSheared())) {
                Integer sheepSheared = container.get(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER);
                if (sheepSheared == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getSheepSheared());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "sheep-sheared");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldSheepShearedFormatted = toolStats.numberFormat.formatInt(sheepSheared);
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
            if (container.has(toolStats.toolStatsKeys.getSheepSheared()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer sheepSheared = 0;
        if (container.has(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER)) {
            sheepSheared = container.get(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER);
        }

        if (sheepSheared == null) {
            sheepSheared = 0;
            toolStats.logger.warn("{} does not have valid sheared set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER, sheepSheared + add);
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.arrows-shot")) {
            if (container.has(toolStats.toolStatsKeys.getArrowsShot())) {
                Integer arrowsShot = container.get(toolStats.toolStatsKeys.getArrowsShot(), PersistentDataType.INTEGER);
                if (arrowsShot == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getArrowsShot());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "arrows-shot");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldArrowsShotFormatted = toolStats.numberFormat.formatInt(arrowsShot);
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
            if (container.has(toolStats.toolStatsKeys.getArrowsShot()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        // read the current stats from the item
        // if they don't exist, then start from 0
        Integer arrowsShot = 0;
        if (container.has(toolStats.toolStatsKeys.getArrowsShot(), PersistentDataType.INTEGER)) {
            arrowsShot = container.get(toolStats.toolStatsKeys.getArrowsShot(), PersistentDataType.INTEGER);
        }

        if (arrowsShot == null) {
            arrowsShot = 0;
            toolStats.logger.warn("{} does not have valid arrows-shot set! Resting to zero. This should NEVER happen.", arrowsShot);
        }

        container.set(toolStats.toolStatsKeys.getArrowsShot(), PersistentDataType.INTEGER, arrowsShot + add);
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
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.fish-caught")) {
            if (container.has(toolStats.toolStatsKeys.getFishCaught())) {
                Integer fishCaught = container.get(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER);
                if (fishCaught == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getFishCaught());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "fish-caught");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldFishCaught = toolStats.numberFormat.formatInt(fishCaught);
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
            if (container.has(toolStats.toolStatsKeys.getFishCaught()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer fishCaught = 0;
        if (container.has(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER)) {
            fishCaught = container.get(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER);
        }

        if (fishCaught == null) {
            fishCaught = 0;
            toolStats.logger.warn("{} does not have valid fish-caught set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER, fishCaught + add);
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
     * Add x to bosses killed stat.
     *
     * @param weapon The weapon used.
     * @param add How many to add.
     * @param boss The boss killed
     */
    public ItemMeta updateBossesKilled(ItemStack weapon, int add, String boss) {
        ItemStack clone = weapon.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        NamespacedKey bossesKey = null;
        if (boss.equalsIgnoreCase("wither")) {
            bossesKey = toolStats.toolStatsKeys.getWitherKills();
        }
        if (boss.equalsIgnoreCase("enderdragon")) {
            bossesKey = toolStats.toolStatsKeys.getEnderDragonKills();
        }

        if (bossesKey == null) {
            return null;
        }

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.bosses-killed." + boss)) {
            if (container.has(bossesKey)) {
                Integer bossesKilled = container.get(bossesKey, PersistentDataType.INTEGER);
                if (bossesKilled == null) {
                    return null;
                }
                container.remove(bossesKey);
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "wither-kills");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldBossesKilled = toolStats.numberFormat.formatInt(bossesKilled);
                    Component lineToRemove = toolStats.configTools.formatLore("bosses-killed." + boss, "{kills}", oldBossesKilled);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "wither-kills");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(bossesKey) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer bossesKilled = 0;
        if (container.has(bossesKey, PersistentDataType.INTEGER)) {
            bossesKilled = container.get(bossesKey, PersistentDataType.INTEGER);
        }

        if (bossesKilled == null) {
            bossesKilled = 0;
            toolStats.logger.warn("{} does not have valid {} set! Resting to zero. This should NEVER happen.", clone, boss);
        }

        container.set(bossesKey, PersistentDataType.INTEGER, bossesKilled + add);
        String oldBossesKilledFormatted = toolStats.numberFormat.formatInt(bossesKilled);
        String newBossesKilledFormatted = toolStats.numberFormat.formatInt(bossesKilled + add);
        Component oldLine = toolStats.configTools.formatLore("bosses-killed." + boss, "{kills}", oldBossesKilledFormatted);
        Component newLine = toolStats.configTools.formatLore("bosses-killed." + boss, "{kills}", newBossesKilledFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add x to critical strikes stat.
     *
     * @param weapon The weapon used.
     */
    public ItemMeta updateCriticalStrikes(ItemStack weapon, int add) {
        ItemStack clone = weapon.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.critical-strikes")) {
            if (container.has(toolStats.toolStatsKeys.getCriticalStrikes())) {
                Integer criticalStrikes = container.get(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER);
                if (criticalStrikes == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getCriticalStrikes());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "critical-strikes");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldCriticalStrikes = toolStats.numberFormat.formatInt(criticalStrikes);
                    Component lineToRemove = toolStats.configTools.formatLore("critical-strikes", "{strikes}", oldCriticalStrikes);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "critical-strikes");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.toolStatsKeys.getCriticalStrikes()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer criticalStrikes = 0;
        if (container.has(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER)) {
            criticalStrikes = container.get(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER);
        }

        if (criticalStrikes == null) {
            criticalStrikes = 0;
            toolStats.logger.warn("{} does not have valid critical-strikes set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER, criticalStrikes + add);
        String oldCriticalStrikesFormatted = toolStats.numberFormat.formatInt(criticalStrikes);
        String newCriticalStrikesFormatted = toolStats.numberFormat.formatInt(criticalStrikes + add);
        Component oldLine = toolStats.configTools.formatLore("critical-strikes", "{strikes}", oldCriticalStrikesFormatted);
        Component newLine = toolStats.configTools.formatLore("critical-strikes", "{strikes}", newCriticalStrikesFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add x to trident throws.
     *
     * @param trident The trident used.
     */
    public ItemMeta updateTridentThrows(ItemStack trident, int add) {
        ItemStack clone = trident.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.trident-throws")) {
            if (container.has(toolStats.toolStatsKeys.getTridentThrows())) {
                Integer tridentThrows = container.get(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER);
                if (tridentThrows == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getTridentThrows());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "trident-throws");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldTridentThrows = toolStats.numberFormat.formatInt(tridentThrows);
                    Component lineToRemove = toolStats.configTools.formatLore("trident-throws", "{times}", oldTridentThrows);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "trident-throws");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.toolStatsKeys.getTridentThrows()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer tridentThrows = 0;
        if (container.has(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER)) {
            tridentThrows = container.get(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER);
        }

        if (tridentThrows == null) {
            tridentThrows = 0;
            toolStats.logger.warn("{} does not have valid trident-throws set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER, tridentThrows + add);
        String oldTridentThrowsFormatted = toolStats.numberFormat.formatInt(tridentThrows);
        String newTridentThrowsFormatted = toolStats.numberFormat.formatInt(tridentThrows + add);
        Component oldLine = toolStats.configTools.formatLore("trident-throws", "{times}", oldTridentThrowsFormatted);
        Component newLine = toolStats.configTools.formatLore("trident-throws", "{times}", newTridentThrowsFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }

    /**
     * Add x to logs stripped.
     *
     * @param axe The axe used.
     */
    public ItemMeta updateLogsStripped(ItemStack axe, int add) {
        ItemStack clone = axe.clone();
        ItemMeta meta = clone.getItemMeta();
        if (meta == null) {
            toolStats.logger.warn("{} does NOT have any meta! Unable to update stats.", clone);
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // if it's disabled, don't update the stats
        // check to see if the item has the stats, remove them if it does
        if (!toolStats.config.getBoolean("enabled.logs-stripped")) {
            if (container.has(toolStats.toolStatsKeys.getLogsStripped())) {
                Integer logsStripped = container.get(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER);
                if (logsStripped == null) {
                    return null;
                }
                container.remove(toolStats.toolStatsKeys.getLogsStripped());
                // remove the applied token if this stat is disabled
                if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
                    String appliedTokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (appliedTokens != null) {
                        // remove the token from the list
                        // if the list is empty, remove the PDC
                        // otherwise set the PDC back with the new list
                        List<String> newTokens = toolStats.itemChecker.removeToken(appliedTokens, "logs-stripped");
                        if (!newTokens.isEmpty()) {
                            container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                        } else {
                            container.remove(toolStats.toolStatsKeys.getTokenApplied());
                        }
                    }
                }
                if (meta.hasLore()) {
                    String oldLogsStripped = toolStats.numberFormat.formatInt(logsStripped);
                    Component lineToRemove = toolStats.configTools.formatLore("logs-stripped", "{logs}", oldLogsStripped);
                    List<Component> newLore = removeLore(meta.lore(), lineToRemove);
                    meta.lore(newLore);
                }
                return meta;
            }
            return null;
        }

        // check for tokens
        boolean validToken = toolStats.itemChecker.checkTokens(container, "logs-stripped");
        // check for tokens
        if (toolStats.config.getBoolean("tokens.enabled")) {
            // if the item has stats but no token, add the token
            if (container.has(toolStats.toolStatsKeys.getLogsStripped()) && !validToken) {
                String newTokens = toolStats.itemChecker.addTokensToExisting(clone);
                if (newTokens != null) {
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
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
                    container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, newTokens);
                }
            }
        }

        Integer logsStripped = 0;
        if (container.has(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER)) {
            logsStripped = container.get(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER);
        }

        if (logsStripped == null) {
            logsStripped = 0;
            toolStats.logger.warn("{} does not have valid logs-stripped set! Resting to zero. This should NEVER happen.", clone);
        }

        container.set(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER, logsStripped + add);
        String oldLogsStrippedFormatted = toolStats.numberFormat.formatInt(logsStripped);
        String newLogsStrippedFormatted = toolStats.numberFormat.formatInt(logsStripped + add);
        Component oldLine = toolStats.configTools.formatLore("logs-stripped", "{logs}", oldLogsStrippedFormatted);
        Component newLine = toolStats.configTools.formatLore("logs-stripped", "{logs}", newLogsStrippedFormatted);
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
        if (container.has(toolStats.toolStatsKeys.getTokenApplied())) {
            container.remove(toolStats.toolStatsKeys.getTokenApplied());
        }

        if (container.has(toolStats.toolStatsKeys.getPlayerKills())) {
            Integer playerKills = container.get(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER);
            if (playerKills != null) {
                container.remove(toolStats.toolStatsKeys.getPlayerKills());

                String playerKillsFormatted = toolStats.numberFormat.formatInt(playerKills);
                Component lineToRemove = toolStats.configTools.formatLore("kills.player", "{kills}", playerKillsFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getMobKills())) {
            Integer mobKills = container.get(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER);
            if (mobKills != null) {
                container.remove(toolStats.toolStatsKeys.getMobKills());
                String mobKillsFormatted = toolStats.numberFormat.formatInt(mobKills);
                Component lineToRemove = toolStats.configTools.formatLore("kills.mob", "{kills}", mobKillsFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getBlocksMined())) {
            Integer blocksMined = container.get(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER);
            if (blocksMined != null) {
                container.remove(toolStats.toolStatsKeys.getBlocksMined());
                String blocksMinedFormatted = toolStats.numberFormat.formatInt(blocksMined);
                Component lineToRemove = toolStats.configTools.formatLore("blocks-mined", "{blocks}", blocksMinedFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getCropsHarvested())) {
            Integer cropsHarvested = container.get(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER);
            if (cropsHarvested != null) {
                container.remove(toolStats.toolStatsKeys.getCropsHarvested());
                String cropsHarvestedFormatted = toolStats.numberFormat.formatInt(cropsHarvested);
                Component lineToRemove = toolStats.configTools.formatLore("crops-harvested", "{crops}", cropsHarvestedFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getFishCaught())) {
            Integer fishCaught = container.get(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER);
            if (fishCaught != null) {
                container.remove(toolStats.toolStatsKeys.getFishCaught());
                String fishCaughtFormatted = toolStats.numberFormat.formatInt(fishCaught);
                Component lineToRemove = toolStats.configTools.formatLore("fished.fish-caught", "{fish}", fishCaughtFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getSheepSheared())) {
            Integer sheepSheared = container.get(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER);
            if (sheepSheared != null) {
                container.remove(toolStats.toolStatsKeys.getSheepSheared());
                String sheepShearedFormatted = toolStats.numberFormat.formatInt(sheepSheared);
                Component lineToRemove = toolStats.configTools.formatLore("sheep.sheared", "{sheep}", sheepShearedFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getArmorDamage())) {
            Double armorDamage = container.get(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE);
            if (armorDamage != null) {
                container.remove(toolStats.toolStatsKeys.getArmorDamage());
                String armorDamageFormatted = toolStats.numberFormat.formatDouble(armorDamage);
                Component lineToRemove = toolStats.configTools.formatLore("damage-taken", "{damage}", armorDamageFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getDamageDone())) {
            Double damageDone = container.get(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE);
            if (damageDone != null) {
                container.remove(toolStats.toolStatsKeys.getDamageDone());
                String damageDoneFormatted = toolStats.numberFormat.formatDouble(damageDone);
                Component lineToRemove = toolStats.configTools.formatLore("damage-done", "{damage}", damageDoneFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getArrowsShot())) {
            Integer arrowsShot = container.get(toolStats.toolStatsKeys.getArrowsShot(), PersistentDataType.INTEGER);
            if (arrowsShot != null) {
                container.remove(toolStats.toolStatsKeys.getArrowsShot());
                String arrowsShotFormatted = toolStats.numberFormat.formatInt(arrowsShot);
                Component lineToRemove = toolStats.configTools.formatLore("arrows-shot", "{arrows}", arrowsShotFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getFlightTime())) {
            Long flightTime = container.get(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG);
            if (flightTime != null) {
                container.remove(toolStats.toolStatsKeys.getFlightTime());
                Map<String, String> flightTimeFormatted = toolStats.numberFormat.formatTime(flightTime);
                Component lineToRemove = toolStats.configTools.formatLoreMultiplePlaceholders("flight-time", flightTimeFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getWitherKills())) {
            Integer witherKills = container.get(toolStats.toolStatsKeys.getWitherKills(), PersistentDataType.INTEGER);
            if (witherKills != null) {
                container.remove(toolStats.toolStatsKeys.getWitherKills());
                String witherKillsFormatted = toolStats.numberFormat.formatInt(witherKills);
                Component lineToRemove = toolStats.configTools.formatLore("bosses-killed.wither", "{kills}", witherKillsFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getEnderDragonKills())) {
            Integer enderDragonKills = container.get(toolStats.toolStatsKeys.getEnderDragonKills(), PersistentDataType.INTEGER);
            if (enderDragonKills != null) {
                container.remove(toolStats.toolStatsKeys.getEnderDragonKills());
                String enderDragonKillsFormatted = toolStats.numberFormat.formatInt(enderDragonKills);
                Component lineToRemove = toolStats.configTools.formatLore("bosses-killed.enderdragon", "{kills}", enderDragonKillsFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getCriticalStrikes())) {
            Integer criticalStrikes = container.get(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER);
            if (criticalStrikes != null) {
                container.remove(toolStats.toolStatsKeys.getCriticalStrikes());
                String criticalStrikesFormatted = toolStats.numberFormat.formatInt(criticalStrikes);
                Component lineToRemove = toolStats.configTools.formatLore("critical-strikes", "{strikes}", criticalStrikesFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getTridentThrows())) {
            Integer tridentThrows = container.get(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER);
            if (tridentThrows != null) {
                container.remove(toolStats.toolStatsKeys.getTridentThrows());
                String tridentThrowsFormatted = toolStats.numberFormat.formatInt(tridentThrows);
                Component lineToRemove = toolStats.configTools.formatLore("trident-throws", "{times}", tridentThrowsFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (container.has(toolStats.toolStatsKeys.getLogsStripped())) {
            Integer logsStripped = container.get(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER);
            if (logsStripped != null) {
                container.remove(toolStats.toolStatsKeys.getLogsStripped());
                String logsStrippedFormatted = toolStats.numberFormat.formatInt(logsStripped);
                Component lineToRemove = toolStats.configTools.formatLore("logs-stripped", "{logs}", logsStrippedFormatted);
                meta.lore(removeLore(meta.lore(), lineToRemove));
                finalItem.setItemMeta(meta);
            }
        }
        if (removeMeta) {
            Integer origin = null;
            if (container.has(toolStats.toolStatsKeys.getOriginType())) {
                origin = container.get(toolStats.toolStatsKeys.getOriginType(), PersistentDataType.INTEGER);
            }

            if (container.has(toolStats.toolStatsKeys.getTimeCreated())) {
                Long timeCreated = container.get(toolStats.toolStatsKeys.getTimeCreated(), PersistentDataType.LONG);
                if (timeCreated != null && origin != null) {
                    container.remove(toolStats.toolStatsKeys.getTimeCreated());
                    Component timeCreatedLore = formatCreationTime(timeCreated, origin, finalItem);
                    meta.lore(removeLore(meta.lore(), timeCreatedLore));
                }
            }
            if (container.has(toolStats.toolStatsKeys.getItemOwner())) {
                UUID owner = container.get(toolStats.toolStatsKeys.getItemOwner(), new UUIDDataType());
                if (owner != null && origin != null) {
                    container.remove(toolStats.toolStatsKeys.getItemOwner());
                    String ownerName = Bukkit.getOfflinePlayer(owner).getName();
                    if (ownerName != null) {
                        Component ownerLore = formatOwner(ownerName, origin, finalItem);
                        meta.lore(removeLore(meta.lore(), ownerLore));
                    }
                }
            }

            if (origin != null) {
                container.remove(toolStats.toolStatsKeys.getOriginType());
            }

            finalItem.setItemMeta(meta);
        }

        return finalItem;
    }
}
