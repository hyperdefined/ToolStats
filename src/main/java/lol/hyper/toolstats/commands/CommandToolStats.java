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
import lol.hyper.toolstats.tools.ItemChecker;
import lol.hyper.toolstats.tools.UUIDDataType;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
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
            audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            audiences.sender(sender).sendMessage(Component.text("ToolStats version " + toolStats.getDescription().getVersion() + ". Created by hyperdefined.").color(NamedTextColor.GREEN));
            return true;
        }
        switch (args[0]) {
            case "reload": {
                if (sender.hasPermission("toolstats.reload")) {
                    toolStats.loadConfig();
                    audiences.sender(sender).sendMessage(Component.text("Configuration reloaded!").color(NamedTextColor.GREEN));
                } else {
                    audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
                }
                return true;
            }
            case "reset": {
                if (!sender.hasPermission("toolstats.reset")) {
                    audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
                    return true;
                }
                if (sender instanceof ConsoleCommandSender) {
                    audiences.sender(sender).sendMessage(Component.text("You must be a player for this command.").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
                    if (!sender.hasPermission("toolstats.reset.confirm")) {
                        audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
                        return true;
                    }
                    Player player = (Player) sender;
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (ItemChecker.isValidItem(heldItem.getType())) {
                        audiences.sender(sender).sendMessage(Component.text("You must hold a valid item.").color(NamedTextColor.RED));
                        return true;
                    }
                    fixItemLore(heldItem, player);
                    audiences.sender(sender).sendMessage(Component.text("The lore was reset!").color(NamedTextColor.GREEN));
                    return true;
                }
                audiences.sender(sender).sendMessage(Component.text("This will remove ALL current lore from the held item and replace it with the correct lore.").color(NamedTextColor.GREEN));
                audiences.sender(sender).sendMessage(Component.text("The item owner will be who ever is currently running this command.").color(NamedTextColor.GREEN));
                audiences.sender(sender).sendMessage(Component.text("Only use this if the tags on the tool are incorrect.").color(NamedTextColor.GREEN));
                audiences.sender(sender).sendMessage(Component.text("Type /toolstats reset confirm to confirm this.").color(NamedTextColor.GREEN));
                return true;
            }
            default: {
                audiences.sender(sender).sendMessage(Component.text("Invalid sub-command.").color(NamedTextColor.RED));
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

        String caughtByLore = toolStats.getLoreFromConfig("fished.caught-by", false);
        String lootedByLore = toolStats.getLoreFromConfig("looted.found-by", false);
        String tradedByLore = toolStats.getLoreFromConfig("traded.traded-by", false);

        // make sure the config messages are not null
        if (caughtByLore == null || lootedByLore == null || tradedByLore == null) {
            return;
        }

        // determine how the item was originally created
        // this doesn't get saved, so we just rely on the lore
        // if there isn't a tag, default to crafted
        String type = "DEFAULT";
        if (finalMeta.hasLore()) {
            if (finalMeta.getLore() != null) {
                for (String line : finalMeta.getLore()) {
                    if (line.contains(caughtByLore)) {
                        type = "CAUGHT";
                    }
                    if (line.contains(lootedByLore)) {
                        type = "LOOTED";
                    }
                    if (line.contains(tradedByLore)) {
                        type = "TRADED";
                    }
                }
            }
        }

        // hard code elytras
        if (finalItem.getType() == Material.ELYTRA) {
            if (toolStats.config.getBoolean("enabled.elytra-tag")) {
                lore.add(toolStats.getLoreFromConfig("looted.found-by", true).replace("{player}", player.getName()));
                if (container.has(toolStats.timeCreated, PersistentDataType.LONG)) {
                    Long time = container.get(toolStats.timeCreated, PersistentDataType.LONG);
                    if (time != null) {
                        lore.add(toolStats.getLoreFromConfig("looted.found-on", true).replace("{date}", toolStats.numberFormat.formatDate(new Date(time))));
                    }
                }
                finalMeta.setLore(lore);
                finalItem.setItemMeta(finalMeta);
                int slot = player.getInventory().getHeldItemSlot();
                player.getInventory().setItem(slot, finalItem);
                return;
            }
        }

        if (toolStats.checkConfig(original, "created-by")) {
            if (container.has(toolStats.genericOwner, new UUIDDataType())) {
                container.set(toolStats.genericOwner, new UUIDDataType(), player.getUniqueId());
                // show how the item was created based on the previous lore
                switch (type) {
                    case "DEFAULT": {
                        lore.add(toolStats.getLoreFromConfig("created.created-by", true).replace("{player}", player.getName()));
                        break;
                    }
                    case "CAUGHT": {
                        lore.add(toolStats.getLoreFromConfig("fished.caught-by", true).replace("{player}", player.getName()));
                        break;
                    }
                    case "LOOTED": {
                        lore.add(toolStats.getLoreFromConfig("looted.found-by", true).replace("{player}", player.getName()));
                        break;
                    }
                    case "TRADED": {
                        lore.add(toolStats.getLoreFromConfig("traded.traded-by", true).replace("{player}", player.getName()));
                        break;
                    }
                }
            }
        }
        if (toolStats.checkConfig(original, "created-date")) {
            if (container.has(toolStats.timeCreated, PersistentDataType.LONG)) {
                Long time = container.get(toolStats.timeCreated, PersistentDataType.LONG);
                if (time != null) {
                    // show how when the item was created based on the previous lore
                    switch (type) {
                        case "DEFAULT": {
                            lore.add(toolStats.getLoreFromConfig("created.created-on", true).replace("{date}", toolStats.numberFormat.formatDate(new Date(time))));
                            break;
                        }
                        case "CAUGHT": {
                            lore.add(toolStats.getLoreFromConfig("fished.caught-on", true).replace("{date}", toolStats.numberFormat.formatDate(new Date(time))));
                            break;
                        }
                        case "LOOTED": {
                            lore.add(toolStats.getLoreFromConfig("looted.found-on", true).replace("{date}", toolStats.numberFormat.formatDate(new Date(time))));
                            break;
                        }
                        case "TRADED": {
                            lore.add(toolStats.getLoreFromConfig("traded.traded-on", true).replace("{date}", toolStats.numberFormat.formatDate(new Date(time))));
                            break;
                        }
                    }
                }
            }
        }
        if (toolStats.checkConfig(original, "player-kills")) {
            if (container.has(toolStats.swordPlayerKills, PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.swordPlayerKills, PersistentDataType.INTEGER);
                if (kills != null) {
                    lore.add(toolStats.getLoreFromConfig("kills.player", true).replace("{kills}", toolStats.numberFormat.formatInt(kills)));
                }
            }
        }
        if (toolStats.checkConfig(original, "mob-kills")) {
            if (container.has(toolStats.swordMobKills, PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.swordMobKills, PersistentDataType.INTEGER);
                if (kills != null) {
                    lore.add(toolStats.getLoreFromConfig("kills.mob", true).replace("{kills}", toolStats.numberFormat.formatInt(kills)));
                }
            }
        }
        if (toolStats.checkConfig(original, "blocks-mined")) {
            if (container.has(toolStats.genericMined, PersistentDataType.INTEGER)) {
                Integer blocksMined = container.get(toolStats.genericMined, PersistentDataType.INTEGER);
                if (blocksMined != null) {
                    lore.add(toolStats.getLoreFromConfig("blocks-mined", true).replace("{blocks}", toolStats.numberFormat.formatInt(blocksMined)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.fish-caught")) {
            if (container.has(toolStats.fishingRodCaught, PersistentDataType.INTEGER)) {
                Integer fish = container.get(toolStats.fishingRodCaught, PersistentDataType.INTEGER);
                if (fish != null) {
                    lore.add(toolStats.getLoreFromConfig("fished.fish-caught", true).replace("{fish}", toolStats.numberFormat.formatInt(fish)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.sheep-sheared")) {
            if (container.has(toolStats.shearsSheared, PersistentDataType.INTEGER)) {
                Integer sheep = container.get(toolStats.shearsSheared, PersistentDataType.INTEGER);
                if (sheep != null) {
                    lore.add(toolStats.getLoreFromConfig("sheep-sheared", true).replace("{sheep}", toolStats.numberFormat.formatInt(sheep)));
                }
            }
        }
        if (toolStats.config.getBoolean("enabled.armor-damage")) {
            if (container.has(toolStats.armorDamage, PersistentDataType.DOUBLE)) {
                Double damage = container.get(toolStats.armorDamage, PersistentDataType.DOUBLE);
                if (damage != null) {
                    lore.add(toolStats.getLoreFromConfig("damage-taken", true).replace("{damage}", toolStats.numberFormat.formatDouble(damage)));
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
