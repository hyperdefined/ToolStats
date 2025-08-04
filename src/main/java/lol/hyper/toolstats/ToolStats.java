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

import lol.hyper.hyperlib.HyperLib;
import lol.hyper.hyperlib.bstats.bStats;
import lol.hyper.hyperlib.releases.hangar.HangarRelease;
import lol.hyper.hyperlib.releases.modrinth.ModrinthPlugin;
import lol.hyper.hyperlib.releases.modrinth.ModrinthRelease;
import lol.hyper.hyperlib.utils.TextUtils;
import lol.hyper.toolstats.commands.CommandToolStats;
import lol.hyper.toolstats.events.*;
import lol.hyper.toolstats.tools.*;
import lol.hyper.toolstats.tools.config.ConfigTools;
import lol.hyper.toolstats.tools.config.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
     * Stores how much damage a weapon has done.
     */
    public final NamespacedKey damageDone = new NamespacedKey(this, "damage-done");
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
     * Key for arrows shot.
     */
    public final NamespacedKey droppedBy = new NamespacedKey(this, "dropped-by");
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

    public final int CONFIG_VERSION = 13;
    public final Logger logger = this.getLogger();
    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public boolean tokens = false;
    public final Set<NamespacedKey> tokenKeys = new HashSet<>();

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
    public TokenData tokenData;
    public AnvilEvent anvilEvent;
    public PrepareCraft prepareCraft;
    public HyperLib hyperLib;
    public TextUtils textUtils;

    @Override
    public void onEnable() {
        hyperLib = new HyperLib(this);
        hyperLib.setup();

        bStats bstats = new bStats(hyperLib, 14110);
        bstats.setup();

        textUtils = new TextUtils(hyperLib);

        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
            logger.info("Copying default config!");
        }

        loadConfig();
        configTools = new ConfigTools(this);
        tokenData = new TokenData(this);
        tokenData.setup();
        for (ShapedRecipe recipe : tokenData.getRecipes()) {
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

        this.getCommand("toolstats").setExecutor(commandToolStats);

        Bukkit.getAsyncScheduler().runNow(this, scheduledTask -> {
            ModrinthPlugin modrinthPlugin = new ModrinthPlugin("oBZj9E15");
            modrinthPlugin.get();

            ModrinthRelease release = modrinthPlugin.getReleaseByVersion(this.getPluginMeta().getVersion());
            if (release == null) {
                logger.warning("You are running a version not published.");
            } else {
                int buildsBehind = modrinthPlugin.buildsVersionsBehind(release);
                if (buildsBehind > 0) {
                    ModrinthRelease latest = modrinthPlugin.getLatestRelease();
                    if (latest != null) {
                        logger.info("You are " + buildsBehind + " versions behind. Please update!");
                        logger.info("The latest version is " + latest.getVersion());
                        logger.info(latest.getVersionPage());
                    }
                } else {
                    logger.info("Yay! You are running the latest version.");
                }
            }
        });
    }

    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warning("Your config file is outdated! We will try to update it, but you should regenerate it!");
            ConfigUpdater configUpdater = new ConfigUpdater(this);
            configUpdater.updateConfig();
        }

        if (config.getBoolean("tokens.enabled")) {
            logger.info("The token system is enabled! This means you must apply tokens to items in order for them to track.");
            logger.info("Item origins (crafted, traded, found, etc) will still apply to items.");
            logger.info("https://github.com/hyperdefined/ToolStats/wiki/Token-System");
        }

        tokens = config.getBoolean("tokens.enabled");

        numberFormat = new NumberFormat(this);
    }
}
