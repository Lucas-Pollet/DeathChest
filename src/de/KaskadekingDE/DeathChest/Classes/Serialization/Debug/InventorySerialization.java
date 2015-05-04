package de.KaskadekingDE.DeathChest.Classes.Serialization.Debug;

import de.KaskadekingDE.DeathChest.Classes.Serialization.ISerialization;
import net.minecraft.server.v1_8_R2.NBTBase;
import net.minecraft.server.v1_8_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.NBTTagList;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.lang.reflect.Method;

// For 1.8.3
public class InventorySerialization implements ISerialization {
    private Method WRITE_NBT;
    private Method READ_NBT;

    @Override
    public String getVersion() {
        return "v1_8_R2_Debug";
    }

    @Override
    public String toBase64(Inventory inventory) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.getSize(); i++) {
            NBTTagCompound outputObject = new NBTTagCompound();
            CraftItemStack craft = getCraftVersion(inventory.getItem(i));
            if (craft != null)
                CraftItemStack.asNMSCopy(craft).save(outputObject);
            itemList.add(outputObject);
        }
        writeNbt(itemList, dataOutput);
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    @Override
    public Inventory fromBase64(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        NBTTagList itemList = (NBTTagList) readNbt(new DataInputStream(inputStream), 0);
        Inventory inventory = new CraftInventoryCustom(null, itemList.size());

        for (int i = 0; i < itemList.size(); i++) {
            NBTTagCompound inputObject = itemList.get(i);

            if (!inputObject.isEmpty()) {
                inventory.setItem(i, CraftItemStack.asCraftMirror(
                        net.minecraft.server.v1_8_R2.ItemStack.createStack(inputObject)));
            }
        }
        return inventory;
    }

    private void writeNbt(NBTBase base, DataOutput output) {
        if (WRITE_NBT == null) {
            try {
                WRITE_NBT = NBTCompressedStreamTools.class.getDeclaredMethod("a", NBTBase.class, DataOutput.class);
                WRITE_NBT.setAccessible(true);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to find private write method.", e);
            }
        }

        try {
            WRITE_NBT.invoke(null, base, output);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to write " + base + " to " + output, e);
        }
    }

    private NBTBase readNbt(DataInput input, int level) {
        if (READ_NBT == null) {
            try {
                READ_NBT = NBTCompressedStreamTools.class.getDeclaredMethod("a", DataInput.class, int.class);
                READ_NBT.setAccessible(true);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to find private read method.", e);
            }
        }

        try {
            return (NBTBase) READ_NBT.invoke(null, input, level);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to read from " + input, e);
        }
    }

    private static CraftItemStack getCraftVersion(ItemStack stack) {
        if (stack instanceof CraftItemStack)
            return (CraftItemStack) stack;
        else if (stack != null)
            return CraftItemStack.asCraftCopy(stack);
        else
            return null;
    }
}