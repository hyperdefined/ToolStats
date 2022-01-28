package lol.hyper.toolstats;

import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.toolstats.events.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public final class ToolStats extends JavaPlugin {

    // stores who crafted an item
    public final NamespacedKey craftedOwner = new NamespacedKey(this, "owner");
    // stores when an item was crafted
    public final NamespacedKey craftedTime = new NamespacedKey(this, "time-created");
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

    public BlocksMined blocksMined;
    public CraftItem craftItem;
    public EntityDeath entityDeath;
    public EntityDamage mobKill;
    public PlayerFish playerFish;
    public SheepShear sheepShear;

    public final Logger logger = this.getLogger();

    @Override
    public void onEnable() {
        blocksMined = new BlocksMined(this);
        craftItem = new CraftItem(this);
        entityDeath = new EntityDeath(this);
        mobKill = new EntityDamage(this);
        playerFish = new PlayerFish(this);
        sheepShear = new SheepShear(this);

        Bukkit.getServer().getPluginManager().registerEvents(blocksMined, this);
        Bukkit.getServer().getPluginManager().registerEvents(craftItem, this);
        Bukkit.getServer().getPluginManager().registerEvents(entityDeath, this);
        Bukkit.getServer().getPluginManager().registerEvents(mobKill, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerFish, this);
        Bukkit.getServer().getPluginManager().registerEvents(sheepShear, this);

        new Metrics(this, 14110);

        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);
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
}
