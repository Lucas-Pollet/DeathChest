package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Config.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Set;

public class HomeChestListener implements Listener {

    public static Set<Player> readyPlayers = new HashSet<Player>();

    @EventHandler
    public void onBlockAction(PlayerInteractEvent e) {

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = e.getClickedBlock();
            if(b.getType() == Material.CHEST) {
                if(readyPlayers.contains(e.getPlayer())) {
                    int x = b.getX();
                    int y = b.getY();
                    int z = b.getZ();
                    World w = b.getWorld();
                    Main.plugin.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + ".home-chest.x", x);
                    Main.plugin.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + ".home-chest.y", y);
                    Main.plugin.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + ".home-chest.z", z);
                    Main.plugin.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + ".home-chest.world", w.getName());
                    Main.plugin.saveConfig();
                    Chest ch = (Chest) b.getState();
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

            if (e.getRawSlot() == e.getSlot() && e.getInventory().getType() == InventoryType.CHEST) {
                if(e.getCursor().getType()!=Material.AIR){
                    e.setCancelled(true);
                }
            }
            else {
                if(e.isShiftClick()) {
                    e.setCancelled(true);
                }
                if(e.getCurrentItem().getType()!=Material.AIR){
                    e.setCancelled(true);
                }
            }


        }
    }

}
