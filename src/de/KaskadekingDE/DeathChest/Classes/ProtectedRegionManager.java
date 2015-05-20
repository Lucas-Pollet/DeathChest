package de.KaskadekingDE.DeathChest.Classes;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import de.KaskadekingDE.DeathChest.Main;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class ProtectedRegionManager {

    public enum PlotSquaredType {
        NotLoaded,
        HasAccess,
        IsAdded,
        NoAccess,
    }

    public boolean HookedWorldGuard;
    public boolean HookedGriefPrevention;
    public boolean HookedTowny;
    public boolean HookedPlotSquared;

    public ProtectedRegionManager() {
        Initialize();
    }

    private boolean wgEnabled() {
        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if(wg != null && wg.isEnabled()) {
            return true;
        }
        return false;
    }

    private boolean psEnabled() {
        Plugin ps = Bukkit.getPluginManager().getPlugin("PlotSquared");
        if(ps != null && ps.isEnabled()) {
            return true;
        }
        return false;
    }

    private boolean gpEnabled() {
        Plugin gp = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if(gp != null && gp.isEnabled()) {
            return true;
        }
        return false;
    }

    private boolean townyEnabled() {
        Plugin towny = Bukkit.getPluginManager().getPlugin("Towny");
        if(towny != null && towny.isEnabled()) {
            return true;
        }
        return false;
    }

    private void Initialize() {
        HookedWorldGuard = wgEnabled();
        HookedGriefPrevention = gpEnabled();
        HookedTowny = townyEnabled();
        HookedPlotSquared = psEnabled();
    }

    public boolean wgRegionAccess(Player p, Location loc, Material type) {
        if(!HookedWorldGuard || !wgEnabled())
            return true;
        if(!WGBukkit.getPlugin().createProtectionQuery().testBlockPlace(p, loc, type)) {
            return false;
        }
        return true;
    }

    public PlotSquaredType psPlotAccess(Player p, Location loc) {
        if(!HookedPlotSquared || !psEnabled())
            return PlotSquaredType.NotLoaded; // PlotSquared not enabled
        PlotAPI api = new PlotAPI();
        Plot currentPlot = api.getPlot(loc);
        if(currentPlot != null) {
            if(currentPlot.isOwner(p.getUniqueId()) || currentPlot.isAdded(p.getUniqueId()) && !currentPlot.isDenied(p.getUniqueId())) {
                return PlotSquaredType.IsAdded; // Can use
            }
            return PlotSquaredType.NoAccess; // Cannot use
        }
        return PlotSquaredType.HasAccess; // No plot
    }

    public boolean gpClaimAccess(Player p, Location loc) {
        if(!HookedGriefPrevention|| !gpEnabled())
            return true;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
        if(claim != null) {
            String errorMessage = claim.allowContainers(p);
            if(errorMessage != null && !Main.SpawnOutside) {
                errorMessage = null;
            }
            if(errorMessage != null)
                return false;
        }
        return true;
    }

    public boolean townyAccess(Player p, Location loc, Material type) {
        if(!HookedTowny|| !townyEnabled())
            return true;
        if(!PlayerCacheUtil.getCachePermission(p, loc, type.getId(), (byte) 0, TownyPermission.ActionType.SWITCH)) {
            return false;
        }
        return true;
    }

    public Location searchValidLocation(Player p, Location chestLoc) {
        Location loc = NormalizeLocation(chestLoc);
        if(checkAccess(p, loc)) {
            loc = GetSafePlotSquaredLocation(p, loc);
            return loc;
        }
        World w = loc.getWorld();
        int locX = loc.getBlockX();
        int locY = loc.getBlockY();
        int locZ = loc.getBlockZ();

        for(int y = 0; y < Main.Radius; y++) {
            for(int x = 0; y < Main.Radius; x++) {
                for(int z = 0; z < Main.Radius; z++) {
                    loc = new Location(w, locX + x, locY + y, locZ + z);
                    if(checkAccess(p, loc)) {
                        loc = GetSafePlotSquaredLocation(p, loc);
                        return loc;
                    }
                    loc = new Location(w, locX - x, locY + y, locZ - z);
                    if(checkAccess(p, loc)) {
                        loc = GetSafePlotSquaredLocation(p, loc);
                        return loc;
                    }
                    loc = new Location(w, locX - x, locY + y, locZ + z);
                    if(checkAccess(p, loc)) {
                        loc = GetSafePlotSquaredLocation(p, loc);
                        return loc;
                    }
                    loc = new Location(w, locX + x, locY + y, locZ - z);
                    if(checkAccess(p, loc)) {
                        loc = GetSafePlotSquaredLocation(p, loc);
                        return loc;
                    }
                }
            }
        }
        return null;
    }

    private Location GetSafePlotSquaredLocation(Player p, Location loc) {
        if(plotSquaredNoAccess.get(loc) == PlotSquaredType.HasAccess) {
            List<Location> locations = Helper.LocationsAround(loc);
            for(Location loc2: locations) {
                if(Helper.ValidLocation(loc2) && checkAccess(p, loc)) {
                    return loc2;
                }
            }
        }
        return loc;
    }

    HashMap<Location, PlotSquaredType> plotSquaredNoAccess = new HashMap<Location, PlotSquaredType>();

    public boolean checkAccess(Player p, Location loc) {
        plotSquaredNoAccess.clear();
        Material mat = Main.UseTombstones ? Material.SIGN_POST : Material.CHEST;
        if(!gpClaimAccess(p, loc))
            return false;
        if(!wgRegionAccess(p, loc, mat))
            return false;
        if(!townyAccess(p, loc, mat))
            return false;
        PlotSquaredType type = psPlotAccess(p, loc);
        plotSquaredNoAccess.put(loc, type);
        switch(type) {
            case NoAccess:
                return false;
        }
        if(Helper.AvailableLocation(loc) == null)
            return false;
        return true;
    }

    private Location NormalizeLocation(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        World w = loc.getWorld();
        return new Location(w, x, y, z);
    }

}
