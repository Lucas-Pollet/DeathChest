package de.KaskadekingDE.DeathChest.Classes.WorldGuard;

import org.bukkit.Location;
import org.bukkit.World;

public interface IWorldGuardFlag {

    public void InitWgp();
    public boolean canPlaceInRegion(World w, Location loc);

}
