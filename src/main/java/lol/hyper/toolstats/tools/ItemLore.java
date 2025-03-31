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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

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
        List<Component> newLore = new ArrayList<>(inputLore);
        newLore.removeIf(line -> PlainTextComponentSerializer.plainText().serialize(line).equals(PlainTextComponentSerializer.plainText().serialize(toRemove)));
        return newLore;
    }

    /**
     * Adds new ownership to an item.
     *
     * @param itemMeta      The item meta.
     * @param playerName    The new owner of item.
     * @param formattedDate The date of the ownership.
     * @return The item's new lore.
     */
    public List<Component> addNewOwner(ItemMeta itemMeta, String playerName, String formattedDate) {
        Component dateCreatedLore;
        Component itemOwnerLore;
        Integer origin = null;
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        if (container.has(toolStats.originType, PersistentDataType.INTEGER)) {
            origin = container.get(toolStats.originType, PersistentDataType.INTEGER);
        }

        // if the origin is broken, don't try to set the lore
        if (origin == null) {
            toolStats.logger.info("Unable to determine origin for item " + itemMeta.getAsString());
            toolStats.logger.info("This IS a bug, please report this to the GitHub.");
            return itemMeta.lore();
        }

        // set the lore based on the origin
        switch (origin) {
            case 2: {
                dateCreatedLore = toolStats.configTools.formatLore("looted.looted-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("looted.looted-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.looted.looted-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.looted.looted-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                break;
            }
            case 3: {
                dateCreatedLore = toolStats.configTools.formatLore("traded.traded-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("traded.traded-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.traded.traded-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.traded.traded-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                break;
            }
            case 4: {
                dateCreatedLore = toolStats.configTools.formatLore("looted.found-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("looted.found-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.looted.found-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.looted.found-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                break;
            }
            case 5: {
                dateCreatedLore = toolStats.configTools.formatLore("fished.caught-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("fished.caught-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.fished.caught-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.fished.caught-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                break;
            }
            case 6: {
                dateCreatedLore = toolStats.configTools.formatLore("spawned-in.spawned-on", "{date}", formattedDate);
                itemOwnerLore = toolStats.configTools.formatLore("spawned-in.spawned-by", "{player}", playerName);

                if (dateCreatedLore == null) {
                    toolStats.logger.warning("messages.spawned-in.spawned-on is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                if (itemOwnerLore == null) {
                    toolStats.logger.warning("messages.spawned-in.spawned-by is not set in your config!");
                    toolStats.logger.warning("Unable to update lore for item.");
                    return itemMeta.lore();
                }
                break;
            }
            default: {
                toolStats.logger.warning("Origin " + origin + " was found. Data was modified OR something REALLY broke.");
                toolStats.logger.warning(itemMeta.getAsString());
                return itemMeta.lore();
            }
        }

        List<Component> newLore;
        if (itemMeta.hasLore()) {
            newLore = itemMeta.lore();
        } else {
            newLore = new ArrayList<>();
        }

        newLore.add(dateCreatedLore);
        newLore.add(itemOwnerLore);
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
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
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
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
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
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
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
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
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
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
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
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
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
                if (meta.hasLore()) {
                    String oldFlightTimeFormatted = toolStats.numberFormat.formatDouble(flightTime);
                    Component lineToRemove = toolStats.configTools.formatLore("flight-time", "{time}", oldFlightTimeFormatted);
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
        String oldFlightFormatted = toolStats.numberFormat.formatDouble((double) flightTime / 1000);
        String newFlightFormatted = toolStats.numberFormat.formatDouble((double) (flightTime + duration) / 1000);
        Component oldLine = toolStats.configTools.formatLore("flight-time", "{time}", oldFlightFormatted);
        Component newLine = toolStats.configTools.formatLore("flight-time", "{time}", newFlightFormatted);
        if (oldLine == null || newLine == null) {
            return null;
        }
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
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
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
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
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
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
        List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
        meta.lore(newLore);
        return meta;
    }
}
