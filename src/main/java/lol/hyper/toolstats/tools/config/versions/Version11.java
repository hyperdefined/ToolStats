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

public class Version11 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 9 to 10.
     *
     * @param toolStats ToolStats instance.
     */
    public Version11(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-10.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config-10.yml!");
            throw new RuntimeException(exception);
        }

        // we make this super verbose so that admins can see what's being added
        toolStats.logger.info("Updating config.yml to version 11.");
        toolStats.config.set("config-version", 11);

        toolStats.logger.info("Updating entry for messages.flight-time");
        toolStats.config.set("messages.flight-time", "&7Flight time: &8{years}y {months}m {days}d {hours}h {minutes}m {seconds}s");
        
        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config.yml!");
            throw new RuntimeException(exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 11. A copy of version 10 has been saved as config-10.yml");
    }
}
