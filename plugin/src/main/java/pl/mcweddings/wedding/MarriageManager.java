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
    private final List<Reward> rewards = new ArrayList<>();
    private final HashMap<String, List<String>> takenRewards = new HashMap<>();
    private final HashMap<String, List<String>> requests = new HashMap<>();
    private int bellCount = 0;
    private int bellTask = -1;
    @Setter
    private int clearRequestsTask = -1;

    public void createMarriage(String first, String second, CommandSender sender) {
        String prefix = plugin.getMessages().getPrefix();
        if(!checkMarriage(first, second, sender)) return;
        Player p = PlayerUtil.getPlayer(first);
        if(p == null) {
            sender.sendMessage(MessageFormat.format(prefix + plugin.getMessages().getCannotFoundPlayer(), first));
            return;
        } else {
            if(!((Player) sender).canSee(p)) {
                sender.sendMessage(MessageFormat.format(prefix + plugin.getMessages().getCannotFoundPlayer(), first));
                return;
            }
        }
        List<Integer> slots = getItemSlots(p, plugin.getDataHandler().getMarryCost());
        if(slots.size() < 1) {
            p.sendMessage(prefix + "§cYou don't have required items to marry someone! Check requirements at /marry requirements");
            sender.sendMessage(prefix + "§cYour partner doesn't have required items. You can't marry right now.");
            return;
        }
        takeItems(p, slots, plugin.getDataHandler().getMarryCost());
        plugin.getServer().broadcastMessage(prefix + MessageFormat.format(plugin.getMessages().getMarryMessage(), first, second));
        Marriage marriage = new Marriage(0, first, second, DateManager.getDate("."),
                MessageFormat.format(plugin.getDataHandler().getSuffixSchema(), "d"));
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
        if(plugin.isLuckPermsAvailable()) {
            plugin.getLuckPermsManager().addMarriagePermission(p);
            plugin.getLuckPermsManager().addSuffix(p,marriage.getSuffix());
            if(p2 != null) {
                plugin.getLuckPermsManager().addMarriagePermission(p2);
                plugin.getLuckPermsManager().addSuffix(p2, marriage.getSuffix());
            }
        }
    }

    public void sendRequest(String nickname, CommandSender sender) {
        String prefix = plugin.getMessages().getPrefix();
        if(!(sender instanceof Player)) {
            sender.sendMessage(prefix + plugin.getMessages().getMustBePlayer());
            return;
        }
        /*if(nickname.equals(sender.getName())) {
            sender.sendMessage(prefix + plugin.getMessages().getMarryHimself());
            return;
        }*/
        if(!checkMarriage(sender.getName(), nickname, sender)) return;
        if(hasRequest(sender.getName(), nickname)) {
            clearRequests(sender.getName());
            createMarriage(nickname, sender.getName(), sender);
            return;
        }
        if(hasRequest(nickname, sender.getName())) {
            sender.sendMessage(prefix + plugin.getMessages().getRequestAlreadySent());
            return;
        }
        Player p = PlayerUtil.getPlayer(nickname);
        if(p == null) {
            sender.sendMessage(MessageFormat.format(prefix + plugin.getMessages().getCannotFoundPlayer(), nickname));
            return;
        } else {
            if(!((Player) sender).canSee(p)) {
                sender.sendMessage(MessageFormat.format(prefix + plugin.getMessages().getCannotFoundPlayer(), nickname));
                return;
            }
        }
        if(getItemSlots((Player) sender, plugin.getDataHandler().getMarryCost()).size() < 1) {
            sender.sendMessage(prefix + "§cYou don't have required items to marry someone! Check requirements at /marry requirements");
            return;
        }
        createRequest(sender.getName(), nickname);
        sender.sendMessage(MessageFormat.format(prefix + plugin.getMessages().getRequestSent(), nickname));
        p.sendMessage(MessageFormat.format(prefix + plugin.getMessages().getMarriageInquiryMessage(), sender.getName()));
    }

    public void divorce(CommandSender sender) {
        String prefix = plugin.getMessages().getPrefix();
        if(!(sender instanceof Player)) {
            sender.sendMessage(prefix + plugin.getMessages().getMustBePlayer());
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
        plugin.getServer().broadcastMessage(MessageFormat.format(prefix + plugin.getMessages().getDivorceMessage(), sender.getName(), secondPlayer));
        for(Player player : plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0F, 3.0F);
        }
        plugin.getDataHandler().removeMarriage(m);
        takenRewards.remove(m.getFirst());
        takenRewards.remove(m.getSecond());
        marriages.remove(m);
        if(plugin.isLuckPermsAvailable()) {
            plugin.getLuckPermsManager().removeMarriagePermission((Player) sender);
            plugin.getLuckPermsManager().removeSuffix((Player) sender, m.getSuffix());
            Player p2 = PlayerUtil.getPlayer(secondPlayer);
            if(p2 != null) {
                plugin.getLuckPermsManager().removeMarriagePermission(p2);
                plugin.getLuckPermsManager().removeSuffix(p2, m.getSuffix());
            }
        }
    }

    public void getReward(CommandSender sender, String id) {
        String prefix = plugin.getMessages().getPrefix();
        if(!(sender instanceof Player)) {
            sender.sendMessage(prefix + plugin.getMessages().getMustBePlayer());
            return;
        }
        Marriage m = getPlayerMarriage(sender.getName());
        if(m == null) {
            sender.sendMessage(prefix + "§cYou're not married!");
            return;
        }
        Reward r = null;
        for(Reward rew : getRewards()) {
            if(rew.getId().equals(id)) {
                r = rew;
                break;
            }
        }
        if(r == null) {
            sender.sendMessage(prefix + "§cCannot find this reward!");
            return;
        }
        long daysOfMarriage = DateManager.calculateDays(m.getDate(), DateManager.getDate("."));
        if(daysOfMarriage < r.getDay()) {
            sender.sendMessage(prefix + "§cYou can't receive this reward yet!");
            return;
        }
        List<String> takenRewards = getTakenRewards().getOrDefault(sender.getName(), new ArrayList<>());
        if(takenRewards.contains(r.getId())) {
            sender.sendMessage(prefix + "§cYou've already received this reward!");
            return;
        }
        Player p = PlayerUtil.getPlayer(sender.getName());
        if(p == null) return;
        takenRewards.add(r.getId());
        getTakenRewards().put(sender.getName(), takenRewards);
        plugin.getDataHandler().takeReward(m, r.getId(), sender.getName());
        ItemStack is = new ItemStack(r.getItemStack());
        HashMap<Integer, ItemStack> another = p.getInventory().addItem(is);
        if(another.size() > 0) {
            for(int key : another.keySet()) {
                p.getWorld().dropItem(p.getLocation(), another.get(key));
            }
        }
        if(r.getExecute() != null) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    MessageFormat.format(r.getExecute(), sender.getName()));
        }
        plugin.getCommands().showRewards(sender);
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

    public void clearRequests(String receiver) {
        requests.remove(receiver);
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
        String prefix = plugin.getMessages().getPrefix();
        if(isPlayerMarried(first)) {
            sender.sendMessage(prefix + plugin.getMessages().getYouAreMarried());
            return false;
        }
        if(isPlayerMarried(second)) {
            sender.sendMessage(prefix + plugin.getMessages().getPlayerAlreadyMarried());
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
