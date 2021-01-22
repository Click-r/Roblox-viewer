package classes;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import main.getInfo;

/** A class for the player which allows for general information to be retrieved.
* Utilizies ROBLOX API endpoints to retieve the data.
*/
public class Player {

    public long id;
    public String name;
    public String created;
    public String description;
    public int friends, followings, followers;
    public boolean banned;

    /**
     * Takes either a user id of type <code>long</code> or a username of type <code>String</code>.  
     * If the value provided is neither, it will retrieve data from the ROBLOX profile by default.
     * @param identifier
     */
    public Player(Object identifier) {
        Class<?> cls = identifier.getClass();
        
        HashMap<String, String> data = new HashMap<String, String>();

        String strContent = identifier.toString();

        if ( Long.class.equals(cls) || Integer.class.equals(cls) ) {
            long converted = 1L;

            try {
                converted = Long.valueOf(strContent);
            } catch (NumberFormatException err) {}

            try {
                data = getInfo.getInformation(converted);
            } catch (SocketTimeoutException t) {}

        } else if (String.class.equals(cls)) {
            data = getInfo.searchByUsername(strContent);
        }

        this.id = Long.valueOf(data.get("id"));
        this.created = data.get("created");
        this.friends = Integer.valueOf(data.get("friends"));
        this.followings = Integer.valueOf(data.get("followings"));
        this.followers = Integer.valueOf(data.get("followers"));
        this.banned = Boolean.valueOf(data.get("banned"));

        //remove the quote marks
        this.description = data.get("description");
        this.description = this.description.substring(1, this.description.length() - 1); 

        this.name = data.get("name");
        this.name = this.name.substring(1, this.name.length()-1);
    }
}
