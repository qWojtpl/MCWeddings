package pl.mcweddings.wedding;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class Reward {

    private final ItemStack itemStack;
    private final String execute;

    public Reward(ItemStack is, String execute) {
        this.itemStack = is;
        this.execute = execute;
    }

}
