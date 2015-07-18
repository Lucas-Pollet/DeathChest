package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.KillChest;
import de.KaskadekingDE.DeathChest.Classes.Helper;
import de.KaskadekingDE.DeathChest.Classes.PermissionManager;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.Iterator;
import java.util.List;

public class ChestProtector implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlockPlaced();
        Location loc = block.getLocation();
        Location loc2 = Helper.ChestNearLocation(loc);
        Helper.ChestState state = Helper.GetChestType(loc2);
        if(block.getType() == Material.CHEST) {
            if(state == Helper.ChestState.DeathChest || state == Helper.ChestState.HomeChest || state == Helper.ChestState.KillChest) {
                if(state == Helper.ChestState.DeathChest) {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.CantPlaceChestInNear.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                    e.setCancelled(true);
                } else if(state == Helper.ChestState.KillChest) {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.CantPlaceChestInNear.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
                    e.setCancelled(true);
                } else {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.CantPlaceChestInNear.replace("%type", LangStrings.HomeChest + " " + LangStrings.TypeChest));
                    e.setCancelled(true);
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void onBlockInteract(PlayerInteractEvent e)  {

        Player p = e.getPlayer();
        if( e.getAction() == Action.RIGHT_CLICK_BLOCK ) {
            if(e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.SIGN_POST &&  e.getClickedBlock().getType() != Material.ENDER_CHEST)
                return;
            if(p.isSneaking() && Main.SneakOpenLoot) {
                Helper.ChestState state = Helper.GetChestType(e.getClickedBlock().getLocation());
                switch(state) {
                    case DeathChest:

                        DeathChest dc = DeathChest.DeathChestByLocation(e.getClickedBlock().getLocation());
                        if(dc.Owner.equals(p)) {
                            for (ItemStack i : dc.DeathInventory.getContents())
                            {
                                if(i != null) {
                                    p.getWorld().dropItemNaturally(p.getLocation(), i);
                                }
                            }
                            dc.RemoveChest(true);
                        }
                        break;
                }
            } else {
                if(e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.SIGN_POST && e.getClickedBlock().getType() != Material.ENDER_CHEST)
                    return;
                Helper.ChestState state = Helper.GetChestType(e.getClickedBlock().getLocation());
                switch(state) {
                    case DeathChest:
                        DeathChest dc = DeathChest.DeathChestByLocation(e.getClickedBlock().getLocation());
                        if(!dc.EqualsOwner(p) && !PermissionManager.PlayerHasPermission(p, PermissionManager.PROTECTION_BYPASS, false)) {
                            p.sendMessage(LangStrings.Prefix + " " + LangStrings.CantOpen.replace("%owner", dc.Owner.getName()).replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                            e.setCancelled(true);
                            return;
                        }
                        e.setCancelled(false);
                        break;
                    case KillChest:
                        e.setCancelled(false);
                        break;
                    case HomeChest:
                        HomeChest hc = HomeChest.HomeChestByLocation(e.getClickedBlock().getLocation());
                        if(!hc.EqualsOwner(p) && !PermissionManager.PlayerHasPermission(p, PermissionManager.PROTECTION_BYPASS, false)) {
                            p.sendMessage(LangStrings.Prefix + " " + LangStrings.CantOpen.replace("%type", LangStrings.HomeChest + " " + LangStrings.TypeChest).replace("%owner", hc.Owner.getName()));
                            e.setCancelled(true);
                            return;
                        }
                        e.setCancelled(false);
                        break;
                }
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Location loc = block.getLocation();
        Helper.ChestState state = Helper.GetChestType(loc);
        if(block.getType() == Material.CHEST ||block.getType() == Material.ENDER_CHEST) {
            if(state == Helper.ChestState.DeathChest) {
                DeathChest dc = DeathChest.DeathChestByLocation(loc);
                if(!Main.AllowBreaking) {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.NotAllowedToBreak.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                    e.setCancelled(true);
                    return;
                }
                if(!dc.Owner.equals(e.getPlayer()) && !PermissionManager.PlayerHasPermission(e.getPlayer(), PermissionManager.PROTECTION_BYPASS, false)) {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.NotAllowedToBreak.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                    e.setCancelled(true);
                } else {
                    e.getPlayer().closeInventory();
                    for (ItemStack i : dc.DeathInventory.getContents())
                    {
                        if(i != null) {
                            e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), i);
                        }
                    }
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.ChestRemoved.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                    dc.RemoveChest(true);
                }
            } else if(state == Helper.ChestState.HomeChest) {
                HomeChest hc = HomeChest.HomeChestByLocation(loc);
                if(!hc.Owner.equals(e.getPlayer()) && !PermissionManager.PlayerHasPermission(e.getPlayer(), PermissionManager.PROTECTION_BYPASS, false)) {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.NotAllowedToBreak.replace("%type", LangStrings.HomeChest + " " + LangStrings.TypeChest));
                    e.setCancelled(true);
                } else {

                    e.getPlayer().closeInventory();
                    for (ItemStack i : hc.HomeInventory.getContents())
                    {
                        if(i != null) {
                            e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), i);
                        }
                    }
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.ChestRemoved.replace("%type", LangStrings.HomeChest + " " + LangStrings.TypeChest));
                    hc.RemoveChest();
                }
            } else if(state == Helper.ChestState.KillChest) {
                if(!Main.AllowBreaking) {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.NotAllowedToBreak.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
                    e.setCancelled(true);
                    return;
                }

                KillChest kc = KillChest.KillChestByLocation(loc);
                e.getPlayer().closeInventory();
                for(ItemStack i: kc.DeathInventory.getContents()) {
                    if(i != null) {
                        e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), i);
                    }
                }
                kc.RemoveChest(true);
            }

        } else if(Main.UseTombstones && block.getType() == Material.SIGN_POST) {
            if(state == Helper.ChestState.DeathChest) {
                DeathChest dc = DeathChest.DeathChestByLocation(loc);
                if(!dc.Owner.equals(e.getPlayer()) && !PermissionManager.PlayerHasPermission(e.getPlayer(), PermissionManager.PROTECTION_BYPASS, false)) {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.NotAllowedToBreakFromOtherPlayers.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                    e.setCancelled(true);
                } else {
                    if(!Main.AllowBreaking) {
                        e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.NotAllowedToBreak.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                        e.setCancelled(true);
                        return;
                    }

                    e.getPlayer().closeInventory();
                    for (ItemStack i : dc.DeathInventory.getContents())
                    {
                        if(i != null) {
                            e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), i);
                        }
                    }
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.ChestRemoved.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                    dc.RemoveChest(true);
                }
            } else if(state == Helper.ChestState.HomeChest) {
                HomeChest hc = HomeChest.HomeChestByLocation(loc);
                if(!hc.Owner.equals(e.getPlayer()) && !PermissionManager.PlayerHasPermission(e.getPlayer(), PermissionManager.PROTECTION_BYPASS, false)) {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.NotAllowedToBreak.replace("%type", LangStrings.HomeChest + " " + LangStrings.TypeChest));
                    e.setCancelled(true);
                } else {
                    e.getPlayer().closeInventory();
                    for (ItemStack i : hc.HomeInventory.getContents())
                    {
                        if(i != null) {
                            e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), i);
                        }
                    }
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.ChestRemoved.replace("%type", LangStrings.HomeChest + " " + LangStrings.TypeChest));
                    hc.RemoveChest();
                }
            } else if(state == Helper.ChestState.KillChest) {
                if(!Main.AllowBreaking) {
                    e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.NotAllowedToBreak.replace("%type", LangStrings.KillChest + " " + LangStrings.ActiveType));
                    e.setCancelled(true);
                    return;
                }
                KillChest kc = KillChest.KillChestByLocation(loc);
                e.getPlayer().closeInventory();
                for(ItemStack i: kc.DeathInventory.getContents()) {
                    if(i != null) {
                        e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), i);
                    }
                }
                kc.RemoveChest(true);
            }
        } else {
            loc = loc.add(0.0, 1.0, 0.0);
            if(loc.getBlock().getType() == Material.SIGN_POST) {
                Helper.ChestState signState = Helper.GetChestType(loc);
                if(signState == Helper.ChestState.DeathChest ) {
                    DeathChest dc = DeathChest.DeathChestByLocation(loc);
                    if(!dc.Owner.equals(e.getPlayer()) && !PermissionManager.PlayerHasPermission(e.getPlayer(), PermissionManager.PROTECTION_BYPASS, false)) {
                        e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.NotAllowedToBreak.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                        e.setCancelled(true);
                    } else {
                        e.getPlayer().closeInventory();
                        for (ItemStack i : dc.DeathInventory.getContents())
                        {
                            if(i != null) {
                                e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), i);
                            }
                        }
                        e.getPlayer().sendMessage(LangStrings.Prefix + " " + LangStrings.ChestRemoved.replace("%type", LangStrings.DeathChest + " " + LangStrings.ActiveType));
                        dc.RemoveChest(true);
                    }
                }  else if(signState == Helper.ChestState.KillChest) {
                    KillChest kc = KillChest.KillChestByLocation(loc);
                    e.getPlayer().closeInventory();
                    for(ItemStack i: kc.DeathInventory.getContents()) {
                        if(i != null) {
                            e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), i);
                        }
                    }
                    kc.RemoveChest(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {
        Iterator<Block> iter = e.blockList().iterator();
        Location blockUnderSign = null;
        while(iter.hasNext()) {
            Block b = iter.next();
            if (b.getType() == Material.CHEST ||b.getType() == Material.ENDER_CHEST) {
                Location loc = b.getLocation();
                Helper.ChestState state = Helper.GetChestType(loc);
                if (state == Helper.ChestState.DeathChest || state == Helper.ChestState.HomeChest || state == Helper.ChestState.KillChest) {
                    iter.remove();
                }
            } else if(b.getType() == Material.SIGN_POST) {
                Location loc = b.getLocation();
                Helper.ChestState state = Helper.GetChestType(loc);
                if (state == Helper.ChestState.DeathChest || state == Helper.ChestState.HomeChest || state == Helper.ChestState.KillChest) {
                    blockUnderSign = b.getLocation().add(0.0, -1.0, 0.0);
                    iter.remove();
                }
            }
        }
        if(blockUnderSign != null && e.blockList().contains(blockUnderSign.getBlock())) {
            e.blockList().remove(blockUnderSign.getBlock());
        }
    }

}
