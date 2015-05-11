package de.KaskadekingDE.DeathChest.Classes.WorldGuard;

import org.bukkit.Location;
import org.bukkit.World;

public interface IWorldGuardFlag {

    //public boolean allowOpen = false;

    public void InitWgp();
    //public boolean canOpenInRegion(World w, Location loc);
    public boolean canPlaceInRegion(World w, Location loc);
}
