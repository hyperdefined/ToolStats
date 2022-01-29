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
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommandToolStats implements CommandExecutor {

    private final ToolStats toolStats;

    public CommandToolStats(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "ToolStats version " + toolStats.getDescription().getVersion() + ". Created by hyperdefined.");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.isOp() || sender.hasPermission("toolstats.reload")) {
                    toolStats.loadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid sub-command.");
            }
        }
        return true;
    }
}
