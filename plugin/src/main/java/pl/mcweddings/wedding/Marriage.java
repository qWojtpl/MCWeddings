package pl.mcweddings.wedding;

import lombok.Getter;
import lombok.Setter;
import pl.mcweddings.MCWeddings;

@Getter
public class Marriage {

    @Setter
    private int id;
    private final String first;
    private final String second;
    private final String date;
    @Setter
    private String suffix;

    public Marriage(int id, String first, String second, String date, String suffix) {
        this.id = id;
        this.first = first;
        this.second = second;
        this.date = date;
        this.suffix = suffix;
        MCWeddings.getInstance().getMarriageManager().getMarriages().add(this);
    }

}
