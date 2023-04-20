package pl.mcweddings.util;

import org.bukkit.entity.Player;
import pl.mcweddings.MCWeddings;

import javax.annotation.Nullable;

public class PlayerUtil {

    private static final MCWeddings plugin = MCWeddings.getInstance();

    @Nullable
    public static Player getPlayer(String nickname) {
        for(Player p : plugin.getServer().getOnlinePlayers()) {
            if(p.getName().equals(nickname)) {
                return p;
            }
        }
        return null;
    }

}
