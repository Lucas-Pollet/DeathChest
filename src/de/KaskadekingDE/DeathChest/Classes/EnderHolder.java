package de.KaskadekingDE.DeathChest.Classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class EnderHolder implements InventoryHolder {
    private Inventory inv;
    private Block enderChest;

    public EnderHolder(String title, int size, Block block) {
        Inventory defaultInv = Bukkit.createInventory(this, size, title);
        setInventory(defaultInv);
        setBlock(block);
    }

    public EnderHolder(String title, int size, Location location) {
        if(location.getBlock().getType() != Material.ENDER_CHEST) {
            throw new IllegalArgumentException("The block at the location must be a ender chest!");
        }
        Block block = location.getBlock();
        Inventory defaultInv = Bukkit.createInventory(this, size, title);
        setInventory(defaultInv);
        setBlock(block);
    }

    public EnderHolder(Inventory inv, Block block) {
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
        return enderChest;
    }

    public void setBlock(Block enderChest) {
        this.enderChest = enderChest;
    }
}
