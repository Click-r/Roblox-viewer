package classes;

import java.lang.reflect.Field;

import java.net.SocketTimeoutException;

import java.util.Map;

import classes.api.getInfo;

import ui.ErrorHandler;

/** A class for the player which allows for general information to be retrieved. Utilizies ROBLOX API endpoints to retrieve the data. */
public class Player {

    public Long id;
    public String name, created, description, status, lastonline, dispname;
    public Integer friends, followings, followers;
    public Boolean banned, online;

    /**
     * <p>Takes any <code>Number</code> as a user ID and retrieves information about the user.</p>
     * <p>May occasionally fail to return a user's information due to API endpoints being wiped of user data.</p>
     * @param id
    */

    public Player(Number id) {
        long num = (long) id;
        this.id = num;

        try {
            load(getInfo.getInformation(num));
        } catch (NumberFormatException | SocketTimeoutException err) {}
    }

    /**
     * <p>Takes any <code>String</code> as a user ID and retrieves information about the user.</p>
     * <p>May occasionally fail to return a user's information due to API endpoints being wiped of user data.</p>
     * @param username
    */

    public Player(String username) {
        this.name = username;
        load(getInfo.searchByUsername(username));
    }

    private void load(Map<String, Object> data) {
        data.forEach((key,val) -> {
            try {
                Field writeTo = this.getClass().getDeclaredField(key);
                writeTo.set(this, val);
            } catch (Exception e) {
                ErrorHandler.report(e, this);
            }
        });
    }
}
