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
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandToolStats implements TabExecutor {

    private final ToolStats toolStats;
    private final BukkitAudiences audiences;

    public CommandToolStats(ToolStats toolStats) {
        this.toolStats = toolStats;
        this.audiences = toolStats.getAdventure();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("toolstats.command")) {
            audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            audiences.sender(sender).sendMessage(Component.text("ToolStats version " + toolStats.getDescription().getVersion() + ". Created by hyperdefined.", NamedTextColor.GREEN));
            return true;
        }
        switch (args[0]) {
            case "reload": {
                if (sender.hasPermission("toolstats.reload")) {
                    toolStats.loadConfig();
                    audiences.sender(sender).sendMessage(Component.text("Configuration reloaded!", NamedTextColor.GREEN));
                } else {
                    audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                }
                return true;
            }
            case "reset": {
                if (!sender.hasPermission("toolstats.reset")) {
                    audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                    return true;
                }
                if (sender instanceof ConsoleCommandSender) {
                    audiences.sender(sender).sendMessage(Component.text("You must be a player for this command.", NamedTextColor.RED));
                    return true;
                }
                if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
                    if (!sender.hasPermission("toolstats.reset.confirm")) {
                        audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.", NamedTextColor.RED));
                        return true;
                    }
                    Player player = (Player) sender;
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (!toolStats.itemChecker.isValidItem(heldItem.getType())) {
                        audiences.sender(sender).sendMessage(Component.text("You must hold a valid item.", NamedTextColor.RED));
                        return true;
                    }
                    fixItemLore(heldItem, player);
                    audiences.sender(sender).sendMessage(Component.text("The lore was reset!", NamedTextColor.GREEN));
                    return true;
                }
                audiences.sender(sender).sendMessage(Component.text("This will remove ALL current lore from the held item and replace it with the correct lore.", NamedTextColor.GREEN));
                audiences.sender(sender).sendMessage(Component.text("If the owner of the item is broken, it will reset to the person holding it.", NamedTextColor.GREEN));
                audiences.sender(sender).sendMessage(Component.text("Only use this if the tags on the tool are incorrect.", NamedTextColor.GREEN));
                audiences.sender(sender).sendMessage(Component.text("Type /toolstats reset confirm to confirm this.", NamedTextColor.GREEN));
                return true;
            }
            default: {
                audiences.sender(sender).sendMessage(Component.text("Invalid sub-command.", NamedTextColor.RED));
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
        List<String> lore = new ArrayList<>();

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
                    String line = toolStats.configTools.formatLore("flight-time", "{time}", toolStats.numberFormat.formatDouble((double) flightTime / 1000));
                    lore.add(line);
                }
            }

            if (timeCreated != null) {
                String timeCreatedLine = toolStats.configTools.formatLore("looted.found-by", "{player}", player.getName());
                String playerOwnerLine = toolStats.configTools.formatLore("looted.found-on", "{date}", toolStats.numberFormat.formatDate(new Date(timeCreated)));
                lore.add(timeCreatedLine);
                lore.add(playerOwnerLine);
            }

            finalMeta.setLore(lore);
            finalItem.setItemMeta(finalMeta);
            int slot = player.getInventory().getHeldItemSlot();
            player.getInventory().setItem(slot, finalItem);
        }

        if (toolStats.configTools.checkConfig(original.getType(), "created-by")) {
            if (container.has(toolStats.genericOwner, new UUIDDataType())) {
                UUID owner = container.get(toolStats.genericOwner, new UUIDDataType());
                String ownerName = null;
                // if we can read the current owner
                if (owner != null) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
                    ownerName = offlinePlayer.getName();
                }

                // if the owner's name is null for whatever reason, set the new owner
                // to the current player running the command
                if (ownerName == null) {
                    audiences.player(player).sendMessage(Component.text("The owner of this item is null. Setting to " + player.getName() + ".", NamedTextColor.RED));
                    ownerName = player.getName();
                    container.set(toolStats.genericOwner, new UUIDDataType(), player.getUniqueId());
                }

                // show how the item was created based on the previous lore
                switch (origin) {
                    case 0: {
                        lore.add(toolStats.configTools.formatLore("created.created-by", "{player}", ownerName));
                        break;
                    }
                    case 2: {
                        lore.add(toolStats.configTools.formatLore("looted.looted-by", "{player}", ownerName));
                        break;
                    }
                    case 3: {
                        lore.add(toolStats.configTools.formatLore("traded.traded-by", "{player}", ownerName));
                        break;
                    }
                    case 4: {
                        lore.add(toolStats.configTools.formatLore("looted.found-by", "{player}", ownerName));
                        break;
                    }
                    case 5: {
                        lore.add(toolStats.configTools.formatLore("fished.caught-by", "{player}", ownerName));
                        break;
                    }
                    case 6: {
                        lore.add(toolStats.configTools.formatLore("spawned-in.spawned-by", "{player}", ownerName));
                        break;
                    }
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "created-date")) {
            if (container.has(toolStats.timeCreated, PersistentDataType.LONG)) {
                Long time = container.get(toolStats.timeCreated, PersistentDataType.LONG);
                if (time != null) {
                    String date = toolStats.numberFormat.formatDate(new Date(time));
                    // show how when the item was created based on the previous lore
                    switch (origin) {
                        case 0: {
                            lore.add(toolStats.configTools.formatLore("created.created-on", "{date}", date));
                            break;
                        }
                        case 2: {
                            lore.add(toolStats.configTools.formatLore("looted.looted-on", "{date}", date));
                            break;
                        }
                        case 3: {
                            lore.add(toolStats.configTools.formatLore("traded.traded-on", "{date}", date));
                            break;
                        }
                        case 4: {
                            lore.add(toolStats.configTools.formatLore("looted.found-on", "{date}", date));
                            break;
                        }
                        case 5: {
                            lore.add(toolStats.configTools.formatLore("fished.caught-on", "{date}", date));
                            break;
                        }
                        case 6: {
                            lore.add(toolStats.configTools.formatLore("spawned-in.spawned-on", "{date}", date));
                            break;
                        }
                    }
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "player-kills")) {
            if (container.has(toolStats.swordPlayerKills, PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.swordPlayerKills, PersistentDataType.INTEGER);
                if (kills != null) {
                    lore.add(toolStats.configTools.formatLore("kills.player", "{kills}", toolStats.numberFormat.formatInt(kills)));
                }
            }
        }
        if (toolStats.configTools.checkConfig(original.getType(), "mob-kills")) {
            if (container.has(toolStats.swordMobKills, PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.swordMobKills, PersistentDataType.INTEGER);
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
            if (container.has(toolStats.genericMined, PersistentDataType.INTEGER)) {
                Integer blocksMined = container.get(toolStats.genericMined, PersistentDataType.INTEGER);
                if (blocksMined != null) {
                    lore.add(toolStats.configTools.formatLore("blocks-mined", "{blocks}", toolStats.numberFormat.formatInt(blocksMined)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.fish-caught")) {
            if (container.has(toolStats.fishingRodCaught, PersistentDataType.INTEGER)) {
                Integer fish = container.get(toolStats.fishingRodCaught, PersistentDataType.INTEGER);
                if (fish != null) {
                    lore.add(toolStats.configTools.formatLore("fished.fish-caught", "{fish}", toolStats.numberFormat.formatInt(fish)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.sheep-sheared")) {
            if (container.has(toolStats.shearsSheared, PersistentDataType.INTEGER)) {
                Integer sheep = container.get(toolStats.shearsSheared, PersistentDataType.INTEGER);
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
        if (toolStats.config.getBoolean("enabled.arrows-shot")) {
            if (container.has(toolStats.arrowsShot, PersistentDataType.INTEGER)) {
                Integer arrows = container.get(toolStats.arrowsShot, PersistentDataType.INTEGER);
                if (arrows != null) {
                    lore.add(toolStats.configTools.formatLore("arrows-shot", "{arrows}", toolStats.numberFormat.formatInt(arrows)));
                }
            }
        }
        finalMeta.setLore(lore);
        finalItem.setItemMeta(finalMeta);
        int slot = player.getInventory().getHeldItemSlot();
        player.getInventory().setItem(slot, finalItem);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("toolstats.reload")) {
                return Arrays.asList("reset", "reload");
            }
            if (sender.hasPermission("toolstats.reset")) {
                return Collections.singletonList("reset");
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                if (sender.hasPermission("toolstats.reset.confirm")) {
                    return Collections.singletonList("confirm");
                }
            }
        }
        return null;
    }
}
