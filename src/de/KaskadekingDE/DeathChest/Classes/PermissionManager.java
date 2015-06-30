package de.KaskadekingDE.DeathChest.Classes;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PermissionManager {

    public static List<String> PROTECTION_BYPASS = CreatePermissionList("deathchest.*", "deathchest.protection.bypass", "deathchest.protection.*");
    public static List<String> KILLER_PERMISSION = CreatePermissionList("deathchest.place.kill", "deathchest.place.*", "deathchest.*");
    public static List<String> KILL_PROTECTION = CreatePermissionList("deathchest.protection.kill", "deathchest.protection.*");
    public static List<String> DEATH_PERMISSION = CreatePermissionList("deathchest.place.death", "deathchest.place.*", "deathchest.*");
    public static List<String> HOME_PERMISSION = CreatePermissionList("deathchest.place.home", "deathchest.place.*", "deathchest.*");
    public static List<String> HELP_PERMISSION = CreatePermissionList("deathchest.help", "deathchest.*");
    public static List<String> RELOAD_PERMISSION = CreatePermissionList("deathchest.reload", "deathchest.*");
    public static List<String> LOCATIONS_PERMISSION = CreatePermissionList("deathchest.locations", "deathchest.locations.*", "deathchest.*");
    public static List<String> LOCATIONS_OTHERS_PERMISSION = CreatePermissionList("deathchest.locations.others","deathchest.locations.*", "deathchest.*");
    public static List<String> REMOVE_PERMISSION = CreatePermissionList("deathchest.remove", "deathchest.*");

    private static String AllPermissions = "deathchest.*";
    private static String Help = "deathchest.help";
    private static String Reload = "deathchest.reload";
    private static String Locations = "deathchest.locations";
    private static String LocationsOthers = "deathchest.locations.others";
    private static String LocationsAll = "deathchest.locations.*";
    private static String PlaceDeath = "deathchest.place.death";
    private static String PlaceKill = "deathchest.place.kill";
    private static String PlaceHome = "deathchest.place.home";
    private static String PlaceAll = "deathchest.place.*";
    private static String ProtectionBypass = "deathchest.protection.bypass";
    private static String ProtectionKill = "deathchest.protection.kill";
    private static String ProtectionAll = "deathchest.protection.*";
    private static String Remove = "deathchest.remove";

    public static boolean PlayerHasPermission(Player p, List<String> permissionList, boolean allRequired) {
        if(permissionList == null) return true;
        HashMap<String, Boolean> hasPermissionList = new HashMap<String, Boolean>();
        for(String permission : permissionList) {
            hasPermissionList.put(permission, p.hasPermission(permission));
        }
        boolean hasPermission = false;
        for(Boolean allowed: hasPermissionList.values()) {
            if(!allowed && allRequired) return false;
            if(allowed) hasPermission = true;
        }
        return hasPermission;
    }

    public static boolean PlayerHasPermission(CommandSender cs, List<String> permissionList, boolean allRequired) {
        if(permissionList == null) return true;
        HashMap<String, Boolean> hasPermissionList = new HashMap<String, Boolean>();
        for(String permission : permissionList) {
            hasPermissionList.put(permission, cs.hasPermission(permission));
        }
        boolean hasPermission = false;
        for(Boolean allowed: hasPermissionList.values()) {
            if(!allowed && allRequired) return false;
            if(allowed) hasPermission = true;
        }
        return hasPermission;
    }

    public static boolean PlayerHasPermission(Player p, String permission) {
        return p.hasPermission(permission);
    }

    private static List<String> CreatePermissionList(String... permisions) {
        List<String> result = new ArrayList<String>();
        for(String perm : permisions) {
             result.add(perm);
        }
        return result;
    }
}
