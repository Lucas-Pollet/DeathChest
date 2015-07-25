package de.KaskadekingDE.DeathChest.Classes.Chests.ChestManager;

import de.KaskadekingDE.DeathChest.Classes.Chests.KillChest;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KillChestManager {
    public static List<KillChest> killChests = new ArrayList<KillChest>();

    public static void Add(KillChest kc) {
        killChests.add(kc);
    }

    public static void Remove(KillChest kc) {
        killChests.remove(kc);
    }

    public static void Remove(Location loc) {
        KillChest chest = GetByLocation(loc);
        if(chest != null) {
            Remove(chest);
        }
    }

    public static List<KillChest> GetByOwner(OfflinePlayer p) {
        List<KillChest> result = new ArrayList<KillChest>();
        for(KillChest kc: killChests) {
            if(kc.Owner.getUniqueId().equals(p.getUniqueId())) {
                result.add(kc);
            }
        }
        return result;
    }

    public static KillChest GetByLocation(Location loc) {
        Iterator<KillChest> iter = killChests.iterator();
        while(iter.hasNext()) {
            KillChest kc = iter.next();
            if(kc == null || kc.ChestLocation == null ) {
                continue;
            }
            if(kc.ChestLocation.equals(loc)) {
                return kc;
            }
        }
        return null;
    }

}
