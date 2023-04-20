package pl.mcweddings.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.data.DataHandler;
import pl.mcweddings.permissions.PermissionManager;

public class Commands implements CommandExecutor {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private final DataHandler dataHandler = plugin.getDataHandler();
    private final PermissionManager permissionManager = plugin.getPermissionManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean marry = false;
        if(label.equalsIgnoreCase("marry") || label.equalsIgnoreCase("slub")) {
            if(!sender.hasPermission(permissionManager.getPermission(dataHandler.getMarryPermission()))) {
                sender.sendMessage(dataHandler.getPrefix() + dataHandler.getNoPermission());
                return true;
            }
            marry = true;
        } else {
            if(!sender.hasPermission(permissionManager.getPermission(dataHandler.getDivorcePermission()))) {
                sender.sendMessage(dataHandler.getPrefix() + dataHandler.getNoPermission());
                return true;
            }
        }
        if(args.length > 0) {
            if(sender.hasPermission(permissionManager.getPermission(dataHandler.getManagePermission()))) {
                if(args[0].equalsIgnoreCase("reload")) {
                    plugin.getDataHandler().loadConfig();
                    sender.sendMessage(dataHandler.getPrefix() + "§aReloaded!");
                    return true;
                }
            }
            if(args[0].equalsIgnoreCase("requirements")) {

            } else if(args[0].equalsIgnoreCase("rewards") && marry) {
                showRewards(sender);
            } else {
                if(marry) {
                    plugin.getMarriageManager().sendRequest(args[0], sender);
                } else {

                }
            }
        } else {
            showHelp(sender);
        }
        return true;
    }

    public void showHelp(CommandSender sender) {
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        sender.sendMessage("§d/marry <nick> §4- §cMarry a plyer (or accept request)");
        sender.sendMessage("§d/marry requirements §4- §cRequirements for getting married");
        sender.sendMessage("§d/marry rewards §4- §cRewards for being married");
        sender.sendMessage("§d/divorce <nick> §4- §cDivorce with player");
        sender.sendMessage("§d/divorce requirements §4- §cRequirements for getting divorced");
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

    public void showRewards(CommandSender sender) {
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        sender.sendMessage("§dRewards for marriage length:");

        sender.sendMessage("- 1 days (READY). CLICK ME");
        sender.sendMessage("- 7 days (remaining 5 days). CLICK ME");
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

}
