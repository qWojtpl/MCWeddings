package pl.mcweddings.data;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.permissions.PermissionManager;
import pl.mcweddings.wedding.Marriage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DataHandler {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private String prefix;
    private String managePermission;
    private String marryPermission;
    private String divorcePermission;
    private String marryStatusPermission;
    private int clearInterval;
    private String noPermission;
    private String marryMessage;
    private String divorceMessage;
    private String marriageInquiryMessage;
    private String marryRequestSentMessage;
    private String cannotFoundPlayer;
    private String mustBePlayer;
    private String marryHimself;
    private String requestAlreadySent;
    private String requestSent;
    private String playerAlreadyMarried;
    private String youAreMarried;
    private final List<ItemStack> marryCost = new ArrayList<>();
    private final List<ItemStack> divorceCost = new ArrayList<>();
    private int maxDataIndex;

    public void loadConfig() {
        plugin.getPermissionManager().getPermissions().clear();
        plugin.getMarriageManager().getMarriages().clear();
        marryCost.clear();
        divorceCost.clear();
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
        this.managePermission = yml.getString("config.managePermission");
        this.marryPermission = yml.getString("config.marryPermission");
        this.divorcePermission = yml.getString("config.divorcePermission");
        this.marryStatusPermission = yml.getString("config.marryStatusPermission");
        this.clearInterval = yml.getInt("config.clearInterval");
        runClearTask();
        this.prefix = getYAMLString(yml, "messages.prefix");
        this.noPermission = getYAMLString(yml, "messages.noPermission");
        this.marryMessage = getYAMLString(yml, "messages.marryMessage");
        this.divorceMessage = getYAMLString(yml, "messages.divorceMessage");
        this.marriageInquiryMessage = getYAMLString(yml, "messages.marriageInquiryMessage");
        this.marryRequestSentMessage = getYAMLString(yml, "messages.marryRequestSentMessage");
        this.cannotFoundPlayer = getYAMLString(yml, "messages.cannotFoundPlayer");
        this.mustBePlayer = getYAMLString(yml, "messages.mustBePlayer");
        this.marryHimself = getYAMLString(yml, "messages.marryHimself");
        this.requestAlreadySent = getYAMLString(yml, "messages.requestAlreadySent");
        this.requestSent = getYAMLString(yml, "messages.requestSent");
        this.playerAlreadyMarried = getYAMLString(yml, "messages.playerAlreadyMarried");
        this.youAreMarried = getYAMLString(yml, "messages.youAreMarried");
        PermissionManager pm = plugin.getPermissionManager();
        pm.registerPermission(managePermission, "Manage MCWeddings plugin");
        pm.registerPermission(marryPermission, "Permission to marry other player");
        pm.registerPermission(divorcePermission, "Permission to divorce with other player");
        pm.registerPermission(marryStatusPermission, "Permission which be added when player is married");
        ConfigurationSection section = yml.getConfigurationSection("config.cost.marry");
        if(section != null) {
            for(String key : section.getKeys(false)) {
                Material m;
                String path = "config.cost.marry." + key;
                if(yml.getString(path + ".item") != null) {
                    m = Material.getMaterial(yml.getString(path + ".item"));
                    if(m == null) {
                        continue;
                    }
                } else {
                    continue;
                }
                ItemStack is = new ItemStack(m);
                is.setAmount(yml.getInt(path + ".count"));
                ItemMeta meta = is.getItemMeta();
                String name = yml.getString(path + ".name");
                if(name != null) {
                    meta.setDisplayName(name.replace('&', '§'));
                }
                List<String> lore = yml.getStringList(path + ".lore");
                List<String> newLore = new ArrayList<>();
                for(String l : lore) {
                    newLore.add(l.replace('&', '§'));
                }
                meta.setLore(newLore);
                is.setItemMeta(meta);
                marryCost.add(is);
            }
        }
        section = yml.getConfigurationSection("config.cost.divorce");
        if(section != null) {
            for(String key : section.getKeys(false)) {
                Material m;
                String path = "config.cost.divorce." + key;
                if(yml.getString(path + ".item") != null) {
                    m = Material.getMaterial(yml.getString(path + ".item"));
                    if(m == null) {
                        continue;
                    }
                } else {
                    continue;
                }
                ItemStack is = new ItemStack(m);
                is.setAmount(yml.getInt(path + ".count"));
                ItemMeta meta = is.getItemMeta();
                String name = yml.getString(path + ".name");
                if(name != null) {
                    meta.setDisplayName(name.replace('&', '§'));
                }
                List<String> lore = yml.getStringList(path + ".lore");
                List<String> newLore = new ArrayList<>();
                for(String l : lore) {
                    newLore.add(l.replace('&', '§'));
                }
                meta.setLore(newLore);
                is.setItemMeta(meta);
                divorceCost.add(is);
            }
        }
        loadData();
    }

    public void loadData() {
        File dataFile = getDataFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = yml.getConfigurationSection("data");
        if(section == null) return;
        maxDataIndex = 0;
        for(String key : section.getKeys(false)) {
            int id = Integer.parseInt(key);
            if(id > maxDataIndex) maxDataIndex = id;
            String path = "data." + key;
            new Marriage(id, yml.getString(path + ".first"), yml.getString(path + ".second"),
                    yml.getString(path + ".date"), yml.getString(path + ".suffix"));
        }
    }

    public int createMarriage(Marriage marriage) {
        File dataFile = getDataFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        String path = "data." + ++maxDataIndex;
        yml.set(path + ".first", marriage.getFirst());
        yml.set(path + ".second", marriage.getSecond());
        yml.set(path + ".date", marriage.getDate());
        yml.set(path + ".suffix", marriage.getSuffix());
        try {
            yml.save(dataFile);
        } catch(IOException e) {
            plugin.getLogger().severe("Cannot save file data.yml (creating new marriage)!");
        }
        return maxDataIndex;
    }

    public File getDataFile() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if(!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        return dataFile;
    }

    public String getYAMLString(YamlConfiguration yml, String path) {
        return (yml.getString(path) != null) ? yml.getString(path).replace('&', '§') : "null";
    }

    private void runClearTask() {
        if(plugin.getMarriageManager().getKillRequestsTask() != -1) return;
        plugin.getMarriageManager().setKillRequestsTask(plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                () -> {
                    plugin.getMarriageManager().getRequests().clear();
                }, 0L, 20L * plugin.getDataHandler().getClearInterval()));
    }

}
