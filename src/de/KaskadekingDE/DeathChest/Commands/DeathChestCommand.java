package de.KaskadekingDE.DeathChest.Commands;

import de.KaskadekingDE.DeathChest.Config.LangStrings;
import de.KaskadekingDE.DeathChest.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DeathChestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
        if(args.length == 0) {
            cs.sendMessage(Main.Prefix + " §aPlugin made by KaskadekingDE.");
            cs.sendMessage(LangStrings.HelpNotice);
            return true;
        }
        if(args[0].equalsIgnoreCase("help")) {
            if(!cs.hasPermission("deathchest.help")) {
                cs.sendMessage(Main.Prefix + LangStrings.NoPermissions);
                return true;
            }
            cs.sendMessage(Main.Prefix + " " + LangStrings.HelpPage + " (1/1):");
            cs.sendMessage("§9/dc reload §7- " + LangStrings.HelpReload);
            cs.sendMessage("§9/dc help   §7- " + LangStrings.HelpHelp);
            return true;
        } else if(args[0].equalsIgnoreCase("reload")) {
            if(!cs.hasPermission("deathchest.reload")) {
                cs.sendMessage(Main.Prefix + LangStrings.NoPermissions);
                return true;
            }
            Main.plugin.reloadConfig();
            Main.plugin.loadConfig();
            cs.sendMessage(Main.Prefix + " " + LangStrings.ConfigReloaded);
            return true;
        }
        return false;
    }
}
