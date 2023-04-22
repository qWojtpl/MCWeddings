package pl.mcweddings.luckperms;

import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.mcweddings.MCWeddings;

import java.time.Duration;

@Getter
public class LuckPermsManager {

    private final LuckPerms luckPermsInstance = LuckPermsProvider.get();
    private final MCWeddings plugin = MCWeddings.getInstance();

    public void addMarriagePermission(Player player) {
        luckPermsInstance.getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().add(Node.builder(plugin.getDataHandler().getMarryStatusPermission()).build());
        });
    }

    public void removeMarriagePermission(Player player) {
        luckPermsInstance.getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().remove(Node.builder(plugin.getDataHandler().getMarryStatusPermission()).build());
        });
    }

    public void addSuffix(Player player, String suffix) {
        luckPermsInstance.getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().add(Node.builder(suffix).build());
        });
    }

    public void removeSuffix(Player player, String suffix) {
        luckPermsInstance.getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().remove(Node.builder(suffix).build());
        });
    }

}
