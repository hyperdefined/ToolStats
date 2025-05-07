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

package lol.hyper.toolstats.tools.config.versions;

import it.unimi.dsi.fastutil.Pair;
import lol.hyper.toolstats.ToolStats;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Version12 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 11 to 12.
     *
     * @param toolStats ToolStats instance.
     */
    public Version12(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-11.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config-11.yml!");
            throw new RuntimeException(exception);
        }

        toolStats.logger.info("Updating config.yml to version 12.");
        toolStats.config.set("config-version", 12);

        transfer("enabled.created-by", "enabled.crafted-by");
        transfer("enabled.created-date", "enabled.crafted-on");

        transfer("enabled.fished-tag", "enabled.fished-by");
        transfer("enabled.fished-tag", "enabled.fished-on");

        transfer("enabled.looted-tag", "enabled.looted-by");
        transfer("enabled.looted-tag", "enabled.looted-on");

        transfer("enabled.traded-tag", "enabled.traded-by");
        transfer("enabled.traded-tag", "enabled.traded-on");

        transfer("enabled.spawned-in", "enabled.spawned-in-by");
        transfer("enabled.spawned-in", "enabled.spawned-in-on");

        transfer("messages.created", "messages.crafted");

        toolStats.config.set("enabled.created-by", null);
        toolStats.config.set("enabled.created-date", null);
        toolStats.config.set("enabled.fished-tag", null);
        toolStats.config.set("enabled.looted-tag", null);
        toolStats.config.set("enabled.traded-tag", null);
        toolStats.config.set("enabled.spawned-in", null);


        // rename crafted to crafted here
        // copy the old ones first
        String craftedByMessage = toolStats.config.getString("messages.created.created-by");
        String craftedOnMessage = toolStats.config.getString("messages.created.created-on");

        toolStats.config.set("messages.created", null);
        toolStats.config.set("messages.crafted.created-by", null);
        toolStats.config.set("messages.crafted.created-on", null);

        toolStats.config.set("messages.crafted.crafted-by", craftedByMessage);
        toolStats.config.set("messages.crafted.crafted-on", craftedOnMessage);

        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config.yml!");
            throw new RuntimeException(exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 12. A copy of version 11 has been saved as config-11.yml");
    }

    private void transfer(String oldSection, String newSection) {
        toolStats.logger.info("Moving " + oldSection + " to " + newSection);
        ConfigurationSection old = toolStats.config.getConfigurationSection(oldSection);
        toolStats.config.set(newSection, old);
    }
}
