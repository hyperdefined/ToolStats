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
import lol.hyper.toolstats.tools.*;
import lol.hyper.toolstats.tools.config.ConfigTools;
import lol.hyper.toolstats.tools.config.ConfigUpdater;
import lol.hyper.toolstats.tools.config.TokenItems;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class ToolStats extends JavaPlugin {

    /**
     * Stores who created an item.
     */
    public final NamespacedKey itemOwner = new NamespacedKey(this, "owner");
    /**
     * Stores when the item was created.
     */
    public final NamespacedKey timeCreated = new NamespacedKey(this, "time-created");
    /**
     * Stores how many player kills.
     */
    public final NamespacedKey playerKills = new NamespacedKey(this, "player-kills");
    /**
     * Stores how many mob kills.
     */
    public final NamespacedKey mobKills = new NamespacedKey(this, "mob-kills");
    /**
     * Stores how many blocks were mined.
     */
    public final NamespacedKey blocksMined = new NamespacedKey(this, "generic-mined");
    /**
     * Stores how many crops were harvested.
     */
    public final NamespacedKey cropsHarvested = new NamespacedKey(this, "crops-mined");
    /**
     * Stores how many fish were caught.
     */
    public final NamespacedKey fishCaught = new NamespacedKey(this, "fish-caught");
    /**
     * Stores how many sheep were sheared.
     */
    public final NamespacedKey sheepSheared = new NamespacedKey(this, "sheared");
    /**
     * Stores how much damage an armor piece has taken.
     */
    public final NamespacedKey armorDamage = new NamespacedKey(this, "damage-taken");
    /**
     * Key for tracking new elytras that spawn.
     */
    public final NamespacedKey newElytra = new NamespacedKey(this, "new");
    /**
     * Key for item has.
     */
    public final NamespacedKey hash = new NamespacedKey(this, "hash");
    /**
     * Key for arrows shot.
     */
    public final NamespacedKey arrowsShot = new NamespacedKey(this, "arrows-shot");
    /**
     * Key for tracking flight time.
     */
    public final NamespacedKey flightTime = new NamespacedKey(this, "flightTime");
    /**
     * Key for token type. This is for the token itself.
     */
    public final NamespacedKey tokenType = new NamespacedKey(this, "token-type");
    /**
     * Key for applied token. This is what goes onto the tool/armor to record the type.
     */
    public final NamespacedKey tokenApplied = new NamespacedKey(this, "token-applied");
    /**
     * Stores how an item was created.
     * 0 = crafted.
     * 1 = dropped.
     * 2 = looted.
     * 3 = traded.
     * 4 = founded (for elytras).
     * 5 = fished.
     * 6 = spawned in (creative).
     */
    public final NamespacedKey originType = new NamespacedKey(this, "origin");

    public final int CONFIG_VERSION = 9;
    public final Logger logger = this.getLogger();
    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public boolean tokens = false;
    public Set<NamespacedKey> tokenKeys = new HashSet<>();

    public BlockBreak blockBreak;
    public ChunkPopulate chunkPopulate;
    public CraftItem craftItem;
    public EntityDeath entityDeath;
    public GenerateLoot generateLoot;
    public PickupItem pickupItem;
    public EntityDamage mobKill;
    public PlayerFish playerFish;
    public PlayerInteract playerInteract;
    public SheepShear sheepShear;
    public VillagerTrade villagerTrade;
    public CommandToolStats commandToolStats;
    public ItemLore itemLore;
    public InventoryOpen inventoryOpen;
    public PlayerJoin playerJoin;
    public NumberFormat numberFormat;
    public YamlConfiguration config;
    public HashMaker hashMaker;
    public CreativeEvent creativeEvent;
    public PlayerMove playerMove;
    public ItemChecker itemChecker;
    public ShootBow shootBow;
    public ConfigTools configTools;
    public TokenItems tokenItems;
    public TokenCrafting tokenCrafting;
    public AnvilEvent anvilEvent;
    public PrepareCraft prepareCraft;
    public GrindstoneEvent grindstoneEvent;

    @Override
    public void onEnable() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
            logger.info("Copying default config!");
        }

        loadConfig();
        configTools = new ConfigTools(this);
        tokenItems = new TokenItems(this);
        tokenCrafting = new TokenCrafting(this);
        tokenCrafting.setup();
        for (ShapedRecipe recipe : tokenCrafting.getRecipes()) {
            if (tokens && config.getBoolean("tokens.craft-tokens")) {
                Bukkit.addRecipe(recipe);
            }
        }
        hashMaker = new HashMaker(this);
        blockBreak = new BlockBreak(this);
        craftItem = new CraftItem(this);
        chunkPopulate = new ChunkPopulate(this);
        entityDeath = new EntityDeath(this);
        generateLoot = new GenerateLoot(this);
        pickupItem = new PickupItem(this);
        mobKill = new EntityDamage(this);
        playerFish = new PlayerFish(this);
        playerInteract = new PlayerInteract(this);
        sheepShear = new SheepShear(this);
        villagerTrade = new VillagerTrade(this);
        commandToolStats = new CommandToolStats(this);
        itemLore = new ItemLore(this);
        inventoryOpen = new InventoryOpen(this);
        playerJoin = new PlayerJoin(this);
        creativeEvent = new CreativeEvent(this);
        playerMove = new PlayerMove(this);
        itemChecker = new ItemChecker(this);
        itemChecker.setup();
        shootBow = new ShootBow(this);
        anvilEvent = new AnvilEvent(this);
        prepareCraft = new PrepareCraft(this);
        grindstoneEvent = new GrindstoneEvent(this);

        // save which stat can be used by a reset token
        tokenKeys.add(blocksMined);
        tokenKeys.add(playerKills);
        tokenKeys.add(mobKills);
        tokenKeys.add(cropsHarvested);
        tokenKeys.add(sheepSheared);
        tokenKeys.add(fishCaught);
        tokenKeys.add(flightTime);
        tokenKeys.add(arrowsShot);
        tokenKeys.add(armorDamage);

        Bukkit.getServer().getPluginManager().registerEvents(blockBreak, this);
        Bukkit.getServer().getPluginManager().registerEvents(chunkPopulate, this);
        Bukkit.getServer().getPluginManager().registerEvents(craftItem, this);
        Bukkit.getServer().getPluginManager().registerEvents(entityDeath, this);
        Bukkit.getServer().getPluginManager().registerEvents(generateLoot, this);
        Bukkit.getServer().getPluginManager().registerEvents(pickupItem, this);
        Bukkit.getServer().getPluginManager().registerEvents(mobKill, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerFish, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerInteract, this);
        Bukkit.getServer().getPluginManager().registerEvents(sheepShear, this);
        Bukkit.getServer().getPluginManager().registerEvents(villagerTrade, this);
        Bukkit.getServer().getPluginManager().registerEvents(inventoryOpen, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerJoin, this);
        Bukkit.getServer().getPluginManager().registerEvents(creativeEvent, this);
        Bukkit.getServer().getPluginManager().registerEvents(shootBow, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerMove, this);
        Bukkit.getServer().getPluginManager().registerEvents(anvilEvent, this);
        Bukkit.getServer().getPluginManager().registerEvents(prepareCraft, this);
        Bukkit.getServer().getPluginManager().registerEvents(grindstoneEvent, this);

        this.getCommand("toolstats").setExecutor(commandToolStats);

        new Metrics(this, 14110);
        Bukkit.getAsyncScheduler().runNow(this, scheduledTask -> checkForUpdates());
    }

    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("Your config file is outdated! We will try to update it, but you should regenerate it!");
            ConfigUpdater configUpdater = new ConfigUpdater(this);
            configUpdater.updateConfig();
        }

        if (config.getBoolean("tokens.enabled")) {
            logger.info("Tokens are enabled! All stat tracking (besides origins) is forced disabled.");
            logger.info("If you want to track stats on items, add the correct token to it!");
        }

        tokens = config.getBoolean("tokens.enabled");

        numberFormat = new NumberFormat(this);
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
        GitHubRelease current = api.getReleaseByTag(this.getPluginMeta().getVersion());
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
}
