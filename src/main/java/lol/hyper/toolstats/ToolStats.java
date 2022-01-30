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

package lol.hyper.toolstats;

import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.toolstats.commands.CommandToolStats;
import lol.hyper.toolstats.events.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class ToolStats extends JavaPlugin {

    // stores who created an item
    public final NamespacedKey genericOwner = new NamespacedKey(this, "owner");
    // stores when an item was created
    public final NamespacedKey timeCreated = new NamespacedKey(this, "time-created");
    // stores how many player kills by sword
    public final NamespacedKey swordPlayerKills = new NamespacedKey(this, "player-kills");
    // stores how many mob kills by sword
    public final NamespacedKey swordMobKills = new NamespacedKey(this, "mob-kills");
    // stores how blocks mined (used for all tools)
    public final NamespacedKey genericMined = new NamespacedKey(this, "generic-mined");
    // stores how many fish were caught
    public final NamespacedKey fishingRodCaught = new NamespacedKey(this, "fish-caught");
    // stores how many times sheep were sheared
    public final NamespacedKey shearsSheared = new NamespacedKey(this, "sheared");
    // stores how much damage armor has taken
    public final NamespacedKey armorDamage = new NamespacedKey(this, "damage-taken");

    public final Set<NamespacedKey> keys = new HashSet<>();

    public BlocksMined blocksMined;
    public CraftItem craftItem;
    public EntityDeath entityDeath;
    public GenerateLoot generateLoot;
    public EntityDamage mobKill;
    public PlayerFish playerFish;
    public SheepShear sheepShear;
    public VillagerTrade villagerTrade;
    public CommandToolStats commandToolStats;

    public final Logger logger = this.getLogger();
    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public FileConfiguration config;
    public final int CONFIG_VERSION = 2;

    @Override
    public void onEnable() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
            logger.info("Copying default config!");
        }
        loadConfig();
        blocksMined = new BlocksMined(this);
        craftItem = new CraftItem(this);
        entityDeath = new EntityDeath(this);
        generateLoot = new GenerateLoot(this);
        mobKill = new EntityDamage(this);
        playerFish = new PlayerFish(this);
        sheepShear = new SheepShear(this);
        villagerTrade = new VillagerTrade(this);
        commandToolStats = new CommandToolStats(this);

        Bukkit.getServer().getPluginManager().registerEvents(blocksMined, this);
        Bukkit.getServer().getPluginManager().registerEvents(craftItem, this);
        Bukkit.getServer().getPluginManager().registerEvents(entityDeath, this);
        Bukkit.getServer().getPluginManager().registerEvents(generateLoot, this);
        Bukkit.getServer().getPluginManager().registerEvents(mobKill, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerFish, this);
        Bukkit.getServer().getPluginManager().registerEvents(sheepShear, this);
        Bukkit.getServer().getPluginManager().registerEvents(villagerTrade, this);

        this.getCommand("toolstats").setExecutor(commandToolStats);

        new Metrics(this, 14110);

        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);

        keys.add(genericOwner);
        keys.add(timeCreated);
        keys.add(swordPlayerKills);
        keys.add(swordMobKills);
        keys.add(genericMined);
        keys.add(fishingRodCaught);
        keys.add(shearsSheared);
        keys.add(armorDamage);
    }

    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("Your config file is outdated! Please regenerate the config.");
        }
    }

    public void checkForUpdates() {
        GitHubReleaseAPI api;
        try {
            api = new GitHubReleaseAPI("ToolStats", "hyperdefined");
        } catch (IOException e) {
            logger.warning("Unable to check updates!");
            e.printStackTrace();
            return;
        }
        GitHubRelease current = api.getReleaseByTag(this.getDescription().getVersion());
        GitHubRelease latest = api.getLatestVersion();
        if (current == null) {
            logger.warning("You are running a version that does not exist on GitHub. If you are in a dev environment, you can ignore this. Otherwise, this is a bug!");
            return;
        }
        int buildsBehind = api.getBuildsBehind(current);
        if (buildsBehind == 0) {
            logger.info("You are running the latest version.");
        } else {
            logger.warning("A new version is available (" + latest.getTagVersion() + ")! You are running version " + current.getTagVersion() + ". You are " + buildsBehind + " version(s) behind.");
        }
    }

    public boolean checkConfig(ItemStack itemStack, String configName) {
        String itemName = itemStack.getType().toString().toLowerCase();
        String itemType = null;
        if (itemName.contains("bow") || itemName.contains("shears")) {
            if (itemName.contains("bow")) {
                itemType = "bow";
            }
            if (itemName.contains("shears")) {
                itemType = "shears";
            }
        } else {
            itemType = itemName.substring(itemName.indexOf("_") + 1);
        }

        if (itemType == null) {
            return false;
        }

        switch (itemType) {
            case "pickaxe": {
                return config.getBoolean("enabled." + configName + ".pickaxe");
            }
            case "sword": {
                return config.getBoolean("enabled." + configName + ".sword");
            }
            case "shovel": {
                return config.getBoolean("enabled." + configName + ".shovel");
            }
            case "axe": {
                return config.getBoolean("enabled." + configName + ".axe");
            }
            case "hoe": {
                return config.getBoolean("enabled." + configName + ".hoe");
            }
            case "shears": {
                return config.getBoolean("enabled." + configName + ".shears");
            }
            case "bow": {
                return config.getBoolean("enabled." + configName + ".bow");
            }
            case "helmet":
            case "chestplate":
            case "leggings":
            case "boots": {
                return config.getBoolean("enabled." + configName + ".armor");
            }
        }
        return false;
    }
}
