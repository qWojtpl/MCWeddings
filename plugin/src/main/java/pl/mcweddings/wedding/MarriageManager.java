package pl.mcweddings.wedding;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.data.DataHandler;
import pl.mcweddings.util.DateManager;
import pl.mcweddings.util.PlayerUtil;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MarriageManager {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private final DataHandler dataHandler = plugin.getDataHandler();
    private final List<Marriage> marriages = new ArrayList<>();
    @Setter
    private int bellCount = 0;
    private int bellTask;

    public void createMarriage(String first, String second, CommandSender sender) {
        String prefix = dataHandler.getPrefix();
        if(isPlayerMarried(first) || isPlayerMarried(second)) {
            sender.sendMessage(prefix + "§cThis player is already married!");
            return;
        }
        plugin.getServer().broadcastMessage(String.format(dataHandler.getMarryMessage(), first, second));
        new Marriage(first, second, DateManager.getDate("-"), "d");
        this.bellTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for(Player p : plugin.getServer().getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1.0F, 1.0F);
            }
            if(bellCount >= 4) {
                plugin.getServer().getScheduler().cancelTask(bellTask);
            } else {
                bellCount++;
            }
        }, 0L, 10L);
    }

    public void sendRequest(String nickname, CommandSender sender) {
        String prefix = dataHandler.getPrefix();
        if(!(sender instanceof Player)) {
            sender.sendMessage(prefix + "§cYou must be a player!");
            return;
        }
        if(nickname.equals(sender.getName())) {
            sender.sendMessage(prefix + "§cYou can't marry yourself!");
            return;
        }
        if(isPlayerMarried(sender.getName())) {
            sender.sendMessage(prefix + "§cYou're already married!");
            return;
        }
        if(isPlayerMarried(nickname)) {
            sender.sendMessage(prefix + "§cThis player is already married!");
            return;
        }
        Player p = PlayerUtil.getPlayer(nickname);
        if(p == null) {
            sender.sendMessage(prefix + "§cCannot find player: " + nickname);
            return;
        }
        p.sendMessage(String.format(dataHandler.getMarriageInquiryMessage(), sender.getName()));
    }

    public boolean isPlayerMarried(String nickname) {
        for(Marriage m : marriages) {
            if(m.getFirst().equals(nickname) || m.getSecond().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

}
