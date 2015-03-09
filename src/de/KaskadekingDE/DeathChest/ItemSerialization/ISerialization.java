package de.KaskadekingDE.DeathChest.ItemSerialization;

import org.bukkit.inventory.Inventory;

import java.io.EOFException;

public interface ISerialization {
    public String getVersion();

    public String toBase64(Inventory inv);
    public Inventory fromBase64(String string) throws EOFException;
}
