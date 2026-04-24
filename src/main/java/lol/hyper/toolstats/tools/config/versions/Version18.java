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

public class Version18 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 17 to 18.
     *
     * @param toolStats ToolStats instance.
     */
    public Version18(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-17.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config-17.yml!", exception);
        }

        toolStats.logger.info("Updating config.yml to version 18.");
        toolStats.config.set("config-version", 18);

        for (String key : toolStats.config.getConfigurationSection("tokens.data").getKeys(false)) {
            toolStats.logger.info("Adding tokens.data.{}.item-model.enabled", key);
            toolStats.config.set("tokens.data." + key + ".item-model.enabled", false);
            toolStats.logger.info("Adding tokens.data.{}.item-model.value", key);
            toolStats.config.set("tokens.data." + key + ".item-model.value", "minecraft:paper");
        }

        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config.yml!", exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 18. A copy of version 17 has been saved as config-17.yml");
    }
}
