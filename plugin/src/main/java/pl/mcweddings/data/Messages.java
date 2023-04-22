package pl.mcweddings.data;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
public class Messages {

    private final HashMap<String, String> messages = new HashMap<>();

    public String getMessage(String key) {
        return messages.getOrDefault(key, "null");
    }

}
