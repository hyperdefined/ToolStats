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

public class Version11 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 10 to 11.
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

        // add new tokens
        toolStats.logger.info("Adding tokens.data.damage-done.title to config.yml.");
        toolStats.config.set("tokens.data.damage-done.title", "&7ToolStats: &8Damage Done Token");
        List<String> damageDoneLore = new ArrayList<>();
        damageDoneLore.add("&8Combine with a melee or ranged weapon in an anvil to track damage done.");
        toolStats.config.set("tokens.data.damage-done.lore", damageDoneLore);
        toolStats.logger.info("Adding tokens.data.damage-done.lore to config.yml.");
        toolStats.config.set("tokens.data.damage-done.levels", 1);
        toolStats.logger.info("Adding tokens.data.damage-done.levels to config.yml.");

        toolStats.logger.info("Adding tokens.data.remove.title to config.yml.");
        toolStats.config.set("tokens.data.remove.title", "&7ToolStats: &8Remove Token");
        List<String> removeLore = new ArrayList<>();
        removeLore.add("&8Combine in an anvil with to REMOVE ALL stats and tokens for this item.");
        toolStats.config.set("tokens.data.remove.lore", removeLore);
        toolStats.logger.info("Adding tokens.data.remove.lore to config.yml.");
        toolStats.config.set("tokens.data.remove.levels", 1);
        toolStats.logger.info("Adding tokens.data.remove.levels to config.yml.");

        toolStats.logger.info("Adding messages.damage-done to config.yml.");
        toolStats.config.set("messages.damage-done", "&7Damage done: &8{damage}");

        toolStats.config.set("enabled.damage-done.sword", true);
        toolStats.config.set("enabled.damage-done.axe", true);
        toolStats.config.set("enabled.damage-done.trident", true);
        toolStats.config.set("enabled.damage-done.bow", true);
        toolStats.config.set("enabled.damage-done.mace", true);
        toolStats.logger.info("Adding enabled.damage-done.sword to config.yml");
        toolStats.logger.info("Adding enabled.damage-done.axe to config.yml");
        toolStats.logger.info("Adding enabled.damage-done.trident to config.yml");
        toolStats.logger.info("Adding enabled.damage-done.bow to config.yml");
        toolStats.logger.info("Adding enabled.damage-done.mace to config.yml");

        toolStats.logger.info("Changing messages.flight-time to new format");
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