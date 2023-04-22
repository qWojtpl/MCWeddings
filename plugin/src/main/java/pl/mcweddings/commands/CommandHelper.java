package pl.mcweddings.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.data.DataHandler;
import pl.mcweddings.util.PlayerUtil;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper implements TabCompleter {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private final DataHandler dataHandler = plugin.getDataHandler();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return null;
        List<String> completions = new ArrayList<>();
        if(args.length == 1) {
            if(label.equalsIgnoreCase("slub") || label.equalsIgnoreCase("marry")) {
                completions.add(dataHandler.getRewardsAlias());
                completions.add(dataHandler.getStatusAlias());
                if(plugin.isLuckPermsAvailable()) completions.add(dataHandler.getColorAlias());
                addPlayers(completions, (Player) sender);
            }
            completions.add(dataHandler.getRequirementsAlias());
        } else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("status")) {
                addPlayers(completions, (Player) sender);
            }
        }
        return StringUtil.copyPartialMatches(args[args.length-1], completions, new ArrayList<>());
    }

    private void addPlayers(List<String> completions, Player sender) {
        for(Player p : plugin.getServer().getOnlinePlayers()) {
            if(p.getName().equals(sender.getName())) continue;
            if(PlayerUtil.isVanished(p)) continue;
            completions.add(p.getName());
        }
    }

}
