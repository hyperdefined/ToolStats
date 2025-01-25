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

public class Version9 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 8 to 9.
     *
     * @param toolStats ToolStats instance.
     */
    public Version9(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-8.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config-8.yml!");
            throw new RuntimeException(exception);
        }

        toolStats.logger.info("Updating config.yml to version 9.");
        toolStats.config.set("config-version", 9);

        toolStats.logger.info("Adding new tokens configuration!");
        // false by default so it doesn't break servers on updating
        toolStats.config.set("tokens.enabled", false);
        toolStats.config.set("tokens.craft-tokens", true);

        List<String> tokenComments = new ArrayList<>();
        tokenComments.add("Use token system for tracking stats.");
        tokenComments.add("See https://github.com/hyperdefined/ToolStats/wiki/Token-System");
        toolStats.config.setComments("tokens", tokenComments);

        addToken("player-kills", "&7ToolStats: &8Player Kills Token", "&8Combine with a melee or ranged weapon in an anvil to track player kills.");
        addToken("mob-kills", "&7ToolStats: &8Mob Kills Token", "&8Combine with a melee or ranged weapon in an anvil to track mob kills.");
        addToken("blocks-mined", "&7ToolStats: &8Blocks Mined Token", "&8Combine with a pickaxe, axe, shovel, or shears in an anvil to track blocks mined.");
        addToken("crops-mined", "&7ToolStats: &8Crops Mined Token", "&8Combine with a hoe in an anvil to track crops broken.");
        addToken("fish-caught", "&7ToolStats: &8Fish Caught Token", "&8Combine with a fishing rod in an anvil to track fish caught.");
        addToken("sheep-sheared", "&7ToolStats: &8Sheep Sheared Token", "&8Combine with shears in an anvil to track sheep sheared.");
        addToken("damage-taken", "&7ToolStats: &8Damage Taken Token", "&8Combine with an armor piece in an anvil to track damage taken.");
        addToken("arrows-shot", "&7ToolStats: &8Arrows Shot Token", "&8Combine with a bow or crossbow in an anvil to track arrows shot.");
        addToken("flight-time", "&7ToolStats: &8Flight Time Token", "&8Combine with an elytra in an anvil to track flight time.");

        // save the config and reload it
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.severe("Unable to save config.yml!");
            throw new RuntimeException(exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 9. A copy of version 8 has been saved as config-8.yml");
    }

    /**
     * Add a given token to the config. Made this since I was lazy.
     *
     * @param tokenType The token type to add.
     * @param title     The title for the item.
     * @param lore      The lore of the item.
     */
    private void addToken(String tokenType, String title, String lore) {
        toolStats.logger.info("Adding token type configuration for " + tokenType);
        toolStats.config.set("tokens.data." + tokenType + ".title", title);
        toolStats.config.set("tokens.data." + tokenType + ".lore", lore);
        toolStats.config.set("tokens.data." + tokenType + ".levels", 1);
    }
}
