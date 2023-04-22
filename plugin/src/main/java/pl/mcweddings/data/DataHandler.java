package pl.mcweddings.data;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.permissions.PermissionManager;
import pl.mcweddings.wedding.Marriage;
import pl.mcweddings.wedding.Reward;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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
    private String suffixColors;
    private String suffixSchema;
    private String requirementsAlias;
    private String statusAlias;
    private String rewardsAlias;
    private String colorAlias;
    private int maxDataIndex;
    private int requestCooldown;
    private int suffixCooldown;

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
        if(plugin.isLuckPermsAvailable()) {
            this.suffixPermission = yml.getString("config.suffixColorPermission");
            this.suffixSchema = yml.getString("config.suffixSchema");
            this.suffixColors = yml.getString("config.suffixColors");
            this.suffixCooldown = yml.getInt("config.suffixCooldown");
        }
        this.requestCooldown = yml.getInt("config.requestCooldown");
        this.requirementsAlias = yml.getString("args-aliases.requirements", "requirements");
        this.statusAlias = yml.getString("args-aliases.status", "status");
        this.rewardsAlias = yml.getString("args-aliases.rewards", "rewards");
        this.colorAlias = yml.getString("args-aliases.color", "color");
        PermissionManager pm = plugin.getPermissionManager();
        pm.registerPermission(managePermission, "Manage MCWeddings plugin");
        pm.registerPermission(marryPermission, "Permission to marry other player");
        pm.registerPermission(divorcePermission, "Permission to divorce with other player");
        pm.registerPermission(marryStatusPermission, "Permission which be added when player is married");
        pm.registerPermission(suffixPermission, "Permission to change suffix color");
        loadMessages(yml);
        loadCost(yml);
        loadRewards(yml);
        loadData();
    }

    public void loadMessages(YamlConfiguration yml) {
        Messages mess = plugin.getMessages();
        ConfigurationSection section = yml.getConfigurationSection("messages");
        for(String key : section.getKeys(false)) {
            mess.getMessages().put(key, getYAMLString(yml, "messages." + key));
        }
    }

    public void loadCost(YamlConfiguration yml) {
        String[] types = {"marry", "divorce"};
        for(String type : types) {
            ConfigurationSection section = yml.getConfigurationSection("config.cost." + type);
            if(section == null) continue;
            for(String key : section.getKeys(false)) {
                Material m;
                String path = "config.cost." + type + "." + key;
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
                if(type.equals("marry")) {
                    marryCost.add(is);
                } else {
                    divorceCost.add(is);
                }
            }
        }
    }

    public void loadRewards(YamlConfiguration yml) {
        ConfigurationSection section = yml.getConfigurationSection("config.rewards");
        if(section == null) return;
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

    public void updateSuffix(Marriage m, String newSuffix) {
        File dataFile = getDataFile();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        String path = "data." + m.getId() + ".suffix";
        yml.set(path, newSuffix);
        try {
            yml.save(dataFile);
        } catch(IOException e) {
            plugin.getLogger().severe("Cannot save file data.yml (saving suffix change)!");
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

}
