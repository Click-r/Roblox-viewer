package classes;

import java.lang.reflect.Field;

import java.net.SocketTimeoutException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.awt.Image;

import classes.api.getInfo;

import ui.gui.err.ErrorHandler;

/** A class for the player which allows for general information to be retrieved. Utilizies ROBLOX API endpoints to retrieve the data. */
public class Player {

    private long delay = -1L; // ping

    public Long id;
    public String name, created, description, lastonline, dispname;
    public Integer friends, followings, followers;
    public Boolean banned, online;
    public Image image;

    /**
     * <p>Takes any <code>Number</code> as a user ID and retrieves information about the user.</p>
     * <p>May occasionally fail to return a user's information due to API endpoints being wiped of user data.</p>
     * @param id
    */

    public Player(Number id) throws UserNotFoundException {
        long num = (long) id;
        this.id = num;

        try {
            long now = System.currentTimeMillis();
            load(getInfo.getInformation(num));
            delay = (System.currentTimeMillis() - now) / getInfo.numData;
        } catch (NumberFormatException | SocketTimeoutException err) {}
    }

    /**
     * <p>Takes any <code>String</code> as a user ID and retrieves information about the user.</p>
     * <p>May occasionally fail to return a user's information due to API endpoints being wiped of user data.</p>
     * @param username
    */

    public Player(String username) throws UserNotFoundException {
        this.name = username;

        long now = System.currentTimeMillis();
        load(getInfo.searchByUsername(username));
        delay = (System.currentTimeMillis() - now) / getInfo.numData;
    }
    
    /**
     * <p>Returns the appearance of the user as an <code>Avatar</code> object.</p>
     * @return The user's current appearance
    */
    
    public Avatar getAppearance() {
        Avatar currentlyWearing = new Avatar(this.id);
        currentlyWearing.setImage(this.image);

        return currentlyWearing;
    }

    /**
     * <p>This method is only used in cases when an extra piece of data may be found in returned json from end points</p>
     * @return A set containing all the valid key names the player class stores.
    */

    public static Set<String> getValidKeys() {
        Set<String> keyNames = new HashSet<>();

        Field[] validFields = Player.class.getFields();

        for (Field f: validFields)
            keyNames.add(f.getName());

        return keyNames;
    }

    /**
     * <p>Returns the average delay that a given <code>Player</code> instance took to initialize.</p>
     * @return delay - measured in milliseconds.
    */

    public long getDelay() {
        return delay;
    }

    private void load(Map<String, Object> data) {
        data.forEach((key,val) -> {
            try {
                Field writeTo = this.getClass().getDeclaredField(key);
                writeTo.set(this, val);
            } catch (NoSuchFieldException e) {}
            catch (IllegalAccessException iae) {
                ErrorHandler.report(iae, this);
            }
        });

        this.image = (Image) data.get("image");
    }
}
