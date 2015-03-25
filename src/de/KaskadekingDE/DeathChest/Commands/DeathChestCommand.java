package de.KaskadekingDE.DeathChest.Commands;

import de.KaskadekingDE.DeathChest.Config.LangStrings;
import de.KaskadekingDE.DeathChest.Events.DeathChestListener;
import de.KaskadekingDE.DeathChest.Events.HomeChestListener;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
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
            cs.sendMessage("§9/dc locations [player] §7- Show's all locations of death chest's from you or another player.");
            return true;
        } else if(args[0].equalsIgnoreCase("reload")) {
            if(!cs.hasPermission("deathchest.reload")) {
                cs.sendMessage(LangStrings.Prefix + LangStrings.NoPermissions);
                return true;
            }
            Main.plugin.reloadConfig();
            Main.plugin.loadConfig();
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
            if(!(cs instanceof Player)) {
                cs.sendMessage(LangStrings.OnlyPlayers);
                return true;
            }
            if(args.length == 2) {
                if(Main.UsePermission && !cs.hasPermission("deathchest.locations.others")) {
                    cs.sendMessage(LangStrings.Prefix + LangStrings.NoPermissions);
                    return true;
                }
                OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
                TreeMap<String, Location> chests = DeathChestListener.playersChest(p.getUniqueId().toString());
                if(chests == null || chests.size() == 0) {
                    cs.sendMessage(LangStrings.Prefix + " §c" + args[1] + " don't have any death chest or a home chest");
                    return true;
                }
                cs.sendMessage(LangStrings.Prefix + " §aDeath Chest's of " + args[1]);
                for(String key: chests.keySet()) {
                    Location value = chests.get(key);
                    cs.sendMessage(LangStrings.Prefix + " §e" + key + ": §a X: " + value.getX() + " Y: " + value.getY() + " Z: " + value.getZ());
                }
            } else if(args.length == 1) {
                if(Main.UsePermission && !cs.hasPermission("deathchest.locations")) {
                    cs.sendMessage(LangStrings.Prefix + LangStrings.NoPermissions);
                    return true;
                }
                Player p = (Player) cs;
                TreeMap<String, Location> chests = DeathChestListener.playersChest(p.getUniqueId().toString());
                if(chests == null || chests.size() == 0) {
                    p.sendMessage(LangStrings.Prefix + " §cYou don't have any death chest or a home chest");
                    return true;
                }
                cs.sendMessage(LangStrings.Prefix + " §aYour death chest's: ");
                for(String key: chests.keySet()) {
                    Location value = chests.get(key);
                    p.sendMessage(LangStrings.Prefix + " §e" + key + ": §a X: " + value.getX() + " Y: " + value.getY() + " Z: " + value.getZ());
                }
            }

        }
        return true;
    }

    private boolean checkAlreadyHome(Player p ) {
        if(Main.plugin.getConfig().get("death-chests." + p.getUniqueId() + ".home-chest.x") != null) {
            if(Main.plugin.getConfig().get("death-chests." + p.getUniqueId() + ".home-chest.y") != null) {
                if(Main.plugin.getConfig().get("death-chests." + p.getUniqueId() + ".home-chest.z") != null) {
                    if(Main.plugin.getConfig().get("death-chests." + p.getUniqueId() + ".home-chest.world") != null) {
                        return true;
                    }
                }

            }
        }
        return false;
    }
}
