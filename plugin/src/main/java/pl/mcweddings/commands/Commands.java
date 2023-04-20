package pl.mcweddings.commands;

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
        if(label.equalsIgnoreCase("marry") || label.equalsIgnoreCase("slub")) {
            if(!sender.hasPermission(permissionManager.getPermission(dataHandler.getMarryPermission()))) {
                sender.sendMessage(dataHandler.getPrefix() + dataHandler.getNoPermission());
                return true;
            }
        } else {
            if(!sender.hasPermission(permissionManager.getPermission(dataHandler.getDivorcePermission()))) {
                sender.sendMessage(dataHandler.getPrefix() + dataHandler.getNoPermission());
                return true;
            }
        }
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("requirements")) {

            } else if(args[0].equalsIgnoreCase("rewards")) {

            } else {
                if(label.equalsIgnoreCase("marry") || label.equalsIgnoreCase("slub")) {
                    plugin.getMarriageManager().sendRequest(args[0], sender);
                } else {

                }
            }
        } else {
            ShowHelp(sender);
        }
        return true;
    }

    public void ShowHelp(CommandSender sender) {
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

}
