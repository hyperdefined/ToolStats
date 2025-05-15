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

package lol.hyper.toolstats.commands;

import lol.hyper.toolstats.ToolStats;
import lol.hyper.toolstats.tools.UUIDDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommandToolStats implements TabExecutor {

    private final ToolStats toolStats;

    public CommandToolStats(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("toolstats.command")) {
            sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text("ToolStats version " + toolStats.getPluginMeta().getVersion() + ". Created by hyperdefined.", NamedTextColor.GREEN));
            return true;
        }
        switch (args[0]) {
            case "reload": {
                if (sender.hasPermission("toolstats.reload")) {
                    boolean oldTokensStatus = toolStats.tokens;
                    toolStats.loadConfig();
                    toolStats.tokenData.getRecipes().clear();
                    toolStats.tokenData.setup();
                    // if the server went from tokens off -> on, add the recipes
                    // if the server went from tokens on -> off, remove the recipes
                    if (toolStats.tokens != oldTokensStatus) {
                        // tokens are now enabled
                        if (toolStats.tokens) {
                            sender.sendMessage(Component.text("It looks like you ENABLED the token system. While this is fine, it can break. Please restart your server instead.", NamedTextColor.YELLOW));
                            if (toolStats.config.getBoolean("tokens.craft-token")) {
                                for (ShapedRecipe recipe : toolStats.tokenData.getRecipes()) {
                                    Bukkit.addRecipe(recipe);
                                }
                            }
                        } else {
                            // tokens are now disabled
                            sender.sendMessage(Component.text("It looks like you DISABLED the token system. While this is fine, it can break. Please restart your server instead.", NamedTextColor.YELLOW));
                            for (ShapedRecipe recipe : toolStats.tokenData.getRecipes()) {
                                Bukkit.removeRecipe(recipe.getKey());
                            }
                        }
                    }
                    sender.sendMessage(Component.text("Configuration reloaded!", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                }
                return true;
            }
            // /toolstats edit stat value
            case "edit": {
                if (!sender.hasPermission("toolstats.edit")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return true;
                }
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Invalid syntax. Usage: /toolstats edit <stat> <value>", NamedTextColor.RED));
                    return true;
                }
                handleEdit(args[1], args[2], (Player) sender);
                return true;
            }
            // /toolstats remove stat
            case "remove": {
                if (!sender.hasPermission("toolstats.remove")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return true;
                }
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Invalid syntax. Usage: /toolstats remove <stat>", NamedTextColor.RED));
                    return true;
                }
                handleRemove(args[1], (Player) sender);
                return true;

            }
            case "reset": {
                if (!sender.hasPermission("toolstats.reset")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return true;
                }
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return true;
                }
                if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
                    if (!sender.hasPermission("toolstats.reset.confirm")) {
                        sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                        return true;
                    }
                    Player player = (Player) sender;
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (!toolStats.itemChecker.isValidItem(heldItem.getType())) {
                        sender.sendMessage(Component.text("You must hold a valid item.", NamedTextColor.RED));
                        return true;
                    }
                    fixItemLore(heldItem, player);
                    sender.sendMessage(Component.text("The lore was reset!", NamedTextColor.GREEN));
                    return true;
                }
                sender.sendMessage(Component.text("This will remove ALL current lore from the held item and replace it with the correct lore.", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("If the owner of the item is broken, it will reset to the person holding it.", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Only use this if the tags on the tool are incorrect.", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Type /toolstats reset confirm to confirm this.", NamedTextColor.GREEN));
                return true;
            }
            case "givetokens": {
                if (!sender.hasPermission("toolstats.givetokens")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return true;
                }
                // Make sure /toolstats givetoken <player> <token> is present
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Invalid syntax. Usage: /toolstats givetokens <player> <token>", NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                    return true;
                }
                String tokenType = args[2];
                if (!toolStats.tokenData.getTokenTypes().contains(tokenType)) {
                    sender.sendMessage(Component.text("Invalid token type.", NamedTextColor.RED));
                    return true;
                }
                // make sure tokens are enabled before giving
                if (!toolStats.config.getBoolean("tokens.enabled")) {
                    sender.sendMessage(Component.text("Unable to give tokens. Tokens are disabled", NamedTextColor.RED));
                    return true;
                }
                // if the user does not send in a number, default to 1
                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                        if (amount <= 0) { // Optional: Prevent negative or zero values
                            sender.sendMessage(Component.text("Token quantity must be above or 1.", NamedTextColor.RED));
                            return true;
                        }
                    } catch (NumberFormatException exception) {
                        sender.sendMessage(Component.text("Invalid token quantity.", NamedTextColor.RED));
                        return true;
                    }
                }
                giveToken(target, tokenType, amount);
                if (sender instanceof Player) {
                    sender.sendMessage(Component.text("Gave " + target.getName() + " " + amount + " " + tokenType + " tokens.", NamedTextColor.GREEN));
                }
                return true;
            }
            default: {
                sender.sendMessage(Component.text("Invalid sub-command.", NamedTextColor.RED));
            }
        }
        return true;
    }

    /**
     * Fixes lore on a given item. This will wipe all lore and reapply our custom ones.
     *
     * @param original The item we are fixing.
     * @param player   The player running the command.
     */
    private void fixItemLore(ItemStack original, Player player) {
        ItemStack finalItem = original.clone();
        ItemMeta finalMeta = finalItem.getItemMeta();
        if (finalMeta == null) {
            return;
        }
        PersistentDataContainer container = finalMeta.getPersistentDataContainer();
        List<Component> lore = new ArrayList<>();

        // set how the item was obtained
        Integer origin = -1;
        if (container.has(toolStats.originType, PersistentDataType.INTEGER)) {
            origin = container.get(toolStats.originType, PersistentDataType.INTEGER);
        }

        // set to -1 if it's invalid
        if (origin == null) {
            origin = -1;
        }

        // hard code elytras
        if (finalItem.getType() == Material.ELYTRA) {
            Long flightTime = null;
            Long timeCreated = null;
            if (container.has(toolStats.timeCreated, PersistentDataType.LONG)) {
                timeCreated = container.get(toolStats.timeCreated, PersistentDataType.LONG);
            }
            if (container.has(toolStats.flightTime, PersistentDataType.LONG)) {
                flightTime = container.get(toolStats.flightTime, PersistentDataType.LONG);
            }

            if (flightTime != null) {
                if (toolStats.config.getBoolean("enabled.flight-time")) {
                    Map<String, String> flightTimeFormatted = toolStats.numberFormat.formatTime(flightTime);
                    Component line = toolStats.configTools.formatLoreMultiplePlaceholders("flight-time", flightTimeFormatted);
                    lore.add(line);
                }
            }

            if (timeCreated != null) {
                Component timeCreatedLine = toolStats.configTools.formatLore("looted.found-by", "{player}", player.getName());
                Component playerOwnerLine = toolStats.configTools.formatLore("looted.found-on", "{date}", toolStats.numberFormat.formatDate(new Date(timeCreated)));
                lore.add(timeCreatedLine);
                lore.add(playerOwnerLine);
            }

            finalMeta.lore(lore);
            finalItem.setItemMeta(finalMeta);
            int slot = player.getInventory().getHeldItemSlot();
            player.getInventory().setItem(slot, finalItem);
        }

        if (container.has(toolStats.droppedBy, PersistentDataType.STRING)) {
            if (toolStats.config.getBoolean("enabled.dropped-by")) {
                if (container.has(toolStats.droppedBy)) {
                    String droppedBy = container.get(toolStats.droppedBy, PersistentDataType.STRING);
                    lore.add(toolStats.configTools.formatLore("dropped-by", "{name}", droppedBy));
                } else {
                    player.sendMessage(Component.text("Unable to set 'dropped-by', as this item has no record of it."));
                }
            }
        }

        if (container.has(toolStats.itemOwner, new UUIDDataType())) {
            UUID owner = container.get(toolStats.itemOwner, new UUIDDataType());
            String ownerName = null;
            // if we can read the current owner
            if (owner != null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
                ownerName = offlinePlayer.getName();
            }

            // if the owner's name is null for whatever reason, set the new owner
            // to the current player running the command
            if (ownerName == null) {
                player.sendMessage(Component.text("The owner of this item is null. Setting to " + player.getName() + ".", NamedTextColor.RED));
                ownerName = player.getName();
                container.set(toolStats.itemOwner, new UUIDDataType(), player.getUniqueId());
            }

            // show how the item was created based on the previous lore
            switch (origin) {
                case 0: {
                    if (toolStats.configTools.checkConfig(original.getType(), "crafted-by")) {
                        lore.add(toolStats.configTools.formatLore("crafted.crafted-by", "{player}", ownerName));
                    }
                    break;
                }
                case 2: {
                    if (toolStats.configTools.checkConfig(original.getType(), "looted-by")) {
                        lore.add(toolStats.configTools.formatLore("looted.looted-by", "{player}", ownerName));
                    }
                    break;
                }
                case 3: {
                    if (toolStats.configTools.checkConfig(original.getType(), "traded-by")) {
                        lore.add(toolStats.configTools.formatLore("traded.traded-by", "{player}", ownerName));
                    }
                    break;
                }
                case 5: {
                    if (toolStats.configTools.checkConfig(original.getType(), "fished-by")) {
                        lore.add(toolStats.configTools.formatLore("fished.caught-by", "{player}", ownerName));
                    }
                    break;
                }
                case 6: {
                    if (toolStats.configTools.checkConfig(original.getType(), "spawned-in-by")) {
                        lore.add(toolStats.configTools.formatLore("spawned-in.spawned-by", "{player}", ownerName));
                    }
                    break;
                }
            }
        }
        if (container.has(toolStats.timeCreated, PersistentDataType.LONG)) {
            Long time = container.get(toolStats.timeCreated, PersistentDataType.LONG);
            if (time != null) {
                String date = toolStats.numberFormat.formatDate(new Date(time));
                // show how when the item was created based on the previous lore
                switch (origin) {
                    case 0: {
                        if (toolStats.configTools.checkConfig(original.getType(), "crafted-on")) {
                            lore.add(toolStats.configTools.formatLore("crafted.crafted-on", "{date}", date));
                        }
                        break;
                    }
                    case 1: {
                        if (toolStats.config.getBoolean("enabled.dropped-on")) {
                            lore.add(toolStats.configTools.formatLore("dropped-on", "{date}", date));
                        }
                        break;
                    }
                    case 2: {
                        if (toolStats.configTools.checkConfig(original.getType(), "looted-on")) {
                            lore.add(toolStats.configTools.formatLore("looted.looted-on", "{date}", date));
                        }
                        break;
                    }
                    case 3: {
                        if (toolStats.configTools.checkConfig(original.getType(), "traded-on")) {
                            lore.add(toolStats.configTools.formatLore("traded.traded-on", "{date}", date));
                        }
                        break;
                    }
                    case 5: {
                        if (toolStats.configTools.checkConfig(original.getType(), "fished-on")) {
                            lore.add(toolStats.configTools.formatLore("fished.caught-on", "{date}", date));
                        }
                        break;
                    }
                    case 6: {
                        if (toolStats.configTools.checkConfig(original.getType(), "spawned-in-on")) {
                            lore.add(toolStats.configTools.formatLore("spawned-in.spawned-on", "{date}", date));
                        }
                        break;
                    }
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "player-kills")) {
            if (container.has(toolStats.playerKills, PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.playerKills, PersistentDataType.INTEGER);
                if (kills != null) {
                    lore.add(toolStats.configTools.formatLore("kills.player", "{kills}", toolStats.numberFormat.formatInt(kills)));
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "mob-kills")) {
            if (container.has(toolStats.mobKills, PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.mobKills, PersistentDataType.INTEGER);
                if (kills != null) {
                    lore.add(toolStats.configTools.formatLore("kills.mob", "{kills}", toolStats.numberFormat.formatInt(kills)));
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "blocks-mined")) {
            if (original.getType().toString().toLowerCase(Locale.ROOT).contains("hoe")) {
                if (container.has(toolStats.cropsHarvested, PersistentDataType.INTEGER)) {
                    Integer crops = container.get(toolStats.cropsHarvested, PersistentDataType.INTEGER);
                    if (crops != null) {
                        lore.add(toolStats.configTools.formatLore("crops-harvested", "{crops}", toolStats.numberFormat.formatInt(crops)));
                    }
                }
            }
            if (container.has(toolStats.blocksMined, PersistentDataType.INTEGER)) {
                Integer blocksMined = container.get(toolStats.blocksMined, PersistentDataType.INTEGER);
                if (blocksMined != null) {
                    lore.add(toolStats.configTools.formatLore("blocks-mined", "{blocks}", toolStats.numberFormat.formatInt(blocksMined)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.fish-caught")) {
            if (container.has(toolStats.fishCaught, PersistentDataType.INTEGER)) {
                Integer fish = container.get(toolStats.fishCaught, PersistentDataType.INTEGER);
                if (fish != null) {
                    lore.add(toolStats.configTools.formatLore("fished.fish-caught", "{fish}", toolStats.numberFormat.formatInt(fish)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.sheep-sheared")) {
            if (container.has(toolStats.sheepSheared, PersistentDataType.INTEGER)) {
                Integer sheep = container.get(toolStats.sheepSheared, PersistentDataType.INTEGER);
                if (sheep != null) {
                    lore.add(toolStats.configTools.formatLore("sheep-sheared", "{sheep}", toolStats.numberFormat.formatInt(sheep)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.armor-damage")) {
            if (container.has(toolStats.armorDamage, PersistentDataType.DOUBLE)) {
                Double damage = container.get(toolStats.armorDamage, PersistentDataType.DOUBLE);
                if (damage != null) {
                    lore.add(toolStats.configTools.formatLore("damage-taken", "{damage}", toolStats.numberFormat.formatDouble(damage)));
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "damage-done")) {
            if (container.has(toolStats.damageDone, PersistentDataType.DOUBLE)) {
                Double damage = container.get(toolStats.damageDone, PersistentDataType.DOUBLE);
                if (damage != null) {
                    lore.add(toolStats.configTools.formatLore("damage-done", "{damage}", toolStats.numberFormat.formatDouble(damage)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.arrows-shot")) {
            if (container.has(toolStats.arrowsShot, PersistentDataType.INTEGER)) {
                Integer arrows = container.get(toolStats.arrowsShot, PersistentDataType.INTEGER);
                if (arrows != null) {
                    lore.add(toolStats.configTools.formatLore("arrows-shot", "{arrows}", toolStats.numberFormat.formatInt(arrows)));
                }
            }
        }
        finalMeta.lore(lore);
        finalItem.setItemMeta(finalMeta);
        int slot = player.getInventory().getHeldItemSlot();
        player.getInventory().setItem(slot, finalItem);
    }

    /**
     * Gives a player a token.
     *
     * @param target    The player.
     * @param tokenType The token type.
     */
    private void giveToken(Player target, String tokenType, int amount) {
        ItemStack token = toolStats.tokenData.createToken(tokenType);
        token.setAmount(amount);
        target.getInventory().addItem(token);
    }

    /**
     * Handle edit subcommand.
     *
     * @param stat      The stat to edit.
     * @param userValue The value the user entered.
     * @param player    The player using the command.
     */
    private void handleEdit(String stat, Object userValue, Player player) {
        ItemStack editedItem = player.getInventory().getItemInMainHand().clone();
        if (!toolStats.itemChecker.isValidItem(editedItem.getType())) {
            player.sendMessage(Component.text("This is not a valid item.", NamedTextColor.RED));
            return;
        }
        ItemMeta editedItemMeta = editedItem.getItemMeta();
        PersistentDataContainer container = editedItemMeta.getPersistentDataContainer();
        switch (stat) {
            case "crops-harvested": {
                if (!toolStats.config.getBoolean("enabled.crops-harvested")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.cropsHarvested)) {
                    int value;
                    try {
                        value = Integer.parseInt((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Integer statValue = container.get(toolStats.cropsHarvested, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateCropsMined(editedItem, difference);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "blocks-mined": {
                if (!toolStats.configTools.checkConfig(editedItem.getType(), "blocks-mined")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.blocksMined)) {
                    int value;
                    try {
                        value = Integer.parseInt((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Integer statValue = container.get(toolStats.blocksMined, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateBlocksMined(editedItem, difference);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "damage-taken": {
                if (!toolStats.config.getBoolean("enabled.armor-damage")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.armorDamage)) {
                    double value;
                    try {
                        value = Double.parseDouble((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Double statValue = container.get(toolStats.armorDamage, PersistentDataType.DOUBLE);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    double difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateArmorDamage(editedItem, difference, false);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "damage-done": {
                if (!toolStats.configTools.checkConfig(editedItem.getType(), "damage-done")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.damageDone)) {
                    double value;
                    try {
                        value = Double.parseDouble((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Double statValue = container.get(toolStats.damageDone, PersistentDataType.DOUBLE);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    double difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateWeaponDamage(editedItem, difference, false);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "mob-kills": {
                if (!toolStats.configTools.checkConfig(editedItem.getType(), "mob-kills")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.mobKills)) {
                    int value;
                    try {
                        value = Integer.parseInt((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Integer statValue = container.get(toolStats.mobKills, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateMobKills(editedItem, difference);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "player-kills": {
                if (!toolStats.configTools.checkConfig(editedItem.getType(), "player-kills")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.playerKills)) {
                    int value;
                    try {
                        value = Integer.parseInt((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Integer statValue = container.get(toolStats.playerKills, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updatePlayerKills(editedItem, difference);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "arrows-shot": {
                if (!toolStats.config.getBoolean("enabled.arrows-shot")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.arrowsShot)) {
                    int value;
                    try {
                        value = Integer.parseInt((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Integer statValue = container.get(toolStats.arrowsShot, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateArrowsShot(editedItem, difference);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "sheep-sheared": {
                if (!toolStats.config.getBoolean("enabled.sheep-sheared")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.sheepSheared)) {
                    int value;
                    try {
                        value = Integer.parseInt((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Integer statValue = container.get(toolStats.sheepSheared, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateSheepSheared(editedItem, difference);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "flight-time": {
                if (!toolStats.config.getBoolean("enabled.flight-time")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.flightTime)) {
                    int value;
                    try {
                        value = Integer.parseInt((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Long statValue = container.get(toolStats.flightTime, PersistentDataType.LONG);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    long difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateFlightTime(editedItem, difference);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "fight-caught": {
                if (!toolStats.config.getBoolean("enabled.fight-caught")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.fishCaught)) {
                    int value;
                    try {
                        value = Integer.parseInt((String) userValue);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(Component.text("That is not a valid number.", NamedTextColor.RED));
                        return;
                    }
                    if (value < 0) {
                        player.sendMessage(Component.text("Number must be positive.", NamedTextColor.RED));
                        return;
                    }
                    Integer statValue = container.get(toolStats.fishCaught, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateFishCaught(editedItem, difference);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            default: {
                player.sendMessage(Component.text("That is not a valid stat to update.", NamedTextColor.RED));
                return;
            }
        }
        editedItem.setItemMeta(editedItemMeta);
        player.getInventory().setItemInMainHand(editedItem);
        player.sendMessage(Component.text("Updated stat " + stat + " for held item!", NamedTextColor.GREEN));
    }

    /**
     * Handle remove subcommand.
     *
     * @param stat   The stat to remove.
     * @param player The player using the command.
     */
    private void handleRemove(String stat, Player player) {
        ItemStack editedItem = player.getInventory().getItemInMainHand().clone();
        if (!toolStats.itemChecker.isValidItem(editedItem.getType())) {
            player.sendMessage(Component.text("This is not a valid item.", NamedTextColor.RED));
            return;
        }
        ItemMeta editedItemMeta = editedItem.getItemMeta();
        PersistentDataContainer container = editedItemMeta.getPersistentDataContainer();
        switch (stat) {
            case "crops-harvested": {
                if (container.has(toolStats.cropsHarvested)) {
                    Integer statValue = container.get(toolStats.cropsHarvested, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.cropsHarvested);
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "crops-mined");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.tokenApplied);
                    } else {
                        container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("crops-harvested", "{crops}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "blocks-mined": {
                if (container.has(toolStats.blocksMined)) {
                    Integer statValue = container.get(toolStats.blocksMined, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.blocksMined);
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "blocks-mined");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.tokenApplied);
                    } else {
                        container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("blocks-mined", "{blocks}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "damage-taken": {
                if (container.has(toolStats.armorDamage)) {
                    Double statValue = container.get(toolStats.armorDamage, PersistentDataType.DOUBLE);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.armorDamage);
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "damage-taken");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.tokenApplied);
                    } else {
                        container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("damage-taken", "{damage}", toolStats.numberFormat.formatDouble(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "damage-done": {
                if (container.has(toolStats.damageDone)) {
                    Double statValue = container.get(toolStats.damageDone, PersistentDataType.DOUBLE);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.damageDone);
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "damage-done");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.tokenApplied);
                    } else {
                        container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("damage-done", "{damage}", toolStats.numberFormat.formatDouble(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "mob-kills": {
                if (container.has(toolStats.mobKills)) {
                    Integer statValue = container.get(toolStats.mobKills, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.mobKills);
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "mob-kills");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.tokenApplied);
                    } else {
                        container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("kills.mob", "{kills}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "player-kills": {
                if (container.has(toolStats.playerKills)) {
                    Integer statValue = container.get(toolStats.playerKills, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.playerKills);
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "player-kills");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.tokenApplied);
                    } else {
                        container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("kills.player", "{kills}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "sheep-sheared": {
                if (container.has(toolStats.sheepSheared)) {
                    Integer statValue = container.get(toolStats.sheepSheared, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.sheepSheared);
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "sheep-sheared");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.tokenApplied);
                    } else {
                        container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("sheep-sheared", "{sheep}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "flight-time": {
                if (container.has(toolStats.flightTime)) {
                    Long statValue = container.get(toolStats.flightTime, PersistentDataType.LONG);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.flightTime);
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "flight-time");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.tokenApplied);
                    } else {
                        container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Map<String, String> timeFormatted = toolStats.numberFormat.formatTime(statValue);
                    Component oldLine = toolStats.configTools.formatLoreMultiplePlaceholders("flight-time", timeFormatted);
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "fight-caught": {
                if (container.has(toolStats.fishCaught)) {
                    Integer statValue = container.get(toolStats.fishCaught, PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.tokenApplied, PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.fishCaught);
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "fight-caught");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.tokenApplied);
                    } else {
                        container.set(toolStats.tokenApplied, PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("fished.fish-caught", "{fish}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            default: {
                player.sendMessage(Component.text("That is not a valid stat to update.", NamedTextColor.RED));
                return;
            }
        }
        editedItem.setItemMeta(editedItemMeta);
        player.getInventory().setItemInMainHand(editedItem);
        player.sendMessage(Component.text("Removed stat " + stat + " for held item!", NamedTextColor.GREEN));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            if (sender.hasPermission("toolstats.reload")) {
                suggestions.add("reload");
            }
            if (sender.hasPermission("toolstats.reset")) {
                suggestions.add("reset");
            }
            if (sender.hasPermission("toolstats.givetokens")) {
                suggestions.add("givetokens");
            }
            if (sender.hasPermission("toolstats.edit")) {
                suggestions.add("edit");
            }
            if (sender.hasPermission("toolstats.remove")) {
                suggestions.add("remove");
            }
            return suggestions.isEmpty() ? null : suggestions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("toolstats.reset.confirm")) {
            return Collections.singletonList("confirm");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("edit") && sender.hasPermission("toolstats.edit")) {
            // yes I am lazy
            return toolStats.tokenData.getTokenTypes().stream()
                    .filter(s -> !s.equals("remove") && !s.equals("reset"))
                    .map(s -> s.equals("crops-mined") ? "crops-harvested" : s)
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove") && sender.hasPermission("toolstats.remove")) {
            // yes I am lazy
            return toolStats.tokenData.getTokenTypes().stream()
                    .filter(s -> !s.equals("remove") && !s.equals("reset"))
                    .map(s -> s.equals("crops-mined") ? "crops-harvested" : s)
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("givetokens") && sender.hasPermission("toolstats.givetokens")) {
            return toolStats.tokenData.getTokenTypes();
        }

        return null;
    }
}
