package lol.hyper.toolstats.tools;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.NamespacedKey;

import java.util.HashSet;
import java.util.Set;

public class ToolStatsKeys {

    private final ToolStats toolStats;
    private final Set<NamespacedKey> tokenKeys = new HashSet<>();

    public ToolStatsKeys(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    private NamespacedKey itemOwner;
    private NamespacedKey timeCreated;
    private NamespacedKey playerKills;
    private NamespacedKey mobKills;
    private NamespacedKey blocksMined;
    private NamespacedKey cropsHarvested;
    private NamespacedKey fishCaught;
    private NamespacedKey sheepSheared;
    private NamespacedKey armorDamage;
    private NamespacedKey damageDone;
    private NamespacedKey newElytra;
    private NamespacedKey hash;
    private NamespacedKey arrowsShot;
    private NamespacedKey droppedBy;
    private NamespacedKey flightTime;
    private NamespacedKey tokenType;
    private NamespacedKey tokenApplied;
    private NamespacedKey witherKills;
    private NamespacedKey enderDragonKills;
    private NamespacedKey criticalStrikes;
    private NamespacedKey tridentThrows;
    private NamespacedKey originType;
    private NamespacedKey logsStripped;

    public void make() {
        itemOwner = new NamespacedKey(toolStats, "owner");
        timeCreated = new NamespacedKey(toolStats, "time-created");
        playerKills = new NamespacedKey(toolStats, "player-kills");
        mobKills = new NamespacedKey(toolStats, "mob-kills");
        blocksMined = new NamespacedKey(toolStats, "generic-mined");
        cropsHarvested = new NamespacedKey(toolStats, "crops-mined");
        fishCaught = new NamespacedKey(toolStats, "fish-caught");
        sheepSheared = new NamespacedKey(toolStats, "sheared");
        armorDamage = new NamespacedKey(toolStats, "damage-taken");
        damageDone = new NamespacedKey(toolStats, "damage-done");
        newElytra = new NamespacedKey(toolStats, "new");
        hash = new NamespacedKey(toolStats, "hash");
        arrowsShot = new NamespacedKey(toolStats, "arrows-shot");
        droppedBy = new NamespacedKey(toolStats, "dropped-by");
        flightTime = new NamespacedKey(toolStats, "flightTime");
        tokenType = new NamespacedKey(toolStats, "token-type");
        tokenApplied = new NamespacedKey(toolStats, "token-applied");
        witherKills = new NamespacedKey(toolStats, "wither-kills");
        enderDragonKills = new NamespacedKey(toolStats, "enderdragon-kills");
        criticalStrikes = new NamespacedKey(toolStats, "critical-strikes");
        tridentThrows = new NamespacedKey(toolStats, "trident-throws");
        originType = new NamespacedKey(toolStats, "origin");
        logsStripped = new NamespacedKey(toolStats, "logs-stripped");

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
        tokenKeys.add(witherKills);
        tokenKeys.add(enderDragonKills);
        tokenKeys.add(criticalStrikes);
        tokenKeys.add(tridentThrows);
        tokenKeys.add(logsStripped);
    }

    public NamespacedKey getItemOwner() {
        return itemOwner;
    }

    public NamespacedKey getTimeCreated() {
        return timeCreated;
    }

    public NamespacedKey getPlayerKills() {
        return playerKills;
    }

    public NamespacedKey getMobKills() {
        return mobKills;
    }

    public NamespacedKey getBlocksMined() {
        return blocksMined;
    }

    public NamespacedKey getCropsHarvested() {
        return cropsHarvested;
    }

    public NamespacedKey getFishCaught() {
        return fishCaught;
    }

    public NamespacedKey getSheepSheared() {
        return sheepSheared;
    }

    public NamespacedKey getArmorDamage() {
        return armorDamage;
    }

    public NamespacedKey getDamageDone() {
        return damageDone;
    }

    public NamespacedKey getElytraKey() {
        return newElytra;
    }

    public NamespacedKey getHash() {
        return hash;
    }

    public NamespacedKey getArrowsShot() {
        return arrowsShot;
    }

    public NamespacedKey getDroppedBy() {
        return droppedBy;
    }

    public NamespacedKey getFlightTime() {
        return flightTime;
    }

    public NamespacedKey getTokenType() {
        return tokenType;
    }

    public NamespacedKey getTokenApplied() {
        return tokenApplied;
    }

    public NamespacedKey getWitherKills() {
        return witherKills;
    }

    public NamespacedKey getEnderDragonKills() {
        return enderDragonKills;
    }

    public NamespacedKey getCriticalStrikes() {
        return criticalStrikes;
    }

    public NamespacedKey getTridentThrows() {
        return tridentThrows;
    }

    public NamespacedKey getLogsStripped() {
        return logsStripped;
    }

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
    public NamespacedKey getOriginType() {
        return originType;
    }

    public Set<NamespacedKey> getTokenKeys() {
        return tokenKeys;
    }
}