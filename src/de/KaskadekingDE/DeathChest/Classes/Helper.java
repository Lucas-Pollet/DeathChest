package de.KaskadekingDE.DeathChest.Classes;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.KillChest;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Helper {

    public enum ChestState {
        Default,
        DeathChest,
        HomeChest,
        KillChest
    }

    public static boolean IsNumeric(String str) {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    public static String ServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(".") + 1);
    }

    public static Location ChestNearLocation(Location loc) {
        if(loc.getBlock().getType() == Material.SIGN_POST) return null;
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

    public static int EmptySlots(Inventory inv, boolean doubleInventory) {
        int slots = 27;
        if(doubleInventory) {
            slots = 54;
        }
        for(ItemStack is: inv.getContents()) {
            if(is != null) {
                slots--;
            }
        }
        return slots;
    }


    public static boolean IsDoubleChest(Block b) {
        if(b.getState() instanceof Chest) {
            Chest ch = (Chest) b.getState();
            InventoryHolder ih = ch.getInventory().getHolder();
            if(ih instanceof DoubleChest) {
                return true;
            }
        }
        return false;
    }

    public static boolean ChestEmpty(Inventory inv) {
        for(ItemStack item: inv.getContents()) {
            if(item != null) {
                return false;
            }
        }
        return true;
    }

    public static ChestState GetChestType(Location loc) {
        if(DeathChest.DeathChestByLocation(loc) != null) {
            return ChestState.DeathChest;
        } else if(HomeChest.HomeChestByLocation(loc) != null) {
            return ChestState.HomeChest;
        } else if(KillChest.KillChestByLocation(loc) != null){
            return ChestState.KillChest;
        } else {
            return ChestState.Default;
        }
    }

    public static List<Location> LocationsAround(Location loc) {
        List<Location> result = new ArrayList<Location>();
        result.add(loc);
        result.add(new Location(loc.getWorld(), loc.getBlockX() + 1, loc.getBlockY(), loc.getBlockZ()));
        result.add(new Location(loc.getWorld(), loc.getBlockX() - 1, loc.getBlockY(), loc.getBlockZ()));
        result.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()));
        result.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ()));
        result.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() + 1));
        result.add(new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() - 1));
        return result;
    }

    public static Location AvailableLocation(Location loc) {
        boolean placeChest = false;
        for(Location currentLoc: Helper.LocationsAround(loc)) {
            Material deathBlockMaterial = currentLoc.getBlock().getType();
            for(String key: Main.AllowedBlocks) {
                if(key.equalsIgnoreCase("*")) {
                    placeChest = true;
                    break;
                }
                Material allowedMat = Material.getMaterial(key);
                if(allowedMat == deathBlockMaterial) {
                    placeChest = true;
                    break;
                }
            }
            if(placeChest) {
                return new Location(currentLoc.getWorld(), currentLoc.getBlockX(), currentLoc.getBlockY(), currentLoc.getBlockZ());
            }
        }
        return null;
    }

    public static void MoveItemsToInventory(Inventory oldInv, Inventory newInv) {
        for(ItemStack item: oldInv.getContents()) {
            if(item != null) {
                newInv.addItem(item);
            }
        }
    }

}
