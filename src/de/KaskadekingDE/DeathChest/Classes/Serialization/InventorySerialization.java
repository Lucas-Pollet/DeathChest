package de.KaskadekingDE.DeathChest.Classes.Serialization;

import de.KaskadekingDE.DeathChest.Classes.Helper;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.inventory.Inventory;

public class InventorySerialization {

    public String Serialize(Inventory inv) {
        String json = de.KaskadekingDE.DeathChest.Classes.Serialization.JSON.InventorySerialization.serializeInventoryAsString(inv);
        return ToBase64(json);
    }

    public Inventory Deserialize(String base64) {
        String json = FromBase64(base64);
        return Helper.InventoryFromItemStack(de.KaskadekingDE.DeathChest.Classes.Serialization.JSON.InventorySerialization.getInventory(json, 54));
    }

    private String ToBase64(String org) {
        byte[] bytesEncoded = Base64.encodeBase64(org.getBytes());
        return new String(bytesEncoded);
    }

    private String FromBase64(String base) {
        byte[] bytesDecoded = Base64.decodeBase64(base.getBytes());
        return new String(bytesDecoded);
    }
}
