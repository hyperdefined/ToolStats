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

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lol.hyper.hyperlib.datatypes.UUIDDataType;
import lol.hyper.toolstats.ToolStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommandToolStats implements BasicCommand {

    private final ToolStats toolStats;

    public CommandToolStats(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @Override
    public void execute(@NonNull CommandSourceStack source, String @NonNull [] args) {
        CommandSender sender = source.getSender();
        if (args.length == 0) {
            sender.sendMessage(Component.text("ToolStats version " + toolStats.getPluginMeta().getVersion() + ". Created by hyperdefined.", NamedTextColor.GREEN));
            return;
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
                return;
            }
            // /toolstats edit stat value
            case "edit": {
                if (!sender.hasPermission("toolstats.edit")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return;
                }
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Invalid syntax. Usage: /toolstats edit <stat> <value>", NamedTextColor.RED));
                    return;
                }
                handleEdit(args[1], args[2], (Player) sender);
                return;
            }
            // /toolstats remove stat
            case "remove": {
                if (!sender.hasPermission("toolstats.remove")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return;
                }
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Invalid syntax. Usage: /toolstats remove <stat>", NamedTextColor.RED));
                    return;
                }
                handleRemove(args[1], (Player) sender);
                return;

            }
            case "reset": {
                if (!sender.hasPermission("toolstats.reset")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return;
                }
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return;
                }
                if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
                    if (!sender.hasPermission("toolstats.reset.confirm")) {
                        sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                        return;
                    }
                    Player player = (Player) sender;
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (!toolStats.itemChecker.isValidItem(heldItem.getType())) {
                        sender.sendMessage(Component.text("You must hold a valid item.", NamedTextColor.RED));
                        return;
                    }
                    fixItemLore(heldItem, player);
                    sender.sendMessage(Component.text("The lore was reset!", NamedTextColor.GREEN));
                    return;
                }
                sender.sendMessage(Component.text("This will remove ALL current lore from the held item and replace it with the correct lore.", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("If the owner of the item is broken, it will reset to the person holding it.", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Only use this if the tags on the tool are incorrect.", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Type /toolstats reset confirm to confirm this.", NamedTextColor.GREEN));
                return;
            }
            case "purge": {
                if (!sender.hasPermission("toolstats.purge")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return;
                }
                if (sender instanceof ConsoleCommandSender) {
                    sender.sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return;
                }
                if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
                    if (!sender.hasPermission("toolstats.purge.confirm")) {
                        sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                        return;
                    }
                    Player player = (Player) sender;
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (!toolStats.itemChecker.isValidItem(heldItem.getType())) {
                        sender.sendMessage(Component.text("You must hold a valid item.", NamedTextColor.RED));
                        return;
                    }
                    ItemStack purgedItem = toolStats.itemLore.removeAll(heldItem, true);
                    player.getInventory().setItemInMainHand(purgedItem);
                    sender.sendMessage(Component.text("The item was purged!", NamedTextColor.GREEN));
                    return;
                }
                sender.sendMessage(Component.text("This will purge ALL ToolStats data from this item.", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("This includes all stats, ownership, and creation time.", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("THIS CANNOT BE UNDONE!", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Type /toolstats purge confirm to confirm this.", NamedTextColor.GREEN));
                return;
            }
            case "givetokens": {
                if (!sender.hasPermission("toolstats.givetokens")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return;
                }
                // Make sure /toolstats givetoken <player> <token> is present
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Invalid syntax. Usage: /toolstats givetokens <player> <token>", NamedTextColor.RED));
                    return;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                    return;
                }
                String tokenType = args[2];
                if (!toolStats.tokenData.getTokenTypes().contains(tokenType)) {
                    sender.sendMessage(Component.text("Invalid token type.", NamedTextColor.RED));
                    return;
                }
                // make sure tokens are enabled before giving
                if (!toolStats.config.getBoolean("tokens.enabled")) {
                    sender.sendMessage(Component.text("Unable to give tokens. Tokens are disabled", NamedTextColor.RED));
                    return;
                }
                // if the user does not send in a number, default to 1
                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                        if (amount <= 0) { // Optional: Prevent negative or zero values
                            sender.sendMessage(Component.text("Token quantity must be above or 1.", NamedTextColor.RED));
                            return;
                        }
                    } catch (NumberFormatException exception) {
                        sender.sendMessage(Component.text("Invalid token quantity.", NamedTextColor.RED));
                        return;
                    }
                }
                giveToken(target, tokenType, amount);
                if (sender instanceof Player) {
                    sender.sendMessage(Component.text("Gave " + target.getName() + " " + amount + " " + tokenType + " tokens.", NamedTextColor.GREEN));
                }
                return;
            }
            default: {
                sender.sendMessage(Component.text("Invalid sub-command.", NamedTextColor.RED));
            }
        }
    }

    @Override
    public @Nullable String permission() {
        return "toolstats.command";
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
        if (container.has(toolStats.toolStatsKeys.getOriginType(), PersistentDataType.INTEGER)) {
            origin = container.get(toolStats.toolStatsKeys.getOriginType(), PersistentDataType.INTEGER);
        }

        // set to -1 if it's invalid
        if (origin == null) {
            origin = -1;
        }

        if (container.has(toolStats.toolStatsKeys.getDroppedBy(), PersistentDataType.STRING)) {
            if (toolStats.config.getBoolean("enabled.dropped-by")) {
                if (container.has(toolStats.toolStatsKeys.getDroppedBy())) {
                    String droppedBy = container.get(toolStats.toolStatsKeys.getDroppedBy(), PersistentDataType.STRING);
                    lore.add(toolStats.configTools.formatLore("dropped-by", "{name}", droppedBy));
                } else {
                    player.sendMessage(Component.text("Unable to set 'dropped-by', as this item has no record of it."));
                }
            }
        }

        if (container.has(toolStats.toolStatsKeys.getItemOwner(), new UUIDDataType())) {
            UUID owner = container.get(toolStats.toolStatsKeys.getItemOwner(), new UUIDDataType());
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
                container.set(toolStats.toolStatsKeys.getItemOwner(), new UUIDDataType(), player.getUniqueId());
            }

            // add the ownership lore
            Component ownerLore = toolStats.itemLore.formatOwner(ownerName, origin, original);
            if (ownerLore != null) {
                lore.add(ownerLore);
            }

        }
        if (container.has(toolStats.toolStatsKeys.getTimeCreated(), PersistentDataType.LONG)) {
            Long time = container.get(toolStats.toolStatsKeys.getTimeCreated(), PersistentDataType.LONG);
            if (time != null) {
                // add the creation time lore
                Component creationTimeLore = toolStats.itemLore.formatCreationTime(time, origin, original);
                if (creationTimeLore != null) {
                    lore.add(creationTimeLore);
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.flight-time")) {
            if (container.has(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG)) {
                Long flightTime = container.get(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG);
                if (flightTime != null) {
                    Map<String, String> flightTimeFormatted = toolStats.numberFormat.formatTime(flightTime);
                    Component line = toolStats.configTools.formatLoreMultiplePlaceholders("flight-time", flightTimeFormatted);
                    lore.add(line);
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "player-kills")) {
            if (container.has(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER);
                if (kills != null) {
                    lore.add(toolStats.configTools.formatLore("kills.player", "{kills}", toolStats.numberFormat.formatInt(kills)));
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "mob-kills")) {
            if (container.has(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER);
                if (kills != null) {
                    lore.add(toolStats.configTools.formatLore("kills.mob", "{kills}", toolStats.numberFormat.formatInt(kills)));
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "blocks-mined")) {
            if (original.getType().toString().toLowerCase(Locale.ROOT).contains("hoe")) {
                if (container.has(toolStats.toolStatsKeys.getCropsHarvested(), PersistentDataType.INTEGER)) {
                    Integer crops = container.get(toolStats.toolStatsKeys.getCropsHarvested(), PersistentDataType.INTEGER);
                    if (crops != null) {
                        lore.add(toolStats.configTools.formatLore("crops-harvested", "{crops}", toolStats.numberFormat.formatInt(crops)));
                    }
                }
            }
            if (container.has(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER)) {
                Integer blocksMined = container.get(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER);
                if (blocksMined != null) {
                    lore.add(toolStats.configTools.formatLore("blocks-mined", "{blocks}", toolStats.numberFormat.formatInt(blocksMined)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.fish-caught")) {
            if (container.has(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER)) {
                Integer fish = container.get(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER);
                if (fish != null) {
                    lore.add(toolStats.configTools.formatLore("fished.fish-caught", "{fish}", toolStats.numberFormat.formatInt(fish)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.sheep-sheared")) {
            if (container.has(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER)) {
                Integer sheep = container.get(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER);
                if (sheep != null) {
                    lore.add(toolStats.configTools.formatLore("sheep-sheared", "{sheep}", toolStats.numberFormat.formatInt(sheep)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.armor-damage")) {
            if (container.has(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE)) {
                Double damage = container.get(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE);
                if (damage != null) {
                    lore.add(toolStats.configTools.formatLore("damage-taken", "{damage}", toolStats.numberFormat.formatDouble(damage)));
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "damage-done")) {
            if (container.has(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE)) {
                Double damage = container.get(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE);
                if (damage != null) {
                    lore.add(toolStats.configTools.formatLore("damage-done", "{damage}", toolStats.numberFormat.formatDouble(damage)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.arrows-shot")) {
            if (container.has(toolStats.toolStatsKeys.getArrowsShot(), PersistentDataType.INTEGER)) {
                Integer arrows = container.get(toolStats.toolStatsKeys.getArrowsShot(), PersistentDataType.INTEGER);
                if (arrows != null) {
                    lore.add(toolStats.configTools.formatLore("arrows-shot", "{arrows}", toolStats.numberFormat.formatInt(arrows)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.bosses-killed.wither")) {
            if (container.has(toolStats.toolStatsKeys.getWitherKills(), PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.toolStatsKeys.getWitherKills(), PersistentDataType.INTEGER);
                if (kills != null) {
                    lore.add(toolStats.configTools.formatLore("bosses-killed.wither", "{kills}", toolStats.numberFormat.formatInt(kills)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.bosses-killed.enderdragon")) {
            if (container.has(toolStats.toolStatsKeys.getEnderDragonKills(), PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.toolStatsKeys.getEnderDragonKills(), PersistentDataType.INTEGER);
                if (kills != null) {
                    lore.add(toolStats.configTools.formatLore("bosses-killed.enderdragon", "{kills}", toolStats.numberFormat.formatInt(kills)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.critical-strikes")) {
            if (container.has(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER)) {
                Integer strikes = container.get(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER);
                if (strikes != null) {
                    lore.add(toolStats.configTools.formatLore("critical-strikes", "{strikes}", toolStats.numberFormat.formatInt(strikes)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.trident-throws")) {
            if (container.has(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER)) {
                Integer tridentThrows = container.get(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER);
                if (tridentThrows != null) {
                    lore.add(toolStats.configTools.formatLore("trident-throws", "{times}", toolStats.numberFormat.formatInt(tridentThrows)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.logs-stripped")) {
            if (container.has(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER)) {
                Integer logsStripped = container.get(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER);
                if (logsStripped != null) {
                    lore.add(toolStats.configTools.formatLore("logs-stripped", "{logs}", toolStats.numberFormat.formatInt(logsStripped)));
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
        boolean updated = false;
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
                if (container.has(toolStats.toolStatsKeys.getCropsHarvested())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getCropsHarvested(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateCropsMined(editedItem, difference);
                    updated = true;
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
                if (container.has(toolStats.toolStatsKeys.getBlocksMined())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateBlocksMined(editedItem, difference);
                    updated = true;
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
                if (container.has(toolStats.toolStatsKeys.getArmorDamage())) {
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
                    Double statValue = container.get(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    double difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateArmorDamage(editedItem, difference, false);
                    updated = true;
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
                if (container.has(toolStats.toolStatsKeys.getDamageDone())) {
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
                    Double statValue = container.get(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    double difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateWeaponDamage(editedItem, difference, false);
                    updated = true;
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
                if (container.has(toolStats.toolStatsKeys.getMobKills())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateMobKills(editedItem, difference);
                    updated = true;
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
                if (container.has(toolStats.toolStatsKeys.getPlayerKills())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updatePlayerKills(editedItem, difference);
                    updated = true;
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
                if (container.has(toolStats.toolStatsKeys.getArrowsShot())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getArrowsShot(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateArrowsShot(editedItem, difference);
                    updated = true;
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
                if (container.has(toolStats.toolStatsKeys.getSheepSheared())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateSheepSheared(editedItem, difference);
                    updated = true;
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
                if (container.has(toolStats.toolStatsKeys.getFlightTime())) {
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
                    Long statValue = container.get(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    long difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateFlightTime(editedItem, difference);
                    updated = true;
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
                if (container.has(toolStats.toolStatsKeys.getFishCaught())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateFishCaught(editedItem, difference);
                    updated = true;
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "wither-kills": {
                if (!toolStats.config.getBoolean("enabled.bosses-killed.wither")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.toolStatsKeys.getWitherKills())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getWitherKills(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateBossesKilled(editedItem, difference, "wither");
                    updated = true;
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "enderdragon-kills": {
                if (!toolStats.config.getBoolean("enabled.bosses-killed.enderdragon")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.toolStatsKeys.getEnderDragonKills())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getEnderDragonKills(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateBossesKilled(editedItem, difference, "enderdragon");
                    updated = true;
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "critical-strikes": {
                if (!toolStats.config.getBoolean("enabled.critical-strikes")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.toolStatsKeys.getCriticalStrikes())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateCriticalStrikes(editedItem, difference);
                    updated = true;
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "trident-throws": {
                if (!toolStats.config.getBoolean("enabled.trident-throws")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.toolStatsKeys.getTridentThrows())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateTridentThrows(editedItem, difference);
                    updated = true;
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "logs-stripped": {
                if (!toolStats.config.getBoolean("enabled.logs-stripped")) {
                    player.sendMessage(Component.text("This stat is disabled.", NamedTextColor.RED));
                    return;
                }
                if (container.has(toolStats.toolStatsKeys.getLogsStripped())) {
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
                    Integer statValue = container.get(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    int difference = value - statValue;
                    editedItemMeta = toolStats.itemLore.updateLogsStripped(editedItem, difference);
                    updated = true;
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

        if (updated) {
            editedItem.setItemMeta(editedItemMeta);
            player.getInventory().setItemInMainHand(editedItem);
            player.sendMessage(Component.text("Updated stat " + stat + " for held item!", NamedTextColor.GREEN));
        }
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
                if (container.has(toolStats.toolStatsKeys.getCropsHarvested())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getCropsHarvested(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getCropsHarvested());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "crops-mined");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
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
                if (container.has(toolStats.toolStatsKeys.getBlocksMined())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getBlocksMined(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getBlocksMined());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "blocks-mined");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
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
                if (container.has(toolStats.toolStatsKeys.getArmorDamage())) {
                    Double statValue = container.get(toolStats.toolStatsKeys.getArmorDamage(), PersistentDataType.DOUBLE);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getArmorDamage());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "damage-taken");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
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
                if (container.has(toolStats.toolStatsKeys.getDamageDone())) {
                    Double statValue = container.get(toolStats.toolStatsKeys.getDamageDone(), PersistentDataType.DOUBLE);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getDamageDone());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "damage-done");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
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
                if (container.has(toolStats.toolStatsKeys.getMobKills())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getMobKills(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getMobKills());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "mob-kills");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
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
                if (container.has(toolStats.toolStatsKeys.getPlayerKills())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getPlayerKills(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getPlayerKills());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "player-kills");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
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
                if (container.has(toolStats.toolStatsKeys.getSheepSheared())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getSheepSheared(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getSheepSheared());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "sheep-sheared");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
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
                if (container.has(toolStats.toolStatsKeys.getFlightTime())) {
                    Long statValue = container.get(toolStats.toolStatsKeys.getFlightTime(), PersistentDataType.LONG);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getFlightTime());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "flight-time");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
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
            case "fish-caught": {
                if (container.has(toolStats.toolStatsKeys.getFishCaught())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getFishCaught(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getFishCaught());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "fish-caught");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("fished.fish-caught", "{fish}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "wither-kills": {
                if (container.has(toolStats.toolStatsKeys.getWitherKills())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getWitherKills(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getWitherKills());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "wither-kills");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("bosses-killed.wither", "{kills}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "enderdragon-kills": {
                if (container.has(toolStats.toolStatsKeys.getEnderDragonKills())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getEnderDragonKills(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getEnderDragonKills());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "enderdragon-kills");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("bosses-killed.enderdragon", "{kills}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "critical-strikes": {
                if (container.has(toolStats.toolStatsKeys.getCriticalStrikes())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getCriticalStrikes(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getCriticalStrikes());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "critical-strikes");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("critical-strikes", "{strikes}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "trident-throws": {
                if (container.has(toolStats.toolStatsKeys.getTridentThrows())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getTridentThrows(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getTridentThrows());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "trident-throws");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("trident-throws", "{times}", toolStats.numberFormat.formatInt(statValue));
                    List<Component> newLore = toolStats.itemLore.removeLore(editedItemMeta.lore(), oldLine);
                    editedItemMeta.lore(newLore);
                } else {
                    player.sendMessage(Component.text("This item does not have that stat.", NamedTextColor.RED));
                }
                break;
            }
            case "logs-stripped": {
                if (container.has(toolStats.toolStatsKeys.getLogsStripped())) {
                    Integer statValue = container.get(toolStats.toolStatsKeys.getLogsStripped(), PersistentDataType.INTEGER);
                    if (statValue == null) {
                        player.sendMessage(Component.text("Unable to get stat from item.", NamedTextColor.RED));
                        return;
                    }
                    String tokens = container.get(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING);
                    if (tokens == null) {
                        player.sendMessage(Component.text("Unable to get tokens from item.", NamedTextColor.RED));
                        return;
                    }
                    container.remove(toolStats.toolStatsKeys.getLogsStripped());
                    List<String> newTokens = toolStats.itemChecker.removeToken(tokens, "logs-stripped");
                    if (newTokens.isEmpty()) {
                        container.remove(toolStats.toolStatsKeys.getTokenApplied());
                    } else {
                        container.set(toolStats.toolStatsKeys.getTokenApplied(), PersistentDataType.STRING, String.join(",", newTokens));
                    }

                    Component oldLine = toolStats.configTools.formatLore("logs-stripped", "{logs}", toolStats.numberFormat.formatInt(statValue));
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

    @Override
    public @NonNull Collection<String> suggest(@NonNull CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        // suggest basic sub commands
        if (args.length == 0) {
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
            if (sender.hasPermission("toolstats.purge")) {
                suggestions.add("purge");
            }
            return suggestions;
        }

        // suggest confirm for reset
        if (args.length == 2 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("toolstats.reset.confirm")) {
            return Collections.singletonList("confirm");
        }
        // suggest confirm for purge
        if (args.length == 2 && args[0].equalsIgnoreCase("purge") && sender.hasPermission("toolstats.purge.confirm")) {
            return Collections.singletonList("confirm");
        }

        // suggest keys for edit
        if (args.length == 2 && args[0].equalsIgnoreCase("edit") && sender.hasPermission("toolstats.edit")) {
            // yes I am lazy
            return toolStats.tokenData.getTokenTypes().stream()
                    .filter(s -> !s.equals("remove") && !s.equals("reset"))
                    .map(s -> s.equals("crops-mined") ? "crops-harvested" : s)
                    .collect(Collectors.toList());
        }

        // suggest keys for remove
        if (args.length == 2 && args[0].equalsIgnoreCase("remove") && sender.hasPermission("toolstats.remove")) {
            // yes I am lazy
            return toolStats.tokenData.getTokenTypes().stream()
                    .filter(s -> !s.equals("remove") && !s.equals("reset"))
                    .map(s -> s.equals("crops-mined") ? "crops-harvested" : s)
                    .collect(Collectors.toList());
        }

        // suggest players for givetokens
        if (args.length == 2 && args[0].equalsIgnoreCase("givetokens") && sender.hasPermission("toolstats.givetokens")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }

        // suggest token types for givetokens
        if (args.length == 3 && args[0].equalsIgnoreCase("givetokens") && sender.hasPermission("toolstats.givetokens")) {
            return toolStats.tokenData.getTokenTypes();
        }
        return Collections.emptyList();
    }
}
