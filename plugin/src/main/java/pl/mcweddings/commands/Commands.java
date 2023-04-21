package pl.mcweddings.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.data.DataHandler;
import pl.mcweddings.permissions.PermissionManager;
import pl.mcweddings.util.DateManager;
import pl.mcweddings.wedding.Marriage;
import pl.mcweddings.wedding.Reward;

import java.util.ArrayList;
import java.util.List;

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
                if(marry) {
                    marriageRequirements(sender);
                } else {
                    divorceRequirements(sender);
                }
            } else {
                if(marry) {
                    if(args[0].equalsIgnoreCase("rewards")) {
                        if(args.length > 1) {
                            plugin.getMarriageManager().getReward(sender, args[1]);
                        } else {
                            showRewards(sender);
                        }
                    } else if(args[0].equalsIgnoreCase("status")) {
                        showStatus(sender);
                    } else {
                        plugin.getMarriageManager().sendRequest(args[0], sender);
                    }
                } else {
                    showHelp(sender);
                }
            }
        } else {
            if(marry) {
                showHelp(sender);
            } else {
                plugin.getMarriageManager().divorce(sender);
            }
        }
        return true;
    }

    public void showHelp(CommandSender sender) {
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        sender.sendMessage("§d/marry <nick> §4- §cMarry a plyer (or accept request)");
        sender.sendMessage("§d/marry requirements §4- §cRequirements for getting married");
        sender.sendMessage("§d/marry rewards §4- §cRewards for being married");
        sender.sendMessage("§d/marry status §4- §cStatus of your marriage");
        sender.sendMessage("§d/divorce §4- §cDivorce with player");
        sender.sendMessage("§d/divorce requirements §4- §cRequirements for getting divorced");
        if(sender.hasPermission(permissionManager.getPermission(dataHandler.getManagePermission()))) {
            sender.sendMessage("§4--------- §dAdmin commands §4---------");
            sender.sendMessage("§d/marry reload §4- §cReload configuration and data");
        }
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

    public void showRewards(CommandSender sender) {
        Marriage playerMarriage = plugin.getMarriageManager().getPlayerMarriage(sender.getName());
        if(playerMarriage == null) {
            sender.sendMessage(plugin.getDataHandler().getPrefix() + "§cYou're not married");
            return;
        }
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        long daysOfMarriage = DateManager.calculateDays(playerMarriage.getDate(), DateManager.getDate("."));
        sender.sendMessage("§dDays of marriage: §c" + daysOfMarriage);
        sender.sendMessage("§dRewards for being married:");
        List<String> takenRewards = plugin.getMarriageManager().getTakenRewards().getOrDefault(sender.getName(), new ArrayList<>());
        for(Reward reward : plugin.getMarriageManager().getRewards()) {
            if(takenRewards.contains(reward.getId())) continue;
            int day = reward.getDay();
            ItemStack is = reward.getItemStack();
            String rewardDescription =
                    (daysOfMarriage < day) ? "§4(§cremaining " + (day - daysOfMarriage) + " days§4)" : "§4(§aREADY§4)";
            String hoverDescription =
                    (daysOfMarriage < day) ? "§c§lRequires " + day + " days of marriage!" : "§a§lClick to receive reward!";
            String rewardText = is.getItemMeta().getDisplayName() +
                    "\n§4(§d" + is.getType() + "§4) x§d" + is.getAmount() + "\n" + hoverDescription;
            TextComponent component = new TextComponent("§4- §d" + day + " days " + rewardDescription);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rewardText).create()));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marry rewards " + reward.getId()));
            sender.spigot().sendMessage(component);
        }
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

    public void showStatus(CommandSender sender) {
        Marriage m = plugin.getMarriageManager().getPlayerMarriage(sender.getName());
        if(m == null) {
            sender.sendMessage(plugin.getDataHandler().getPrefix() + "§cYou're not married");
            return;
        }
        String first = m.getFirst();
        String second = m.getSecond();
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        sender.sendMessage("§d§l" + first + " §4❤ §d§l" + second);
        sender.sendMessage("§dMarriage date: §c" + m.getDate());
        sender.sendMessage("§dMarriage length: §c" +
                DateManager.calculateDays(m.getDate(), DateManager.getDate(".")) + " days");
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

    public void marriageRequirements(CommandSender sender) {
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        sender.sendMessage("§dMarriage requirements:");
        for(ItemStack is : plugin.getDataHandler().getMarryCost()) {
            String name = "";
            if(!is.getItemMeta().getDisplayName().equals("")) {
                name = is.getItemMeta().getDisplayName() + " ";
            }
            sender.sendMessage(" §4- §f" + name +
                    "§4(§d" + is.getType().name() + "§4) §4x§d" + is.getAmount());
        }
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

    public void divorceRequirements(CommandSender sender) {
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        sender.sendMessage("§dDivorce requirements:");
        for(ItemStack is : plugin.getDataHandler().getDivorceCost()) {
            String name = "";
            if(!is.getItemMeta().getDisplayName().equals("")) {
                name = is.getItemMeta().getDisplayName() + " ";
            }
            sender.sendMessage(" §4- §f" + name +
                    "§4(§d" + is.getType().name() + "§4) §4x§d" + is.getAmount());
        }
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

}
