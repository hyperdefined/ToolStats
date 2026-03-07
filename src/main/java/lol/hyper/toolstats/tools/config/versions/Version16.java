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

public class Version16 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 15 to 16.
     *
     * @param toolStats ToolStats instance.
     */
    public Version16(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-15.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config-15.yml!", exception);
        }

        // we make this super verbose so that admins can see what's being added
        toolStats.logger.info("Updating config.yml to version 16.");
        toolStats.config.set("config-version", 16);

        toolStats.logger.info("Adding enabled.crafted-on.shield to config.yml.");
        toolStats.config.set("enabled.crafted-on.shield", true);
        toolStats.logger.info("Adding enabled.crafted-by.shield to config.yml.");
        toolStats.config.set("enabled.crafted-by.shield", true);

        toolStats.logger.info("Adding enabled.traded-on.shield to config.yml.");
        toolStats.config.set("enabled.traded-on.shield", true);
        toolStats.logger.info("Adding enabled.traded-by.shield to config.yml.");
        toolStats.config.set("enabled.traded-by.shield", true);

        toolStats.logger.info("Adding enabled.looted-on.shield to config.yml.");
        toolStats.config.set("enabled.looted-on.shield", true);
        toolStats.logger.info("Adding enabled.looted-by.shield to config.yml.");
        toolStats.config.set("enabled.looted-by.shield", true);

        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config.yml!", exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 16. A copy of version 6 has been saved as config-15.yml");
    }
}
