package de.KaskadekingDE.DeathChest.Classes.Chests.ChestManager;

import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HomeChestManager {
    public static List<HomeChest> homeChests = new ArrayList<HomeChest>();

    public static void Add(HomeChest dc) {
        homeChests.add(dc);
    }

    public static void Remove(HomeChest dc) {
        homeChests.remove(dc);
    }

    public static void Remove(Location loc) {
        HomeChest chest = GetByLocation(loc);
        if(chest != null) {
            Remove(chest);
        }
    }

    public static HomeChest GetByOwner(OfflinePlayer p) {
        for(HomeChest hc: homeChests) {
            if(hc.Owner.getUniqueId().equals(p.getUniqueId())) {
                return hc;
            }
        }
        return null;
    }

    public static HomeChest GetByLocation(Location loc) {
        for(HomeChest dc: homeChests) {
            if(dc == null || dc.ChestLocation == null ) {
                continue;
            }
            if(dc.ChestLocation.equals(loc)) {
                return dc;
            }
        }
        return null;
    }
}
