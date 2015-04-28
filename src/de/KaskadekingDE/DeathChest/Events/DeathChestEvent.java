package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
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

public class DeathChestEvent implements Listener {

    public static HashMap<Player, Inventory> suppressEvent = new HashMap<Player, Inventory>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Location loc = p.getLocation();
        if (p.getKiller() != null && p.getKiller().hasPermission("deathchest.place.kill")) {
            // Let do killer chest to the work
            return;
        }
        if (p.hasPermission("deathchest.place.death")) {
            if (checkRequirements(p, e.getDrops())) {
                loc = Helper.AvailableLocation(loc);
                Inventory inv = null;
                boolean spawnSign = true;
                if (Main.UseTombstones) {
                    Location blockUnderSign = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                    if(!Main.SolidBlockManager.IsSolid(blockUnderSign)) {
                       if(!Main.SpawnTombstonesOnNonSolid) {
                           if(!Main.SpawnChestIfNotAbleToPlaceTombstone) {
                               p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingDeathChest.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                               return;
                           } else {
                               loc.getBlock().setType(Material.CHEST);
                               Chest deathChest = (Chest) loc.getBlock().getState();
                               inv = Bukkit.getServer().createInventory(deathChest.getInventory().getHolder(), 54, LangStrings.DeathChestInv);
                               spawnSign = false;
                           }
                       }
                    }
                    if(spawnSign) {
                        Block block = loc.getBlock();
                        block.setType(Material.SIGN_POST);
                        Sign sign = (Sign) block.getState();
                        DateFormat dateFormat = new SimpleDateFormat("dd.MM HH:mm:ss");
                        Date date = new Date();
                        String dateString = dateFormat.format(date);
                        String lineOne = LangStrings.LineOne.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.DeathChestInv).replace("%displayname", p.getDisplayName());
                        String lineTwo = LangStrings.LineTwo.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.DeathChestInv).replace("%displayname", p.getDisplayName());;
                        String lineThree = LangStrings.LineThree.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.DeathChestInv).replace("%displayname", p.getDisplayName());;
                        String lineFour = LangStrings.LineFour.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.DeathChestInv).replace("%displayname", p.getDisplayName());;
                        sign.setLine(0, lineOne);
                        sign.setLine(1, lineTwo);
                        sign.setLine(2, lineThree);
                        sign.setLine(3, lineFour);
                        sign.update();
                        SignHolder sh = new SignHolder(LangStrings.DeathChestInv, 54, sign);
                        inv = sh.getInventory();
                    }
                } else {
                    loc.getBlock().setType(Material.CHEST);
                    Chest deathChest = (Chest) loc.getBlock().getState();
                    inv = Bukkit.getServer().createInventory(deathChest.getInventory().getHolder(), 54, LangStrings.DeathChestInv);
                }

                for (ItemStack drop : e.getDrops()) {
                    inv.addItem(drop);
                }
                e.getDrops().clear();
                DeathChest chest = new DeathChest(p, loc, inv);
                chest.SaveDeathChest();
                if (Main.ShowCoords) {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestWithCoords.replace("%x", Integer.toString(loc.getBlockX())).replace("%y", Integer.toString(loc.getBlockY())).replace("%z", Integer.toString(loc.getBlockZ())).replace("%world", loc.getWorld().getName()));
                } else {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestPlaced);
                }
                if (Main.SecondsToRemove > 0) {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.TimeToFindDeathChest.replace("%time", Integer.toString(Main.SecondsToRemove)));
                    chest.RegisterTask(p, loc);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.SIGN_POST) {
            Player p = e.getPlayer();
            Location loc = e.getClickedBlock().getLocation();
            DeathChest dc = DeathChest.DeathChestByLocation(loc);
            if (dc == null) {
                return;
            }
            if (!dc.EqualsOwner(p) && !p.hasPermission("deathchest.protection.bypass")) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.CantOpen.replace("%owner", dc.Owner.getName()).replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                e.setCancelled(true);
                return;
            } else if (p.hasPermission("deathchest.protection.bypass") & !dc.EqualsOwner(p)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.ThisChestBelongsTo.replace("%owner", dc.Owner.getName()));
            }
            if (Main.HookedPacketListener) {
                Main.ProtocolManager.SendChestOpenPacket(dc.ChestLocation, p);
            }
            p.openInventory(dc.DeathInventory);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        if (suppressEvent.containsKey(p)) {
            Inventory inv = suppressEvent.get(p);
            if (inv.equals(e.getInventory())) {
                suppressEvent.remove(p);
                return;
            }
        }
        InventoryHolder ih = e.getInventory().getHolder();
        if (ih instanceof Chest) {
            Chest ch = (Chest) ih;
            DeathChest dc = DeathChest.DeathChestByLocation(ch.getLocation());
            if (dc == null) {
                return;
            }
            if (!dc.EqualsOwner(p) && !p.hasPermission("deathchest.protection.bypass")) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.CantOpen.replace("%owner", dc.Owner.getName()).replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                e.setCancelled(true);
                return;
            } else if (p.hasPermission("deathchest.protection.bypass") & !dc.EqualsOwner(p)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.ThisChestBelongsTo.replace("%owner", dc.Owner.getName()));
            }

            e.setCancelled(true);
            suppressEvent.put(p, dc.DeathInventory);
            if (Main.HookedPacketListener) {
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
        if (ih instanceof Chest) {
            Chest ch = (Chest) ih;
            Location loc = new Location(ch.getLocation().getWorld(), ch.getLocation().getBlockX(), ch.getLocation().getBlockY(), ch.getLocation().getBlockZ());
            DeathChest dc = DeathChest.DeathChestByLocation(loc);
            if (dc == null) {
                return;
            }
            if (Main.HookedPacketListener) {
                AnimationManager.Remove(p);
                Main.ProtocolManager.SendChestClosePacket(dc.ChestLocation, p);
            }
            if (Helper.ChestEmpty(dc.DeathInventory)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestRemoved);
                dc.RemoveChest(true);
            }
        } else if (ih instanceof SignHolder) {
            SignHolder sh = (SignHolder) ih;
            Sign sign = sh.getSign();
            Location loc = new Location(sign.getLocation().getWorld(), sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ());
            DeathChest dc = DeathChest.DeathChestByLocation(loc);
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

    private boolean checkRequirements(Player p, List<ItemStack> drops) {
        HomeChest hc = HomeChest.HomeChestByPlayer(p);
        if (hc != null && !hc.IsFull()) {
            // Let do the home chest event do their work.
            return false;
        }
        if (drops == null || drops.size() == 0) {
            return false;
        }
        Location loc = p.getLocation();
        if (Main.MaximumDeathChests != -1) {
            if (DeathChest.DeathChestsByOwner(p).size() >= Main.MaximumDeathChests || DeathChest.NextAvailableId(p) == -1) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.ReachedMaximumChests.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                return false;
            }
        }
        boolean worldAllowed = false;
        for (String key : Main.AllowedWorlds) {
            if (key.equalsIgnoreCase("*")) {
                worldAllowed = true;
                break;
            }
            World w = Bukkit.getWorld(key);
            if (w != null) {
                if (w == loc.getWorld()) {
                    worldAllowed = true;
                    break;
                }
            }
        }
        if (!worldAllowed) {
            return false;
        }
        if(Main.WorldGuardManager != null && !Main.WorldGuardManager.canPlaceInRegion(loc.getWorld(), loc)) {
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingDeathChest.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
            return false;
        }
        if (p.getLocation().getBlockY() < 0) {
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingDeathChest.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
            return false;
        }
        Location availableLocation = Helper.AvailableLocation(loc);
        if(availableLocation == null) {
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingDeathChest.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
            return false;
        }
        return true;
    }
}
