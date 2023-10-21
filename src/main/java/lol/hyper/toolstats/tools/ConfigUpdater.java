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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigUpdater {

    private final ToolStats toolStats;

    public ConfigUpdater(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    public void updateConfig() throws IOException {
        // get a copy of the current config
        int version = toolStats.config.getInt("config-version");

        if (version == 5) {
            // save the old config first
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-5.yml");

            // we make this super verbose so that admins can see what's being added
            toolStats.logger.info("Updating config.yml to version 6.");
            toolStats.config.set("config-version", 6);
            toolStats.logger.info("Adding enabled.spawned-in.pickaxe to config.yml.");
            toolStats.config.set("enabled.spawned-in.pickaxe", true);
            toolStats.logger.info("Adding enabled.spawned-in.sword to config.yml.");
            toolStats.config.set("enabled.spawned-in.sword", true);
            toolStats.logger.info("Adding enabled.spawned-in.shovel to config.yml.");
            toolStats.config.set("enabled.spawned-in.shovel", true);
            toolStats.logger.info("Adding enabled.spawned-in.axe to config.yml.");
            toolStats.config.set("enabled.spawned-in.axe", true);
            toolStats.logger.info("Adding enabled.spawned-in.hoe to config.yml.");
            toolStats.config.set("enabled.spawned-in.hoe", true);
            toolStats.logger.info("Adding enabled.spawned-in.fishing-rod to config.yml.");
            toolStats.config.set("enabled.spawned-in.shears", true);
            toolStats.logger.info("Adding enabled.spawned-in.shears to config.yml.");
            toolStats.config.set("enabled.spawned-in.bow", true);
            toolStats.logger.info("Adding enabled.spawned-in.bow to config.yml.");
            toolStats.config.set("enabled.spawned-in.armor", true);
            toolStats.logger.info("Adding enabled.spawned-in.armor to config.yml.");
            toolStats.config.set("messages.spawned-in.spawned-by", "&7Spawned in by: &8{player}");
            toolStats.logger.info("Adding messages.spawned-in.spawned-by to config.yml.");
            toolStats.config.set("messages.spawned-in.spawned-on", "&7Spawned on: &8{date}");
            toolStats.logger.info("Adding messages.spawned-in.spawned-on to config.yml.");
            toolStats.config.set("generate-hash-for-items", true);
            toolStats.logger.info("Adding generate-hash-for-items to config.yml.");

            List<String> hashComments = new ArrayList<>();
            hashComments.add("When any tool is created, it will generate a hash for the item.");
            hashComments.add("This hash is not on the item lore, only stored in the NBT data.");
            hashComments.add("This has no use currently, but can be used for future features for dupe detection.");
            toolStats.config.setComments("generate-hash-for-items", hashComments);

            toolStats.config.setComments("enabled.spawned-in", Collections.singletonList("Will show \"Spawned in by <player>\""));

            // save the config and reload it
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
            toolStats.loadConfig();
            toolStats.logger.info("Config has been updated to version 6. A copy of version 5 has been saved as config-5.yml");
        }
    }
}
