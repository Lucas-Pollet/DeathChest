package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Classes.*;
import de.KaskadekingDE.DeathChest.Classes.Chests.KillChest;
import de.KaskadekingDE.DeathChest.Classes.Tasks.Animation.Animation;
import de.KaskadekingDE.DeathChest.Classes.Tasks.Animation.AnimationManager;
import de.KaskadekingDE.DeathChest.Classes.Tasks.PVPTag;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class KillChestEvent implements Listener {

    public static HashMap<Player, Inventory> suppressEvent = new HashMap<Player, Inventory>();
    public static HashMap<Player, Location> chestSpawnLocation = new HashMap<Player, Location>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Player killer = e.getEntity().getKiller();
        if(killer == null) {
            if(!PVPTag.TaggedPlayers.containsKey(p)) {
                return;
            }
            killer = PVPTag.TaggedPlayers.get(p).Damager;
        } else if(!PVPTag.TaggedPlayers.containsKey(p)) {
            return;
        }

        Location loc = p.getLocation();
        if(PermissionManager.PlayerHasPermission(killer, PermissionManager.KILLER_PERMISSION, false) && !PermissionManager.PlayerHasPermission(p, PermissionManager.KILL_PROTECTION, false)) {
            if(checkRequirements(killer, loc, e.getDrops())) {
                loc = chestSpawnLocation.get(killer);
                Inventory inv = null;
                boolean spawnSign = true;
                if (Main.UseTombstones) {
                    Location blockUnderSign = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                    if(!Main.SolidBlockManager.IsSolid(blockUnderSign)) {
                        if (!Main.SpawnTombstonesOnNonSolid) {
                            if (!Main.SpawnChestIfNotAbleToPlaceTombstone) {
                                p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingDeathChest.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                                return;
                            } else {
                                loc.getBlock().setType(Material.CHEST);
                                Chest killChest = (Chest) loc.getBlock().getState();
                                inv = Bukkit.getServer().createInventory(killChest.getInventory().getHolder(), 54, LangStrings.KillChestInv);
                                spawnSign = false;
                            }
                        }
                    }

                    if(spawnSign) {
                        Block block = loc.getBlock();
                        block.setType(Material.SIGN_POST);
                        Sign sign = (Sign) block;
                        DateFormat dateFormat = new SimpleDateFormat("MM.dd HH:mm:ss");
                        Date date = new Date();
                        String dateString = dateFormat.format(date);
                        String lineOne = LangStrings.LineOne.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.KillChestInv);
                        String lineTwo = LangStrings.LineTwo.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.KillChestInv);
                        String lineThree = LangStrings.LineThree.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.KillChestInv);
                        String lineFour = LangStrings.LineFour.replace("%player", p.getName()).replace("%date", dateString).replace("%chest", LangStrings.KillChestInv);
                        sign.setLine(0, lineOne);
                        sign.setLine(1, lineTwo);
                        sign.setLine(2, lineThree);
                        sign.setLine(3, lineFour);
                        sign.update();
                        SignHolder sh = new SignHolder(LangStrings.KillChestInv, 54, sign);
                        inv = sh.getInventory();
                    }
                } else {
                    if(Main.KillChestType.equalsIgnoreCase("CHEST")) {
                        loc.getBlock().setType(Material.CHEST);
                        Chest killChest = (Chest) loc.getBlock().getState();
                        inv = Bukkit.getServer().createInventory(killChest.getInventory().getHolder(), 54, LangStrings.KillChestInv);
                    } else if(Main.KillChestType.equalsIgnoreCase("ENDER_CHEST")) {
                        loc.getBlock().setType(Material.ENDER_CHEST);
                        Block block = loc.getBlock();
                        EnderHolder eh = new EnderHolder(LangStrings.KillChestInv, 54, block);
                        inv = eh.getInventory();
                    } else {
                        Material material = Material.getMaterial(Main.KillChestType);
                        loc.getBlock().setType(material);
                        Block block = loc.getBlock();
                        BlockHolder bh = new BlockHolder(LangStrings.DeathChestInv, 54, block);
                        inv = bh.getInventory();
                    }

                }
                boolean playerWantToPay = Main.playerData.getPlayerConfig().getBoolean("players." + killer.getUniqueId() + ".pay-enabled", false);
                if(!playerWantToPay && Main.ForceToPay) playerWantToPay = true;
                if(Main.VaultEnabled && playerWantToPay && Main.KillChestCost > 0) {
                    EconomyResponse r = Main.Economy.withdrawPlayer(killer, Main.KillChestCost);
                    String price = Main.Economy.format(Main.KillChestCost);
                    if(r.transactionSuccess()) {
                        killer.sendMessage(LangStrings.Prefix + " " + LangStrings.TakenFromAccountKC.replace("%price", price));
                    } else {
                        killer.sendMessage(LangStrings.Prefix + " " + LangStrings.NotEnoughMoney.replace("%price", price));
                        loc.getBlock().setType(Material.AIR);
                        return;
                    }
                } else if(Main.VaultEnabled && !playerWantToPay && Main.DeathChestCost > 0) {
                    loc.getBlock().setType(Material.AIR);
                    return;
                }

                for(ItemStack drop: e.getDrops()) {
                    inv.addItem(drop);
                }
                e.getDrops().clear();
                KillChest chest = new KillChest(killer, loc, inv);
                chest.SaveKillChest();
                chestSpawnLocation.remove(killer);
                killer.sendMessage(LangStrings.Prefix + " " + LangStrings.KillChestPlaced.replace("%player", p.getDisplayName()));
                if(Main.SecondsToRemove > 0) {
                    killer.sendMessage(LangStrings.Prefix + " " + LangStrings.TimeToTakeLoot.replace("%time", Integer.toString(Main.SecondsToRemove)));
                    chest.RegisterTask(killer, loc);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(Helper.GetChestType(e.getClickedBlock().getLocation()) == Helper.ChestState.Default) return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.SIGN_POST) {
            Player p = e.getPlayer();
            Location loc = e.getClickedBlock().getLocation();
            KillChest kc = KillChest.KillChestByLocation(loc);
            if (kc == null) {
                return;
            }
            if (Main.HookedPacketListener) {
                Main.ProtocolManager.SendChestOpenPacket(kc.ChestLocation, p);
            }
            e.setCancelled(false);
            p.openInventory(kc.DeathInventory);
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CHEST) {
            Location loc = e.getClickedBlock().getLocation();
            KillChest kc = KillChest.KillChestByLocation(loc);
            if (kc == null) {
                return;
            }
            e.setCancelled(false);
        } else if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.ENDER_CHEST) {
            Player p = e.getPlayer();
            Location loc = e.getClickedBlock().getLocation();
            KillChest kc = KillChest.KillChestByLocation(loc);
            if (kc == null) {
                return;
            }
            e.setCancelled(true);
            if (Main.HookedPacketListener) {
                AnimationManager.Create(p, loc);
                Main.ProtocolManager.SendChestOpenPacket(kc.ChestLocation, p);
            }
            p.openInventory(kc.DeathInventory);
        } else {
            Player p = e.getPlayer();
            Location loc = e.getClickedBlock().getLocation();
            KillChest kc = KillChest.KillChestByLocation(loc);
            if (kc == null) {
                return;
            }
            e.setCancelled(true);
            p.openInventory(kc.DeathInventory);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        if(suppressEvent.containsKey(p)) {
            Inventory inv = suppressEvent.get(p);
            if(inv.equals(e.getInventory())) {
                e.setCancelled(false);
                suppressEvent.remove(p);
                return;
            }
        }
        InventoryHolder ih = e.getInventory().getHolder();
        if(ih instanceof Chest) {
            Chest ch = (Chest) ih;
            KillChest kc = KillChest.KillChestByLocation(ch.getLocation());
            if(kc == null) {
                return;
            }
            suppressEvent.put(p, kc.DeathInventory);
            if(Main.HookedPacketListener) {
                AnimationManager.Create(p, kc.ChestLocation);
                Main.ProtocolManager.SendChestOpenPacket(kc.ChestLocation, p);
            }
            e.setCancelled(true);
            p.openInventory(kc.DeathInventory);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent e) {
        InventoryHolder ih = e.getInventory().getHolder();
        Player p = (Player) e.getPlayer();
        if(ih instanceof Chest) {
            Chest ch = (Chest) ih;
            Location loc = new Location(ch.getLocation().getWorld(), ch.getLocation().getBlockX(), ch.getLocation().getBlockY(), ch.getLocation().getBlockZ());
            KillChest kc = KillChest.KillChestByLocation(loc);
            if(kc == null) {
                return;
            }
            if(Main.HookedPacketListener) {
                AnimationManager.Remove(p);
                Main.ProtocolManager.SendChestClosePacket(kc.ChestLocation, p);
            }
            if(Helper.ChestEmpty(kc.DeathInventory)) {
                kc.RemoveChest(true);
            }
        } else if (ih instanceof SignHolder) {
            SignHolder sh = (SignHolder) ih;
            Sign sign = sh.getSign();
            Location loc = new Location(sign.getLocation().getWorld(), sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ());
            KillChest dc = KillChest.KillChestByLocation(loc);
            if (dc == null) {
                return;
            }
            if (Main.HookedPacketListener) {
                Main.ProtocolManager.SendChestClosePacket(dc.ChestLocation, p);
            }
            if (Helper.ChestEmpty(dc.DeathInventory)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestRemoved);
                dc.RemoveChest(true);
            }
        } else if(ih instanceof EnderHolder) {
            EnderHolder eh = (EnderHolder) ih;
            Block block = eh.getBlock();
            Location loc = new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
            KillChest dc = KillChest.KillChestByLocation(loc);
            if (dc == null) {
                return;
            }
            if (Main.HookedPacketListener) {
                AnimationManager.Remove(p);
                Main.ProtocolManager.SendChestClosePacket(dc.ChestLocation, p);
            }
            if (Helper.ChestEmpty(dc.DeathInventory)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestRemoved);
                dc.RemoveChest(true);
            }
        } else if(ih instanceof BlockHolder) {
            BlockHolder eh = (BlockHolder) ih;
            Block block = eh.getBlock();
            Location loc = new Location(block.getLocation().getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
            KillChest dc = KillChest.KillChestByLocation(loc);
            if (dc == null) {
                return;
            }
            if (Helper.ChestEmpty(dc.DeathInventory)) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestRemoved);
                dc.RemoveChest(true);
            }
        }
    }

    private boolean checkRequirements(Player p, Location deathLoc, List<ItemStack> drops) {
        if(drops == null || drops.size() == 0) {
            return false;
        }
        if(Main.DisableKillChests) {
            return false;
        }
        if(Main.MaximumKillChests != -1) {
            if(KillChest.KillChestsByOwner(p).size() >= Main.MaximumKillChests || KillChest.NextAvailableId(p) == -1) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.ReachedMaximumChests.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
                return false;
            }
        }
        boolean worldAllowed = false;
        for(String key: Main.AllowedWorlds) {
            if(key.equalsIgnoreCase("*")) {
                worldAllowed = true;
                break;
            }
            World w = Bukkit.getWorld(key);
            if(w != null) {
                if(w == deathLoc.getWorld()) {
                    worldAllowed = true;
                    break;
                }
            }
        }
        if(!worldAllowed) {
            return false;
        }
        if(deathLoc.getBlockY() < 0) {
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingKillChest.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
            return false;
        }
        if(Main.SpawnOutside) {
            Location chestLoc = Main.ProtectedRegionManager.searchValidLocation(p, deathLoc);
            if(chestLoc == null) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingKillChest.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
                return false;
            } else {
                chestSpawnLocation.put(p, chestLoc);
            }
        } else {
            Location chestLoc = Helper.AvailableLocation(deathLoc);
            if(chestLoc == null) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.FailedPlacingKillChest.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
                return false;
            } else {
                chestSpawnLocation.put(p, chestLoc);
            }
        }
        return true;
    }
}
