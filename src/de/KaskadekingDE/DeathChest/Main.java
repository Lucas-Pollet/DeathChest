package de.KaskadekingDE.DeathChest;

import de.KaskadekingDE.DeathChest.Classes.Chests.ChestManager.DeathChestManager;
import de.KaskadekingDE.DeathChest.Classes.Chests.ChestManager.HomeChestManager;
import de.KaskadekingDE.DeathChest.Classes.Chests.ChestManager.KillChestManager;
import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.KillChest;
import de.KaskadekingDE.DeathChest.Classes.Helper;
import de.KaskadekingDE.DeathChest.Classes.PacketManagement.IProtocolManager;
import de.KaskadekingDE.DeathChest.Classes.Serialization.ISerialization;
import de.KaskadekingDE.DeathChest.Classes.SignHolder;
import de.KaskadekingDE.DeathChest.Classes.SolidBlockManager.IBlockManager;
import de.KaskadekingDE.DeathChest.Classes.SolidBlockManager.v1_8_R1.SolidBlockManager;
import de.KaskadekingDE.DeathChest.Classes.Tasks.TaskScheduler;
import de.KaskadekingDE.DeathChest.Commands.DeathChestCommand;
import de.KaskadekingDE.DeathChest.Config.LanguageConfig;
import de.KaskadekingDE.DeathChest.Config.PlayerData;
import de.KaskadekingDE.DeathChest.Events.ChestProtector;
import de.KaskadekingDE.DeathChest.Events.DeathChestEvent;
import de.KaskadekingDE.DeathChest.Events.HomeChestEvent;
import de.KaskadekingDE.DeathChest.Events.KillChestEvent;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    public static Main plugin;
    public final static Logger log = Logger.getLogger("Minecraft");

    public static ISerialization Serialization;
    public static IProtocolManager ProtocolManager;
    public static IBlockManager SolidBlockManager;

    // Player-Data & Language
    public static PlayerData playerData;
    public static LanguageConfig languageConfig;

    // Config-Options
    public static List<String> AllowedBlocks;
    public static List<String> AllowedWorlds;
    public static int MaximumDeathChests;
    public static int MaximumKillChests;
    public static int SecondsToRemove;
    public static boolean ShowCoords;
    public static boolean AllowHomeChestInAllWorlds;
    public static boolean HookedPacketListener;
    public static boolean UseTombstones;

    @Override
    public void onEnable() {
        plugin = this;
        if(Helper.ServerVersion().startsWith("v1_8_R1")) {
            Serialization = new de.KaskadekingDE.DeathChest.Classes.Serialization.v1_8_R1.InventorySerialization();
            SolidBlockManager = new de.KaskadekingDE.DeathChest.Classes.SolidBlockManager.v1_8_R1.SolidBlockManager();
        } else if(Helper.ServerVersion().startsWith("v1_8_R2")) {
            Serialization = new de.KaskadekingDE.DeathChest.Classes.Serialization.v1_8_R2.InventorySerialization();
            SolidBlockManager = new de.KaskadekingDE.DeathChest.Classes.SolidBlockManager.v1_8_R2.SolidBlockManager();
        } else {
            log.severe("[DeathChest] This server version is not supported (" + Helper.ServerVersion() + ")");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        checkPacketListener();
        LoadConfig();
        PluginDescriptionFile pdf = getDescription();
        getCommand("deathchest").setExecutor(new DeathChestCommand());
        Bukkit.getPluginManager().registerEvents(new DeathChestEvent(), this);
        Bukkit.getPluginManager().registerEvents(new ChestProtector(), this);
        Bukkit.getPluginManager().registerEvents(new HomeChestEvent(), this);
        Bukkit.getPluginManager().registerEvents(new KillChestEvent(), this);
        if(ProtocolManager != null) {
            ProtocolManager.RegisterEvents();
        }
        log.info("[DeathChest] DeathChest v" + pdf.getVersion() + " by KaskadekingDE has been enabled!");
    }

    @Override
    public void onDisable() {
        log.info("[DeathChest] Saving chest inventory...");
        SaveConfig();
        TaskScheduler.RemoveAll();
        if(ProtocolManager != null) {
            ProtocolManager.UnregisterEvents();
        }
        plugin = null;
        log.info("[DeathChest] DeathChest has been disabled!");
    }

    private void checkPacketListener() {
        if(Bukkit.getPluginManager().isPluginEnabled("PacketListenerApi")) {
            if(Helper.ServerVersion().startsWith("v1_8_R1")) {
                ProtocolManager = new de.KaskadekingDE.DeathChest.Classes.PacketManagement.v1_8_R1.ProtocolManager();
                log.info("[DeathChest] Hooked into PacketListenerApi");
                HookedPacketListener = true;
            } else if(Helper.ServerVersion().startsWith("v1_8_R2")) {
                ProtocolManager = new de.KaskadekingDE.DeathChest.Classes.PacketManagement.v1_8_R2.ProtocolManager();
                log.info("[DeathChest] Hooked into PacketListenerApi");
                HookedPacketListener = true;
            } else {
                log.severe("[DeathChest] This server version is not supported (" + Helper.ServerVersion() + ")");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        } else {
            HookedPacketListener = false;
        }
    }

    public void LoadConfig() {
        String[] defaultList = {"AIR", "STONE", "DEAD_BUSH", "LEAVES", "RED_ROSE", "YELLOW_FLOWER", "VINE", "LONG_GRASS", "TALL_GRASS"};
        String[] defaultWorlds = {"world"};
        getConfig().addDefault("maximum-deathchests", 3);
        getConfig().addDefault("maximum-killchests", 2);
        getConfig().addDefault("remove-chest-after-x-seconds", 120);
        getConfig().addDefault("show-coords", false);
        getConfig().addDefault("allow-home-chests-in-all-worlds", false);
        getConfig().addDefault("use-tombstones", false);
        getConfig().addDefault("allowed-blocks", Arrays.asList(defaultList));
        getConfig().addDefault("allowed-worlds", Arrays.asList(defaultWorlds));
        getConfig().options().copyDefaults(true);
        saveConfig();
        MaximumDeathChests = getConfig().getInt("maximum-deathchests");
        MaximumKillChests = getConfig().getInt("maximum-killchests");
        SecondsToRemove = getConfig().getInt("remove-chest-after-x-seconds");
        AllowedBlocks = getConfig().getStringList("allowed-blocks");
        AllowedWorlds = getConfig().getStringList("allowed-worlds");
        ShowCoords = getConfig().getBoolean("show-coords");
        AllowHomeChestInAllWorlds = getConfig().getBoolean("allow-home-chest-in-all-worlds");
        UseTombstones = getConfig().getBoolean("use-tombstones");
        playerData = new PlayerData(this);
        languageConfig = new LanguageConfig(this);
        playerData.saveDefaultPlayerConfig();
        languageConfig.saveDefaultLanguageConfig();
        playerData.reloadPlayerConfig();
        languageConfig.reloadLanguageConfig();
        LoadLanguage();

        // Load player data

        if(playerData.getPlayerConfig().contains("players")) {
            for(String uuidKey: playerData.getPlayerConfig().getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidKey);
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                if(playerData.getPlayerConfig().contains("players." + uuidKey + ".death-chests")) {
                    for(String id: playerData.getPlayerConfig().getConfigurationSection("players." + uuidKey + ".death-chests").getKeys(false)) {
                        int x;
                        int y;
                        int z;
                        String world;
                        String base;
                        x = Main.playerData.getPlayerConfig().getInt("players." + p.getUniqueId() + ".death-chests." + id + ".x", 0);
                        y = Main.playerData.getPlayerConfig().getInt("players." + p.getUniqueId() + ".death-chests." + id + ".y", -1);
                        z = Main.playerData.getPlayerConfig().getInt("players." + p.getUniqueId() + ".death-chests." + id + ".z", 0);
                        world = Main.playerData.getPlayerConfig().getString("players." + p.getUniqueId() + ".death-chests." + id + ".world", null);
                        base = Main.playerData.getPlayerConfig().getString("players." + p.getUniqueId() + ".death-chests." + id + ".inventory", null);
                        World w;
                        if(world == null) {
                            w = null;
                        } else {
                            w = Bukkit.getWorld(world);
                        }
                        if(x == 0 && y == -1 && z == 0) {
                            log.warning("[DeathChest] Found invalid death chest! It will be removed from the player data!");
                            Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".death-chests." + id, null);
                            continue;
                        }
                        if(w != null) {
                            Location loc = new Location(w, x, y, z);
                            Inventory inv;
                            if(base != null) {
                                Inventory baseInv = Serialization.fromBase64(base);
                                BlockState bs = loc.getBlock().getState();
                                if(bs instanceof Chest) {
                                    Chest ch = (Chest) bs;
                                    InventoryHolder ih = ch.getInventory().getHolder();
                                    inv = Bukkit.createInventory(ih, 54, LangStrings.DeathChestInv);
                                } else if(UseTombstones && loc.getWorld().getBlockAt(loc).getType() == Material.SIGN_POST) {
                                    SignHolder sh = new SignHolder(LangStrings.DeathChestInv, 54, loc);
                                    inv = sh.getInventory();
                                } else{
                                    log.warning("[DeathChest] Found invalid death chest! It will be removed from the player data!");
                                    Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".death-chests." + id, null);
                                    continue;
                                }
                                Helper.MoveItemsToInventory(baseInv, inv);
                            } else {
                                BlockState bs = loc.getBlock().getState();
                                if(bs instanceof Chest) {
                                    Chest ch = (Chest) bs;
                                    InventoryHolder ih = ch.getInventory().getHolder();
                                    inv = Bukkit.createInventory(ih, 54, LangStrings.DeathChestInv);
                                } else if(UseTombstones && bs instanceof Sign) {
                                    SignHolder sh = new SignHolder(LangStrings.DeathChestInv, 54, loc);
                                    inv = sh.getInventory();
                                } else {
                                    log.warning("[DeathChest] Found invalid death chest! It will be removed from the player data!");
                                    Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".death-chests." + id, null);
                                    continue;
                                }
                            }
                            DeathChest chest = new DeathChest(p, loc, inv);
                            chest.RegisterTask(p, loc);
                        } else {
                            log.warning("[DeathChest] Found invalid death chest! It will be removed from the player data!");
                            Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".death-chests." + id, null);
                        }
                    }
                }

                // Load kill chests
                if(playerData.getPlayerConfig().contains("players." + uuidKey + ".kill-chests")) {
                    for(String id: playerData.getPlayerConfig().getConfigurationSection("players." + uuidKey + ".kill-chests").getKeys(false)) {
                        int x;
                        int y;
                        int z;
                        String world;
                        String base;
                        x = Main.playerData.getPlayerConfig().getInt("players." + p.getUniqueId() + ".kill-chests." + id + ".x", 0);
                        y = Main.playerData.getPlayerConfig().getInt("players." + p.getUniqueId() + ".kill-chests." + id + ".y", -1);
                        z = Main.playerData.getPlayerConfig().getInt("players." + p.getUniqueId() + ".kill-chests." + id + ".z", 0);
                        world = Main.playerData.getPlayerConfig().getString("players." + p.getUniqueId() + ".kill-chests." + id + ".world", null);
                        base = Main.playerData.getPlayerConfig().getString("players." + p.getUniqueId() + ".kill-chests." + id + ".inventory", null);
                        World w;
                        if(world == null) {
                            w = null;
                        } else {
                            w = Bukkit.getWorld(world);
                        }
                        if(x == 0 && y == -1 && z == 0) {
                            log.warning("[DeathChest] Found invalid kill chest! It will be removed from the player data!");
                            Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".kill-chests." + id, null);
                            continue;
                        }
                        if(w != null) {
                            Location loc = new Location(w, x, y, z);
                            Inventory inv;
                            if(base != null) {
                                Inventory baseInv = Serialization.fromBase64(base);
                                BlockState bs = loc.getBlock().getState();
                                if(bs instanceof Chest) {
                                    Chest ch = (Chest) bs;
                                    InventoryHolder ih = ch.getInventory().getHolder();
                                    inv = Bukkit.createInventory(ih, 54, LangStrings.KillChestInv);
                                } else if(UseTombstones && bs instanceof Sign) {
                                    SignHolder sh = new SignHolder(LangStrings.KillChestInv, 54, loc);
                                    inv = sh.getInventory();
                                } else {
                                    log.warning("[DeathChest] Found invalid kill-chests chest! It will be removed from the player data!");
                                    Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".kill-chests." + id, null);
                                    continue;
                                }
                                Helper.MoveItemsToInventory(baseInv, inv);
                            } else {
                                BlockState bs = loc.getBlock().getState();
                                if(bs instanceof Chest) {
                                    Chest ch = (Chest) bs;
                                    InventoryHolder ih = ch.getInventory().getHolder();
                                    inv = Bukkit.createInventory(ih, 54, LangStrings.KillChestInv);
                                } else if(UseTombstones && bs instanceof Sign) {
                                    SignHolder sh = new SignHolder(LangStrings.KillChestInv, 54, loc);
                                    inv = sh.getInventory();
                                } else {
                                    log.warning("[DeathChest] Found invalid kill chest! It will be removed from the player data!");
                                    Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".kill-chests." + id, null);
                                    continue;
                                }
                            }
                            KillChest chest = new KillChest(p, loc, inv);
                            chest.RegisterTask(p, loc);
                        } else {
                            log.warning("[DeathChest] Found invalid kill chest! It will be removed from the player data!");
                            Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".kill-chests." + id, null);
                        }
                    }
                }

                // Load home chest

                if(playerData.getPlayerConfig().contains("players." + uuidKey + ".home-chest")) {
                    if(playerData.getPlayerConfig().getConfigurationSection("players." + uuidKey + ".home-chest").getKeys(true).size() == 0) {
                        Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".home-chest", null);
                        continue;
                    }
                    int x = 0;
                    int y = -1;
                    int z = 0;
                    String world = null;
                    String base = null;
                    if(Main.playerData.getPlayerConfig().contains("players." + p.getUniqueId() + ".home-chest.x")) { x = Main.playerData.getPlayerConfig().getInt("players." + p.getUniqueId() + ".home-chest.x"); }
                    if(Main.playerData.getPlayerConfig().contains("players." + p.getUniqueId() + ".home-chest.y")) { y = Main.playerData.getPlayerConfig().getInt("players." + p.getUniqueId() + ".home-chest.y"); }
                    if(Main.playerData.getPlayerConfig().contains("players." + p.getUniqueId() + ".home-chest.z")) { z = Main.playerData.getPlayerConfig().getInt("players." + p.getUniqueId() + ".home-chest.z"); }
                    if(Main.playerData.getPlayerConfig().contains("players." + p.getUniqueId() + ".home-chest.world")) { world = Main.playerData.getPlayerConfig().getString("players." + p.getUniqueId() + ".home-chest.world"); }
                    if(Main.playerData.getPlayerConfig().contains("players." + p.getUniqueId() + ".home-chest.inventory")) { base = Main.playerData.getPlayerConfig().getString("players." + p.getUniqueId() + ".home-chest.inventory"); }
                    World w;
                    if(world == null) {
                        w = null;
                    } else {
                        w = Bukkit.getWorld(world);
                    }
                    if(x == 0 && y == -1 && z == 0) {
                        log.warning("[DeathChest] Found invalid home chest! It will be removed from the player data!");
                        Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".home-chest", null);
                        continue;
                    }
                    if(w != null) {
                        Location loc = new Location(w, x, y, z);
                        Inventory inv;
                        if(base != null) {
                            Inventory baseInv = Serialization.fromBase64(base);
                            BlockState bs = loc.getBlock().getState();
                            if(bs instanceof Chest) {
                                Chest ch = (Chest) bs;
                                InventoryHolder ih = ch.getInventory().getHolder();
                                inv = Bukkit.createInventory(ih, 54, LangStrings.HomeChestInv);
                            } else {
                                log.warning("[DeathChest] Found invalid home chest! It will be removed from the player data!");
                                Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".home-chest", null);
                                continue;
                            }
                            Helper.MoveItemsToInventory(baseInv, inv);
                        } else {
                            BlockState bs = loc.getBlock().getState();
                            if(bs instanceof Chest) {
                                Chest ch = (Chest) bs;
                                InventoryHolder ih = ch.getInventory().getHolder();
                                inv = Bukkit.createInventory(ih, 54, LangStrings.HomeChestInv);
                            } else {
                                log.warning("[DeathChest] Found invalid home chest! It will be removed from the player data!");
                                Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".home-chest", null);
                                continue;
                            }
                        }
                        HomeChest.CreateHomeChest(p, loc, inv);
                    } else {
                        log.warning("[DeathChest] Found invalid home chest! It will be removed from the player data!");
                        Main.playerData.getPlayerConfig().set("players." + p.getUniqueId() + ".home-chest", null);
                    }
                }

            }
        }
        Main.playerData.savePlayerConfig();
    }

    public void LoadLanguage() {
        LangStrings.Prefix = languageConfig.getLanguageConfig().getString("prefix").replace('&', '§');
        LangStrings.DeathChest = languageConfig.getLanguageConfig().getString("death-chest").replace('&', '§');
        LangStrings.KillChest = languageConfig.getLanguageConfig().getString("kill-chest").replace('&', '§');
        LangStrings.HomeChest = languageConfig.getLanguageConfig().getString("home-chest").replace('&', '§');
        LangStrings.DeathChestInv = languageConfig.getLanguageConfig().getString("death-inv-name").replace('&', '§');
        LangStrings.KillChestInv = languageConfig.getLanguageConfig().getString("kill-inv-name").replace('&', '§');
        LangStrings.HomeChestInv = languageConfig.getLanguageConfig().getString("home-inv-name").replace('&', '§');
        LangStrings.TypeChest = languageConfig.getLanguageConfig().getString("type-chest").replace('&', '§');
        LangStrings.TypeTombstone = languageConfig.getLanguageConfig().getString("type-tombstone").replace('&', '§');
        LangStrings.NoPermissions = languageConfig.getLanguageConfig().getString("no-permissions").replace('&', '§');
        LangStrings.OnlyPlayers = languageConfig.getLanguageConfig().getString("only-players").replace('&', '§');
        LangStrings.SeeHelp = languageConfig.getLanguageConfig().getString("see-help").replace('&', '§');
        LangStrings.HelpTitle = languageConfig.getLanguageConfig().getString("help-title").replace('&', '§');
        LangStrings.ConfigReloaded = languageConfig.getLanguageConfig().getString("config-reloaded").replace('&', '§');
        LangStrings.AlreadyAHomeChest = languageConfig.getLanguageConfig().getString("you-have-already-a-home-chest").replace('&', '§');
        LangStrings.SetupHomeChest = languageConfig.getLanguageConfig().getString("setup-home-chest").replace('&', '§');
        LangStrings.NoDeathChest = languageConfig.getLanguageConfig().getString("no-death-chests").replace('&', '§');
        LangStrings.YourDeathChests = languageConfig.getLanguageConfig().getString("your-death-chests").replace('&', '§');
        LangStrings.YourKillChests = languageConfig.getLanguageConfig().getString("your-kill-chests").replace('&', '§');
        LangStrings.PlayerNoDeathChest = languageConfig.getLanguageConfig().getString("player-has-no-death-chest").replace('&', '§');
        LangStrings.PlayerDeathChests = languageConfig.getLanguageConfig().getString("players-death-chests").replace('&', '§');
        LangStrings.PlayerKillerChests = languageConfig.getLanguageConfig().getString("players-kill-chests").replace('&', '§');
        LangStrings.ReachedMaximumChests = languageConfig.getLanguageConfig().getString("reached-maximum-chests").replace('&', '§');
        LangStrings.FailedPlacingDeathChest = languageConfig.getLanguageConfig().getString("failed-to-place-death-chest").replace('&', '§');
        LangStrings.FailedPlacingKillChest = languageConfig.getLanguageConfig().getString("failed-to-place-kill-chest").replace('&', '§');
        LangStrings.CantPlaceChestInNear = languageConfig.getLanguageConfig().getString("cant-place-chests-near-death-chests").replace('&', '§');
        LangStrings.NotAllowedToBreak = languageConfig.getLanguageConfig().getString("not-allowed-to-break").replace('&', '§');
        LangStrings.TooManyOrTooFewArguments = languageConfig.getLanguageConfig().getString("too-few-or-too-many-arguments").replace('&', '§');
        LangStrings.DeathChestPlaced = languageConfig.getLanguageConfig().getString("death-chest-placed").replace('&', '§');
        LangStrings.DeathChestWithCoords = languageConfig.getLanguageConfig().getString("death-chest-placed-with-coords").replace('&', '§');
        LangStrings.TimeToFindDeathChest = languageConfig.getLanguageConfig().getString("time-to-find-death-chest").replace('&', '§');
        LangStrings.DeathChestRemoved = languageConfig.getLanguageConfig().getString("death-chest-removed-empty").replace('&', '§');
        LangStrings.KillChestPlaced = languageConfig.getLanguageConfig().getString("kill-chest-placed").replace('&', '§');
        LangStrings.TimeToTakeLoot = languageConfig.getLanguageConfig().getString("time-to-take-loot").replace('&', '§');
        LangStrings.LootStoredInHome = languageConfig.getLanguageConfig().getString("loot-stored-in-home-chest").replace('&', '§');
        LangStrings.OnePartPlaced = languageConfig.getLanguageConfig().getString("one-part-placed").replace('&', '§');
        LangStrings.OnePartPlaced2 = languageConfig.getLanguageConfig().getString("one-part-placed2").replace('&', '§');
        LangStrings.HomeChestSet = languageConfig.getLanguageConfig().getString("successfully-set-your-home-chest").replace('&', '§');
        LangStrings.Cancelled = languageConfig.getLanguageConfig().getString("cancelled").replace('&', '§');
        LangStrings.CantUseThisChest = languageConfig.getLanguageConfig().getString("cant-use-this-chest").replace('&', '§');
        LangStrings.CantSetHomeChestInWorld = languageConfig.getLanguageConfig().getString("cant-set-home-chest-in-this-world").replace('&', '§');
        LangStrings.ThisChestBelongsTo = languageConfig.getLanguageConfig().getString("this-chest-belongs-to").replace('&', '§');
        LangStrings.CantOpen = languageConfig.getLanguageConfig().getString("cant-open").replace('&', '§');
        LangStrings.ChestRemoved = languageConfig.getLanguageConfig().getString("chest-removed").replace('&', '§');
        LangStrings.Despawned = languageConfig.getLanguageConfig().getString("despawned").replace('&', '§');
        LangStrings.InvalidId = languageConfig.getLanguageConfig().getString("invalid-id").replace('&', '§');
        LangStrings.InvalidType = languageConfig.getLanguageConfig().getString("invalid-type").replace('&', '§');
        LangStrings.RemoveHomeChestWarning = languageConfig.getLanguageConfig().getString("remove-home-chest-warning").replace('&', '§');
        LangStrings.NoHomeChest = languageConfig.getLanguageConfig().getString("no-home-chest").replace('&', '§');
        LangStrings.NoHomeChestPlayer = languageConfig.getLanguageConfig().getString("no-home-chest-player").replace('&', '§');
        LangStrings.RemoveWarning = languageConfig.getLanguageConfig().getString("remove-warning").replace('&', '§').replace("\\n", "\n");
        LangStrings.HomeChestRemoved = languageConfig.getLanguageConfig().getString("home-chest-removed").replace('&', '§');
        LangStrings.HomeChestRemovedPlayer = languageConfig.getLanguageConfig().getString("home-chest-removed-player").replace('&', '§');
        LangStrings.TryWithType = languageConfig.getLanguageConfig().getString("try-with-type").replace('&', '§');
        LangStrings.TryWithTypePlayer = languageConfig.getLanguageConfig().getString("try-with-type-player").replace('&', '§');
        LangStrings.NoChestWithId = languageConfig.getLanguageConfig().getString("no-chest-with-id").replace('&', '§');
        LangStrings.NoChestWithIdPlayer = languageConfig.getLanguageConfig().getString("no-chest-with-id-player").replace('&', '§');
        LangStrings.SuccessfullyRemoved = languageConfig.getLanguageConfig().getString("successfully-removed").replace('&', '§');
        LangStrings.SuccessfullyRemovedPlayer = languageConfig.getLanguageConfig().getString("successfully-removed-player").replace('&', '§');
        LangStrings.IdIsNotANumber = languageConfig.getLanguageConfig().getString("id-is-not-a-number").replace('&', '§');
        LangStrings.LineOne = languageConfig.getLanguageConfig().getString("line-one").replace('&', '§');
        LangStrings.LineTwo = languageConfig.getLanguageConfig().getString("line-two").replace('&', '§');
        LangStrings.LineThree = languageConfig.getLanguageConfig().getString("line-three").replace('&', '§');
        LangStrings.LineFour = languageConfig.getLanguageConfig().getString("line-four").replace('&', '§');
        if(UseTombstones) {
            LangStrings.ActiveType = LangStrings.TypeTombstone;
        } else {
            LangStrings.ActiveType = LangStrings.TypeChest;
        }
    }

    public void SaveConfig() {
        for(DeathChest dc: DeathChestManager.deathChests) {
            dc.SaveDeathChest();
        }
        for(HomeChest hc: HomeChestManager.homeChests) {
            hc.SaveHomeChest();
        }
        for(KillChest kc: KillChestManager.killChests) {
            kc.SaveKillChest();
        }
        saveConfig();
        DeathChestManager.deathChests.clear();
        KillChestManager.killChests.clear();
        HomeChestManager.homeChests.clear();
    }

}
