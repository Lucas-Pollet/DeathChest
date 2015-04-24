package de.KaskadekingDE.DeathChest.Classes.SolidBlockManager.v1_8_R2;

import de.KaskadekingDE.DeathChest.Classes.SolidBlockManager.IBlockManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R2.util.CraftMagicNumbers;

public class SolidBlockManager implements IBlockManager {
    @Override
    public boolean IsSolid(Location loc) {
        Block block = loc.getBlock();
        net.minecraft.server.v1_8_R2.Block b = CraftMagicNumbers.getBlock(block);
        return b.getMaterial().isSolid();
    }

    @Override
    public boolean IsSolid(Block block) {
        net.minecraft.server.v1_8_R2.Block b = CraftMagicNumbers.getBlock(block);
        return b.getMaterial().isSolid();
    }
}
