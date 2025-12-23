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

public class Version14 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 13 to 14.
     *
     * @param toolStats ToolStats instance.
     */
    public Version14(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-13.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config-13.yml!", exception);
        }

        toolStats.logger.info("Updating config.yml to version 14.");
        toolStats.config.set("config-version", 14);

        // add spear to sections to be a toggle
        toolStats.config.set("enabled.crafted-by.spear", true);
        toolStats.logger.info("Adding enabled.crafted-by.spear");

        toolStats.config.set("enabled.crafted-on.spear", true);
        toolStats.logger.info("Adding enabled.crafted-on.spear");

        toolStats.config.set("enabled.looted-by.spear", true);
        toolStats.logger.info("Adding enabled.looted-by.spear");

        toolStats.config.set("enabled.looted-on.spear", true);
        toolStats.logger.info("Adding enabled.looted-on.spear");

        toolStats.config.set("enabled.damage-done.spear", true);
        toolStats.logger.info("Adding enabled.damage-done.spear");

        toolStats.config.set("enabled.player-kills.spear", true);
        toolStats.logger.info("Adding enabled.player-kills.spear");

        toolStats.config.set("enabled.mob-kills.spear", true);
        toolStats.logger.info("Adding enabled.mob-kills.spear");

        toolStats.config.set("enabled.spawned-in-by.spear", true);
        toolStats.logger.info("Adding enabled.spawned-in-by.spear");

        toolStats.config.set("enabled.spawned-in-on.spear", true);
        toolStats.logger.info("Adding enabled.spawned-in-on.spear");

        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config.yml!", exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 14. A copy of version 13 has been saved as config-13.yml");
    }
}
