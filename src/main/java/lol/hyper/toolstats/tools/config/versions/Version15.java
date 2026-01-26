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
import java.util.Arrays;
import java.util.List;

public class Version15 {

    private final ToolStats toolStats;

    /**
     * Used for updating from version 14 to 15.
     *
     * @param toolStats ToolStats instance.
     */
    public Version15(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    /**
     * Perform the config update.
     */
    public void update() {
        // save the old config first
        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config-14.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config-14.yml!", exception);
        }

        toolStats.logger.info("Updating config.yml to version 15.");
        toolStats.config.set("config-version", 15);

        // wither kills token
        toolStats.logger.info("Adding new token to config: wither-kills");
        toolStats.config.set("tokens.data.wither-kills.title", "&7ToolStats: &8Wither Kills Token");
        toolStats.config.set("tokens.data.wither-kills.lore", List.of(
                "&8Combine with a melee or ranged weapon in an anvil to track wither kills.",
                "&8Uses &7{levels} &8level."
        ));
        toolStats.config.set("tokens.data.wither-kills.levels", 1);
        toolStats.config.set("tokens.data.wither-kills.material", "PAPER");

        toolStats.config.set("tokens.data.wither-kills.custom-model-data.enabled", false);
        toolStats.config.set("tokens.data.wither-kills.custom-model-data.type", "float");
        toolStats.config.set("tokens.data.wither-kills.custom-model-data.value", 1001);

        // ender dragon kills token
        toolStats.logger.info("Adding new token to config: enderdragon-kills");
        toolStats.config.set("tokens.data.enderdragon-kills.title", "&7ToolStats: &8Ender Dragon Kills Token");
        toolStats.config.set("tokens.data.enderdragon-kills.lore", List.of(
                "&8Combine with a melee or ranged weapon in an anvil to track Ender Dragon kills.",
                "&8Uses &7{levels} &8level."
        ));
        toolStats.config.set("tokens.data.enderdragon-kills.levels", 1);
        toolStats.config.set("tokens.data.enderdragon-kills.material", "PAPER");

        toolStats.config.set("tokens.data.enderdragon-kills.custom-model-data.enabled", false);
        toolStats.config.set("tokens.data.enderdragon-kills.custom-model-data.type", "float");
        toolStats.config.set("tokens.data.enderdragon-kills.custom-model-data.value", 1001);

        // critical strikes token
        toolStats.logger.info("Adding new token to config: critical-strikes");
        toolStats.config.set("tokens.data.critical-strikes.title", "&7ToolStats: &8Critical Strikes Token");
        toolStats.config.set("tokens.data.critical-strikes.lore", List.of(
                "&8Combine with a melee or ranged weapon in an anvil to track critical strikes.",
                "&8Uses &7{levels} &8level."
        ));
        toolStats.config.set("tokens.data.critical-strikes.levels", 1);
        toolStats.config.set("tokens.data.critical-strikes.material", "PAPER");

        toolStats.config.set("tokens.data.critical-strikes.custom-model-data.enabled", false);
        toolStats.config.set("tokens.data.critical-strikes.custom-model-data.type", "float");
        toolStats.config.set("tokens.data.critical-strikes.custom-model-data.value", 1001);

        // trident throws token
        toolStats.logger.info("Adding new token to config: trident-throws");
        toolStats.config.set("tokens.data.trident-throws.title", "&7ToolStats: &8Trident Throws Token");
        toolStats.config.set("tokens.data.trident-throws.lore", List.of(
                "&8Combine with a trident in an anvil to track times thrown.",
                "&8Uses &7{levels} &8level."
        ));
        toolStats.config.set("tokens.data.trident-throws.levels", 1);
        toolStats.config.set("tokens.data.trident-throws.material", "PAPER");

        toolStats.config.set("tokens.data.trident-throws.custom-model-data.enabled", false);
        toolStats.config.set("tokens.data.trident-throws.custom-model-data.type", "float");
        toolStats.config.set("tokens.data.trident-throws.custom-model-data.value", 1001);

        // bosses-killed stuff
        toolStats.logger.info("Adding enabled.bosses-killed.wither");
        toolStats.config.set("enabled.bosses-killed.wither", true);
        toolStats.logger.info("enabled.bosses-killed.enderdragon");
        toolStats.config.set("enabled.bosses-killed.enderdragon", true);

        // critical strikes
        toolStats.config.set("enabled.critical-strikes", true);
        toolStats.logger.info("Adding enabled.critical-strikes");

        //trident throws
        toolStats.config.set("enabled.trident-throws", true);
        toolStats.logger.info("Adding enabled.trident-throws");

        // default for new stats
        toolStats.logger.info("Adding new default messages");
        toolStats.config.set("messages.bosses-killed.wither", "&7Withers killed: &8{kills}");
        toolStats.config.set("messages.bosses-killed.enderdragon", "&7Ender Dragons killed: &8{kills}");
        toolStats.config.set("messages.critical-strikes", "&7Critical strikes: &8{strikes}");
        toolStats.config.set("messages.trident-throws", "&7Times thrown: &8{times}");

        // blacklist feature
        toolStats.logger.info("Adding new blacklist-worlds feature");
        List<String> worlds = Arrays.asList("world_1", "world_2");
        toolStats.config.set("blacklist-worlds", worlds);

        try {
            toolStats.config.save("plugins" + File.separator + "ToolStats" + File.separator + "config.yml");
        } catch (IOException exception) {
            toolStats.logger.error("Unable to save config.yml!", exception);
        }
        toolStats.loadConfig();
        toolStats.logger.info("Config has been updated to version 15. A copy of version 14 has been saved as config-14.yml");
    }
}
