package de.KaskadekingDE.DeathChest.Classes.SolidBlockManager;

import org.bukkit.Location;
import org.bukkit.block.Block;

public interface IBlockManager {

    public boolean IsSolid(Location loc);
    public boolean IsSolid(Block block);
}
