package pl.mcweddings;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import pl.mcweddings.commands.CommandHelper;
import pl.mcweddings.commands.Commands;
import pl.mcweddings.data.DataHandler;
import pl.mcweddings.events.Events;
import pl.mcweddings.luckperms.LuckPermsManager;
import pl.mcweddings.permissions.PermissionManager;
import pl.mcweddings.wedding.MarriageManager;

@Getter
public final class MCWeddings extends JavaPlugin {

    private static MCWeddings main;
    private DataHandler dataHandler;
    private PermissionManager permissionManager;
    private MarriageManager marriageManager;
    private LuckPermsManager luckPermsManager;
    private Commands commands;
    private CommandHelper commandHelper;
    private Events events;
    private boolean luckPermsAvailable;

    @Override
    public void onEnable() {
        main = this;
        if(getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            getLogger().info("LuckPerms is available! Suffixes are now possible.");
            luckPermsAvailable = true;
            this.luckPermsManager = new LuckPermsManager();
        } else {
            getLogger().info("LuckPerms not found! Remember - you can use LuckPerms to manage suffixes!");
        }
        this.permissionManager = new PermissionManager();
        this.marriageManager = new MarriageManager();
        this.dataHandler = new DataHandler();
        dataHandler.loadConfig();
        this.commands = new Commands();
        this.commandHelper = new CommandHelper();
        this.events = new Events();
        getServer().getPluginManager().registerEvents(events, this);
        String[] cmds = {"marry", "divorce"};
        for(int i = 0; i < cmds.length; i++) {
            getCommand(cmds[i]).setExecutor(this.commands);
            getCommand(cmds[i]).setTabCompleter(this.commandHelper);
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
