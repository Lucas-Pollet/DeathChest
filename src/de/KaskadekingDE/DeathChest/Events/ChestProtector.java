package de.KaskadekingDE.DeathChest.Events;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.KillChest;
import de.KaskadekingDE.DeathChest.Classes.Helper;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

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

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Location loc = block.getLocation();
        Helper.ChestState state = Helper.GetChestType(loc);
        if(block.getType() == Material.CHEST) {
            if(state == Helper.ChestState.DeathChest) {
                DeathChest dc = DeathChest.DeathChestByLocation(loc);
                if(!dc.Owner.equals(e.getPlayer()) && !e.getPlayer().hasPermission("deathchest.protection.bypass")) {
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
                if(!hc.Owner.equals(e.getPlayer()) && !e.getPlayer().hasPermission("deathchest.protection.bypass")) {
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
                if(!dc.Owner.equals(e.getPlayer()) && !e.getPlayer().hasPermission("deathchest.protection.bypass")) {
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
                if(!hc.Owner.equals(e.getPlayer()) && !e.getPlayer().hasPermission("deathchest.protection.bypass")) {
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
                    if(!dc.Owner.equals(e.getPlayer()) && !e.getPlayer().hasPermission("deathchest.protection.bypass")) {
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
            if (b.getType() == Material.CHEST) {
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
