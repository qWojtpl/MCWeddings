package pl.mcweddings.wedding;

import lombok.Getter;
import pl.mcweddings.MCWeddings;

@Getter
public class Marriage {

    private final String first;
    private final String second;

    public Marriage(String first, String second, String date, String suffix) {
        this.first = first;
        this.second = second;
        MCWeddings.getInstance().getMarriageManager().getMarriages().add(this);
    }

}
