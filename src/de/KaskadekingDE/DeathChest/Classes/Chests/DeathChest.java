package de.KaskadekingDE.DeathChest.Classes.Chests;

import de.KaskadekingDE.DeathChest.Classes.Chests.ChestManager.DeathChestManager;
import de.KaskadekingDE.DeathChest.Classes.Tasks.TaskScheduler;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class DeathChest implements Comparable<DeathChest>{

    public OfflinePlayer Owner;
    public Location ChestLocation;
    public Inventory DeathInventory;
    public Runnable Task;
    public int TaskId = -1;

    public DeathChest(Player owner, Location loc, Inventory inv) {
        Owner = owner;
        ChestLocation = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        DeathInventory = inv;
        DeathChestManager.Add(this);
    }

    public DeathChest(OfflinePlayer owner, Location loc, Inventory inv) {
        Owner = owner;
        ChestLocation = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        DeathInventory = inv;
        DeathChestManager.Add(this);
    }

    /**
     * An alternative to the constructor if you don't want to create a new variable.
     */
    public static void CreateDeathChest(Player owner, Location loc, Inventory inv) {
        new DeathChest(owner, loc, inv);
    }

    public static void CreateDeathChest(OfflinePlayer owner, Location loc, Inventory inv) {new DeathChest(owner, loc, inv); }

    public void SaveDeathChest() {
        int nextId = GetId();
        if(nextId == -1) {
            nextId = NextAvailableId(Owner);
        }
        String base = Main.Serialization.Serialize(DeathInventory);
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".name", Owner.getName());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".death-chests." + nextId + ".x", ChestLocation.getBlockX());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".death-chests." + nextId + ".y", ChestLocation.getBlockY());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".death-chests." + nextId + ".z", ChestLocation.getBlockZ());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".death-chests." + nextId + ".world", ChestLocation.getWorld().getName());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".death-chests." + nextId + ".inventory", base);
        Main.playerData.savePlayerConfig();
    }

    public int GetId() {
        if(Main.playerData.getPlayerConfig().contains("players." + Owner.getUniqueId()) && Main.playerData.getPlayerConfig().contains("players." + Owner.getUniqueId() + ".death-chests")) {
            for(int i = 1; i <= Main.MaximumDeathChests; i++) {
                if(Main.playerData.getPlayerConfig().contains("players." + Owner.getUniqueId() + ".death-chests." + i)) {
                    int x;
                    int y;
                    int z;
                    String world;
                    x = Main.playerData.getPlayerConfig().getInt("players." + Owner.getUniqueId() + ".death-chests." + i + ".x", 0);
                    y = Main.playerData.getPlayerConfig().getInt("players." + Owner.getUniqueId() + ".death-chests." + i + ".y", -1);
                    z = Main.playerData.getPlayerConfig().getInt("players." + Owner.getUniqueId() + ".death-chests." + i + ".z", 0);
                    world = Main.playerData.getPlayerConfig().getString("players." + Owner.getUniqueId() + ".death-chests." + i + ".world", null);
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

    public void RegisterTask(final OfflinePlayer p, final Location loc) {
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if(loc.getWorld().getBlockAt(loc).getType() == Material.CHEST || loc.getWorld().getBlockAt(loc).getType() == Material.SIGN_POST) {
                    loc.getBlock().setType(Material.AIR);
                    RemoveChest();
                    if(p.isOnline()) {
                        p.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.Despawned.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
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
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".death-chests." + GetId(), null);
        Main.playerData.savePlayerConfig();
        DeathChestManager.Remove(ChestLocation);
        if(ChestLocation.getBlock().getType() == Material.CHEST || ChestLocation.getBlock().getType() == Material.SIGN_POST) {
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

    public void RemoveChest(boolean runTask) {
        if(Owner == null || DeathInventory == null || ChestLocation == null) {
            return;
        }
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".death-chests." + GetId(), null);
        Main.playerData.savePlayerConfig();
        DeathChestManager.Remove(ChestLocation);
        if(ChestLocation.getBlock().getType() == Material.CHEST || ChestLocation.getBlock().getType() == Material.SIGN_POST) {
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
        for(int i = 1; i <= Main.MaximumDeathChests; i++) {
           if(!Main.playerData.getPlayerConfig().contains("players." + p.getUniqueId() + ".death-chests." + i)) {;
              return i;
           }
        }
        return -1;
    }

    public static List<DeathChest> DeathChestsByOwner(Player owner) {
        return DeathChestManager.GetByOwner(owner);
    }

    public static List<DeathChest> DeathChestsByOwner(OfflinePlayer owner) {
        return DeathChestManager.GetByOwner(owner);
    }

    public static DeathChest DeathChestByLocation(Location loc) {
        return DeathChestManager.GetByLocation(loc);
    }

    public static DeathChest DeathChestById(int id) {
        for(DeathChest dc: DeathChestManager.deathChests) {
            if(dc.GetId() == id) {
                return dc;
            }
        }
        return null;
    }

    public boolean EqualsOwner(Player p) {
        return Owner.getUniqueId().equals(p.getUniqueId());
    }

    @Override
    public int compareTo(DeathChest deathChest) {
        String id = Integer.toString(GetId());
        String id2 = Integer.toString(deathChest.GetId());
        return id.compareTo(id2);
    }
}
