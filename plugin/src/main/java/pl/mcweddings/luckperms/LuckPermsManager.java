package pl.mcweddings.luckperms;

import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.entity.Player;

@Getter
public class LuckPermsManager {

    private final LuckPerms luckPermsInstance = LuckPermsProvider.get();

    public void addMarriagePermission(Player player) {

    }

    public void removeMarriagePermission(Player player) {

    }

}
