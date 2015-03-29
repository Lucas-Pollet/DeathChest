package de.KaskadekingDE.DeathChest;

import com.avaje.ebeaninternal.server.lib.sql.Prefix;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import de.KaskadekingDE.DeathChest.Commands.DeathChestCommand;
import de.KaskadekingDE.DeathChest.Config.KillerChestsConfig;
import de.KaskadekingDE.DeathChest.Config.LangStrings;
import de.KaskadekingDE.DeathChest.Config.LanguageConfig;
import de.KaskadekingDE.DeathChest.Config.PlayerData;
import de.KaskadekingDE.DeathChest.Events.DeathChestListener;
import de.KaskadekingDE.DeathChest.Events.HomeChestListener;
import de.KaskadekingDE.DeathChest.ItemSerialization.ISerialization;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.EOFException;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    public static Main plugin;
    public static LanguageConfig langConfig;
    public static KillerChestsConfig killerConfig;
    Logger log = Logger.getLogger("Minecraft");
    public static ProtocolManager protocol;

    public static boolean UsePermission;
    public static boolean OnlyReplaceWhitelistedBlocks;
    public static boolean ShowCoords;
    public static boolean RemoveEmptyDeathChest;
    public static boolean ProtectedChest;
    public static int MaxChests;
    public static int Seconds;
    public static boolean ExecludeHomeChestFromWhitelist;
    public static boolean RemoveChestAfterXSeconds;
    public static List<?> whitelistedBlocks;
    public static PlayerData playerData;
    public static List<World> enabledWorlds = new ArrayList<World>();
    private static String[] defaultList = {"AIR", "STONE", "DEAD_BUSH", "LEAVES", "RED_ROSE", "YELLOW_FLOWER", "VINE", "LONG_GRASS", "TALL_GRASS"};
    private static String[] defaultWorld = {"world"};

    public static ISerialization Serialization;

    public enum ChestStates {
        DeathChest,
        KillerChest,
        HomeChest,
        None
    }

    @Override
    public void onEnable() {
        String version = Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(".") + 1);
        if(version.startsWith("v1_8_R1")) {
            Serialization = new de.KaskadekingDE.DeathChest.ItemSerialization.v1_8_R1.ItemSerialization();
        } else if(version.startsWith("v1_8_R2")) {
            Serialization = new de.KaskadekingDE.DeathChest.ItemSerialization.v1_8_R2.ItemSerialization();
        } else {
            log.severe("This server version is not supported! (" + version + ")");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.plugin = this;
        langConfig = new LanguageConfig(this);
        killerConfig = new KillerChestsConfig(this);
        playerData = new PlayerData(this);
        loadConfig();
        checkForProtocolLib();
        getCommand("deathchest").setExecutor(new DeathChestCommand());
        Bukkit.getPluginManager().registerEvents(new DeathChestListener(), this);
        Bukkit.getPluginManager().registerEvents(new HomeChestListener(), this);
        PluginDescriptionFile pdf = getDescription();
        log.info("[DeathChest] DeathChest v" + pdf.getVersion() + " has been enabled! :)");

    }

    private void checkForProtocolLib() {
        Plugin proLib = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");
        if (proLib == null) {
            log.severe("Couldn't hook into ProtocolLib!");
        } else {
            protocol = ProtocolLibrary.getProtocolManager();
            protocol.addPacketListener(new PacketAdapter(this,
                    PacketType.Play.Server.BLOCK_ACTION, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    PacketType type = event.getPacketType();

                    if (type == PacketType.Play.Server.BLOCK_ACTION) {
                        World world = event.getPlayer().getWorld();
                        BlockPosition bpos;
                        try {
                            bpos =  event.getPacket().getBlockPositionModifier().read(0);
                        } catch(Exception ex) {
                            return;
                        }
                        int blockX = bpos.getX();
                        int blockY = bpos.getY();
                        int blockZ = bpos.getZ();
                        Location loc = new Location(world, blockX, blockY, blockZ);
                        if (world.getBlockAt(loc).getType() == Material.CHEST) {

                            Player p = DeathChestListener.getKeyForDeathChest(loc);
                            if(p == null) return;
                            if(!p.equals(event.getPlayer()) && !event.getPlayer().hasPermission("deathchest.protection.bypass")) {
                                event.setCancelled(true);
                            }
                        }
                    } else if (type == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                        String soundEffectName = event.getPacket().getSpecificModifier(String.class).read(0);
                        World world = event.getPlayer().getWorld();
                        BlockPosition bpos;
                        try {
                            bpos =  event.getPacket().getBlockPositionModifier().read(0);
                        } catch(Exception ex) {
                            return;
                        }
                        int blockX = bpos.getX();
                        int blockY = bpos.getY();
                        int blockZ = bpos.getZ();
                        Location loc = new Location(world, blockX, blockY, blockZ);
                        if (soundEffectName.contains("chest")) {
                            Player p = DeathChestListener.getKeyForDeathChest(loc);
                            if(p == null) return;
                            if(!p.equals(event.getPlayer()) && !event.getPlayer().hasPermission("deathchest.protection.bypass")) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {
        log.info("[DeathChest] Saving chest inventorys... This can take a while!");
        saveDeathChestInventory();
        saveKillerChestInventory();
        saveHomeChestInventory();
        saveConfig();
        reloadConfig();
        this.plugin = null;
        log.info("[DeathChest] DeathChest has been disabled!");
    }

    public void createDefaultConfig() {
        File f = new File(getDataFolder(), "players.yml");
        if(!f.exists()) {
            playerData.saveDefaultPlayerConfig();
        }
        f = new File(plugin.getDataFolder() + File.separator + "store");
        if(!f.exists()) {
            f.mkdirs();
        }
        f = new File(plugin.getDataFolder() + File.separator + "store", "killerchest.yml");
        if(!f.exists()) {
            killerConfig.saveDefaultKillerConfig();
        }
    }

    public void loadConfig() {
        DeathChestListener.chestRemover.clear();
        DeathChestListener.chestInventory.clear();
        DeathChestListener.homeChest.clear();
        DeathChestListener.deathChests.clear();
        DeathChestListener.killerChests.clear();
        getConfig().addDefault("prefix", "&6[&cDeathChest&6] ");
        getConfig().addDefault("need-permission", true);
        getConfig().addDefault("only-replace-whitelisted-blocks", true);
        getConfig().addDefault("show-coords", false);
        getConfig().addDefault("max-death-chests", 5);
        getConfig().addDefault("remove-empty-death-chest", true);
        getConfig().addDefault("protected-death-chest", false);
        getConfig().addDefault("remove-death-chest-after-x-seconds", true);
        getConfig().addDefault("exclude-home-chests-from-whitelist", false);
        getConfig().addDefault("time-in-seconds", 60);
        getConfig().addDefault("enabled-worlds", Arrays.asList(defaultWorld));
        getConfig().addDefault("whitelisted-blocks", Arrays.asList(defaultList));
        createDefaultConfig();
        ConvertPlayerData();
        langConfig.reloadLangConfig();
        killerConfig.reloadKillerConfig();
        playerData.reloadPlayerConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadDeathChests();
        loadKillerChests();
        UsePermission = getConfig().getBoolean("need-permission");
        OnlyReplaceWhitelistedBlocks = getConfig().getBoolean("only-replace-whitelisted-blocks");
        ShowCoords = getConfig().getBoolean("show-coords");
        RemoveEmptyDeathChest = getConfig().getBoolean("remove-empty-death-chest");
        ProtectedChest = getConfig().getBoolean("protected-death-chest");
        MaxChests = getConfig().getInt("max-death-chests");
        whitelistedBlocks = getConfig().getList("whitelisted-blocks");
        RemoveChestAfterXSeconds = getConfig().getBoolean("remove-death-chest-after-x-seconds");
        Seconds = getConfig().getInt("time-in-seconds");
        ExecludeHomeChestFromWhitelist = getConfig().getBoolean("exclude-home-chests-from-whitelist");
        List<?> worlds = getConfig().getList("enabled-worlds");
        for(Object obj: worlds) {
            String name = obj.toString();
            try {
                World w = Bukkit.getWorld(name);
                enabledWorlds.add(w);
            } catch(IllegalArgumentException ex) {
                // World doesn't exists
                //continue;
            }
        }

        LangStrings.Prefix = langConfig.getLangConfig().getString("prefix").replace('&', '§').trim();

        LangStrings.HelpHelp = langConfig.getLangConfig().getString("help-help").replace('&', '§');
        LangStrings.HelpNotice = langConfig.getLangConfig().getString("help-notice").replace('&', '§');
        LangStrings.HelpPage = langConfig.getLangConfig().getString("help-page").replace('&', '§');
        LangStrings.HelpReload= langConfig.getLangConfig().getString("help-reload").replace('&', '§');
        LangStrings.HelpHome  = langConfig.getLangConfig().getString("help-home").replace('&', '§');
        LangStrings.HelpLocations  = langConfig.getLangConfig().getString("help-locations").replace('&', '§');
        LangStrings.HelpRemove  = langConfig.getLangConfig().getString("help-remove").replace('&', '§');

        LangStrings.ConfigReloaded = langConfig.getLangConfig().getString("config-reloaded").replace('&', '§');
        LangStrings.NoPermissions = langConfig.getLangConfig().getString("no-permissions").replace('&', '§');

        LangStrings.NotOwner = langConfig.getLangConfig().getString("your-not-owner").replace('&', '§');
        LangStrings.DontBreak = langConfig.getLangConfig().getString("dont-break").replace('&', '§');
        LangStrings.TimeOver = langConfig.getLangConfig().getString("time-is-up").replace('&', '§').replace("%s", Integer.toString(Seconds));
        LangStrings.TimeOverKiller = langConfig.getLangConfig().getString("time-is-up-killer").replace('&', '§').replace("%s", Integer.toString(Seconds));
        LangStrings.OnlyPlayers = langConfig.getLangConfig().getString("only-players").replace('&', '§');
        LangStrings.Cancelled = langConfig.getLangConfig().getString("cancelled").replace('&', '§');
        LangStrings.MaxExceeded = langConfig.getLangConfig().getString("max-death-chests-exceeded").replace('&', '§').replace("%n", Integer.toString(MaxChests));
        LangStrings.FailedPlacing = langConfig.getLangConfig().getString("failed-to-place-death-chest").replace('&', '§');
        LangStrings.RemoveBeforeBreak = langConfig.getLangConfig().getString("remove-loot-before-breaking").replace('&', '§');
        LangStrings.AlreadySet = langConfig.getLangConfig().getString("already-home-chest-set").replace('&', '§');
        LangStrings.NotEnabled = langConfig.getLangConfig().getString("not-enabled-on-this-world").replace('&', '§');
        LangStrings.ChestAlreadyUsed = langConfig.getLangConfig().getString("chest-already-used-by-another-player").replace('&', '§');
        LangStrings.CantPlaceChestNearChest = langConfig.getLangConfig().getString("cant-place-chest-near-death-chests").replace('&', '§');
        LangStrings.CantPlaceDeathChest = langConfig.getLangConfig().getString("cant-place-deathchest-near-chest").replace('&', '§');
        LangStrings.CantPlaceKillerChest = langConfig.getLangConfig().getString("cant-place-killerchest-near-chest").replace('&', '§');
        LangStrings.InvalidArgument = langConfig.getLangConfig().getString("too-few-or-too-many-arguments").replace('&', '§');
        LangStrings.InvalidId = langConfig.getLangConfig().getString("invalid-id").replace('&', '§');

        LangStrings.SetupHome = langConfig.getLangConfig().getString("setup-home-chest").replace('&', '§');
        LangStrings.HomeChestSet = langConfig.getLangConfig().getString("home-chest-set").replace('&', '§');
        LangStrings.NoDeathChest = langConfig.getLangConfig().getString("no-deathchest").replace('&', '§');
        LangStrings.NoDeathChestOther = langConfig.getLangConfig().getString("no-deathchest-other").replace('&', '§');
        LangStrings.DeathChestOf = langConfig.getLangConfig().getString("death-chests").replace('&', '§');
        LangStrings.DeathChestOfOther = langConfig.getLangConfig().getString("death-chests-of").replace('&', '§');
        LangStrings.RemoveWarning = langConfig.getLangConfig().getString("remove-warning").replace('&', '§');
        LangStrings.RemoveSuccessful = langConfig.getLangConfig().getString("removed-successful").replace('&', '§');
        LangStrings.RemoveFailed = langConfig.getLangConfig().getString("removed-failed").replace('&', '§');

        LangStrings.ChestRemoved = langConfig.getLangConfig().getString("death-chest-removed").replace('&', '§');
        LangStrings.ChestSpawned = langConfig.getLangConfig().getString("death-chest-spawned").replace('&', '§');
        LangStrings.VictimsLootStored = langConfig.getLangConfig().getString("victims-loot-stored").replace('&', '§');
        LangStrings.TimeStarted = langConfig.getLangConfig().getString("you-have-x-seconds").replace('&', '§').replace("%s", Integer.toString(Seconds));
        LangStrings.TimeStartedKiller = langConfig.getLangConfig().getString("you-have-x-seconds-killer").replace('&', '§').replace("%s", Integer.toString(Seconds));
        LangStrings.StoredInHomeChest = langConfig.getLangConfig().getString("stored-in-home-chest").replace('&', '§');
        LangStrings.Removed  = langConfig.getLangConfig().getString("removed-home-chest").replace('&', '§');
        LangStrings.HomeChestFullOne  = langConfig.getLangConfig().getString("home-chest-full-1").replace('&', '§');
        LangStrings.HomeChestFullSecond  = langConfig.getLangConfig().getString("home-chest-full-2").replace('&', '§');
        LangStrings.BelongsTo = langConfig.getLangConfig().getString("belongs-to").replace('&', '§');
    }

    private void loadDeathChests() {
        try {
            for(String key: playerData.getConfig().getConfigurationSection("death-chests").getKeys(false)) {
                List<Location> locations = new ArrayList<Location>();
                Player player;
                
                try {
                    UUID uuid = UUID.fromString(key);
                    Player p = Bukkit.getOfflinePlayer(uuid).getPlayer();
                    player = p;
                    for (String dc : playerData.getConfig().getConfigurationSection("death-chests." + key).getKeys(false)) {
                        if(dc.equals("home-chest")) continue;
                        int x = playerData.getConfig().getInt("death-chests." + key + "." + dc + ".x");
                        int y = playerData.getConfig().getInt("death-chests." + key + "." + dc + ".y");
                        int z = playerData.getConfig().getInt("death-chests." + key + "." + dc + ".z");
                        String w = playerData.getConfig().getString("death-chests." + key + "." + dc + ".world");


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
                            Inventory inv;
                            try {
                                String base = playerData.getConfig().getString("death-chests." + key + "." + dc + ".inventory");
                                inv = Serialization.fromBase64(base);
                                DeathChestListener.chestInventory.put(chest, inv);
                            } catch(NullPointerException | EOFException ex) {
                            }

                        }
                    }
                    DeathChestListener.deathChests.put(player, locations);
                    
                    int xHome = playerData.getConfig().getInt("death-chests." + key + ".home-chest.x");
                    int yHome = playerData.getConfig().getInt("death-chests." + key + ".home-chest.y");
                    int zHome = playerData.getConfig().getInt("death-chests." + key + ".home-chest.z");
                    String wHome = playerData.getConfig().getString("death-chests." + key + ".home-chest.world");
                    String baseHome;
                    if(playerData.getConfig().contains("death-chests." + key + ".home-chest.inventory")) {
                        baseHome = playerData.getConfig().getString("death-chests." + key + ".home-chest.inventory");
                    } else {
                        baseHome = null;
                    }
                    
                    Inventory invHome;
                    if(baseHome != null) {
                        invHome = Serialization.fromBase64(baseHome);
                    } else {
                        invHome = null;
                    }

                    World worldHome;
                    try {
                        worldHome = Bukkit.getWorld(wHome);
                    } catch(IllegalArgumentException ex) {
                        continue;
                    }
                    Location locHome = new Location(worldHome, xHome, yHome, zHome);
                    if(locHome.getWorld().getBlockAt(locHome).getType() == Material.CHEST) {
                        Chest chest = (Chest)locHome.getWorld().getBlockAt(locHome).getState();
                        DeathChestListener.homeChest.put(player, locHome);
                        Inventory inv = Bukkit.createInventory(chest.getInventory().getHolder(), 54, "Home Chest");
                        if(invHome != null) {
                            for(ItemStack stack: invHome.getContents()) {
                                if(stack != null) {
                                    inv.addItem(stack);
                                }
                            }
                        }
                        DeathChestListener.chestInventory.put(chest, inv);
                    }
                    
                } catch(NullPointerException ex) {
                    continue;
                } catch (EOFException e) {
                    e.printStackTrace();
                }

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
                        Inventory inv = Serialization.fromBase64(base);

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
                    DeathChestListener.killerChests.put(player, locations);
                } catch(NullPointerException ex) {
                    continue;
                } catch (EOFException e) {
                    e.printStackTrace();
                }

            }
        } catch(NullPointerException ex) {
            return;
        }
    }

    public static void saveHomeChestInventory() {
        if(DeathChestListener.homeChest.keySet() == null) return;
        Set<Player> players = DeathChestListener.homeChest.keySet();
        for(Player p: players) {
            Location loc = DeathChestListener.homeChest.get(p);
                if(chestState(p, loc) != ChestStates.HomeChest) {
                    continue;
                } else {
                    if (loc.getWorld().getBlockAt(loc).getState() == null) continue;
                    Chest chest = (Chest) loc.getWorld().getBlockAt(loc).getState();
                    Inventory inv = DeathChestListener.chestInventory.get(chest);
                    String base = Serialization.toBase64(inv);
                    playerData.reloadConfig();
                    if(p == null) {
                        return;
                    }
                    playerData.getConfig().set("death-chests." + p.getUniqueId() + ".home-chest.inventory", base);
                    playerData.saveConfig();
                    playerData.reloadConfig();
                }
        }
    }

    public static void saveDeathChestInventory() {
        
        if(DeathChestListener.deathChests.keySet() == null) return;
        Set<Player> players = DeathChestListener.deathChests.keySet();
        for(Player p: players) {
            List<Location> locs = DeathChestListener.deathChests.get(p);
            for(Location loc: locs) {
                if(chestState(p, loc) != ChestStates.DeathChest) {
                    continue;
                } else {
                    int dcc;
                    try {
                        dcc = DeathChestListener.DeathChestCount(loc, p.getUniqueId());
                    } catch(NullPointerException npe) {
                        continue;
                    }

                    if(dcc == -1) continue;
                    Chest chest = (Chest) loc.getWorld().getBlockAt(loc).getState();
                    Inventory inv = DeathChestListener.chestInventory.get(chest);
                    String base = Serialization.toBase64(inv);
                    playerData.getConfig().set("death-chests." + p.getUniqueId() + "." + dcc + ".inventory", base);
                    playerData.saveConfig();
                }
            }
        }
    }

    public static void saveKillerChestInventory() {
        
        if(DeathChestListener.killerChests.keySet() == null) return;
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
                    String base = Serialization.toBase64(inv);

                    killerConfig.getKillerConfig().set("death-chests." + p.getUniqueId() + "." + dcc + ".inventory", base);
                    killerConfig.saveKillerConfig();

                }
            }
        }
    }

    public static void ConvertPlayerData() {
        if(Main.plugin.getConfig().contains("death-chests")) {
            System.out.println("-------------------------------");
            System.out.println("-    Converting player data   -");
            System.out.println("-------------------------------");
            Map<String, Object> objects = Main.plugin.getConfig().getConfigurationSection("death-chests").getValues(true);
            for (String key : objects.keySet()) {
                Object value = Main.plugin.getConfig().get("death-chests." + key);
                Main.playerData.getPlayerConfig().set("death-chests." + key, value);
            }
            Main.plugin.getConfig().set("death-chests", null);
            Main.playerData.savePlayerConfig();
            Main.plugin.saveConfig();
        }
    }

    private static ChestStates chestState(Player p, Location loc) {
        if(DeathChestListener.deathChests.containsKey(p) && DeathChestListener.deathChests.get(p).contains(loc)) {
            return ChestStates.DeathChest;
        } else if(DeathChestListener.killerChests.containsKey(p) &&DeathChestListener.killerChests.get(p).contains(loc)) {
            return ChestStates.KillerChest;
        } else if(DeathChestListener.homeChest.containsKey(p) &&DeathChestListener.homeChest.get(p).equals(loc)) {
            return ChestStates.HomeChest;
        } else {
            return ChestStates.None;
        }
    }

}
