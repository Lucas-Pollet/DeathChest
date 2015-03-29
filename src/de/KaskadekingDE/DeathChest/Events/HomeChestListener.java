package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Config.LangStrings;
import de.KaskadekingDE.DeathChest.Helper;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class HomeChestListener implements Listener {

    public static Set<Player> readyPlayers = new HashSet<Player>();

    public static boolean loaded;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        // worst hack ever :P
        if(!loaded) {
            Main.plugin.getServer().dispatchCommand(Main.plugin.getServer().getConsoleSender(), "dc reload");
            loaded = true;
        }

    }

    @EventHandler
    public void onBlockAction(PlayerInteractEvent e) {

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = e.getClickedBlock();
            if(b.getType() == Material.CHEST) {
                if(readyPlayers.contains(e.getPlayer())) {
                    if(Helper.IsDoubleChest(b)) {
                        e.setCancelled(true);
                        return;
                    }
                    Chest ch = (Chest) b.getState();
                    if(DeathChestListener.homeChest.containsValue(b.getLocation())) {
                        e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.ChestAlreadyUsed);
                        readyPlayers.remove(e.getPlayer());
                        e.setCancelled(true);
                        return;
                    }
                    int x = b.getX();
                    int y = b.getY();
                    int z = b.getZ();
                    World w = b.getWorld();
                    Main.playerData.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + "." + "last-known-name", e.getPlayer().getName());
                    Main.playerData.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + ".home-chest.x", x);
                    Main.playerData.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + ".home-chest.y", y);
                    Main.playerData.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + ".home-chest.z", z);
                    Main.playerData.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + ".home-chest.world", w.getName());
                    Main.playerData.saveConfig();

                    Inventory inv = Bukkit.createInventory(ch.getInventory().getHolder(), 54, "Home Chest");
                    DeathChestListener.chestInventory.put(ch, inv);
                    DeathChestListener.homeChest.put(e.getPlayer(), b.getLocation());
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.HomeChestSet);
                    readyPlayers.remove(e.getPlayer());
                    e.setCancelled(true);
                }
            }
        } else if(e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if(readyPlayers.contains(e.getPlayer())) {
                e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.Cancelled);
                readyPlayers.remove(e.getPlayer());
                e.setCancelled(true);
            }
        }
    }

    private ItemStack doubleClickedItem;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getInventory().getType() == InventoryType.CHEST ) {
            Chest ch;
            try {
                ch = (Chest) e.getInventory().getHolder();
            } catch(ClassCastException ccex) {
                // Chest is a doublechest
                return;
            }
            Player p = (Player) e.getWhoClicked();
            try {
                if(!DeathChestListener.checkForHomeChest(ch.getLocation(),p)) {
                    return;
                }
            } catch(NullPointerException npe) {
                return;
            }

            if (e.getRawSlot() != e.getSlot() || e.getInventory().getType() != InventoryType.CHEST) {
                // Player-Inventory
                if(e.isShiftClick()) {
                    e.setCancelled(true);
                }
                if(e.getCurrentItem().getType()!=Material.AIR){
                    e.setCancelled(true);
                }
            } else {
                if(e.getCursor().getType() != Material.AIR && doubleClickedItem != null &&e.getCursor() == doubleClickedItem) {
                    e.setCancelled(true);
                }
                if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    doubleClickedItem = e.getCurrentItem();
                }
            }


        }
    }

}
