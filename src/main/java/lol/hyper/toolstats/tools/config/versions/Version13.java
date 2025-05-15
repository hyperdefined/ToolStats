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
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;

public class Version13 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 12 to 13.
     *
     * @param toolStats ToolStats instance.
     */
    public Version13(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-12.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config-12.yml!");
            throw new RuntimeException(exception);
        }

        toolStats.logger.info("Updating config.yml to version 13.");
        toolStats.config.set("config-version", 13);

        for (String key : toolStats.config.getConfigurationSection("tokens.data").getKeys(false)) {
            toolStats.logger.info("Adding tokens.data." + key + ".material");
            toolStats.config.set("tokens.data." + key + ".material", "PAPER");
            toolStats.logger.info("Adding tokens.data." + key + ".custom-model-data.enabled");
            toolStats.config.set("tokens.data." + key + ".custom-model-data.enabled", false);
            toolStats.logger.info("Adding tokens.data." + key + ".custom-model-data.type");
            toolStats.config.set("tokens.data." + key + ".custom-model-data.type", "float");
            toolStats.logger.info("Adding tokens.data." + key + ".custom-model-data.value");
            toolStats.config.set("tokens.data." + key + ".custom-model-data.value", 1001);
        }

        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config.yml!");
            throw new RuntimeException(exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 13. A copy of version 12 has been saved as config-12.yml");
    }
}
