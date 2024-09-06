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

public class Version7 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 6 to 7.
     *
     * @param toolStats ToolStats instance.
     */
    public Version7(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-6.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config-5.yml!");
            throw new RuntimeException(exception);
        }

        // we make this super verbose so that admins can see what's being added
        toolStats.logger.info("Updating config.yml to version 7.");
        toolStats.config.set("config-version", 7);

        toolStats.logger.info("Adding messages.flight-time to config.yml.");
        toolStats.config.set("messages.flight-time", "&7Flight time: &8{time}");

        toolStats.logger.info("Adding enabled.flight-time to config.yml.");
        toolStats.config.set("enabled.flight-time", true);

        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config.yml!");
            throw new RuntimeException(exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 7. A copy of version 5 has been saved as config-6.yml");
    }
}
