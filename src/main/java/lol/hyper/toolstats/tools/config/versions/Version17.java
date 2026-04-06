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
import java.util.List;

public class Version17 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 16 to 17.
     *
     * @param toolStats ToolStats instance.
     */
    public Version17(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-16.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config-16.yml!", exception);
        }

        toolStats.logger.info("Updating config.yml to version 17.");
        toolStats.config.set("config-version", 17);

        toolStats.logger.info("Adding new token to config: logs-stripped");
        toolStats.config.set("tokens.data.logs-stripped.title", "&7ToolStats: &8Logs Stripped Token");
        toolStats.config.set("tokens.data.logs-stripped.lore", List.of(
                "&8Combine with an axe in an anvil to track logs stripped.",
                "&8Uses &7{levels} &8level."
        ));
        toolStats.config.set("tokens.data.logs-stripped.levels", 1);
        toolStats.config.set("tokens.data.logs-stripped.material", "PAPER");

        toolStats.config.set("tokens.data.logs-stripped.custom-model-data.enabled", false);
        toolStats.config.set("tokens.data.logs-stripped.custom-model-data.type", "float");
        toolStats.config.set("tokens.data.logs-stripped.custom-model-data.value", 1001);

        toolStats.logger.info("Adding enabled.logs-stripped");
        toolStats.config.set("enabled.logs-stripped", true);

        toolStats.logger.info("Adding messages.logs-stripped");
        toolStats.config.set("messages.logs-stripped", "&7Logs stripped: &8{logs}");


        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config.yml!", exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 17. A copy of version 16 has been saved as config-16.yml");
    }
}
