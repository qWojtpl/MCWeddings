package pl.mcweddings.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.data.DataHandler;
import pl.mcweddings.data.Messages;
import pl.mcweddings.permissions.PermissionManager;
import pl.mcweddings.util.DateManager;
import pl.mcweddings.wedding.Marriage;
import pl.mcweddings.wedding.Reward;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private final DataHandler dataHandler = plugin.getDataHandler();
    private final Messages messages = plugin.getMessages();
    private final PermissionManager permissionManager = plugin.getPermissionManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean marry = false;
        if(label.equalsIgnoreCase("marry") || label.equalsIgnoreCase("slub")) {
            if(!sender.hasPermission(permissionManager.getPermission(dataHandler.getMarryPermission()))) {
                sender.sendMessage(messages.getMessage("prefix") + messages.getMessage("noPermission"));
                return true;
            }
            marry = true;
        } else if(label.equalsIgnoreCase("rozwod") || label.equalsIgnoreCase("divorce")) {
            if(!sender.hasPermission(permissionManager.getPermission(dataHandler.getDivorcePermission()))) {
                sender.sendMessage(messages.getMessage("prefix") + messages.getMessage("noPermission"));
                return true;
            }
        } else {
            sender.sendMessage(messages.getMessage("prefix") + messages.getMessage("cantDoIt"));
            return true;
        }
        if(args.length > 0) {
            if(sender.hasPermission(permissionManager.getPermission(dataHandler.getManagePermission()))) {
                if(args[0].equalsIgnoreCase("reload")) {
                    dataHandler.loadConfig();
                    sender.sendMessage(messages.getMessage("prefix") + "§aReloaded!");
                    return true;
                }
            }
            if(args[0].equalsIgnoreCase(dataHandler.getRequirementsAlias())) {
                if(marry) {
                    marriageRequirements(sender);
                } else {
                    divorceRequirements(sender);
                }
            } else {
                if(marry) {
                    if(args[0].equalsIgnoreCase(dataHandler.getRewardsAlias())) {
                        if(args.length > 1) {
                            plugin.getMarriageManager().getReward(sender, args[1]);
                        } else {
                            showRewards(sender);
                        }
                    } else if(args[0].equalsIgnoreCase(dataHandler.getStatusAlias())) {
                        if(args.length > 1) {
                            showStatus(sender, args[1]);
                        } else {
                            showStatus(sender, sender.getName());
                        }
                    } else if(args[0].equalsIgnoreCase(dataHandler.getColorAlias()) && plugin.isLuckPermsAvailable()) {
                        if(args.length > 1) {
                            plugin.getMarriageManager().changeSuffix(sender, args[1]);
                        } else {
                            sender.sendMessage(messages.getMessage("prefix") +
                                    MessageFormat.format(messages.getMessage("correctUsage"),
                                            "/marry color <" + args[1] + ":" + dataHandler.getSuffixColors() + ">"));
                        }
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
        sender.sendMessage(messages.getMessage("help-marry"));
        sender.sendMessage(messages.getMessage("help-mRequirements"));
        sender.sendMessage(messages.getMessage("help-rewards"));
        sender.sendMessage(messages.getMessage("help-status"));
        if(plugin.isLuckPermsAvailable()) {
            sender.sendMessage(messages.getMessage("help-color"));
        }
        sender.sendMessage(messages.getMessage("help-divorce"));
        sender.sendMessage(messages.getMessage("help-dRequirements"));
        if(sender.hasPermission(permissionManager.getPermission(dataHandler.getManagePermission()))) {
            sender.sendMessage("§4----------------------------------");
            sender.sendMessage(messages.getMessage("help-reload"));
        }
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

    public void showRewards(CommandSender sender) {
        Marriage playerMarriage = plugin.getMarriageManager().getPlayerMarriage(sender.getName());
        if(playerMarriage == null) {
            sender.sendMessage(messages.getMessage("prefix") + messages.getMessage("notMarried"));
            return;
        }
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        long daysOfMarriage = DateManager.calculateDays(playerMarriage.getDate(), DateManager.getDate("."));
        sender.sendMessage(MessageFormat.format(messages.getMessage("marriageLength"), daysOfMarriage));
        sender.sendMessage(messages.getMessage("rewards"));
        List<String> takenRewards = plugin.getMarriageManager().getTakenRewards().getOrDefault(sender.getName(), new ArrayList<>());
        for(Reward reward : plugin.getMarriageManager().getRewards()) {
            if(takenRewards.contains(reward.getId())) continue;
            int day = reward.getDay();
            ItemStack is = reward.getItemStack();
            String rewardDescription =
                    (daysOfMarriage < day) ? "§4(" + MessageFormat.format(messages.getMessage("remaining"),
                            (day - daysOfMarriage)) + "§4)" : "§4(" + messages.getMessage("ready") + "§4)";
            String hoverDescription =
                    (daysOfMarriage < day) ? MessageFormat.format(messages.getMessage("requiresDaysOfMarriage"), day)
                            : messages.getMessage("clickToReceive");
            String rewardText = is.getItemMeta().getDisplayName() +
                    "\n§4(§d" + is.getType() + "§4) x§d" + is.getAmount() + "\n" + hoverDescription;
            TextComponent component = new TextComponent("§4- §d" + day + " " + messages.getMessage("days") + " " + rewardDescription);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(rewardText).create()));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/marry " + dataHandler.getRewardsAlias() + " " + reward.getId()));
            sender.spigot().sendMessage(component);
        }
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

    public void showStatus(CommandSender sender, String player) {
        Marriage m = plugin.getMarriageManager().getPlayerMarriage(player);
        if(m == null) {
            if(sender.getName().equals(player)) {
                sender.sendMessage(messages.getMessage("prefix") + messages.getMessage("notMarried"));
            } else {
                sender.sendMessage(messages.getMessage("prefix") + messages.getMessage("playerNotMarried"));
            }
            return;
        }
        String first = m.getFirst();
        String second = m.getSecond();
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        sender.sendMessage("§d§l" + first + " §4❤ §d§l" + second);
        sender.sendMessage(MessageFormat.format(messages.getMessage("marriageDate"), m.getDate()));
        sender.sendMessage(MessageFormat.format(messages.getMessage("marriageLength"),
                DateManager.calculateDays(m.getDate(), DateManager.getDate("."))));
        if(plugin.isLuckPermsAvailable()) {
            String[] suffixSplit = m.getSuffix().split("&");
            String suffix = "";
            for(int i = 1; i < suffixSplit.length; i++) {
                suffix += "§" + suffixSplit[i];
            }
            sender.sendMessage(MessageFormat.format(messages.getMessage("suffix"), suffix));
        }
        sender.sendMessage(" ");
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
    }

    public void marriageRequirements(CommandSender sender) {
        sender.sendMessage("§c<----------> §dMCWeddings §c<---------->");
        sender.sendMessage(" ");
        sender.sendMessage(messages.getMessage("marriageRequirements"));
        for(ItemStack is : dataHandler.getMarryCost()) {
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
        sender.sendMessage(messages.getMessage("divorceRequirements"));
        for(ItemStack is : dataHandler.getDivorceCost()) {
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
