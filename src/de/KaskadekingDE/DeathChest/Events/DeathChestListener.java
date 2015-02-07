package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Config.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.block.Chest;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DeathChestListener implements Listener {

    public static HashMap<Player, List<Location>> deathChests = new HashMap<Player, List<Location>>();
    public static HashMap<Player, List<Location>> killerChests = new HashMap<Player, List<Location>>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent e) {
        if(!(e.getEntity() instanceof Player)) return;
        for(ItemStack item: e.getDrops()) {
            if(item == null) {
                return;
            } else {
                break;
            }
        }
        if(e.getEntity().getKiller() == null) {
            placeDeathChest((Player)e.getEntity(), e.getEntity().getLocation(), e.getDrops());
            if(e.getEntity().hasPermission("deathchest.place"))
                 e.getDrops().clear();
        } else {
            placeKillerChest((Player)e.getEntity(), e.getEntity().getKiller(), e.getEntity().getLocation(), e.getDrops());
            if(e.getEntity().hasPermission("deathchest.kill.place"))
                e.getDrops().clear();
        }
    }

    private void placeDeathChest(Player p, Location loc, List<ItemStack> drops) {
        if(Main.UsePermission && !p.hasPermission("deathchest.place")) {
            return;
        }
        if(playersDeathChests(p.getUniqueId().toString()) != null && Main.MaxChests != -1 && playersDeathChests(p.getUniqueId().toString()).size() >= Main.MaxChests) {
            p.sendMessage(Main.Prefix + " " + LangStrings.MaxExceeded);
            return;
        }

        Location deathLoc = loc;
        boolean placeingSuccessful = false;
        if(Main.OnlyReplaceWhitelistedBlocks) {
            for(Object b: Main.whitelistedBlocks) {
                String block = b.toString();
                Material material = Material.getMaterial(block);
                if(deathLoc.getBlock().getType() == material) {
                    placeingSuccessful = true;
                }
            }
            if(!placeingSuccessful) {
                p.sendMessage(Main.Prefix + " " + LangStrings.FailedPlacing);
                return;
            }

        }
        deathLoc.getWorld().getBlockAt(deathLoc).setType(Material.CHEST);
        Location blockLoc = deathLoc.getWorld().getBlockAt(deathLoc).getLocation();
        Chest deathChest = (Chest) deathLoc.getWorld().getBlockAt(deathLoc).getState();
        Inventory chestInv = deathChest.getInventory();
        for(ItemStack drop: drops) {
            chestInv.addItem(drop);
        }
        drops.clear();
        if(Main.ShowCoords) {
            String message = LangStrings.ChestSpawned.replace("%x", Integer.toString(blockLoc.getBlockX())).replace("%y", Integer.toString(blockLoc.getBlockY())).replace("%z", Integer.toString(blockLoc.getBlockZ()));
            p.sendMessage(Main.Prefix + " " + message);
        }
        int x = blockLoc.getBlockX();
        int y = blockLoc.getBlockY();
        int z = blockLoc.getBlockZ();
        int count;
        try {
            Object[] countArray = Main.plugin.getConfig().getConfigurationSection("death-chests." + p.getUniqueId()).getKeys(false).toArray();
            Object countObj = countArray[countArray.length-1];
            count = Integer.parseInt(countObj.toString());
            count++;
        } catch(NullPointerException npe) {
            count = 0;
        } catch(ArrayIndexOutOfBoundsException aioobe) {
            count = 0;
        }
        List<Location> locations;
        locations = deathChests.get(p);
        if(locations == null)
            locations = new ArrayList<Location>();
        locations.add(blockLoc);
        deathChests.put(p, locations);
        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + count + ".x", x);
        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + count + ".y", y);
        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + count + ".z", z);
        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + count + ".world", p.getWorld().getName());
        Main.plugin.saveConfig();
    }

    private void placeKillerChest(Player p, Player killer, Location loc, List<ItemStack> drops) {
        if (!killer.hasPermission("deathchest.kill.place")) {
            return;
        }
        Location deathLoc = loc;
        boolean placeingSuccessful = false;
        if(Main.OnlyReplaceWhitelistedBlocks) {
            for(Object b: Main.whitelistedBlocks) {
                String block = b.toString();
                Material material = Material.getMaterial(block);
                if(deathLoc.getBlock().getType() == material) {
                    placeingSuccessful = true;
                }
            }
            if(!placeingSuccessful) {
                killer.sendMessage(Main.Prefix + " " + LangStrings.FailedPlacing);
                return;
            }
        }

        deathLoc.getWorld().getBlockAt(deathLoc).setType(Material.CHEST);
        Location blockLoc = deathLoc.getWorld().getBlockAt(deathLoc).getLocation();
        Chest deathChest = (Chest) deathLoc.getWorld().getBlockAt(deathLoc).getState();
        Inventory chestInv = deathChest.getInventory();
        for(ItemStack drop: drops) {
            chestInv.addItem(drop);
        }

        int x = blockLoc.getBlockX();
        int y = blockLoc.getBlockY();
        int z = blockLoc.getBlockZ();
        int count;
        try {
            Object[] countArray = Main.killerConfig.getKillerConfig().getConfigurationSection("death-chests." + killer.getUniqueId()).getKeys(false).toArray();
            Object countObj = countArray[countArray.length-1];
            count = Integer.parseInt(countObj.toString());
            count++;
        } catch(NullPointerException npe) {
            count = 0;
        } catch(ArrayIndexOutOfBoundsException aioobe) {
            count = 0;
        }
        List<Location> tmp;
        if(killerChests == null)
            killerChests = new HashMap<Player, List<Location>>();
        if(!killerChests.containsKey(killer)) {
            tmp = new ArrayList<Location>();
        } else {
            tmp = killerChests.get(killer);
        }
        tmp.add(blockLoc);
        killerChests.put(killer, tmp);
        Main.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count + ".x", x);
        Main.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count + ".y", y);
        Main.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count + ".z", z);
        Main.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count + ".world", killer.getWorld().getName());
        Main.killerConfig.saveKillerConfig();
        killer.sendMessage(Main.Prefix + " " + LangStrings.VictimsLootStored);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if(e.getInventory().getHolder() instanceof Chest) {
            Location loc = ((Chest) e.getInventory().getHolder()).getLocation();
            Player p = (Player) e.getPlayer();
            if(!checkForDeathChest(p, loc) && !checkForKillerChest(loc, p))
                return;
            if(checkForDeathChest(p, loc)) {
                openDeathChest(p,  loc, e);
            }
        }
    }

    private void openDeathChest(Player p, Location loc, InventoryOpenEvent e) {
        if(Main.UsePermission && !p.hasPermission("deathchest.place")) {
            return;
        }
        if(!Main.ProtectedChest)
            return;
        Player owner = getKey(loc);
        if(owner != null) {
            if(!p.equals(owner) && !p.hasPermission("deathchest.protection.bypass")) {
                p.sendMessage(Main.Prefix + " " + LangStrings.NotOwner);
                e.setCancelled(true);
                return;
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent e) {
        if(e.getInventory().getHolder() instanceof Chest){
            Player p = (Player) e.getPlayer();
            if(checkForDeathChest(p, ((Chest) e.getInventory().getHolder()).getLocation())) {
                closeDeathChest(p, ((Chest) e.getInventory().getHolder()).getLocation(), e.getInventory().getHolder(), e.getInventory().getContents());
            } else if(checkForKillerChest(((Chest) e.getInventory().getHolder()).getLocation(), p)) {
                closeKillerChest(p,((Chest) e.getInventory().getHolder()).getLocation(), e.getInventory().getHolder(), e.getInventory().getContents());
            }

        }
    }

    public void closeDeathChest(Player p, Location loc, InventoryHolder ih,ItemStack[] items) {
        if(Main.UsePermission && !p.hasPermission("deathchest.place")) {
            return;
        }
        if(playersDeathChests(p.getUniqueId().toString()) == null)
            return;
        for(Location loc2: playersDeathChests(p.getUniqueId().toString())) {
            if(loc != null) {
                Location curChest = loc;
                if(curChest.equals(loc)) {
                    for(ItemStack item: items) {
                        if(item != null)
                            return;
                    }
                    Chest c = (Chest) ih;
                    c.getBlock().setType(Material.AIR);
                    int dcc = DeathChestCount(loc, p.getUniqueId());
                    deathChests.get(p).remove(loc);
                    Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + dcc, null);
                    Main.plugin.saveConfig();
                    p.sendMessage(Main.Prefix + " " + LangStrings.ChestRemoved);
                    break;
                } else {
                    continue;
                }
            }
        }
    }

    public void closeKillerChest(Player killer, Location loc, InventoryHolder ih, ItemStack[] items) {
        if(!killer.hasPermission("deathchest.place")) {
            return;
        }
        if(killerChests.get(killer).contains(loc)) {
            for(ItemStack item: items) {
                if(item != null)
                    return;
            }
            Chest c = (Chest) ih;
            c.getBlock().setType(Material.AIR);
            killerChests.remove(loc);
            int count;
            try {
                Object[] countArray = Main.killerConfig.getKillerConfig().getConfigurationSection("death-chests." + killer.getUniqueId()).getKeys(false).toArray();
                Object countObj = countArray[countArray.length-1];
                count = Integer.parseInt(countObj.toString());
                count++;
            } catch(NullPointerException npe) {
                count = 0;
            } catch(ArrayIndexOutOfBoundsException aioobe) {
                count = 0;
            }
            Main.plugin.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count, null);
            Main.plugin.killerConfig.saveKillerConfig();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.getBlock().getType() == Material.CHEST) {
            if(deathChests.containsValue(e.getBlock().getLocation()) || killerChests.get(e.getPlayer()).contains(e.getBlock().getLocation())) {
                e.getPlayer().sendMessage(Main.Prefix + " " + LangStrings.DontBreak);
                e.setCancelled(true);
            }
        }
    }

    private int DeathChestCount(Location loc, UUID uuid) {
        int count = Main.plugin.getConfig().getInt("death-chests." + uuid + ".count");
        for(String key: Main.plugin.getConfig().getConfigurationSection("death-chests." + uuid).getKeys(false)) {
            try {
                int x = Main.plugin.getConfig().getInt("death-chests." + uuid + "." + key + ".x");
                int y = Main.plugin.getConfig().getInt("death-chests." + uuid + "." + key + ".y");
                int z = Main.plugin.getConfig().getInt("death-chests." + uuid + "." + key + ".z");
                String w = Main.plugin.getConfig().getString("death-chests." + uuid + "." + key + ".world");
                World world = Bukkit.getWorld(w);
                Location loc2 = new Location(world, x, y, z, 0.0F, 0.0F);
                int result;
                try {
                    result = Integer.parseInt(key);
                } catch(NumberFormatException ex) {
                    continue;
                }
                if(loc2.equals(loc)) {
                    return result;
                } else {
                    continue;
                }
            } catch(NullPointerException npe) {
                continue;
            } catch(IllegalArgumentException iae) {
                continue;
            }
        }
        return -1;
    }

    private ArrayList<Location> playersDeathChests(String sUuid) {
        ArrayList<Location> result = new ArrayList<Location>();
        try {
            for(String key: Main.plugin.getConfig().getConfigurationSection("death-chests." + sUuid).getKeys(false)) {
                int x;
                int y;
                int z;
                String w;
                try {
                    x = Main.plugin.getConfig().getInt("death-chests." + sUuid + "." + key + ".x");
                    y = Main.plugin.getConfig().getInt("death-chests." + sUuid + "." + key + ".y");
                    z = Main.plugin.getConfig().getInt("death-chests." + sUuid + "." + key + ".z");
                    w = Main.plugin.getConfig().getString("death-chests." + sUuid + "." + key + ".world");
                } catch(NullPointerException npe) {
                    continue;
                }

                World world;
                try {
                    world = Bukkit.getWorld(w);
                } catch(IllegalArgumentException iae) {
                    continue;
                }
                Location tmp = new Location(world, x, y, z, 0.0F, 0.0F);
                result.add(tmp);

            }
        } catch(NullPointerException npe) {
            return null;
        }

        return result;
    }

    private Player getKey(Location loc) {
        for(Player p: deathChests.keySet()) {
            if(deathChests.get(p).contains(loc)) {
                return p;
            }
        }
        return null;
    }

    private boolean checkForDeathChest(Player p, Location loc) {
        if(deathChests.containsKey(p)) {
            if(deathChests.get(p).contains(loc)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkForKillerChest(Location loc, Player p) {
        if(killerChests.get(p).contains(loc)) {
            return true;
        }
        return false;
    }
}
