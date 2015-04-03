package de.KaskadekingDE.DeathChest.Classes.Chests;

import de.KaskadekingDE.DeathChest.Classes.Chests.ChestManager.KillChestManager;
import de.KaskadekingDE.DeathChest.Classes.Tasks.TaskScheduler;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class KillChest implements Comparable<KillChest>{

    public OfflinePlayer Owner;
    public Location ChestLocation;
    public Inventory DeathInventory;
    public Runnable Task;
    public int TaskId = -1;

    public KillChest(Player owner, Location loc, Inventory inv) {
        Owner = owner;
        ChestLocation = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        DeathInventory = inv;
        KillChestManager.Add(this);
    }

    public KillChest(OfflinePlayer owner, Location loc, Inventory inv) {
        Owner = owner;
        ChestLocation = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        DeathInventory = inv;
        KillChestManager.Add(this);
    }

    /**
     * An alternative to the constructor if you don't want to create a new variable.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void CreateKillChest(Player owner, Location loc, Inventory inv) {
        new KillChest(owner, loc, inv);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void CreateKillChest(OfflinePlayer owner, Location loc, Inventory inv) {new KillChest(owner, loc, inv); }

    @SuppressWarnings("UnusedDeclaration")
    public void SaveKillChest() {
        int nextId = GetId();
        if(nextId == -1) {
            nextId = NextAvailableId(Owner);
        }
        String base = Main.Serialization.toBase64(DeathInventory);
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".name", Owner.getName());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".kill-chests." + nextId + ".x", ChestLocation.getBlockX());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".kill-chests." + nextId + ".y", ChestLocation.getBlockY());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".kill-chests." + nextId + ".z", ChestLocation.getBlockZ());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".kill-chests." + nextId + ".world", ChestLocation.getWorld().getName());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".kill-chests." + nextId + ".inventory", base);
        Main.playerData.savePlayerConfig();
    }

    public int GetId() {
        if(Main.playerData.getPlayerConfig().contains("players." + Owner.getUniqueId()) && Main.playerData.getPlayerConfig().contains("players." + Owner.getUniqueId() + ".kill-chests")) {
            for(int i = 1; i <= Main.MaximumKillChests; i++) {
                if(Main.playerData.getPlayerConfig().contains("players." + Owner.getUniqueId() + ".kill-chests." + i)) {
                    int x;
                    int y;
                    int z;
                    String world;
                    x = Main.playerData.getPlayerConfig().getInt("players." + Owner.getUniqueId() + ".kill-chests." + i + ".x", 0);
                    y = Main.playerData.getPlayerConfig().getInt("players." + Owner.getUniqueId() + ".kill-chests." + i + ".y", -1);
                    z = Main.playerData.getPlayerConfig().getInt("players." + Owner.getUniqueId() + ".kill-chests." + i + ".z", 0);
                    world = Main.playerData.getPlayerConfig().getString("players." + Owner.getUniqueId() + ".kill-chests." + i + ".world", null);
                    if(world != null) {
                        World w = Bukkit.getWorld(world);
                        if(w != null) {
                            Location loc = new Location(w, x, y, z);
                            if(loc.equals(ChestLocation)) {
                                return i;
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void RegisterTask(final OfflinePlayer p, final Location loc) {
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if(loc.getWorld().getBlockAt(loc).getType() == Material.CHEST) {
                    loc.getBlock().setType(Material.AIR);
                    RemoveChest();
                    if(p.isOnline()) {
                        p.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.Despawned.replace("%type", LangStrings.KillChest));
                    }
                }
                TaskScheduler.RemoveTask(TaskId);
            }
        };
        Task = timeout;
        TaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, timeout, Main.SecondsToRemove * 20L);
        TaskScheduler.AddTask(TaskId);
    }

    public void RemoveChest() {
        if(Owner == null || DeathInventory == null || ChestLocation == null) {
            return;
        }
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".kill-chests." + GetId(), null);
        Main.playerData.savePlayerConfig();
        KillChestManager.Remove(ChestLocation);
        if(ChestLocation.getBlock().getType() == Material.CHEST) {
            ChestLocation.getBlock().setType(Material.AIR);
        }
        Owner = null;
        ChestLocation = null;
        DeathInventory = null;
        if(TaskId != -1) {
            if(Bukkit.getScheduler().isQueued(TaskId) || Bukkit.getScheduler().isCurrentlyRunning(TaskId)) {
                Bukkit.getScheduler().cancelTask(TaskId);
            }
        }
        TaskId = -1;
        Task = null;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void RemoveChest(boolean runTask) {
        if(Owner == null || DeathInventory == null || ChestLocation == null) {
            return;
        }
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".kill-chests." + GetId(), null);
        Main.playerData.savePlayerConfig();
        KillChestManager.Remove(ChestLocation);
        if(ChestLocation.getBlock().getType() == Material.CHEST) {
            ChestLocation.getBlock().setType(Material.AIR);
        }
        Owner = null;
        ChestLocation = null;
        DeathInventory = null;
        if(TaskId != -1) {
            if(Bukkit.getScheduler().isQueued(TaskId) || Bukkit.getScheduler().isCurrentlyRunning(TaskId)) {
                if(runTask) {
                    Task.run();
                } else {
                    Bukkit.getScheduler().cancelTask(TaskId);
                }
            }
        }
        TaskId = -1;
        Task = null;
    }

    public static int NextAvailableId(OfflinePlayer p) {
        for(int i = 1; i <= Main.MaximumKillChests; i++) {
            if(!Main.playerData.getPlayerConfig().contains("players." + p.getUniqueId() + ".kill-chests." + i)) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static List<KillChest> KillChestsByOwner(Player owner) {
        return KillChestManager.GetByOwner(owner);
    }

    public static List<KillChest> KillChestsByOwner(OfflinePlayer owner) {
        return KillChestManager.GetByOwner(owner);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static KillChest KillChestByLocation(Location loc) {
        return KillChestManager.GetByLocation(loc);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static KillChest KillChestById(int id) {
        for(KillChest kc: KillChestManager.killChests) {
            if(kc.GetId() == id) {
                return kc;
            }
        }
        return null;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean EqualsOwner(Player p) {
        return Owner.getUniqueId().equals(p.getUniqueId());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(KillChest killChest) {
        String id = Integer.toString(GetId());
        String id2 = Integer.toString(killChest.GetId());
        return id.compareTo(id2);
    }
}
