package de.KaskadekingDE.DeathChest.Commands;

import de.KaskadekingDE.DeathChest.Classes.Chests.DeathChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.HomeChest;
import de.KaskadekingDE.DeathChest.Classes.Chests.KillChest;
import de.KaskadekingDE.DeathChest.Classes.Helper;
import de.KaskadekingDE.DeathChest.Events.HomeChestEvent;
import de.KaskadekingDE.DeathChest.Language.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class DeathChestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if(args.length == 0) {
            cs.sendMessage("§6[§cDeathChest§6] §aDeathChest v" + Main.plugin.getDescription().getVersion() + " by KaskadekingDE");
            cs.sendMessage(LangStrings.SeeHelp);
            return true;
        }
        if(args[0].equalsIgnoreCase("help")) {
            if(!cs.hasPermission("deathchest.help")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                return true;
            }
            cs.sendMessage(LangStrings.Prefix + " " + LangStrings.HelpTitle);
            cs.sendMessage("§e/dchest help §7- §aShow the help page");
            cs.sendMessage("§e/dchest reload §7- §aReloads the plugin config");
            cs.sendMessage("§e/dchest home §7- §aSet your home chest");
            cs.sendMessage("§e/dchest locations [player] §7- §aShow all locations of your death chest or from another player");
            cs.sendMessage("§e/dchest remove <id> [death/kill/home] [player] §7- §aRemoves a death chest from the config & memory.");
            return true;
        } else if(args[0].equalsIgnoreCase("reload")) {
            if(!cs.hasPermission("deathchest.reload")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                return true;
            }
            Main.languageConfig.reloadLanguageConfig();
            Main.plugin.reloadConfig();
            Main.playerData.reloadPlayerConfig();
            Main.plugin.SaveConfig();
            Main.plugin.LoadConfig();
            cs.sendMessage(LangStrings.Prefix + " " + LangStrings.ConfigReloaded);
            return true;
        } else if(args[0].equalsIgnoreCase("home")) {
            if(!(cs instanceof Player)) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.OnlyPlayers);
                return true;
            }
            Player p = (Player) cs;
            if(!p.hasPermission("deathchest.place.home")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                return true;
            }
            HomeChest hc = HomeChest.HomeChestByPlayer(p);
            if(hc != null) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.AlreadyAHomeChest);
                return true;
            }
            HomeChestEvent.setHome.add(p);
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.SetupHomeChest);
            return true;
        } else if(args[0].equalsIgnoreCase("locations")) {
            if(args.length == 1) {
                if(!(cs instanceof Player)) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.OnlyPlayers);
                    return true;
                } else if(!cs.hasPermission("deathchest.locations")) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                    return true;
                }
                Player p = (Player)cs;
                List<DeathChest> deathChestList = DeathChest.DeathChestsByOwner(p);
                List<KillChest> killChestList = KillChest.KillChestsByOwner(p);
                Collections.sort(killChestList);
                Collections.sort(deathChestList);
                HomeChest hc = HomeChest.HomeChestByPlayer(p);
                if(hc == null && deathChestList.size() == 0 && killChestList.size() == 0) {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.NoDeathChest);
                    return true;
                }
                if(deathChestList.size() != 0 || hc != null) {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.YourDeathChests);
                    if(hc != null) {
                        int x = hc.ChestLocation.getBlockX();
                        int y = hc.ChestLocation.getBlockY();
                        int z = hc.ChestLocation.getBlockZ();
                        World w = hc.ChestLocation.getWorld();
                        p.sendMessage("§e'Home' §7: §aX: " + x + " Y: " + y + " Z: " + z + " World: " + w.getName());
                    }
                }
                for(DeathChest dc: deathChestList) {
                    String id = Integer.toString(dc.GetId());
                    int x = dc.ChestLocation.getBlockX();
                    int y = dc.ChestLocation.getBlockY();
                    int z = dc.ChestLocation.getBlockZ();
                    World w = dc.ChestLocation.getWorld();
                    p.sendMessage("§6'" + id + "' §7: §aX: " + x + " Y: " + y + " Z: " + z + " World: " + w.getName());
                }
                if(killChestList.size() != 0) {
                    p.sendMessage(LangStrings.Prefix + " " + LangStrings.YourKillChests);
                    for(KillChest kc: killChestList) {
                        String id = Integer.toString(kc.GetId());
                        int x = kc.ChestLocation.getBlockX();
                        int y = kc.ChestLocation.getBlockY();
                        int z = kc.ChestLocation.getBlockZ();
                        World w = kc.ChestLocation.getWorld();
                        p.sendMessage("§6'" + id + "' §7: §aX: " + x + " Y: " + y + " Z: " + z + " World: " + w.getName());
                    }
                }
            } else if(args.length == 2) {
                if(!cs.hasPermission("deathchest.locations.others")) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                    return true;
                }
                String playerName = args[1];
                Player sender = (Player) cs;
                OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
                List<DeathChest> deathChestList = DeathChest.DeathChestsByOwner(p);
                List<KillChest> killChestList = KillChest.KillChestsByOwner(p);
                Collections.sort(killChestList);
                Collections.sort(deathChestList);
                HomeChest hc = HomeChest.HomeChestByPlayer(p);
                if(hc == null && deathChestList.size() == 0 && killChestList.size() == 0) {
                    sender.sendMessage(LangStrings.Prefix + " " + LangStrings.PlayerNoDeathChest.replace("%player", playerName));
                    return true;
                }
                sender.sendMessage(LangStrings.Prefix + " " + LangStrings.PlayerDeathChests.replace("%player", playerName));
                if(hc != null) {
                    int x = hc.ChestLocation.getBlockX();
                    int y = hc.ChestLocation.getBlockY();
                    int z = hc.ChestLocation.getBlockZ();
                    World w = hc.ChestLocation.getWorld();
                    sender.sendMessage("§e'Home' §7: §aX: " + x + " Y: " + y + " Z: " + z + " World: " + w.getName());
                }
                for(DeathChest dc: deathChestList) {
                    String id = Integer.toString(dc.GetId());
                    int x = dc.ChestLocation.getBlockX();
                    int y = dc.ChestLocation.getBlockY();
                    int z = dc.ChestLocation.getBlockZ();
                    World w = dc.ChestLocation.getWorld();
                    sender.sendMessage("§e'" + id + "' §7: §aX: " + x + " Y: " + y + " Z: " + z + " World: " + w.getName());
                }
                if(killChestList.size() != 0) {
                    sender.sendMessage(LangStrings.Prefix + " " + LangStrings.PlayerKillerChests.replace("%player", playerName));
                    for(KillChest kc: killChestList) {
                        String id = Integer.toString(kc.GetId());
                        int x = kc.ChestLocation.getBlockX();
                        int y = kc.ChestLocation.getBlockY();
                        int z = kc.ChestLocation.getBlockZ();
                        World w = kc.ChestLocation.getWorld();
                        sender.sendMessage("§6'" + id + "' §7: §aX: " + x + " Y: " + y + " Z: " + z + " World: " + w.getName());
                    }
                }
            } else {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.TooManyOrTooFewArguments);
                return true;
            }
        } else if(args[0].equalsIgnoreCase("remove")) {
            return RemoveCommand(cs, cmd, label, args);
        }
        return true;
    }

    public boolean RemoveCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if(args.length == 2) {
            if(!args[1].equalsIgnoreCase("home")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidId + " " + LangStrings.RemoveHomeChestWarning);
                return true;
            }
            if(!(cs instanceof Player)) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.OnlyPlayers);
                return true;
            } else if(!cs.hasPermission("deathchest.remove")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                return true;
            }
            Player p = (Player) cs;
            HomeChest hc = HomeChest.HomeChestByPlayer(p);
            if(hc == null) {
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.NoHomeChest);
                return true;
            }
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveWarning);
            p.closeInventory();
            for (ItemStack i : hc.HomeInventory.getContents())
            {
                if(i != null) {
                    p.getWorld().dropItemNaturally(p.getLocation(), i);
                }
            }
            Location loc = hc.ChestLocation;
            hc.RemoveChest();
            if(loc.getBlock().getType() == Material.CHEST) {
                loc.getBlock().setType(Material.AIR);
            }
            p.sendMessage(LangStrings.Prefix + " " + LangStrings.HomeChestRemoved);
        } else if(args.length == 3) {
            String type = args[2];
            String id = args[1];
            if(type.equalsIgnoreCase("home")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidType + " " + LangStrings.RemoveHomeChestWarning);
                return true;
            }
            if(!type.equalsIgnoreCase("death") && !type.equalsIgnoreCase("kill")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidType + " " + LangStrings.TryWithType.replace("%id", id));
                return true;
            }
            if(!(cs instanceof Player)) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.OnlyPlayers);
                return true;
            } else if(!cs.hasPermission("deathchest.remove")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                return true;
            }
            Player p = (Player) cs;
            if(type.equalsIgnoreCase("death")) {
                if(!Helper.IsNumeric(id)) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidId + " " + LangStrings.IdIsNotANumber.replace("%id", id));
                    return true;
                }
                int numId = Integer.parseInt(id);
                DeathChest dc = DeathChest.DeathChestById(numId);
                if(dc == null) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoChestWithId.replace("%type", LangStrings.DeathChest).replace("%id", id));
                    return true;
                }
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveWarning);
                p.closeInventory();
                for (ItemStack i : dc.DeathInventory.getContents())
                {
                    if(i != null) {
                        p.getWorld().dropItemNaturally(p.getLocation(), i);
                    }
                }
                dc.RemoveChest();
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.SuccessfullyRemoved.replace("%id", id));
            } else if(type.equalsIgnoreCase("kill")) {
                if(!Helper.IsNumeric(id)) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidId + " " + LangStrings.IdIsNotANumber.replace("%id", id));
                    return true;
                }
                int numId = Integer.parseInt(id);
                KillChest kc = KillChest.KillChestById(numId);
                if(kc == null) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoChestWithId.replace("%type", LangStrings.KillChest).replace("%id", id));
                    return true;
                }
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveWarning);
                p.closeInventory();
                for (ItemStack i : kc.DeathInventory.getContents())
                {
                    if(i != null) {
                        p.getWorld().dropItemNaturally(p.getLocation(), i);
                    }
                }
                kc.RemoveChest();
                p.sendMessage(LangStrings.Prefix + " " + LangStrings.SuccessfullyRemoved.replace("%id", id));
                return true;
            }
        } else if(args.length == 4) {
            String type = args[2];
            String id = args[1];
            String playerName = args[3];
            if(!type.equalsIgnoreCase("death") && !type.equalsIgnoreCase("kill") && !type.equalsIgnoreCase("home")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidType + " " + LangStrings.TryWithTypePlayer.replace("%id", id).replace("%player", playerName));
                return true;
            }
            if(!cs.hasPermission("deathchest.remove")) {
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoPermissions);
                return true;
            }
            OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
            if(type.equalsIgnoreCase("death")) {
                if(!Helper.IsNumeric(id)) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidId + " " + LangStrings.IdIsNotANumber.replace("%id", id));
                    return true;
                }
                int numId = Integer.parseInt(id);
                DeathChest dc = DeathChest.DeathChestById(numId);
                if(dc == null) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoChestWithIdPlayer.replace("%type", LangStrings.DeathChest).replace("%id", id).replace("%player", playerName));
                    return true;
                }
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveWarning);
                if(p.isOnline()) {
                    for (ItemStack i : dc.DeathInventory.getContents())
                    {
                        if(i != null) {
                            p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), i);
                        }
                    }
                }
                dc.RemoveChest();
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.SuccessfullyRemovedPlayer.replace("%id", id).replace("%player", playerName));
            } else if(type.equalsIgnoreCase("kill")) {
                if(!Helper.IsNumeric(id)) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.InvalidId + " " + LangStrings.IdIsNotANumber.replace("%id", id));
                    return true;
                }
                int numId = Integer.parseInt(id);
                KillChest kc = KillChest.KillChestById(numId);
                if(kc == null) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoChestWithIdPlayer.replace("%type", LangStrings.KillChest).replace("%id", id).replace("%player", playerName));
                    return true;
                }
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveWarning);
                if(p.isOnline()) {
                    for (ItemStack i : kc.DeathInventory.getContents())
                    {
                        if(i != null) {
                            p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), i);
                        }
                    }
                }
                kc.RemoveChest();
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.SuccessfullyRemovedPlayer.replace("%id", id).replace("%player", playerName));
                return true;
            } else if(type.equalsIgnoreCase("home")) {
                HomeChest hc = HomeChest.HomeChestByPlayer(p);
                if(hc == null) {
                    cs.sendMessage(LangStrings.Prefix + " " + LangStrings.NoHomeChestPlayer.replace("%player", p.getName()));
                    return true;
                }
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.RemoveWarning);
                if(p.isOnline()) {
                    for (ItemStack i : hc.HomeInventory.getContents())
                    {
                        if(i != null) {
                            p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), i);
                        }
                    }
                }
                Location loc = hc.ChestLocation;
                hc.RemoveChest();
                if(loc.getBlock().getType() == Material.CHEST) {
                    loc.getBlock().setType(Material.AIR);
                }
                cs.sendMessage(LangStrings.Prefix + " " + LangStrings.HomeChestRemovedPlayer.replace("%player", p.getName()));
            }
        } else {
            cs.sendMessage(LangStrings.Prefix + " " + LangStrings.TooManyOrTooFewArguments);
            return true;
        }
        return true;
    }
}
