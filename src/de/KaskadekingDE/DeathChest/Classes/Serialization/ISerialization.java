package de.KaskadekingDE.DeathChest.Classes.Serialization;

import org.bukkit.inventory.Inventory;

import java.io.EOFException;

public interface ISerialization {
    public String getVersion();

    public String toBase64(Inventory inv);
    public Inventory fromBase64(String string);
}
