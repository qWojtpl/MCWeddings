package pl.mcweddings.wedding;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.mcweddings.MCWeddings;
import pl.mcweddings.data.Messages;
import pl.mcweddings.util.DateManager;
import pl.mcweddings.util.PlayerUtil;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class MarriageManager {

    private final MCWeddings plugin = MCWeddings.getInstance();
    private final Messages messages = plugin.getMessages();
    private final List<Marriage> marriages = new ArrayList<>();
    private final List<Reward> rewards = new ArrayList<>();
    private final HashMap<String, List<String>> takenRewards = new HashMap<>();
    private final HashMap<String, List<String>> requests = new HashMap<>();
    private final List<String> colorChanges = new ArrayList<>();
    private int bellCount = 0;
    private int bellTask = -1;
    @Setter
    private int clearRequestsTask = -1;

    /**
     * Creates marriage. Method checks if these players are online,
     * takes items from first player, plays bell sound and if available -
     * adds permissions and suffixes
     *
     * @param   first   First player
     * @param   second  Second player
     * @param   sender  Request accepter (Command sender)
     */
    public void createMarriage(String first, String second, Player sender, boolean forced) {
        String prefix = messages.getMessage("prefix");
        if(!checkMarriage(sender, second)) return;
        Player p = PlayerUtil.getPlayer(first);
        Player p2 = PlayerUtil.getPlayer(second);
        if(p == null || p2 == null) {
            sender.sendMessage(MessageFormat.format(prefix + messages.getMessage("cannotFoundPlayer"), first));
            return;
        } else {
            if(PlayerUtil.isVanished(p)) {
                sender.sendMessage(MessageFormat.format(prefix + messages.getMessage("cannotFoundPlayer"), first));
                return;
            }
        }
        if(!forced) {
            if(plugin.getDataHandler().getMaxDistance() != 0) {
                if (!p.getWorld().equals(p2.getWorld())) {
                    sender.sendMessage(prefix + messages.getMessage("tooFarAway"));
                    return;
                }
                if (p.getLocation().distance(p2.getLocation()) > plugin.getDataHandler().getMaxDistance()) {
                    sender.sendMessage(prefix + messages.getMessage("tooFarAway"));
                    return;
                }
            }
            List<Integer> slots = getItemSlots(p, plugin.getDataHandler().getMarryCost());
            if(slots.size() < 1) {
                p.sendMessage(prefix + messages.getMessage("marryNoRequiredItems"));
                sender.sendMessage(prefix + messages.getMessage("partnerWithoutItems"));
                return;
            }
            takeItems(p, slots, plugin.getDataHandler().getMarryCost());
        }
        String suffix = "";
        if (plugin.isLuckPermsAvailable()) {
            suffix = MessageFormat.format(plugin.getDataHandler().getSuffixSchema(), "d");
        }
        plugin.getServer().broadcastMessage(prefix + MessageFormat.format(messages.getMessage("marryMessage"), first, second));
        Marriage marriage = new Marriage(0, first, second, DateManager.getDate("."),
                suffix);
        marriage.setId(plugin.getDataHandler().createMarriage(marriage));
        Location p1loc = p.getLocation();
        p1loc.setY(p1loc.getY() + 2);
        p.getWorld().spawnParticle(Particle.HEART, p1loc, 10);
        Location p2loc = p2.getLocation();
        p2loc.setY(p2loc.getY() + 2);
        p2.getWorld().spawnParticle(Particle.HEART, p2loc, 10);
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
            plugin.getLuckPermsManager().addMarriagePermission(p2);
            plugin.getLuckPermsManager().addSuffix(p2, marriage.getSuffix());
        }
        createColorCooldown(p.getName());
        createColorCooldown(p2.getName());
    }

    /**
     * Sending marriage request to player. If there's already request
     * from that player, then marriage is being created.
     *
     * @param   nickname    To which player we're sending marry request
     * @param   sender      Request sender
     */
    public void sendRequest(String nickname, Player sender) {
        String prefix = messages.getMessage("prefix");
        if(nickname.equals(sender.getName())) {
            sender.sendMessage(prefix + messages.getMessage("marryHimself"));
            return;
        }
        if(!checkMarriage(sender, nickname)) return;
        if(hasRequest(sender.getName(), nickname)) {
            clearRequests(sender.getName());
            createMarriage(nickname, sender.getName(), sender, false);
            return;
        }
        if(hasRequest(nickname, sender.getName())) {
            sender.sendMessage(prefix + messages.getMessage("requestAlreadySent"));
            return;
        }
        Player p = PlayerUtil.getPlayer(nickname);
        if(p == null) {
            sender.sendMessage(
                    MessageFormat.format(prefix + messages.getMessage("cannotFoundPlayer"), nickname));
            return;
        } else {
            if(PlayerUtil.isVanished(p)) {
                sender.sendMessage(
                        MessageFormat.format(prefix + messages.getMessage("cannotFoundPlayer"), nickname));
                return;
            }
        }
        if(plugin.getDataHandler().getMaxDistance() != 0) {
            if (!p.getWorld().equals(sender.getWorld())) {
                sender.sendMessage(prefix + messages.getMessage("tooFarAway"));
                return;
            }
            if (p.getLocation().distance(sender.getLocation()) > plugin.getDataHandler().getMaxDistance()) {
                sender.sendMessage(prefix + messages.getMessage("tooFarAway"));
                return;
            }
        }
        if(getItemSlots(sender, plugin.getDataHandler().getMarryCost()).size() < 1) {
            sender.sendMessage(prefix + messages.getMessage("marryNoRequiredItems"));
            return;
        }
        createRequest(sender.getName(), nickname);
        sender.sendMessage(MessageFormat.format(prefix + messages.getMessage("requestSent"), nickname));
        p.sendMessage(MessageFormat.format(prefix + messages.getMessage("marriageInquiryMessage"), sender.getName()));
    }

    /**
     * If player has required items and is married,
     * then marriage is being deleted.
     *
     * @param   sender  Divorce request sender
     */
    public void divorce(String sender, boolean forced) {
        String prefix = messages.getMessage("prefix");
        Marriage m = getPlayerMarriage(sender);
        Player player = PlayerUtil.getPlayer(sender);
        if(m == null) {
            if(player != null) player.sendMessage(prefix + messages.getMessage("notMarried"));
            return;
        }
        if(!forced) {
            List<Integer> slots = getItemSlots(player, plugin.getDataHandler().getDivorceCost());
            if (slots.size() < 1) {
                if(player != null) player.sendMessage(prefix + messages.getMessage("divorceNoRequiredItems"));
                return;
            }
            takeItems(player, slots, plugin.getDataHandler().getDivorceCost());
        }
        String firstPlayer = m.getFirst();
        String secondPlayer = m.getSecond();
        if(!forced && player != null) {
            player.sendMessage(MessageFormat.format(prefix + messages.getMessage("successfullyDivorced"), secondPlayer));
        }
        plugin.getServer().broadcastMessage(MessageFormat.format(prefix + messages.getMessage("divorceMessage"), firstPlayer, secondPlayer));
        for(Player p : plugin.getServer().getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0F, 3.0F);
        }
        plugin.getDataHandler().removeMarriage(m);
        takenRewards.remove(m.getFirst());
        takenRewards.remove(m.getSecond());
        marriages.remove(m);
        if(plugin.isLuckPermsAvailable()) {
            Player p1 = PlayerUtil.getPlayer(firstPlayer);
            if(p1 != null) {
                plugin.getLuckPermsManager().removeMarriagePermission(p1);
                plugin.getLuckPermsManager().removeSuffix(p1, m.getSuffix());
            }
            Player p2 = PlayerUtil.getPlayer(secondPlayer);
            if(p2 != null) {
                plugin.getLuckPermsManager().removeMarriagePermission(p2);
                plugin.getLuckPermsManager().removeSuffix(p2, m.getSuffix());
            }
        }
    }

    /**
     * Force marriage between two players.
     *
     * @param   sender  Force-marriage sender
     * @param   first   First player
     * @param   second  Second player
     */
    public void forceMarriage(Player sender, String first, String second) {
        Player p1 = PlayerUtil.getPlayer(first);
        Player p2 = PlayerUtil.getPlayer(second);
        if(p1 == null) {
            sender.sendMessage(messages.getMessage("prefix") +
                    MessageFormat.format(messages.getMessage("cannotFoundPlayer"), first));
            return;
        }
        if(p2 == null) {
            sender.sendMessage(messages.getMessage("prefix") +
                    MessageFormat.format(messages.getMessage("cannotFoundPlayer"), second));
            return;
        }
        if(isPlayerMarried(first) || isPlayerMarried(second)) {
            sender.sendMessage(messages.getMessage("prefix") + messages.getMessage("playerAlreadyMarried"));
            return;
        }
        createMarriage(first, second, sender, true);
    }

    /**
     * Force divorce between two players.
     *
     * @param   sender  Force-divorce sender
     * @param   player  Married player nickname
     */
    public void forceDivorce(Player sender, String player) {
        if(!isPlayerMarried(player)) {
            sender.sendMessage(messages.getMessage("prefix") + messages.getMessage("playerNotMarried"));
            return;
        }
        divorce(player, true);
    }

    /**
     * Plugin is trying to change suffix for sender's
     * marriage. If new suffix is the same as already suffix,
     * then error will appear. This feature requires LuckPerms.
     * Available suffix colors can be set in config.
     *
     * @param   sender  Change suffix request sender
     * @param   color   New suffix color
     */
    public void changeSuffix(CommandSender sender, String color) {
        String prefix = messages.getMessage("prefix");
        if(!plugin.isLuckPermsAvailable()) {
            sender.sendMessage(prefix + messages.getMessage("cantDoIt"));
            return;
        }
        if(!(sender instanceof Player)) {
            sender.sendMessage(prefix + messages.getMessage("mustBePlayer"));
            return;
        }
        if(!sender.hasPermission(plugin.getDataHandler().getSuffixPermission())) {
            sender.sendMessage(prefix + messages.getMessage("noPermission"));
            return;
        }
        Marriage m = getPlayerMarriage(sender.getName());
        if(m == null) {
            sender.sendMessage(prefix + messages.getMessage("notMarried"));
            return;
        }
        color = color.charAt(0) + "";
        String suffixColors = plugin.getDataHandler().getSuffixColors();
        if(suffixColors == null) suffixColors = "d";
        boolean found = false;
        for(int i = 0; i < suffixColors.length(); i++) {
            if(suffixColors.charAt(i) == color.charAt(0)) {
                found = true;
                break;
            }
        }
        if(!found) {
            sender.sendMessage(prefix + messages.getMessage("cantUseColor") + suffixColors);
            return;
        }
        Player p1 = PlayerUtil.getPlayer(m.getFirst());
        Player p2 = PlayerUtil.getPlayer(m.getSecond());
        if(p1 == null || p2 == null) {
            sender.sendMessage(prefix + messages.getMessage("partnerMustBeOnline"));
            return;
        }
        if(colorChanges.contains(p1.getName()) || colorChanges.contains(p2.getName())) {
            sender.sendMessage(prefix + messages.getMessage("suffixHasCooldown"));
            return;
        }
        String newSuffix = MessageFormat.format(plugin.getDataHandler().getSuffixSchema(), color);
        if(newSuffix.equals(m.getSuffix())) {
            sender.sendMessage(prefix + messages.getMessage("suffixActive"));
            return;
        }
        plugin.getLuckPermsManager().updateSuffix(p1, m.getSuffix(), newSuffix);
        if(!p1.equals(p2)) plugin.getLuckPermsManager().updateSuffix(p2, m.getSuffix(), newSuffix);
        m.setSuffix(newSuffix);
        plugin.getDataHandler().updateSuffix(m, m.getSuffix());
        createColorCooldown(p1.getName());
        createColorCooldown(p2.getName());
        sender.sendMessage(prefix + messages.getMessage("changedSuffix"));
    }

    /**
     * Checks if player is married and if this player can
     * receive reward with that ID (marriage has required
     * days etc.). If player don't have space in their
     * inventory, then items will be dropped.
     *
     * @param   sender  Player which want to receive reward
     * @param   id      Reward ID
     */
    public void getReward(CommandSender sender, String id) {
        String prefix = messages.getMessage("prefix");
        if(!(sender instanceof Player)) {
            sender.sendMessage(prefix + messages.getMessage("mustBePlayer"));
            return;
        }
        Marriage m = getPlayerMarriage(sender.getName());
        if(m == null) {
            sender.sendMessage(prefix + messages.getMessage("notMarried"));
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
            sender.sendMessage(prefix + messages.getMessage("cannotFindReward"));
            return;
        }
        long daysOfMarriage = DateManager.calculateDays(m.getDate(), DateManager.getDate("."));
        if(daysOfMarriage < r.getDay()) {
            sender.sendMessage(prefix + messages.getMessage("cantReceiveReward"));
            return;
        }
        List<String> takenRewards = getTakenRewards().getOrDefault(sender.getName(), new ArrayList<>());
        if(takenRewards.contains(r.getId())) {
            sender.sendMessage(prefix + messages.getMessage("rewardReceived"));
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

    /**
     * Returns true if player with that nickname is married.
     *
     * @param   nickname  Player which you want to check
     * @return True or false
     */
    public boolean isPlayerMarried(String nickname) {
        for(Marriage m : marriages) {
            if(m.getFirst().equals(nickname) || m.getSecond().equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns player's marriage if available
     *
     * @param   nickname  Player which you want to check
     * @return Player's marriage or null
     */
    @Nullable
    public Marriage getPlayerMarriage(String nickname) {
        for(Marriage m : marriages) {
            if(m.getFirst().equals(nickname) || m.getSecond().equals(nickname)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Returns true if player has marriage request
     * from second player.
     *
     * @param   who     Request receiver
     * @param   from    Request sender
     * @return True or false
     */
    public boolean hasRequest(String who, String from) {
        List<String> playerRequests = requests.getOrDefault(who, new ArrayList<>());
        return playerRequests.contains(from);
    }

    /**
     * Clears all receiver's marriage requests.
     *
     * @param   receiver     Request receiver
     */
    public void clearRequests(String receiver) {
        requests.remove(receiver);
    }

    /**
     * Create marriage request.
     *
     * @param   from    Request sender
     * @param   to      Request receiver
     */
    public void createRequest(String from, String to) {
        List<String> playerRequests = requests.getOrDefault(to, new ArrayList<>());
        playerRequests.add(from);
        requests.put(to, playerRequests);
        Player p = PlayerUtil.getPlayer(to);
        if(p != null) {
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            List<String> req = requests.getOrDefault(to, new ArrayList<>());
            req.remove(from);
            requests.put(to, req);
        }, 20L * plugin.getDataHandler().getRequestCooldown());
    }

    /**
     * Creates cooldown for changing suffix for player.
     *
     * @param   nickname    Player nickname
     */
    public void createColorCooldown(String nickname) {
        colorChanges.add(nickname);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            colorChanges.remove(nickname);
        }, 20L * plugin.getDataHandler().getSuffixCooldown());
    }

    /**
     * Checks if sender or second player
     * is married. Sends message to sender.
     *
     * @param   sender  Command sender (first player)
     * @param   second  Second player
     * @return True or false
     */
    public boolean checkMarriage(Player sender, String second) {
        String prefix = messages.getMessage("prefix");
        if(isPlayerMarried(sender.getName())) {
            sender.sendMessage(prefix + messages.getMessage("youAreMarried"));
            return false;
        }
        if(isPlayerMarried(second)) {
            sender.sendMessage(prefix + messages.getMessage("playerAlreadyMarried"));
            return false;
        }
        return true;
    }

    /**
     * Get slots where items from list are.
     * If there's no all items in player inventory,
     * then it will return empty integer list.
     *
     * @param   player  Player to check
     * @param   items   List of items
     * @return Empty integer list or list filled with slots
     */
    public List<Integer> getItemSlots(Player player, List<ItemStack> items) {
        List<Integer> slots = new ArrayList<>();
        int goodItems = 0;
        for(ItemStack is : items) {
            int totalAmount = 0;
            for(int i = 0; i < 36; i++) {
                ItemStack inventoryItem = player.getInventory().getItem(i);
                if(!isSimilar(inventoryItem, is)) continue;
                totalAmount += inventoryItem.getAmount();
                slots.add(i);
            }
            if(totalAmount < is.getAmount()) return new ArrayList<>();
            goodItems++;
        }
        if(goodItems < items.size()) return new ArrayList<>();
        return slots;
    }

    /**
     * Takes items from list from player inventory.
     * You must define which slots must be checked.
     *
     * @param   player      Player
     * @param   checkSlots  Slots to check
     * @param   items       List of items
     */
    public void takeItems(Player player, List<Integer> checkSlots, List<ItemStack> items) {
        for(ItemStack is : items) {
            int required = is.getAmount();
            for(int slot : checkSlots) {
                ItemStack inventoryItem = player.getInventory().getItem(slot);
                if(!isSimilar(inventoryItem, is)) continue;
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

    /**
     * Checks if inventory item and itemstack
     * are similar (name, lore, enchantments, unbreakable)
     *
     * @param   inventoryItem   Inventory itemstack
     * @param   is              Itemstack to check
     * @return True or false
     */
    public boolean isSimilar(ItemStack inventoryItem, ItemStack is) {
        if(inventoryItem == null || is == null) return false;
        if(!inventoryItem.getType().equals(is.getType())) return false;
        if(!inventoryItem.getItemMeta().getDisplayName().equals(is.getItemMeta().getDisplayName())) return false;
        if(inventoryItem.getItemMeta().getLore() == null && is.getItemMeta().getLore() != null) return false;
        if(inventoryItem.getItemMeta().getLore() != null && is.getItemMeta().getLore() == null) return false;
        if(inventoryItem.getItemMeta().getLore() != null) {
            if(is.getItemMeta().getLore() != null) {
                if(!inventoryItem.getItemMeta().getLore().equals(is.getItemMeta().getLore())) return false;
            }
        }
        if(!inventoryItem.getItemMeta().getEnchants().equals(is.getEnchantments())) return false;
        if(inventoryItem.getItemMeta().isUnbreakable() != is.getItemMeta().isUnbreakable()) return false;
        return true;
    }

}
