package pl.mcweddings.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.wedding.Marriage;

public class Events implements Listener {

    private final MCWeddings plugin = MCWeddings.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(!plugin.isLuckPermsAvailable()) return;
        Player p = event.getPlayer();
        if(p.hasPermission(plugin.getDataHandler().getMarryStatusPermission())) {
            Marriage m = plugin.getMarriageManager().getPlayerMarriage(p.getName());
            if(m == null) {
                plugin.getLuckPermsManager().removeMarriagePermission(p);
            } else {
                plugin.getLuckPermsManager().addSuffix(p, m.getSuffix());
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if(!plugin.isLuckPermsAvailable()) return;
        Player p = event.getPlayer();
        Marriage m = plugin.getMarriageManager().getPlayerMarriage(p.getName());
        if(m != null) {
            plugin.getLuckPermsManager().removeSuffix(p, m.getSuffix());
        }
    }

}
