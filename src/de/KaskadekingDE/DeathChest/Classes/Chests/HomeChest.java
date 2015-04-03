package de.KaskadekingDE.DeathChest.Classes.Chests;

import de.KaskadekingDE.DeathChest.Classes.Chests.ChestManager.HomeChestManager;
import de.KaskadekingDE.DeathChest.Classes.Helper;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class HomeChest {

    public OfflinePlayer Owner;
    public Location ChestLocation;
    public Inventory HomeInventory;

    public HomeChest(Player owner, Location loc, Inventory inv) {
        Owner = owner;
        ChestLocation = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        HomeInventory = inv;
        HomeChestManager.Add(this);
    }

    public HomeChest(OfflinePlayer owner, Location loc, Inventory inv) {
        Owner = owner;
        ChestLocation = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        HomeInventory = inv;
        HomeChestManager.Add(this);
    }

    public static void CreateHomeChest(Player owner, Location loc, Inventory inv) { new HomeChest(owner, loc, inv); }

    public static void CreateHomeChest(OfflinePlayer owner, Location loc, Inventory inv) { new HomeChest(owner, loc, inv); }

    public void SaveHomeChest() {
        String base = Main.Serialization.toBase64(HomeInventory);
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".name", Owner.getName());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".home-chest.x", ChestLocation.getBlockX());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".home-chest.y", ChestLocation.getBlockY());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".home-chest.z", ChestLocation.getBlockZ());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".home-chest.world", ChestLocation.getWorld().getName());
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".home-chest.inventory", base);
        Main.playerData.savePlayerConfig();
    }

    public boolean IsFull() {
        return Helper.EmptySlots(HomeInventory, true) <= 0;
    }

    public void RemoveChest() {
        Main.playerData.getPlayerConfig().set("players." + Owner.getUniqueId() + ".home-chest", null);
        Main.playerData.savePlayerConfig();
        HomeChestManager.Remove(ChestLocation);
        Owner = null;
        ChestLocation = null;
        HomeInventory = null;
    }

    public static HomeChest HomeChestByPlayer(OfflinePlayer p) {
        return HomeChestManager.GetByOwner(p);
    }

    public static HomeChest HomeChestByLocation(Location loc) {
        return HomeChestManager.GetByLocation(loc);
    }

    public boolean EqualsOwner(Player p) {
        return Owner.getUniqueId().equals(p.getUniqueId());
    }

}
