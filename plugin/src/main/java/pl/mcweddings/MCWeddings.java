package pl.mcweddings;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import pl.mcweddings.commands.CommandHelper;
import pl.mcweddings.commands.Commands;
import pl.mcweddings.data.DataHandler;
import pl.mcweddings.luckperms.LuckPermsManager;
import pl.mcweddings.permissions.PermissionManager;

@Getter
public final class MCWeddings extends JavaPlugin {

    private static MCWeddings main;
    private DataHandler dataHandler;
    private PermissionManager permissionManager;
    private LuckPermsManager luckPermsManager;
    private boolean luckPermsAvailable;

    @Override
    public void onEnable() {
        if(getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            getLogger().info("LuckPerms is available! Suffixes are now possible.");
            luckPermsAvailable = true;
            this.luckPermsManager = new LuckPermsManager();
        } else {
            getLogger().info("LuckPerms not found! Remember - you can use LuckPerms to manage suffixes!");
        }
        this.permissionManager = new PermissionManager();
        this.dataHandler = new DataHandler();
        dataHandler.loadConfig();
        String[] commands = {"marry", "divorce"};
        for(int i = 0; i < commands.length; i++) {
            getCommand(commands[i]).setExecutor(new Commands());
            getCommand(commands[i]).setTabCompleter(new CommandHelper());
        }
        getLogger().info("Loaded.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Bye! <3");
    }

    public static MCWeddings getInstance() {
        return main;
    }
}
