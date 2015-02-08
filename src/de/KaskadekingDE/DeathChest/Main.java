package de.KaskadekingDE.DeathChest;

import de.KaskadekingDE.DeathChest.Commands.DeathChestCommand;
import de.KaskadekingDE.DeathChest.Config.KillerChestsConfig;
import de.KaskadekingDE.DeathChest.Config.LangStrings;
import de.KaskadekingDE.DeathChest.Config.LanguageConfig;
import de.KaskadekingDE.DeathChest.Events.DeathChestListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    public static Main plugin;
    public static LanguageConfig langConfig;
    public static KillerChestsConfig killerConfig;
    Logger log = Logger.getLogger("Minecraft");

    public static String Prefix;
    public static boolean UsePermission;
    public static boolean OnlyReplaceWhitelistedBlocks;
    public static boolean ShowCoords;
    public static boolean RemoveEmptyDeathChest;
    public static boolean ProtectedChest;
    public static int MaxChests;
    public static int Seconds;
    public static boolean RemoveChestAfterXSeconds;
    public static List<?> whitelistedBlocks;
    private static String[] defaultList = {"AIR", "STONE", "DEAD_BUSH", "LEAVES", "RED_ROSE", "YELLOW_FLOWER", "VINE", "GRASS"};

    public enum ChestStates {
        DeathChest,
        KillerChest,
        None
    }

    public void onEnable() {
        this.plugin = this;
        langConfig = new LanguageConfig(this);
        killerConfig = new KillerChestsConfig(this);
        loadConfig();
        getCommand("deathchest").setExecutor(new DeathChestCommand());
        Bukkit.getPluginManager().registerEvents(new DeathChestListener(), this);
        PluginDescriptionFile pdf = getDescription();
        log.info("[DeathChest] DeathChest v" + pdf.getVersion() + " has been enabled! :)");
    }

    @Override
    public void onDisable() {
        log.info("[DeathChest] Saving chest inventorys... This can take a while!");
        saveDeathChestInventory();
        saveKillerChestInventory();
        this.plugin = null;
        log.info("[DeathChest] DeathChest has been disabled!");
    }

    public void loadConfig() {
        getConfig().addDefault("prefix", "&6[&cDeathChest&6] ");
        getConfig().addDefault("need-permission", true);
        getConfig().addDefault("only-replace-whitelisted-blocks", true);
        getConfig().addDefault("show-coords", false);
        getConfig().addDefault("max-death-chests", 5);
        getConfig().addDefault("remove-empty-death-chest", true);
        getConfig().addDefault("protected-death-chest", false);
        getConfig().addDefault("remove-death-chest-after-x-seconds", true);
        getConfig().addDefault("time-in-seconds", 60);
        getConfig().addDefault("whitelisted-blocks", Arrays.asList(defaultList));
        langConfig.saveDefaultLangConfig();
        langConfig.reloadLangConfig();
        killerConfig.reloadKillerConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadDeathChests();
        loadKillerChests();
        Prefix = getConfig().getString("prefix").replace('&', '§').trim();
        UsePermission = getConfig().getBoolean("need-permission");
        OnlyReplaceWhitelistedBlocks = getConfig().getBoolean("only-replace-whitelisted-blocks");
        ShowCoords = getConfig().getBoolean("show-coords");
        RemoveEmptyDeathChest = getConfig().getBoolean("remove-empty-death-chest");
        ProtectedChest = getConfig().getBoolean("protected-death-chest");
        MaxChests = getConfig().getInt("max-death-chests");
        whitelistedBlocks = getConfig().getList("whitelisted-blocks");
        RemoveChestAfterXSeconds = getConfig().getBoolean("remove-death-chest-after-x-seconds");
        Seconds = getConfig().getInt("time-in-seconds");
        LangStrings.ChestRemoved = langConfig.getLangConfig().getString("death-chest-removed").replace('&', '§');
        LangStrings.ChestSpawned = langConfig.getLangConfig().getString("death-chest-spawned").replace('&', '§');
        LangStrings.ConfigReloaded = langConfig.getLangConfig().getString("config-reloaded").replace('&', '§');
        LangStrings.FailedPlacing = langConfig.getLangConfig().getString("failed-to-place-death-chest").replace('&', '§');
        LangStrings.HelpHelp = langConfig.getLangConfig().getString("help-help").replace('&', '§');
        LangStrings.HelpNotice = langConfig.getLangConfig().getString("help-notice").replace('&', '§');
        LangStrings.HelpPage = langConfig.getLangConfig().getString("help-page").replace('&', '§');
        LangStrings.HelpReload= langConfig.getLangConfig().getString("help-reload").replace('&', '§');
        LangStrings.NoPermissions = langConfig.getLangConfig().getString("no-permissions").replace('&', '§');
        LangStrings.NotOwner = langConfig.getLangConfig().getString("your-not-owner").replace('&', '§');
        LangStrings.DontBreak = langConfig.getLangConfig().getString("dont-break").replace('&', '§');
        LangStrings.MaxExceeded = langConfig.getLangConfig().getString("max-death-chests-exceeded").replace('&', '§').replace("%n", Integer.toString(MaxChests));
        LangStrings.VictimsLootStored = langConfig.getLangConfig().getString("victims-loot-stored").replace('&', '§');
        LangStrings.TimeStarted = langConfig.getLangConfig().getString("you-have-x-seconds").replace('&', '§').replace("%s", Integer.toString(Seconds));
        LangStrings.TimeOver = langConfig.getLangConfig().getString("time-is-up").replace('&', '§').replace("%s", Integer.toString(Seconds));
        LangStrings.TimeStartedKiller = langConfig.getLangConfig().getString("you-have-x-seconds-killer").replace('&', '§').replace("%s", Integer.toString(Seconds));
        LangStrings.TimeOverKiller = langConfig.getLangConfig().getString("time-is-up-killer").replace('&', '§').replace("%s", Integer.toString(Seconds));
    }

    private void loadDeathChests() {
        try {

            for(String key: getConfig().getConfigurationSection("death-chests").getKeys(false)) {
                List<Location> locations = new ArrayList<Location>();
                Player player;
                try {
                    UUID uuid = UUID.fromString(key);
                    Player p = Bukkit.getOfflinePlayer(uuid).getPlayer();
                    player = p;
                    for (String dc : getConfig().getConfigurationSection("death-chests." + key).getKeys(false)) {
                        int x = getConfig().getInt("death-chests." + key + "." + dc + ".x");
                        int y = getConfig().getInt("death-chests." + key + "." + dc + ".y");
                        int z = getConfig().getInt("death-chests." + key + "." + dc + ".z");
                        String w = getConfig().getString("death-chests." + key + "." + dc + ".world");
                        String base = getConfig().getString("death-chests." + key + "." + dc + ".inventory");
                        Inventory inv = ItemSerialization.fromBase64(base);
                        World world;
                        try {
                            world = Bukkit.getWorld(w);
                        } catch(IllegalArgumentException ex) {
                            continue;
                        }
                        Location loc = new Location(world, x, y, z, 0.0F, 0.0F);
                        locations.add(loc);
                        if(loc.getWorld().getBlockAt(loc).getType() == Material.CHEST) {
                            Chest chest = (Chest)loc.getWorld().getBlockAt(loc).getState();
                            DeathChestListener.chestInventory.put(chest, inv);
                        }

                    }
                } catch(NullPointerException ex) {
                    continue;
                }
                DeathChestListener.deathChests.put(player, locations);
            }

        } catch(NullPointerException ex) {
            return;
        }
    }

    private void loadKillerChests() {
        try {
            for(String key: killerConfig.getKillerConfig().getConfigurationSection("death-chests").getKeys(false)) {
                List<Location> locations = new ArrayList<Location>();
                Player player;
                try {
                    UUID uuid = UUID.fromString(key);
                    Player p = Bukkit.getOfflinePlayer(uuid).getPlayer();
                    player = p;
                    for (String dc : getConfig().getConfigurationSection("death-chests." + key).getKeys(false)) {
                        int x = getConfig().getInt("death-chests." + key + "." + dc + ".x");
                        int y = getConfig().getInt("death-chests." + key + "." + dc + ".y");
                        int z = getConfig().getInt("death-chests." + key + "." + dc + ".z");
                        String w = getConfig().getString("death-chests." + key + "." + dc + ".world");
                        String base = getConfig().getString("death-chests." + key + "." + dc + ".inventory");
                        Inventory inv = ItemSerialization.fromBase64(base);

                        World world;
                        try {
                            world = Bukkit.getWorld(w);
                        } catch(IllegalArgumentException ex) {
                            continue;
                        }
                        Location loc = new Location(world, x, y, z, 0.0F, 0.0F);
                        locations.add(loc);
                        if(loc.getWorld().getBlockAt(loc).getType() == Material.CHEST) {
                            Chest chest = (Chest)loc.getWorld().getBlockAt(loc).getState();
                            DeathChestListener.chestInventory.put(chest, inv);
                        }

                    }
                } catch(NullPointerException ex) {
                    continue;
                }
                DeathChestListener.killerChests.put(player, locations);
            }
        } catch(NullPointerException ex) {
            return;
        }
    }


    private void saveDeathChestInventory() {
        Set<Player> players = DeathChestListener.deathChests.keySet();
        for(Player p: players) {
            List<Location> locs = DeathChestListener.deathChests.get(p);
            for(Location loc: locs) {
                if(chestState(p, loc) != ChestStates.DeathChest) {
                    continue;
                } else {
                    int dcc = DeathChestListener.DeathChestCount(loc, p.getUniqueId());
                    Chest chest = (Chest) loc.getWorld().getBlockAt(loc).getState();
                    Inventory inv = DeathChestListener.chestInventory.get(chest);
                    String base = ItemSerialization.toBase64(inv);

                    getConfig().set("death-chests." + p.getUniqueId() + "." + dcc + ".inventory", base);
                    saveConfig();
                }
            }
        }
    }

    private void saveKillerChestInventory() {
        Set<Player> players = DeathChestListener.killerChests.keySet();
        for(Player p: players) {
            List<Location> locs = DeathChestListener.killerChests.get(p);
            for(Location loc: locs) {
                if(chestState(p, loc) != ChestStates.KillerChest) {
                    continue;
                } else {
                    int dcc = DeathChestListener.KillerChestCount(loc, p.getUniqueId());
                    Chest chest = (Chest) loc.getWorld().getBlockAt(loc).getState();
                    Inventory inv = DeathChestListener.chestInventory.get(chest);
                    String base = ItemSerialization.toBase64(inv);

                    killerConfig.getKillerConfig().set("death-chests." + p.getUniqueId() + "." + dcc + ".inventory", base);
                    killerConfig.saveKillerConfig();
                }
            }
        }
    }

    private ChestStates chestState(Player p, Location loc) {
        if(DeathChestListener.deathChests.get(p).contains(loc)) {
            return ChestStates.DeathChest;
        } else if(DeathChestListener.killerChests.get(p).contains(loc)) {
            return ChestStates.KillerChest;
        } else {
            return ChestStates.None;
        }
    }

    private Player getKey(Location loc) {
        for(Player p: DeathChestListener.deathChests.keySet()) {
            if(DeathChestListener.deathChests.get(p).contains(loc)) {
                return p;
            }
        }
        return null;
    }

}
