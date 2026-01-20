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
import lol.hyper.hyperlib.bstats.HyperStats;
import lol.hyper.hyperlib.releases.HyperUpdater;
import lol.hyper.hyperlib.utils.TextUtils;
import lol.hyper.toolstats.commands.CommandToolStats;
import lol.hyper.toolstats.events.*;
import lol.hyper.toolstats.tools.*;
import lol.hyper.toolstats.tools.config.ConfigTools;
import lol.hyper.toolstats.tools.config.ConfigUpdater;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ToolStats extends JavaPlugin {

    public final int CONFIG_VERSION = 14;
    public final ComponentLogger logger = this.getComponentLogger();
    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public boolean tokens = false;

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
    public BlockDispenseEvent blockDispenseEvent;
    public HyperLib hyperLib;
    public TextUtils textUtils;
    public ProjectileShoot projectileShoot;
    public ToolStatsKeys toolStatsKeys;

    @Override
    public void onEnable() {
        hyperLib = new HyperLib(this);
        hyperLib.setup();

        HyperStats stats = new HyperStats(hyperLib, 14110);
        stats.setup();

        textUtils = new TextUtils(hyperLib);

        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
            logger.info("Copying default config!");
        }

        loadConfig();
        configTools = new ConfigTools(this);
        toolStatsKeys = new ToolStatsKeys(this);
        toolStatsKeys.make();
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
        blockDispenseEvent = new BlockDispenseEvent(this);
        projectileShoot = new ProjectileShoot(this);

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
        Bukkit.getServer().getPluginManager().registerEvents(blockDispenseEvent, this);
        Bukkit.getServer().getPluginManager().registerEvents(projectileShoot, this);

        this.getCommand("toolstats").setExecutor(commandToolStats);

        HyperUpdater updater = new HyperUpdater(hyperLib);
        updater.setGitHub("hyperdefined", "ToolStats");
        updater.setModrinth("oBZj9E15");
        updater.setHangar("ToolStats", "paper");
        updater.check();
    }

    public void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warn("Your config file is outdated! We will try to update it, but you should regenerate it!");
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
