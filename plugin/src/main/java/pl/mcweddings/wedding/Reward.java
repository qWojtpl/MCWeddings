package pl.mcweddings.wedding;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class Reward {

    private final String id;
    private final ItemStack itemStack;
    private final int day;
    private final String execute;

    public Reward(String id, ItemStack is, int day, String execute) {
        this.id = id;
        this.itemStack = is;
        this.day = day;
        this.execute = execute;
    }

}
