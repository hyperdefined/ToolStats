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
import lol.hyper.toolstats.UUIDDataType;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommandToolStats implements TabExecutor {

    private final ToolStats toolStats;

    public CommandToolStats(ToolStats toolStats) {
        this.toolStats = toolStats;
    }
    private final SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "ToolStats version " + toolStats.getDescription().getVersion() + ". Created by hyperdefined.");
        }
        switch (args[0]) {
            case "reload": {
                if (sender.isOp() || sender.hasPermission("toolstats.reload")) {
                    toolStats.loadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                }
                return true;
            }
            case "reset": {
                if (args.length == 2 && args[1].equalsIgnoreCase("confirm")) {
                    Player player = (Player) sender;
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (heldItem.getType() == Material.AIR) {
                        sender.sendMessage(ChatColor.RED + "You must hold an item!");
                        return true;
                    }
                    fixItemLore(heldItem, player);
                    sender.sendMessage(ChatColor.GREEN + "The lore was reset!");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "This will remove ALL current lore from the held item and replace it with the correct lore.");
                sender.sendMessage(ChatColor.GREEN + "The item owner will be who ever is currently running this command.");
                sender.sendMessage(ChatColor.GREEN + "Only use this if the tags on the tool are incorrect.");
                sender.sendMessage(ChatColor.GREEN + "Type /toolstats reset confirm to confirm this.");
                return true;
            }
            default: {
                sender.sendMessage(ChatColor.RED + "Invalid sub-command.");
            }
        }
        return true;
    }

    private ItemStack fixItemLore(ItemStack original, Player player) {
        ItemStack finalItem = original.clone();
        ItemMeta finalMeta = finalItem.getItemMeta();
        if (finalMeta == null) {
            return null;
        }
        PersistentDataContainer container = finalMeta.getPersistentDataContainer();
        List<String> lore = new ArrayList<>();

        String caughtByLore = toolStats.getLoreFromConfig("fished.caught-by", false);
        String lootedByLore = toolStats.getLoreFromConfig("looted.found-by", false);
        String tradedByLore = toolStats.getLoreFromConfig("traded.traded-by", false);

        if (caughtByLore == null || lootedByLore == null || tradedByLore == null) {
            return null;
        }

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
        if (toolStats.checkConfig(original, "created-by")) {
            if (container.has(toolStats.genericOwner, new UUIDDataType())) {
                container.set(toolStats.genericOwner, new UUIDDataType(), player.getUniqueId());
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
                        lore.add(toolStats.getLoreFromConfig("looted.looted-by", true).replace("{player}", player.getName()));
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
                if (time == null) {
                    return null;
                }
                switch (type) {
                    case "DEFAULT": {
                        lore.add(toolStats.getLoreFromConfig("created.created-by", true).replace("{date}", format.format(new Date(time))));
                        break;
                    }
                    case "CAUGHT": {
                        lore.add(toolStats.getLoreFromConfig("fished.caught-on", true).replace("{date}", format.format(new Date(time))));
                        break;
                    }
                    case "LOOTED": {
                        lore.add(toolStats.getLoreFromConfig("looted.looted-on", true).replace("{date}", format.format(new Date(time))));
                        break;
                    }
                    case "TRADED": {
                        lore.add(toolStats.getLoreFromConfig("traded.traded-on", true).replace("{date}", format.format(new Date(time))));
                        break;
                    }
                }
            }
        }
        if (toolStats.checkConfig(original, "player-kills")) {
            if (container.has(toolStats.swordPlayerKills, PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.swordPlayerKills, PersistentDataType.INTEGER);
                if (kills == null) {
                    return null;
                }
                lore.add(toolStats.getLoreFromConfig("kills.player", true).replace("{kills}", Integer.toString(kills)));
            }
        }
        if (toolStats.checkConfig(original, "mob-kills")) {
            if (container.has(toolStats.swordMobKills, PersistentDataType.INTEGER)) {
                Integer kills = container.get(toolStats.swordMobKills, PersistentDataType.INTEGER);
                if (kills == null) {
                    return null;
                }
                lore.add(toolStats.getLoreFromConfig("kills.mob", true).replace("{kills}", Integer.toString(kills)));
            }
        }
        if (toolStats.checkConfig(original, "blocks-mined")) {
            if (container.has(toolStats.genericMined, PersistentDataType.INTEGER)) {
                Integer blocksMined = container.get(toolStats.genericMined, PersistentDataType.INTEGER);
                if (blocksMined == null) {
                    return null;
                }
                lore.add(toolStats.getLoreFromConfig("blocks-mined", true).replace("{blocks}", Integer.toString(blocksMined)));
            }
        }
        if (toolStats.config.getBoolean("enabled.fish-caught")) {
            if (container.has(toolStats.fishingRodCaught, PersistentDataType.INTEGER)) {
                Integer fish = container.get(toolStats.fishingRodCaught, PersistentDataType.INTEGER);
                if (fish == null) {
                    return null;
                }
                lore.add(toolStats.getLoreFromConfig("fished.fish-caught", true).replace("{fish}", Integer.toString(fish)));
            }
        }
        if (toolStats.config.getBoolean("enabled.sheep-sheared")) {
            if (container.has(toolStats.shearsSheared, PersistentDataType.INTEGER)) {
                Integer sheep = container.get(toolStats.shearsSheared, PersistentDataType.INTEGER);
                if (sheep == null) {
                    return null;
                }
                lore.add(toolStats.getLoreFromConfig("sheep-sheared", true).replace("{sheep}", Integer.toString(sheep)));
            }
        }
        if (toolStats.config.getBoolean("enabled.armor-damage")) {
            if (container.has(toolStats.armorDamage, PersistentDataType.INTEGER)) {
                Integer damage = container.get(toolStats.armorDamage, PersistentDataType.INTEGER);
                if (damage == null) {
                    return null;
                }
                lore.add(toolStats.getLoreFromConfig("damage-taken", true).replace("{damage}", Integer.toString(damage)));
            }
        }
        finalMeta.setLore(lore);
        finalItem.setItemMeta(finalMeta);
        return finalItem;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("toolstats.reload")) {
                return Arrays.asList("reset", "reload");
            } else {
                return Collections.singletonList("reset");
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                return Collections.singletonList("confirm");
            }
        }
        return null;
    }
}
