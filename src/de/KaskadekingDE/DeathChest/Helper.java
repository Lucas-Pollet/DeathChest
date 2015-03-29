package de.KaskadekingDE.DeathChest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

public class Helper {

    public Helper() {
        throw new UnsupportedOperationException("You're not allowed to create a new instance of Helper!");
    }

    public static Location ChestNearLocation(Location loc) {
        Location locEast = new Location(loc.getWorld(), loc.getX() + 1, loc.getY(), loc.getZ());
        Location locSouth = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() + 1);
        Location locWest = new Location(loc.getWorld(), loc.getX() - 1, loc.getY(), loc.getZ());
        Location locNorth =  new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() - 1);
        if(locEast.getBlock().getType() == Material.CHEST) {
            return locEast;
        } else if(locSouth.getBlock().getType() == Material.CHEST) {
            return locSouth;
        } else if(locWest.getBlock().getType() == Material.CHEST){
            return locWest;
        } else if(locNorth.getBlock().getType() == Material.CHEST) {
            return locNorth;
        }
        return null;
    }

    public static boolean IsDoubleChest(Block chest) {
        if(chest.getState() instanceof Chest) {
            Chest ch = (Chest) chest.getState();
            InventoryHolder ih = ch.getInventory().getHolder();
            if(ih instanceof DoubleChest) {
                return true;
            }
        }
        return false;
    }

    public static boolean IsNumeric(String str) {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }
}
