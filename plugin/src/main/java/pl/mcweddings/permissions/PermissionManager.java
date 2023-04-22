package pl.mcweddings.permissions;

import lombok.Getter;
import org.bukkit.permissions.Permission;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.data.DataHandler;

import java.util.HashMap;

@Getter
public class PermissionManager {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private final DataHandler dataHandler = plugin.getDataHandler();
    private final HashMap<String, Permission> permissions = new HashMap<>();

    public void registerPermission(String permission, String description) {
        if(permission == null || description == null) return;
        Permission perm = new Permission(permission, description);
        plugin.getServer().getPluginManager().removePermission(perm);
        plugin.getServer().getPluginManager().addPermission(perm);
        permissions.put(permission, perm);
    }

    public Permission getPermission(String permission) {
        return permissions.getOrDefault(permission, null);
    }

    public void clearPermissions() {
        for(String key : permissions.keySet()) {
            plugin.getServer().getPluginManager().removePermission(permissions.get(key));
        }
        permissions.clear();
    }

}
