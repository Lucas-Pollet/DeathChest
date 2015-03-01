package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Config.LangStrings;
import de.KaskadekingDE.DeathChest.ItemSerialization;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
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
    public static HashMap<Chest, Inventory> chestInventory = new HashMap<Chest, Inventory>();
    public static HashMap<Player, Inventory> suppressInventoryOpenEvent = new HashMap<Player, Inventory>();
    public static HashMap<Location, Integer> chestRemover = new HashMap<Location, Integer>();
    public static HashMap<Player, Location> homeChest = new HashMap<Player, Location>();


    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent e) {
        if(!(e.getEntity() instanceof Player)) return;
        if(e.getDrops() == null ||e.getDrops().size() == 0) {
            return;
        }
        if(e.getEntity().getKiller() == null) {
            if(placeDeathChest((Player)e.getEntity(), e.getEntity().getLocation(), e.getDrops())) {
                e.getDrops().clear();
            }
        } else {
            if(placeKillerChest((Player) e.getEntity(), e.getEntity().getKiller(), e.getEntity().getLocation(), e.getDrops())) {
                e.getDrops().clear();
            }
        }
    }

    private boolean placeDeathChest(final Player p, Location loc, List<ItemStack> drops) {
        if(Main.UsePermission && !p.hasPermission("deathchest.place")) {
            return false;
        }
        Location homeLoc = homeChestLoc(p);
        if(homeLoc != null) {
            Chest chest = (Chest)homeLoc.getWorld().getBlockAt(homeLoc).getState();
            int empty = emptySlots(chest, true);
            if(empty > drops.size()) {
                Inventory homeInv = chestInventory.get(chest);
                for(ItemStack drop: drops) {
                    homeInv.addItem(drop);
                }
                drops.clear();
                p.sendMessage(Main.Prefix + " " + LangStrings.StoredInHomeChest);
                String base = ItemSerialization.toBase64(homeInv);
                Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + ".home-chest.inventory", base);
                return true;
            }
        }
        if(playersDeathChests(p.getUniqueId().toString()) != null && Main.MaxChests != -1 && playersDeathChests(p.getUniqueId().toString()).size() >= Main.MaxChests) {
            p.sendMessage(Main.Prefix + " " + LangStrings.MaxExceeded);
            return false;
        }

        Location deathLoc = loc;
        boolean placingSuccessful = false;
        if(Main.OnlyReplaceWhitelistedBlocks) {
            for(Object b: Main.whitelistedBlocks) {
                String block = b.toString();
                Material material = Material.getMaterial(block);
                if(deathLoc.getBlock().getType() == material) {
                    placingSuccessful = true;
                }
            }
            if(!placingSuccessful) {
                p.sendMessage(Main.Prefix + " " + LangStrings.FailedPlacing);
                return false;
            }

        }
        deathLoc.getWorld().getBlockAt(deathLoc).setType(Material.CHEST);
        final Location blockLoc = deathLoc.getWorld().getBlockAt(deathLoc).getLocation();
        Chest deathChest = (Chest) deathLoc.getWorld().getBlockAt(deathLoc).getState();
        Inventory inv = Bukkit.getServer().createInventory(deathChest.getInventory().getHolder(), 54, "DeathChest");
        for(ItemStack drop: drops) {
            inv.addItem(drop);
        }
        drops.clear();
        chestInventory.put(deathChest, inv);
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
            if(countObj.toString() == "home-chest") {
                countObj = countArray[countArray.length-2];
            }
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
        String base = ItemSerialization.toBase64(inv);
        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + count + ".x", x);
        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + count + ".y", y);
        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + count + ".z", z);
        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + count + ".world", p.getWorld().getName());
        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + count + ".inventory", base);
        Main.plugin.saveConfig();
        if(Main.RemoveChestAfterXSeconds) {

            p.sendMessage(Main.Prefix + " " + LangStrings.TimeStarted);
            int task = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
                @Override
                public void run() {
                    if(blockLoc.getWorld().getBlockAt(blockLoc).getType() != Material.AIR) {
                        blockLoc.getWorld().getBlockAt(blockLoc).setType(Material.AIR);
                        chestInventory.remove(blockLoc.getWorld().getBlockAt(blockLoc).getState());
                        int dcc = DeathChestCount(blockLoc, p.getUniqueId());
                        deathChests.get(p).remove(blockLoc);
                        chestInventory.remove(blockLoc.getWorld().getBlockAt(blockLoc).getState());
                        Main.plugin.getConfig().set("death-chests." + p.getUniqueId() + "." + dcc, null);
                        Main.plugin.saveConfig();
                        chestRemover.remove(blockLoc);
                        p.sendMessage(Main.Prefix + " " + LangStrings.TimeOver);
                    }
                }
            }, Main.Seconds * 20L);
            chestRemover.put(blockLoc, task);
        }
        return true;
    }

    private boolean placeKillerChest(final Player p, final Player killer, Location loc, List<ItemStack> drops) {
        if (!killer.hasPermission("deathchest.kill.place")) {
            return false;
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
                return false;
            }
        }
        deathLoc.getWorld().getBlockAt(deathLoc).setType(Material.CHEST);
        final Location blockLoc = deathLoc.getWorld().getBlockAt(deathLoc).getLocation();
        Chest deathChest = (Chest) deathLoc.getWorld().getBlockAt(deathLoc).getState();
        Inventory inv = Bukkit.getServer().createInventory(deathChest.getInventory().getHolder(), 54, "DeathChest");
        for(ItemStack drop: drops) {
            inv.addItem(drop);
        }
        drops.clear();
        chestInventory.put(deathChest, inv);
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
        String base = ItemSerialization.toBase64(inv);
        Main.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count + ".x", x);
        Main.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count + ".y", y);
        Main.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count + ".z", z);
        Main.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count + ".world", killer.getWorld().getName());
        Main.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + count + ".inventory", base);
        Main.killerConfig.saveKillerConfig();
        killer.sendMessage(Main.Prefix + " " + LangStrings.VictimsLootStored);
        if(Main.RemoveChestAfterXSeconds) {
            killer.sendMessage(Main.Prefix + " " + LangStrings.TimeStartedKiller);
            int task = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
                @Override
                public void run() {
                    if(blockLoc.getWorld().getBlockAt(blockLoc).getType() != Material.AIR) {
                        blockLoc.getWorld().getBlockAt(blockLoc).setType(Material.AIR);
                        chestInventory.remove(blockLoc.getWorld().getBlockAt(blockLoc).getState());
                        int dcc = KillerChestCount(blockLoc, killer.getUniqueId());
                        killerChests.get(killer).remove(blockLoc);
                        chestInventory.remove(blockLoc.getWorld().getBlockAt(blockLoc).getState());
                        Main.plugin.killerConfig.getKillerConfig().set("death-chests." + killer.getUniqueId() + "." + dcc, null);
                        Main.plugin.killerConfig.saveKillerConfig();
                        killer.sendMessage(Main.Prefix + " " + LangStrings.TimeOverKiller);
                    }
                }
            }, Main.Seconds * 20L);
            chestRemover.put(blockLoc, task);
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if(suppressInventoryOpenEvent.containsKey(e.getPlayer())) {
            if(suppressInventoryOpenEvent.get(e.getPlayer()).equals(e.getInventory())) {
                suppressInventoryOpenEvent.remove(e.getPlayer());
                return;
            }
        }
        if(e.getInventory().getHolder() instanceof Chest) {
            Location loc = ((Chest) e.getInventory().getHolder()).getLocation();
            Player p = (Player) e.getPlayer();
            Player p2 = getKeyForDeathChest(loc);
            if(p2 == null) {
                p2 = getKeyForKillerChest(loc);
                if(p2 == null) {
                    p2 = getKeyForHomeChest(loc);
                    if(p2 == null) {
                        return;
                    }
                }
            }
            if(checkForDeathChest(p2, loc) || checkForHomeChest(loc, p2)) {
                e.setCancelled(true);
                openDeathChest(p, loc, e);
            } else {
                e.setCancelled(true);
                openKillerChest(p, loc, e);

            }
        }
    }

    private void openDeathChest(Player p, Location loc, InventoryOpenEvent e) {
        if(Main.UsePermission && !p.hasPermission("deathchest.place")) {
            return;
        }
        Player ownerHome = getKeyForHomeChest(loc);
        Location homeLoc = homeChestLoc(p);
        if(ownerHome != null) {
            if(!ownerHome.equals(p) && !p.hasPermission("deathchest.protection.bypass")) {
                p.sendMessage(Main.Prefix + " " + LangStrings.NotOwner);
                e.setCancelled(true);
                return;
            }
            if(homeLoc == null) return;
            Chest chest = (Chest)homeLoc.getWorld().getBlockAt(homeLoc).getState();
            Inventory inv = chestInventory.get(chest);
            suppressInventoryOpenEvent.put(p, inv);
            p.openInventory(inv);
            return;
        }
        Player owner = getKeyForDeathChest(loc);
        if(owner != null) {
            if(Main.ProtectedChest && !p.equals(owner) && !p.hasPermission("deathchest.protection.bypass")) {
                p.sendMessage(Main.Prefix + " " + LangStrings.NotOwner);
            } else {
                Inventory inv = chestInventory.get(loc.getWorld().getBlockAt(loc).getState());
                suppressInventoryOpenEvent.put(p, inv);
                p.openInventory(inv);
            }

        }
    }

    private void openKillerChest(Player p, Location loc, InventoryOpenEvent e) {
        if(Main.UsePermission && !p.hasPermission("deathchest.kill.place")) {
            return;
        }
        p.closeInventory();
        Inventory inv = chestInventory.get(loc.getWorld().getBlockAt(loc).getState());
        suppressInventoryOpenEvent.put(p, inv);
        p.openInventory(inv);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent e) {
        if(e.getInventory().getType() == InventoryType.CHEST){
            InventoryHolder ih;
            Chest chest;
            try {
                ih = e.getInventory().getHolder();
                chest = (Chest) ih;
            } catch(ClassCastException ccex) {
                // Don't now why this exception sometimes occures.
                return;
            }

            Location loc = chest.getLocation();
            if(getKeyForDeathChest(loc) == null && getKeyForKillerChest(loc) == null && getKeyForHomeChest(loc) == null) return;
            Player p = getKeyForDeathChest(loc);
            if(p == null) {
                p = getKeyForKillerChest(loc);
                if(p == null) {
                    p = getKeyForHomeChest(loc);
                }
            }
            Player p2= (Player) e.getPlayer();
            if(checkForDeathChest(p, loc)) {
                Inventory inv = chestInventory.get((loc.getWorld().getBlockAt(loc).getState()));
                for(ItemStack item: inv.getContents()) {
                    if(item != null) {
                        return;
                    }
                }
                closeDeathChest(p,p2, ((Chest) e.getInventory().getHolder()).getLocation(), e.getInventory().getHolder(), inv.getContents());
            } else if(checkForKillerChest(loc, p)) {
                Inventory inv = chestInventory.get((loc.getWorld().getBlockAt(loc).getState()));
                for(ItemStack item: inv.getContents()) {
                    if(item != null) {
                        return;
                    }
                }
                closeKillerChest(p, p2,((Chest) e.getInventory().getHolder()).getLocation(), e.getInventory().getHolder(), inv.getContents());
            } else if(checkForHomeChest(loc, p)) {

            }

        }
    }

    public void closeDeathChest(Player owner, Player p, Location loc, InventoryHolder ih,ItemStack[] items) {
        if(Main.UsePermission && !p.hasPermission("deathchest.place")) {
            return;
        }
        for(Location loc2: playersDeathChests(owner.getUniqueId().toString())) {
            if(loc != null) {
                if(loc.equals(loc2)) {
                    if(Main.RemoveChestAfterXSeconds && chestRemover.containsKey(loc)) {
                        Integer task = chestRemover.get(loc);
                        Bukkit.getScheduler().cancelTask(task);
                        chestRemover.remove(loc);
                    }

                    Chest c = (Chest) ih;
                    c.getBlock().setType(Material.AIR);
                    int dcc = DeathChestCount(loc, p.getUniqueId());
                    deathChests.get(owner).remove(loc);
                    chestInventory.remove(loc.getWorld().getBlockAt(loc).getState());
                    Main.plugin.getConfig().set("death-chests." + owner.getUniqueId() + "." + dcc, null);
                    Main.plugin.saveConfig();
                    p.sendMessage(Main.Prefix + " " + LangStrings.ChestRemoved);
                    break;
                } else {
                    continue;
                }
            }
        }
    }

    public void closeKillerChest(Player owner, Player killer, Location loc, InventoryHolder ih, ItemStack[] items) {
        if(!killer.hasPermission("deathchest.place")) {
            return;
        }
        if(killerChests.get(owner).contains(loc)) {
            if(Main.RemoveChestAfterXSeconds && chestRemover.containsKey(loc)) {
                Integer task = chestRemover.get(loc);
                Bukkit.getScheduler().cancelTask(task);
                chestRemover.remove(loc);
            }
            Chest c = (Chest) ih;
            c.getBlock().setType(Material.AIR);
            killerChests.remove(loc);
            chestInventory.remove(loc.getWorld().getBlockAt(loc).getState());
            int count = KillerChestCount(loc, owner.getUniqueId());
            Main.plugin.killerConfig.getKillerConfig().set("death-chests." + owner.getUniqueId() + "." + count, null);
            Main.plugin.killerConfig.saveKillerConfig();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        if(e.getBlock().getType() == Material.CHEST) {
            if(getKeyForHomeChest(e.getBlock().getLocation()) != null) {
                Player key = getKeyForHomeChest(loc);
                if(!key.equals(e.getPlayer())) {
                    e.setCancelled(true);
                    return;
                }
                Chest chest = (Chest)loc.getWorld().getBlockAt(loc).getState();
                Inventory inv = chestInventory.get(chest);
                for(ItemStack stack: inv.getContents()) {
                    if(stack != null) {
                        e.getPlayer().sendMessage(Main.Prefix + " " + LangStrings.RemoveBeforeBreak);
                        e.setCancelled(true);
                        return;
                    }
                }
                e.getPlayer().sendMessage(Main.Prefix + " " + LangStrings.Removed);
                chestInventory.remove(chest);
                homeChest.remove(e.getPlayer());
                Main.plugin.getConfig().set("death-chests." + e.getPlayer().getUniqueId() + ".home-chest", null);
                Main.plugin.saveConfig();
            }
            if(getKeyForDeathChest(e.getBlock().getLocation()) != null | getKeyForKillerChest(e.getBlock().getLocation()) != null) {
                e.getPlayer().sendMessage(Main.Prefix + " " + LangStrings.DontBreak);
                e.setCancelled(true);
            }
        }
    }

    public static int DeathChestCount(Location loc, UUID uuid) {
        try {
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
        } catch(NullPointerException ex) {
            return -1;
        }

        return -1;
    }

    public static int KillerChestCount(Location loc, UUID uuid) {
        for(String key: Main.plugin.killerConfig.getKillerConfig().getConfigurationSection("death-chests." + uuid).getKeys(false)) {
            try {
                int x = Main.plugin.killerConfig.getKillerConfig().getInt("death-chests." + uuid + "." + key + ".x");
                int y = Main.plugin.killerConfig.getKillerConfig().getInt("death-chests." + uuid + "." + key + ".y");
                int z = Main.plugin.killerConfig.getKillerConfig().getInt("death-chests." + uuid + "." + key + ".z");
                String w = Main.plugin.killerConfig.getKillerConfig().getString("death-chests." + uuid + "." + key + ".world");
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
                if(key.equals("home-chest")) continue;
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

    public static Player getKeyForDeathChest(Location loc) {
        for(Player p: deathChests.keySet()) {
            if(deathChests.get(p).contains(loc)) {
                return p;
            }
        }
        return null;
    }

    private Player getKeyForKillerChest(Location loc) {
        for(Player p: killerChests.keySet()) {
            if(killerChests.get(p).contains(loc)) {
                return p;
            }
        }
        return null;
    }

    private Player getKeyForHomeChest(Location loc) {
        for(Player p: homeChest.keySet()) {
            if(homeChest.get(p).equals(loc)) {
                return p;
            }
        }
        return  null;
    }


    public static boolean checkForDeathChest(Player p, Location loc) {
        if(deathChests.containsKey(p)) {
            if(deathChests.get(p).contains(loc)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkForKillerChest(Location loc, Player p) {
        if(killerChests.containsKey(p)) {
            if(killerChests.get(p).contains(loc)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkForHomeChest(Location loc, Player p) {
        if(homeChest.containsKey(p)) {
            if(homeChest.get(p).equals(loc)) {
                return true;
            }
        }
        return false;
    }


    private Location homeChestLoc(Player p) {
        if(homeChest.containsKey(p)) {
            return homeChest.get(p);
        }
        return null;
    }

    private int emptySlots(Chest c, boolean doubleChest) {
        int slots;
        if(doubleChest) {
            slots = 54;
        } else {
            slots = 27;
        }
        for(ItemStack is: c.getInventory().getContents()) {
            if(is != null) {
                slots--;
            }
        }
        return slots;
    }
}
