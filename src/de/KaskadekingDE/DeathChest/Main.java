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
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
    public static List<?> whitelistedBlocks;
    private static String[] defaultList = {"AIR", "STONE", "DEAD_BUSH", "LEAVES", "RED_ROSE", "YELLOW_FLOWER", "VINE", "GRASS"};

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
        getConfig().addDefault("whitelisted-blocks", Arrays.asList(defaultList));
        langConfig.saveDefaultLangConfig();
        langConfig.reloadLangConfig();
        killerConfig.saveDefaultKillerConfig();
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
                        World world;
                        try {
                            world = Bukkit.getWorld(w);
                        } catch(IllegalArgumentException ex) {
                            continue;
                        }
                        Location loc = new Location(world, x, y, z, 0.0F, 0.0F);
                        locations.add(loc);

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
                        World world;
                        try {
                            world = Bukkit.getWorld(w);
                        } catch(IllegalArgumentException ex) {
                            continue;
                        }
                        Location loc = new Location(world, x, y, z, 0.0F, 0.0F);
                        locations.add(loc);

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

}
