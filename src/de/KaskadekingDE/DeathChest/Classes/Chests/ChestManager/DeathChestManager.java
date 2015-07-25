package de.KaskadekingDE.DeathChest.Classes.Chests.ChestManager;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DeathChestManager {

    public static List<DeathChest> deathChests = new ArrayList<DeathChest>();

    public static void Add(DeathChest dc) {
        deathChests.add(dc);
    }

    public static void Remove(DeathChest dc) {
        deathChests.remove(dc);
    }

    public static void Remove(Location loc) {
        DeathChest chest = GetByLocation(loc);
        if(chest != null) {
            Remove(chest);
        }
    }

    public static List<DeathChest> GetByOwner(OfflinePlayer p) {
        List<DeathChest> result = new ArrayList<DeathChest>();
        Iterator<DeathChest> deathChestIterator = deathChests.iterator();
        while(deathChestIterator.hasNext()) {
            DeathChest dc = deathChestIterator.next();
            if(dc == null) {
                deathChests.remove(null);
                continue;
            } else if(dc.Owner == null) {
                deathChests.remove(dc);
                continue;
            }
            if(dc.Owner.getUniqueId().equals(p.getUniqueId())) {
                result.add(dc);
            }
        }
        return result;
    }

    public static DeathChest GetByLocation(Location loc) {
        Iterator<DeathChest> deathChestIterator = deathChests.iterator();
        while(deathChestIterator.hasNext()) {
            DeathChest dc = deathChestIterator.next();
            if(dc == null || dc.ChestLocation == null ) {
                deathChests.remove(dc);
                continue;
            }
            if(dc.ChestLocation.equals(loc)) {
                return dc;
            }
        }
        return null;
    }

}
