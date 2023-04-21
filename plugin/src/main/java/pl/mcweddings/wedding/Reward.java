package pl.mcweddings.wedding;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class Reward {

    private final ItemStack itemStack;
    private final int day;
    private final String execute;

    public Reward(ItemStack is, int day, String execute) {
        this.itemStack = is;
        this.day = day;
        this.execute = execute;
    }

}
