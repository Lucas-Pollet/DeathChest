package de.KaskadekingDE.DeathChest.Classes.WorldGuard;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class DeathChestWGFlag implements IWorldGuardFlag {

    public static final StateFlag AllowDeathChest = new StateFlag("allow-deathchests", true);
    public WorldGuardPlugin wgp;

    public boolean canPlaceInRegion(World w, Location loc) {
        WorldGuardPlugin worldGuard = getWorldGuard();
        if(worldGuard != null) {
            ApplicableRegionSet regions = worldGuard.getRegionManager(w).getApplicableRegions(loc);
            if(regions.queryState(null, AllowDeathChest) == StateFlag.State.DENY) {
                return false;
            }
        }
        return true;
    }

    public void InitWgp() {
        wgp = getWorldGuard();
        if(wgp != null) {
            addWGFlag(AllowDeathChest);
        }
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Main.plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }

    private static synchronized void addWGFlag(Flag<?> flag) {
        try {
            Field flagField = DefaultFlag.class.getField("flagsList");

            Flag<?>[] flags = new Flag[DefaultFlag.flagsList.length + 1];
            System.arraycopy(DefaultFlag.flagsList, 0, flags, 0, DefaultFlag.flagsList.length);

            flags[DefaultFlag.flagsList.length] = flag;

            if (flag == null) {
                throw new RuntimeException("flag is null");
            }

            ClassHacker.setStaticValue(flagField, flags);
        } catch (Exception ex) {
            Bukkit.getServer().getLogger().log(Level.WARNING, "Could not add flag {0} to WorldGuard", flag.getName());
        }

        for (int i = 0; i < DefaultFlag.getFlags().length; i++) {
            Flag<?> flag1 = DefaultFlag.getFlags()[i];
            if (flag1 == null) {
                throw new RuntimeException("Flag[" + i + "] is null");
            }
        }
    }
}
