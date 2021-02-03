package classes;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import main.getInfo;

/** A class for the player which allows for general information to be retrieved.
* Utilizies ROBLOX API endpoints to retieve the data.
*/
public class Player {

    public long id;
    public String name, created, description, status;
    public int friends, followings, followers;
    public boolean banned;

    public Player(Number id) {
        long num = (long) id;

        try {
            load(getInfo.getInformation(num));
        } catch (NumberFormatException | SocketTimeoutException err) {}
    }

    public Player(String username) {
        load(getInfo.searchByUsername(username));
    }

    private void load(HashMap<String, String> data) {
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
        this.name = this.name.substring(1, this.name.length() - 1);

        this.status = data.get("status");
        this.status = this.status.substring(1, this.status.length() - 1);
    }
}
