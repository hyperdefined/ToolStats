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
import java.util.ArrayList;
import java.util.List;

public class Version8 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 7 to 8.
     *
     * @param toolStats ToolStats instance.
     */
    public Version8(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-7.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config-7.yml!", exception);
        }

        // we make this super verbose so that admins can see what's being added
        toolStats.logger.info("Updating config.yml to version 8.");
        toolStats.config.set("config-version", 8);

        // Add example to setting mob names
        toolStats.logger.info("Adding example for messages.mob.ZOMBIE");
        toolStats.config.set("messages.mob.ZOMBIE", "Zombie");

        // Add mace to enabled sections
        toolStats.logger.info("Adding entry for enabled.created-by.mace");
        toolStats.config.set("enabled.created-by.mace", true);

        toolStats.logger.info("Adding entry for enabled.created-date.mace");
        toolStats.config.set("enabled.created-date.mace", true);

        toolStats.logger.info("Adding entry for enabled.player-kills.mace");
        toolStats.config.set("enabled.player-kills.mace", true);

        toolStats.logger.info("Adding entry for enabled.mob-kills.mace");
        toolStats.config.set("enabled.mob-kills.mace", true);

        List<String> mobComments = new ArrayList<>();
        mobComments.add("Set display name for mobs. See: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html");
        toolStats.config.setComments("messages.mob", mobComments);

        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config.yml!", exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 8. A copy of version 7 has been saved as config-7.yml");
    }
}
