package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.KillChest;
import de.KaskadekingDE.DeathChest.Classes.Helper;
import de.KaskadekingDE.DeathChest.Classes.SignHolder;
import de.KaskadekingDE.DeathChest.Classes.Tasks.Animation.AnimationManager;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class KillChestEvent implements Listener {

    public static HashMap<Player, Inventory> suppressEvent = new HashMap<Player, Inventory>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Player killer = e.getEntity().getKiller();
        if(killer == null) {
            // Killer is null. Let the death chest do the work.
            return;
        }
        Location loc = p.getLocation();
        if(killer.hasPermission("deathchest.place.kill")) {
            if(checkRequirements(killer, loc, e.getDrops())) {
                loc = Helper.AvailableLocation(loc);
                Inventory inv;
                if (Main.UseTombstones) {
                    Block block = loc.getWorld().getBlockAt(loc);
                    block.setType(Material.SIGN_POST);
                    Sign sign = (Sign) block;
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM HH:mm:ss");
                    Date date = new Date();
                    String dateString = dateFormat.format(date);
                    String lineOne = LangStrings.LineOne.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.KillChestInv);
                    String lineTwo = LangStrings.LineTwo.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.KillChestInv);
                    String lineThree = LangStrings.LineThree.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.KillChestInv);
                    String lineFour = LangStrings.LineFour.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.KillChestInv);
                    sign.setLine(0, lineOne);
                    sign.setLine(1, lineTwo);
                    sign.setLine(2, lineThree);
                    sign.setLine(3, lineFour);
                    sign.update();
                    SignHolder sh = new SignHolder(LangStrings.KillChestInv, 54, sign);
                    inv = sh.getInventory();
                } else {
                    loc.getBlock().setType(Material.CHEST);
                    Chest killChest = (Chest) loc.getBlock().getState();
                    inv = Bukkit.getServer().createInventory(killChest.getInventory().getHolder(), 54, LangStrings.KillChestInv);
                }
                for(ItemStack drop: e.getDrops()) {
                    inv.addItem(drop);
                }
                e.getDrops().clear();
                KillChest chest = new KillChest(killer, loc, inv);
                chest.SaveKillChest();
                killer.sendMessage(LangStrings.Prefix + " " + LangStrings.KillChestPlaced.replace("%player", p.getDisplayName()));
                if(Main.SecondsToRemove > 0) {
                    killer.sendMessage(LangStrings.Prefix + " " + LangStrings.TimeToTakeLoot.replace("%time", Integer.toString(Main.SecondsToRemove)));
                    chest.RegisterTask(killer, loc);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.SIGN_POST) {
            Player p = e.getPlayer();
            Location loc = e.getClickedBlock().getLocation();
            KillChest kc = KillChest.KillChestByLocation(loc);
            if (kc == null) {
                return;
            }
            if (!kc.EqualsOwner(p) && !p.hasPermission("deathchest.protection.bypass")) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.CantOpen.replace("%owner", kc.Owner.getName()).replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                e.setCancelled(true);
                return;
            } else if (p.hasPermission("deathchest.protection.bypass") & !kc.EqualsOwner(p)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.ThisChestBelongsTo.replace("%owner", kc.Owner.getName()));
            }
            if (Main.HookedPacketListener) {
                Main.ProtocolManager.SendChestOpenPacket(kc.ChestLocation, p);
            }
            p.openInventory(kc.DeathInventory);
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
            KillChest kc = KillChest.KillChestByLocation(ch.getLocation());
            if(kc == null) {
                return;
            }
            e.setCancelled(true);
            suppressEvent.put(p, kc.DeathInventory);
            if(Main.HookedPacketListener) {
                AnimationManager.Create(p, kc.ChestLocation);
                Main.ProtocolManager.SendChestOpenPacket(kc.ChestLocation, p);
            }
            p.openInventory(kc.DeathInventory);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent e) {
        InventoryHolder ih = e.getInventory().getHolder();
        Player p = (Player) e.getPlayer();
        if(ih instanceof Chest) {
            Chest ch = (Chest) ih;
            Location loc = new Location(ch.getLocation().getWorld(), ch.getLocation().getBlockX(), ch.getLocation().getBlockY(), ch.getLocation().getBlockZ());
            KillChest kc = KillChest.KillChestByLocation(loc);
            if(kc == null) {
                return;
            }
            if(Main.HookedPacketListener) {
                AnimationManager.Remove(p);
                Main.ProtocolManager.SendChestClosePacket(kc.ChestLocation, p);
            }
            if(Helper.ChestEmpty(kc.DeathInventory)) {
                kc.RemoveChest(true);
            }
        } else if (ih instanceof SignHolder) {
            SignHolder sh = (SignHolder) ih;
            Sign sign = sh.getSign();
            Location loc = new Location(sign.getLocation().getWorld(), sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ());
            KillChest dc = KillChest.KillChestByLocation(loc);
            if (dc == null) {
                return;
            }
            if (Main.HookedPacketListener) {
                Main.ProtocolManager.SendChestClosePacket(dc.ChestLocation, p);
            }
            if (Helper.ChestEmpty(dc.DeathInventory)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestRemoved);
                dc.RemoveChest(true);
            }
        }
    }

    private boolean checkRequirements(Player p, Location deathLoc, List<ItemStack> drops) {
        if(drops == null || drops.size() == 0) {
            return false;
        }
        if(Main.MaximumKillChests != -1) {
            if(KillChest.KillChestsByOwner(p).size() >= Main.MaximumKillChests || KillChest.NextAvailableId(p) == -1) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.ReachedMaximumChests.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
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
                if(w == deathLoc.getWorld()) {
                    worldAllowed = true;
                    break;
                }
            }
        }
        if(!worldAllowed) {
            return false;
        }
        boolean placeChest = false;
        if(deathLoc.getBlockY() < 0) {
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingKillChest.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
            return false;
        }
        for(Location currentLoc: Helper.LocationsAround(deathLoc)) {
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
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingKillChest.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
            return false;
        }
        return true;
    }
}
