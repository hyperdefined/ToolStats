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

import lol.hyper.toolstats.ToolStats;

import java.io.File;
import java.io.IOException;

public class Version10 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 9 to 10.
     *
     * @param toolStats ToolStats instance.
     */
    public Version10(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-9.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config-9.yml!", exception);
        }

        // we make this super verbose so that admins can see what's being added
        toolStats.logger.info("Updating config.yml to version 10.");
        toolStats.config.set("config-version", 10);

        // Add missing values I forgot...
        toolStats.logger.info("Adding entry for enabled.created-by.fishing-rod");
        toolStats.config.set("enabled.created-by.fishing-rod", true);
        toolStats.logger.info("Adding entry for enabled.created-date.fishing-rod");
        toolStats.config.set("enabled.created-date.fishing-rod", true);
        toolStats.logger.info("Adding entry for enabled.fished-tag.fishing-rod");
        toolStats.config.set("enabled.fished-tag.fishing-rod", true);
        toolStats.logger.info("Adding entry for enabled.looted-tag.fishing-rod");
        toolStats.config.set("enabled.looted-tag.fishing-rod", true);
        toolStats.logger.info("Adding entry for enabled.traded-tag.fishing-rod");
        toolStats.config.set("enabled.traded-tag.fishing-rod", true);
        toolStats.logger.info("Adding entry for enabled.spawned-in.fishing-rod");
        toolStats.config.set("enabled.spawned-in.fishing-rod", true);

        toolStats.logger.info("Adding entry for enabled.crops-harvested");
        toolStats.config.set("enabled.crops-harvested", true);

        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config.yml!", exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 10. A copy of version 9 has been saved as config-9.yml");
    }
}
