package pl.mcweddings.wedding;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.util.DateManager;
import pl.mcweddings.util.PlayerUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class MarriageManager {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private final List<Marriage> marriages = new ArrayList<>();
    private final HashMap<Integer, Reward> rewards = new HashMap<>();
    private final HashMap<String, List<String>> requests = new HashMap<>();
    private int bellCount = 0;
    private int bellTask = -1;
    @Setter
    private int killRequestsTask = -1;

    public void createMarriage(String first, String second, CommandSender sender) {
        String prefix = plugin.getDataHandler().getPrefix();
        if(!checkMarriage(first, second, sender)) return;
        Player p = PlayerUtil.getPlayer(first);
        if(p == null) {
            sender.sendMessage(MessageFormat.format(prefix + plugin.getDataHandler().getCannotFoundPlayer(), first));
            return;
        }
        List<Integer> slots = getItemSlots(p, plugin.getDataHandler().getMarryCost());
        if(slots.size() < 1) {
            p.sendMessage(prefix + "§cYou don't have required items to marry someone! Check requirements at /marry requirements");
            sender.sendMessage(prefix + "§cYour partner doesn't have required items. You can't marry right now.");
            return;
        }
        takeItems(p, slots, plugin.getDataHandler().getMarryCost());
        plugin.getServer().broadcastMessage(prefix + MessageFormat.format(plugin.getDataHandler().getMarryMessage(), first, second));
        Marriage marriage = new Marriage(0, first, second, DateManager.getDate("."), "d");
        marriage.setId(plugin.getDataHandler().createMarriage(marriage));
        Location p1loc = p.getLocation();
        p1loc.setY(p1loc.getY() + 2);
        p.getWorld().spawnParticle(Particle.HEART, p1loc, 10);
        Player p2 = PlayerUtil.getPlayer(second);
        if(p2 != null) {
            Location p2loc = p2.getLocation();
            p2loc.setY(p2loc.getY() + 2);
            p2.getWorld().spawnParticle(Particle.HEART, p2loc, 10);
        }
        this.bellCount = 0;
        if(bellTask != -1) {
            plugin.getServer().getScheduler().cancelTask(bellTask);
        }
        this.bellTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for(Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0F, 1.0F);
            }
            if(bellCount >= 4) {
                plugin.getServer().getScheduler().cancelTask(bellTask);
            } else {
                bellCount++;
            }
        }, 0L, 10L);
    }

    public void sendRequest(String nickname, CommandSender sender) {
        String prefix = plugin.getDataHandler().getPrefix();
        if(!(sender instanceof Player)) {
            sender.sendMessage(prefix + plugin.getDataHandler().getMustBePlayer());
            return;
        }
        /*if(nickname.equals(sender.getName())) {
            sender.sendMessage(prefix + plugin.getDataHandler().getMarryHimself());
            return;
        }*/
        if(!checkMarriage(sender.getName(), nickname, sender)) return;
        if(hasRequest(sender.getName(), nickname)) {
            clearRequests(sender.getName());
            createMarriage(nickname, sender.getName(), sender);
            return;
        }
        if(hasRequest(nickname, sender.getName())) {
            sender.sendMessage(prefix + plugin.getDataHandler().getRequestAlreadySent());
            return;
        }
        Player p = PlayerUtil.getPlayer(nickname);
        if(p == null) {
            sender.sendMessage(MessageFormat.format(prefix + plugin.getDataHandler().getCannotFoundPlayer(), nickname));
            return;
        } else {
            if(!((Player) sender).canSee(p)) {
                sender.sendMessage(MessageFormat.format(prefix + plugin.getDataHandler().getCannotFoundPlayer(), nickname));
                return;
            }
        }
        if(getItemSlots((Player) sender, plugin.getDataHandler().getMarryCost()).size() < 1) {
            sender.sendMessage(prefix + "§cYou don't have required items to marry someone! Check requirements at /marry requirements");
            return;
        }
        createRequest(sender.getName(), nickname);
        sender.sendMessage(MessageFormat.format(prefix + plugin.getDataHandler().getRequestSent(), nickname));
        p.sendMessage(MessageFormat.format(prefix + plugin.getDataHandler().getMarriageInquiryMessage(), sender.getName()));
    }

    public void divorce(CommandSender sender) {
        String prefix = plugin.getDataHandler().getPrefix();
        if(!(sender instanceof Player)) {
            sender.sendMessage(prefix + plugin.getDataHandler().getMustBePlayer());
            return;
        }
        if(!isPlayerMarried(sender.getName())) {
            sender.sendMessage(prefix + "§cYou're not married!");
            return;
        }
        List<Integer> slots = getItemSlots((Player) sender, plugin.getDataHandler().getDivorceCost());
        if(slots.size() < 1) {
            sender.sendMessage(prefix + "§cYou don't have required items to get divorced! Check requirements at /divorce requirements");
            return;
        }
        takeItems((Player) sender, slots, plugin.getDataHandler().getDivorceCost());
        Marriage m = getPlayerMarriage(sender.getName());
        String secondPlayer = (m.getFirst().equals(sender.getName()) ? m.getSecond() : m.getFirst());
        sender.sendMessage(MessageFormat.format(prefix + "§aYou successfully divorced with {0}!", secondPlayer));
        plugin.getServer().broadcastMessage(MessageFormat.format(prefix + plugin.getDataHandler().getDivorceMessage(), sender.getName(), secondPlayer));
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0F, 3.0F);
        }
        plugin.getDataHandler().removeMarriage(m);
        marriages.remove(m);
    }

    public boolean isPlayerMarried(String nickname) {
        for(Marriage m : marriages) {
            if(m.getFirst().equals(nickname) || m.getSecond().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    public Marriage getPlayerMarriage(String nickname) {
        for(Marriage m : marriages) {
            if(m.getFirst().equals(nickname) || m.getSecond().equals(nickname)) {
                return m;
            }
        }
        return null;
    }

    public boolean hasRequest(String who, String from) {
        List<String> playerRequests = requests.getOrDefault(who, new ArrayList<>());
        return playerRequests.contains(from);
    }

    public void clearRequests(String who) {
        requests.remove(who);
    }

    public void createRequest(String from, String to) {
        List<String> playerRequests = requests.getOrDefault(to, new ArrayList<>());
        playerRequests.add(from);
        requests.put(to, playerRequests);
        Player p = PlayerUtil.getPlayer(to);
        if(p != null) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        }
    }

    public boolean checkMarriage(String first, String second, CommandSender sender) {
        String prefix = plugin.getDataHandler().getPrefix();
        if(isPlayerMarried(first)) {
            sender.sendMessage(prefix + plugin.getDataHandler().getYouAreMarried());
            return false;
        }
        if(isPlayerMarried(second)) {
            sender.sendMessage(prefix + plugin.getDataHandler().getPlayerAlreadyMarried());
            return false;
        }

        return true;
    }

    public List<Integer> getItemSlots(Player player, List<ItemStack> items) {
        List<Integer> slots = new ArrayList<>();
        int goodItems = 0;
        for(ItemStack is : items) {
            int totalAmount = 0;
            for(int i = 0; i < 36; i++) {
                ItemStack inventoryItem = player.getInventory().getItem(i);
                if(inventoryItem == null) continue;
                if(!is.getType().equals(Material.AIR)) {
                    if(!inventoryItem.getType().equals(is.getType())) continue;
                }
                if(!inventoryItem.getItemMeta().getDisplayName().equals(is.getItemMeta().getDisplayName())) continue;
                if(inventoryItem.getItemMeta().getLore() == null && is.getItemMeta().getLore() != null) continue;
                if(inventoryItem.getItemMeta().getLore() != null && is.getItemMeta().getLore() == null) continue;
                if(inventoryItem.getItemMeta().getLore() != null) {
                    if(is.getItemMeta().getLore() != null) {
                        if(!inventoryItem.getItemMeta().getLore().equals(is.getItemMeta().getLore())) continue;
                    }
                }
                totalAmount += inventoryItem.getAmount();
                slots.add(i);
            }
            if(totalAmount < is.getAmount()) return new ArrayList<>();
            goodItems++;
        }
        if(goodItems < items.size()) return new ArrayList<>();
        return slots;
    }

    public void takeItems(Player player, List<Integer> checkSlots, List<ItemStack> items) {
        for(ItemStack is : items) {
            int required = is.getAmount();
            for(int slot : checkSlots) {
                ItemStack inventoryItem = player.getInventory().getItem(slot);
                if(inventoryItem == null) continue;
                if(!is.getType().equals(Material.AIR)) {
                    if(!inventoryItem.getType().equals(is.getType())) continue;
                }
                if(!inventoryItem.getItemMeta().getDisplayName().equals(is.getItemMeta().getDisplayName())) continue;
                if(inventoryItem.getItemMeta().getLore() == null && is.getItemMeta().getLore() != null) continue;
                if(inventoryItem.getItemMeta().getLore() != null && is.getItemMeta().getLore() == null) continue;
                if(inventoryItem.getItemMeta().getLore() != null) {
                    if(is.getItemMeta().getLore() != null) {
                        if(!inventoryItem.getItemMeta().getLore().equals(is.getItemMeta().getLore())) continue;
                    }
                }
                required -= inventoryItem.getAmount();
                if(required > 0) {
                    inventoryItem.setAmount(0);
                } else {
                    inventoryItem.setAmount(-required);
                    break;
                }
            }
        }
        player.updateInventory();
    }

}
