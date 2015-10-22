package de.KaskadekingDE.DeathChest.Classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BlockHolder implements InventoryHolder {
    private Inventory inv;
    private Block block;

    public BlockHolder(String title, int size, Block block) {
        Inventory defaultInv = Bukkit.createInventory(this, size, title);
        setInventory(defaultInv);
        setBlock(block);
    }

    public BlockHolder(String title, int size, Location location) {
        if(location.getBlock().getType() == Material.AIR)
            throw new UnsupportedOperationException("The block at " + location + " is AIR");
        Block block = location.getBlock();
        Inventory defaultInv = Bukkit.createInventory(this, size, title);
        setInventory(defaultInv);
        setBlock(block);
    }

    public BlockHolder(Inventory inv, Block block) {
        setInventory(inv);
        setBlock(block);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    public void setInventory(Inventory inv) {
        this.inv = inv;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
