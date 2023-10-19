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
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigUpdater {

    private final ToolStats toolStats;

    public ConfigUpdater(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    public void updateConfig() {
        // get a copy of the current config
        FileConfiguration newConfig = toolStats.config;
        int version = newConfig.getInt("config-version");

        if (version == 5) {
            newConfig.set("config-version", 6);
            newConfig.set("enabled.spawned-in.pickaxe", true);
            newConfig.set("enabled.spawned-in.sword", true);
            newConfig.set("enabled.spawned-in.shovel", true);
            newConfig.set("enabled.spawned-in.axe", true);
            newConfig.set("enabled.spawned-in.hoe", true);
            newConfig.set("enabled.spawned-in.shears", true);
            newConfig.set("enabled.spawned-in.bow", true);
            newConfig.set("enabled.spawned-in.armor", true);
            newConfig.set("messages.spawned.spawned-by", "&7Spawned in by: &8{player}");
            newConfig.set("messages.spawned.spawned-on", "&7Spawned on: &8{date}");
            newConfig.set("generate-hash-for-items", "true");

            try {
                toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-5.yml");
                newConfig.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
            } catch (IOException exception) {
                toolStats.logger.severe("Unable to save config.yml");
                throw new RuntimeException(exception);
            }
            toolStats.loadConfig();
        }
    }
}
