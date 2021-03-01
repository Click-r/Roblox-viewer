package classes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import main.getInfo;

/** A class for the player which allows for general information to be retrieved.
* Utilizies ROBLOX API endpoints to retieve the data.
*/
public class Player {

    public Long id;
    public String name, created, description, status, lastonline;
    public Integer friends, followings, followers;
    public Boolean banned, online;

    public Player(Number id) {
        long num = (long) id;

        try {
            load(getInfo.getInformation(num));
        } catch (NumberFormatException | SocketTimeoutException err) {}
    }

    public Player(String username) {
        load(getInfo.searchByUsername(username));
    }

    private void load(HashMap<String, Object> data) {
        data.forEach((key,val) -> {
            try {
                Field writeTo = this.getClass().getDeclaredField((String) key);
                Class<?> type = writeTo.getType();

                if (type.getName().equals("java.lang.String")) {
                    writeTo.set(this, (String) val);
                    return;
                }

                Method cast = type.getMethod("valueOf", String.class);
                writeTo.set(this, cast.invoke(null, val));
            } catch (Exception e) {}
        });

        modify();
    }

    private void modify(){
        this.description = this.description.substring(1, this.description.length() - 1); 
        this.name = this.name.substring(1, this.name.length() - 1);
        this.status = this.status.substring(1, this.status.length() - 1);
        this.lastonline = this.lastonline.substring(1); 
    }
}
