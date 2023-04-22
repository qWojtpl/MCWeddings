package pl.mcweddings.data;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.permissions.PermissionManager;
import pl.mcweddings.wedding.Marriage;
import pl.mcweddings.wedding.Reward;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DataHandler {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private final List<ItemStack> marryCost = new ArrayList<>();
    private final List<ItemStack> divorceCost = new ArrayList<>();
    private String managePermission;
    private String marryPermission;
    private String divorcePermission;
    private String marryStatusPermission;
    private String suffixPermission;
    private String suffixSchema;
    private int maxDataIndex;
    private int clearInterval;

    public void loadConfig() {
        plugin.getPermissionManager().clearPermissions();
        plugin.getMarriageManager().getMarriages().clear();
        plugin.getMarriageManager().getRewards().clear();
        plugin.getMarriageManager().getTakenRewards().clear();
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
        this.suffixPermission = yml.getString("config.suffixColorPermission");
        this.suffixSchema = getYAMLString(yml, "config.suffixSchema");
        this.clearInterval = yml.getInt("config.clearInterval");
        PermissionManager pm = plugin.getPermissionManager();
        pm.registerPermission(managePermission, "Manage MCWeddings plugin");
        pm.registerPermission(marryPermission, "Permission to marry other player");
        pm.registerPermission(divorcePermission, "Permission to divorce with other player");
        pm.registerPermission(marryStatusPermission, "Permission which be added when player is married");
        pm.registerPermission(suffixPermission, "Permission to change suffix color");
        runClearTask();
        loadMessages(yml);
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
                if (name != null) {
                    meta.setDisplayName(name.replace('&', '§'));
                }
                List<String> lore = yml.getStringList(path + ".lore");
                List<String> newLore = new ArrayList<>();
                for (String l : lore) {
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
                if(is.hasItemMeta()) {
                    ItemMeta meta = is.getItemMeta();
                    String name = yml.getString(path + ".name");
                    if (name != null) {
                        meta.setDisplayName(name.replace('&', '§'));
                    }
                    List<String> lore = yml.getStringList(path + ".lore");
                    List<String> newLore = new ArrayList<>();
                    for (String l : lore) {
                        newLore.add(l.replace('&', '§'));
                    }
                    meta.setLore(newLore);
                    is.setItemMeta(meta);
                }
                divorceCost.add(is);
            }
        }
        section = yml.getConfigurationSection("config.rewards");
        if(section != null) {
            for(String key : section.getKeys(false)) {
                String path = "config.rewards." + key;
                int day = yml.getInt(path + ".requiredDays");
                Material m = Material.AIR;
                String item = yml.getString(path + ".item");
                if(item != null) {
                    m = Material.getMaterial(item);
                    if(m == null) {
                        m = Material.AIR;
                        plugin.getLogger().warning("Cannot compare " + item + " with a correct material!");
                    }
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
                if (lore.size() > 0) meta.setLore(newLore);
                meta.setUnbreakable(yml.getBoolean(path + ".unbreakable"));
                List<String> givenEnch = yml.getStringList(path + ".enchantments");
                for(String enchant : givenEnch) {
                    String[] split = enchant.split(":");
                    if(split.length != 2) continue;
                    Enchantment enchantment = Enchantment.getByName(split[0]);
                    if(enchantment == null) {
                        plugin.getLogger().warning("Cannot compare " + split[0] + " with a correct enchantment!");
                        continue;
                    }
                    int level;
                    try {
                        level = Integer.parseInt(split[1]);
                    } catch(NumberFormatException e) {
                        plugin.getLogger().warning("Cannot compare " + split[1] + " with a correct enchantment level!");
                        continue;
                    }
                    meta.addEnchant(enchantment, level, true);
                }
                is.setItemMeta(meta);
                Reward reward = new Reward(key, is, day, yml.getString(path + ".execute"));
                plugin.getMarriageManager().getRewards().add(reward);
            }
        }
        loadData();
    }

    public void loadMessages(YamlConfiguration yml) {
        Messages mess = plugin.getMessages();
        mess.setPrefix(getYAMLString(yml, "messages.prefix"));
        mess.setNoPermission(getYAMLString(yml, "messages.noPermission"));
        mess.setMarryMessage(getYAMLString(yml, "messages.marryMessage"));
        mess.setDivorceMessage(getYAMLString(yml, "messages.divorceMessage"));
        mess.setMarriageInquiryMessage(getYAMLString(yml, "messages.marriageInquiryMessage"));
        mess.setMarryRequestSentMessage(getYAMLString(yml, "messages.marryRequestSentMessage"));
        mess.setCannotFoundPlayer(getYAMLString(yml, "messages.cannotFoundPlayer"));
        mess.setMustBePlayer(getYAMLString(yml, "messages.mustBePlayer"));
        mess.setMarryHimself(getYAMLString(yml, "messages.marryHimself"));
        mess.setRequestAlreadySent(getYAMLString(yml, "messages.requestAlreadySent"));
        mess.setRequestSent(getYAMLString(yml, "messages.requestSent"));
        mess.setPlayerAlreadyMarried(getYAMLString(yml, "messages.playerAlreadyMarried"));
        mess.setYouAreMarried(getYAMLString(yml, "messages.youAreMarried"));
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
            String first = yml.getString(path + ".first");
            String second = yml.getString(path + ".second");
            new Marriage(id, first, second, yml.getString(path + ".date"), yml.getString(path + ".suffix"));
            plugin.getMarriageManager().getTakenRewards().put(first, yml.getStringList(path + ".rewards." + first));
            plugin.getMarriageManager().getTakenRewards().put(second, yml.getStringList(path + ".rewards." + second));
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

    public void removeMarriage(Marriage m) {
        File dataFile = getDataFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        String path = "data." + m.getId();
        yml.set(path, null);
        try {
            yml.save(dataFile);
        } catch(IOException e) {
            plugin.getLogger().severe("Cannot save file data.yml (removing marriage)!");
        }
    }

    public void takeReward(Marriage m, String reward_id, String player) {
        File dataFile = getDataFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        String path = "data." + m.getId() + ".rewards." + player;
        List<String> rewards = yml.getStringList(path);
        rewards.add(reward_id);
        yml.set(path, rewards);
        try {
            yml.save(dataFile);
        } catch(IOException e) {
            plugin.getLogger().severe("Cannot save file data.yml (saving reward take)!");
        }
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
        if(plugin.getMarriageManager().getClearRequestsTask() != -1) return;
        plugin.getMarriageManager().setClearRequestsTask(plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                () -> {
                    plugin.getMarriageManager().getRequests().clear();
                }, 0L, 20L * plugin.getDataHandler().getClearInterval()));
    }

}
