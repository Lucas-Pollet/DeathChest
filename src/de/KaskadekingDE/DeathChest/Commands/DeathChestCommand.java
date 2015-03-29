package de.KaskadekingDE.DeathChest.Commands;

import de.KaskadekingDE.DeathChest.Config.LangStrings;
import de.KaskadekingDE.DeathChest.Events.DeathChestListener;
import de.KaskadekingDE.DeathChest.Events.HomeChestListener;
import de.KaskadekingDE.DeathChest.Helper;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.TreeMap;
import java.util.UUID;

public class DeathChestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if(args.length == 0) {
            cs.sendMessage(LangStrings.Prefix + " §aPlugin made by KaskadekingDE.");
            cs.sendMessage(LangStrings.HelpNotice);
            return true;
        }
        if(args[0].equalsIgnoreCase("help")) {
            if(!cs.hasPermission("deathchest.help")) {
                cs.sendMessage(LangStrings.Prefix + LangStrings.NoPermissions);
                return true;
            }
            cs.sendMessage(LangStrings.Prefix + " " + LangStrings.HelpPage + " (1/1):");
            cs.sendMessage("§9/dc reload §7- " + LangStrings.HelpReload);
            cs.sendMessage("§9/dc help   §7- " + LangStrings.HelpHelp);
            cs.sendMessage("§9/dc home   §7- " + LangStrings.HelpHome);
            cs.sendMessage("§9/dc locations [player] §7- " + LangStrings.HelpLocations);
            cs.sendMessage("§9/dc remove <id> [player] §7- " + LangStrings.HelpRemove);
            return true;
        } else if(args[0].equalsIgnoreCase("reload")) {
            if(!cs.hasPermission("deathchest.reload")) {
                cs.sendMessage(LangStrings.Prefix + LangStrings.NoPermissions);
                return true;
            }
            System.out.println("[DeathChest] Saving chest inventorys... This can take a while!");
            Main.saveDeathChestInventory();
            Main.saveKillerChestInventory();
            Main.saveHomeChestInventory();
            Main.playerData.saveConfig();
            Main.plugin.reloadConfig();
            Main.plugin.loadConfig();
            Main.playerData.reloadConfig();
            cs.sendMessage(LangStrings.Prefix + " " + LangStrings.ConfigReloaded);
            return true;
        } else if(args[0].equalsIgnoreCase("home")) {
            if(!(cs instanceof Player)) {
                cs.sendMessage(LangStrings.OnlyPlayers);
                return true;
            }
            if(Main.UsePermission && !cs.hasPermission("deathchest.home")) {
                cs.sendMessage(LangStrings.Prefix + LangStrings.NoPermissions);
                return true;
            }
            Player p = (Player) cs;
            if(checkAlreadyHome(p)) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.AlreadySet);
                return true;
            }
            World w = p.getWorld();
            if(!Main.enabledWorlds.contains(w) && !Main.ExecludeHomeChestFromWhitelist) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.NotEnabled);
                return true;
            }
            cs.sendMessage(LangStrings.Prefix + " " + LangStrings.SetupHome);

            HomeChestListener.readyPlayers.add(p);
            return true;
        } else if(args[0].equalsIgnoreCase("locations")) {
            if(args.length == 2) {
                if(Main.UsePermission && !cs.hasPermission("deathchest.locations.others")) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                    return true;
                }
                OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                TreeMap<String, Location> chests = DeathChestListener.playersChest(p.getUniqueId().toString());
                if(chests == null || chests.size() == 0) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoDeathChestOther.replace("%p", args[1]));
                    return true;
                }
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestOfOther.replace("%p", args[1]));
                for(String key: chests.keySet()) {
                    Location value = chests.get(key);
                    cs.sendMessage(LangStrings.Prefix + " §e" + key + ": §a World: " + value.getWorld().getName() + " X: " + value.getX() + " Y: " + value.getY() + " Z: " + value.getZ());
                }
            } else if(args.length == 1) {
                if(!(cs instanceof Player)) {
                    cs.sendMessage(LangStrings.OnlyPlayers);
                    return true;
                }
                if(Main.UsePermission && !cs.hasPermission("deathchest.locations")) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                    return true;
                }
                Player p = (Player) cs;
                TreeMap<String, Location> chests = DeathChestListener.playersChest(p.getUniqueId().toString());
                if(chests == null || chests.size() == 0) {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.NoDeathChest);
                    return true;
                }
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.DeathChestOf);
                for(String key: chests.keySet()) {
                    Location value = chests.get(key);
                    p.sendMessage(LangStrings.Prefix + " §e" + key + ": §a World: " + value.getWorld().getName() + " X: " + value.getX() + " Y: " + value.getY() + " Z: " + value.getZ());
                }
            } else {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidArgument);
            }
        } else if(args[0].equalsIgnoreCase("remove")) {
            if(args.length == 2) {
                if(!(cs instanceof Player)) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                    return true;
                }
                Player p = (Player)cs;
                if(!p.hasPermission("deathchest.remove")) {
                    p.sendMessage(LangStrings.Prefix + " "+ LangStrings.NoPermissions);
                    return true;
                }
                String id = args[1];
                if(id.equalsIgnoreCase("home") || Helper.IsNumeric(id)) {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveWarning);
                    boolean success = RemoveChest(id, p);
                    if(success) {
                        p.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveSuccessful.replace("%id", id));
                    } else {
                        p.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveFailed.replace("%id", id));
                    }

                } else {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidId);
                    return true;
                }


            } else if (args.length == 3) {
                if(!cs.hasPermission("deathchest.remove.others")) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                    return true;
                }

                String id = args[1];
                if(!id.equalsIgnoreCase("home") || !Helper.IsNumeric(id)) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidArgument);
                    return true;
                }
                cs.sendMessage(LangStrings.Prefix + " "+ LangStrings.RemoveWarning);
                Player target = Bukkit.getOfflinePlayer(args[2]).getPlayer();
                boolean success = RemoveChest(id, target);
                if(success) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveSuccessful.replace("%id", id));
                } else {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveFailed);
                }
            } else {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidArgument);
            }
        }
        return true;
    }

    private boolean checkAlreadyHome(Player p ) {
        if(Main.playerData.getPlayerConfig().get("death-chests." + p.getUniqueId() + ".home-chest.x") != null) {
            if(Main.playerData.getPlayerConfig().get("death-chests." + p.getUniqueId() + ".home-chest.y") != null) {
                if(Main.playerData.getPlayerConfig().get("death-chests." + p.getUniqueId() + ".home-chest.z") != null) {
                    if(Main.playerData.getPlayerConfig().get("death-chests." + p.getUniqueId() + ".home-chest.world") != null) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    private boolean RemoveChest(String id, Player p) {
        if(id.equalsIgnoreCase("home")) {
            id = "home-chest";
        }
        try {
            if(Main.playerData.getPlayerConfig().contains("death-chests." + p.getUniqueId()) && Main.playerData.getPlayerConfig().contains("death-chests." + p.getUniqueId() + "." + id)) {
                int x = Main.playerData.getPlayerConfig().getInt("death-chests." + p.getUniqueId() + "." + id + ".x");
                int y = Main.playerData.getPlayerConfig().getInt("death-chests." + p.getUniqueId()+ "." + id + ".y");
                int z = Main.playerData.getPlayerConfig().getInt("death-chests." + p.getUniqueId()+ "." + id  + ".z");
                String w = Main.playerData.getPlayerConfig().getString("death-chests." + p.getUniqueId() + "." + id + ".world");
                World world = Bukkit.getWorld(w);
                if(world != null) {
                    Location loc = new Location(world, x, y, z);
                    if(loc.getWorld().getBlockAt(loc).getType() == Material.CHEST) {
                        Chest chest = (Chest) loc.getWorld().getBlockAt(loc).getState();
                        DeathChestListener.chestInventory.remove(chest);
                        loc.getWorld().getBlockAt(loc).setType(Material.AIR);
                    }
                    if(id.equals("home-chest")) {
                        DeathChestListener.homeChest.remove(p);
                    } else {
                        DeathChestListener.deathChests.get(p).remove(loc);
                    }
                    DeathChestListener.chestRemover.remove(loc);
                }
                Main.playerData.getPlayerConfig().set("death-chests." + p.getUniqueId() + "." + id, null);
                Main.playerData.savePlayerConfig();
                return true;
            } else {
                return false;
            }
        } catch(NullPointerException npe) {
            return false;
        }
    }
}
