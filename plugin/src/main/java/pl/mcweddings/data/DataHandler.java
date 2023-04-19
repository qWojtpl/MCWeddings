package pl.mcweddings.data;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.permissions.PermissionManager;

import java.io.File;

@Getter
public class DataHandler {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private String prefix;
    private String managePermission;
    private String marryPermission;
    private String divorcePermission;
    private String marryStatusPermission;
    private String noPermission;
    private String marryMessage;
    private String divorceMessage;
    private String marriageInquiryMessage;
    private String marryRequestSentMessage;

    public void loadConfig() {
        plugin.getPermissionManager().getPermissions().clear();
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        this.prefix = yml.getString("config.prefix");
        this.managePermission = yml.getString("config.managePermission");
        this.marryPermission = yml.getString("config.marryPermission");
        this.divorcePermission = yml.getString("config.divorcePermission");
        this.marryStatusPermission = yml.getString("config.marryStatusPermission");
        this.noPermission = getYAMLString(yml, "config.noPermission");
        this.marryMessage = getYAMLString(yml, "config.marryMessage");
        this.divorceMessage = getYAMLString(yml, "config.divorceMessage");
        this.marriageInquiryMessage = getYAMLString(yml, "config.marriageInquiryMessage");
        this.marryRequestSentMessage = getYAMLString(yml, "config.marryRequestSentMessage");
        PermissionManager pm = plugin.getPermissionManager();
        pm.registerPermission(managePermission, "Manage MCWeddings plugin");
        pm.registerPermission(marryPermission, "Permission to marry other player");
        pm.registerPermission(divorcePermission, "Permission to divorce with other player");
        pm.registerPermission(marryStatusPermission, "Permission which be added when player is married");
        loadData();
    }

    public void loadData() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if(!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);

    }

    public String getYAMLString(YamlConfiguration yml, String path) {
        return (yml.getString(path) != null) ? yml.getString(path).replace('&', '§') : "null";
    }

}
