package pl.mcweddings.luckperms;

import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

@Getter
public class LuckPermsManager {

    private final LuckPerms luckPermsInstance = LuckPermsProvider.get();

}
