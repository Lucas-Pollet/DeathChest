package de.KaskadekingDE.DeathChest.Classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SignHolder implements InventoryHolder {

    private Inventory inv;
    private Sign sign;

    public SignHolder(String title, int size, Sign sign) {
        Inventory defaultInv = Bukkit.createInventory(this, size, title);
        setInventory(defaultInv);
        setSign(sign);
    }

    public SignHolder(String title, int size, Location signLocation) {
        if(signLocation.getBlock().getType() != Material.SIGN_POST) {
            throw new IllegalArgumentException("The block at signLocation must be a sign!");
        }
        Sign sign = (Sign) signLocation.getBlock().getState();
        Inventory defaultInv = Bukkit.createInventory(this, size, title);
        setInventory(defaultInv);
        setSign(sign);
    }

    public SignHolder(Inventory inv, Sign sign) {
        setInventory(inv);
        setSign(sign);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    public void setInventory(Inventory inv) {
        this.inv = inv;
    }

    public Sign getSign() {
        return sign;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
    }
}
