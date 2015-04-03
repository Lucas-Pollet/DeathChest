package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import de.KaskadekingDE.DeathChest.Classes.Helper;
import de.KaskadekingDE.DeathChest.Classes.Tasks.Animation.AnimationManager;
import de.KaskadekingDE.DeathChest.Classes.Tasks.TaskScheduler;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class DeathChestEvent implements Listener {

    public static HashMap<Player, Inventory> suppressEvent = new HashMap<Player, Inventory>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Location loc = p.getLocation();
        if(p.getKiller() != null) {
            // Let do killer chest to the work.
            return;
        }
        if(p.hasPermission("deathchest.place.death")) {
            if(checkRequirements(p, e.getDrops())) {
                loc = Helper.AvailableLocation(loc);
                loc.getBlock().setType(Material.CHEST);
                Chest deathChest = (Chest) loc.getBlock().getState();
                Inventory inv = Bukkit.getServer().createInventory(deathChest.getInventory().getHolder(), 54, LangStrings.DeathChestInv);
                for(ItemStack drop: e.getDrops()) {
                    inv.addItem(drop);
                }
                e.getDrops().clear();
                DeathChest chest = new DeathChest(p, loc, inv);
                chest.SaveDeathChest();
                if(Main.ShowCoords) {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestWithCoords.replace("%x", Integer.toString(loc.getBlockX())).replace("%y", Integer.toString(loc.getBlockY())).replace("%z", Integer.toString(loc.getBlockZ())).replace("%world", loc.getWorld().getName()));
                } else {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestPlaced);
                }
                if(Main.SecondsToRemove > 0) {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.TimeToFindDeathChest.replace("%time", Integer.toString(Main.SecondsToRemove)));
                    chest.RegisterTask(p, loc);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        if(suppressEvent.containsKey(p)) {
            Inventory inv = suppressEvent.get(p);
            if(inv.equals(e.getInventory())) {
                suppressEvent.remove(p);
                return;
            }
        }
        InventoryHolder ih = e.getInventory().getHolder();
        if(ih instanceof Chest) {
            Chest ch = (Chest) ih;
            DeathChest dc = DeathChest.DeathChestByLocation(ch.getLocation());
            if(dc == null) {
                return;
            }
            if(!dc.EqualsOwner(p) && !p.hasPermission("deathchest.protection.bypass")) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.CantOpen.replace("%owner", dc.Owner.getName()).replace("%type", LangStrings.DeathChest));
                e.setCancelled(true);
                return;
            } else if(p.hasPermission("deathchest.protection.bypass") & !dc.EqualsOwner(p)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.ThisChestBelongsTo.replace("%owner", dc.Owner.getName()));
            }

            e.setCancelled(true);
            suppressEvent.put(p, dc.DeathInventory);
            if(Main.HookedPacketListener) {
                AnimationManager.Create(p, dc.ChestLocation);
                Main.ProtocolManager.SendChestOpenPacket(dc.ChestLocation, p);
            }
            p.openInventory(dc.DeathInventory);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent e) {
        InventoryHolder ih = e.getInventory().getHolder();
        Player p = (Player) e.getPlayer();
        if(ih instanceof Chest) {
            Chest ch = (Chest) ih;
            Location loc = new Location(ch.getLocation().getWorld(), ch.getLocation().getBlockX(), ch.getLocation().getBlockY(), ch.getLocation().getBlockZ());
            DeathChest dc = DeathChest.DeathChestByLocation(loc);
            if(dc == null) {
                return;
            }
            if(Main.HookedPacketListener) {
                AnimationManager.Remove(p);
                Main.ProtocolManager.SendChestClosePacket(dc.ChestLocation, p);
            }
            if(Helper.ChestEmpty(dc.DeathInventory)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestRemoved);
                dc.RemoveChest(true);
            }
        }
    }

    private boolean checkRequirements(Player p, List<ItemStack> drops) {
        HomeChest hc = HomeChest.HomeChestByPlayer(p);
        if(hc != null && !hc.IsFull()) {
            // Let do the home chest event do their work.
            return false;
        }
        if(drops == null || drops.size() == 0) {
            return false;
        }
        Location loc = p.getLocation();
        if(Main.MaximumDeathChests != -1) {
            if(DeathChest.DeathChestsByOwner(p).size() >= Main.MaximumDeathChests || DeathChest.NextAvailableId(p) == -1) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.ReachedMaximumChests.replace("%type", LangStrings.DeathChest));
                return false;
            }
        }
        boolean worldAllowed = false;
        for(String key: Main.AllowedWorlds) {
            if(key.equalsIgnoreCase("*")) {
                worldAllowed = true;
                break;
            }
            World w = Bukkit.getWorld(key);
            if(w != null) {
                if(w == loc.getWorld()) {
                    worldAllowed = true;
                    break;
                }
            }
        }
        if(!worldAllowed) {
            return false;
        }
        boolean placeChest = false;
        if(p.getLocation().getBlockY() < 0) {
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingDeathChest.replace("%type", LangStrings.DeathChest));
            return false;
        }
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
                break;
            }
        }
        if(!placeChest) {
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingDeathChest.replace("%type", LangStrings.DeathChest));
            return false;
        }
        return true;
    }
}
