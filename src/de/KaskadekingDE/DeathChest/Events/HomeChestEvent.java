package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import de.KaskadekingDE.DeathChest.Classes.Helper;
import de.KaskadekingDE.DeathChest.Classes.Tasks.Animation.AnimationManager;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HomeChestEvent implements Listener {

    public static List<Player> setHome = new ArrayList<Player>();
    public static HashMap<Player, Inventory> suppressEvent = new HashMap<Player, Inventory>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if(setHome.contains(p)) {
            if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block b = e.getClickedBlock();
                if(b.getType() == Material.CHEST) {
                    if(Helper.IsDoubleChest(b)) {
                        e.setCancelled(true);
                        return;
                    }
                    Chest ch = (Chest) b.getState();
                    Location loc = new Location(b.getWorld(), b.getLocation().getBlockX(), b.getLocation().getBlockY(), b.getLocation().getBlockZ());
                    if(checkRequirements(loc, p)) {
                        if(Helper.GetChestType(loc) != Helper.ChestState.Default) {
                            p.sendMessage(LangStrings.Prefix + " " + LangStrings.CantUseThisChest);
                            setHome.remove(p);
                            e.setCancelled(true);
                            return;
                        }
                        Inventory inv = Bukkit.createInventory(ch.getInventory().getHolder(), 54, LangStrings.HomeChestInv);
                        HomeChest home = new HomeChest(p, loc, inv);
                        home.SaveHomeChest();
                        p.sendMessage(LangStrings.Prefix + " " + LangStrings.HomeChestSet);
                        setHome.remove(p);
                        e.setCancelled(true);
                    }
                }
            } else if(e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.Cancelled);
                setHome.remove(p);
                e.setCancelled(true);
            }
        }
    }

    private HashMap<Player, ItemStack> doubleClickedItemList = new HashMap<Player, ItemStack>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getInventory().getType() == InventoryType.CHEST) {
            if(e.getInventory().getHolder() instanceof Chest) {
                Chest ch = (Chest) e.getInventory().getHolder();
                Player p = (Player) e.getWhoClicked();
                if(Helper.GetChestType(ch.getLocation()) == Helper.ChestState.HomeChest) {
                    if (e.getRawSlot() != e.getSlot() || e.getInventory().getType() != InventoryType.CHEST) {
                        if(e.isShiftClick()) {
                            e.setCancelled(true);
                        }
                        if(e.getCurrentItem().getType()!=Material.AIR){
                            e.setCancelled(true);
                        }
                    } else {
                        ItemStack doubleClickedItem = null;
                        if(doubleClickedItemList.containsKey(p)) {
                            doubleClickedItem = doubleClickedItemList.get(p);
                        }
                        if(e.getCursor().getType() != Material.AIR && doubleClickedItem != null && e.getCursor() == doubleClickedItem) {
                            e.setCancelled(true);
                        }
                        if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                            doubleClickedItemList.put(p, e.getCurrentItem());
                        }
                    }
                }
            }
        }
    }

    public boolean checkRequirements(Location loc, Player p) {
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
        if(!worldAllowed && !Main.AllowHomeChestInAllWorlds) {
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.CantSetHomeChestInWorld);
            return false;
        }
        if(!Main.ProtectedRegionManager.checkAccess(p, loc))
            return false;
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if(p.hasPermission("deathchest.place.home")) {
            HomeChest hc = HomeChest.HomeChestByPlayer(p);
            if(hc != null && !hc.IsFull()) {
                String w = p.getWorld().getName();
                if(Main.HomeChestOnlyActiveWhitelistedWorlds && !Main.AllowedWorlds.contains(w)) {
                    return;
                }
                Iterator<ItemStack> iter = e.getDrops().iterator();
                List<ItemStack> itemsRemoved = new ArrayList<ItemStack>();
                boolean placedAllItems = true;
                while(iter.hasNext()) {
                    int empty = Helper.EmptySlots(hc.HomeInventory, true);
                    if(empty > 0) {
                        ItemStack drop = iter.next();
                        hc.HomeInventory.addItem(drop);
                        iter.remove();
                        itemsRemoved.add(drop);
                    } else {
                        placedAllItems = false;
                        break;
                    }
                }
                if(placedAllItems) {
                    e.getDrops().clear();
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.LootStoredInHome);
                    hc.SaveHomeChest();
                } else {
                    for(ItemStack drop: itemsRemoved) {
                        if(drop != null) {
                            e.getDrops().remove(drop);
                        }
                    }
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.OnePartPlaced);
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.OnePartPlaced2);
                    hc.SaveHomeChest();
                    DeathChestEvent dce = new DeathChestEvent();
                    dce.onPlayerDeath(e);
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
            HomeChest hc = HomeChest.HomeChestByLocation(ch.getLocation());
            if(hc == null) {
                return;
            }
            if(!hc.EqualsOwner(p) && !p.hasPermission("deathchest.protection.bypass")) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.CantOpen.replace("%type", LangStrings.HomeChest + " " + LangStrings.TypeChest).replace("%owner", hc.Owner.getName()));
                e.setCancelled(true);
                return;
            } else if(p.hasPermission("deathchest.protection.bypass") & !hc.EqualsOwner(p)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.ThisChestBelongsTo.replace("%owner", hc.Owner.getName()));
            }

            e.setCancelled(true);
            suppressEvent.put(p, hc.HomeInventory);
            if(Main.HookedPacketListener) {
                AnimationManager.Create(p, hc.ChestLocation);
                Main.ProtocolManager.SendChestOpenPacket(hc.ChestLocation, p);
            }
            p.openInventory(hc.HomeInventory);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent e) {
        InventoryHolder ih = e.getInventory().getHolder();
        Player p = (Player) e.getPlayer();
        if(ih instanceof Chest) {
            Chest ch = (Chest) ih;
            Location loc = new Location(ch.getLocation().getWorld(), ch.getLocation().getBlockX(), ch.getLocation().getBlockY(), ch.getLocation().getBlockZ());
            HomeChest dc = HomeChest.HomeChestByLocation(loc);
            if(dc == null) {
                return;
            }
            if(Main.HookedPacketListener) {
                AnimationManager.Remove(p);
                Main.ProtocolManager.SendChestClosePacket(dc.ChestLocation, p);
            }
        }
    }
}
