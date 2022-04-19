package classes.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.concurrent.*;

import java.io.*;

import java.net.*;

import java.time.*;

import classes.UserNotFoundException;
import classes.Link;
import classes.Player;

import loaders.AdvancedSettings;
import loaders.SearchSettings;

import ui.gui.err.ErrorHandler;

public class getInfo {

    final public static int numData = classes.Player.class.getFields().length;

    @SuppressWarnings("static-access")

    private static String dateLocalTime(String time, String... prefered) {
        String abbrev = Calendar
          .getInstance()
          .getTimeZone()
          .getDisplayName(false, TimeZone.SHORT);

        ZoneId zId = Calendar
          .getInstance()
          .getTimeZone()
          .toZoneId();
        
        Calendar.getInstance().getTimeZone().setDefault(TimeZone.getTimeZone(zId)); // set default time zone to local
        
        if (prefered.length == 1) {
            String timezone = prefered[0].toUpperCase();
            TimeZone chosen = Calendar.getInstance().getTimeZone().getTimeZone(timezone); // resorts to the default set earlier if it can't find it
            String timezoneDisp = chosen.getDisplayName(false, TimeZone.SHORT);

            zId = chosen.toZoneId();
            abbrev = timezoneDisp;
        }

        String local = ZonedDateTime
          .parse(time)
          .toInstant()
          .atZone(zId)
          .toString()
          .split("\\.")[0];
        
        local = local.replaceFirst("T", " @ ");
        
        return local + " " + abbrev;
    }

    private static boolean validateData(Map<String, Object> dataSource) {
        if (dataSource.size() >= numData) {
            String prefered = "";

            try {
                SearchSettings searchSettings = new SearchSettings();

                prefered = Boolean.valueOf(searchSettings.get("local")) ? TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT) : searchSettings.get("timezone");
            } catch (IOException ioex) {
                ErrorHandler.report(ioex);
                // note: the dateLocalTime method could handle if it was fed incorrect input due to an io exception, but if
                // an io exception occurs then it is best to inform the user as something is most likely very wrong
            }

            String creationDate = (String) dataSource.get("created");
            creationDate = dateLocalTime(creationDate, prefered);
            dataSource.replace("created", creationDate);

            boolean beaned = (boolean) dataSource.get("isBanned");
            dataSource.remove("isBanned");
            dataSource.put("banned", beaned);

            boolean online = (boolean) dataSource.get("IsOnline");
            dataSource.remove("IsOnline");
            dataSource.put("online", online);

            String lastOnline = (String) dataSource.get("LastOnline");
            lastOnline = dateLocalTime(lastOnline, prefered);
            dataSource.remove("LastOnline");
            dataSource.put("lastonline", lastOnline);

            long id = Long.valueOf(dataSource.get("id").toString());
            dataSource.replace("id", id);

            String dispname = (String) dataSource.get("displayName");
            dataSource.remove("displayName");
            dataSource.put("dispname", dispname);

            if (dataSource.size() > numData) {
                Map<String, Object> dataSrcCopy = new HashMap<>();
                dataSource.forEach((key, val) -> dataSrcCopy.put(key.toLowerCase(), val));
    
                Set<String> validKeys = Player.getValidKeys();
                Set<String> receivedKeys = dataSrcCopy.keySet();
    
                receivedKeys.removeAll(validKeys);
    
                receivedKeys.forEach((key) -> {
                    String keyStr = key.toString();
    
                    System.out.println("Removed unexpected key: " + keyStr);
                    dataSource.remove(key);
                });
            }

            return true;
        }

        return false;
    }

    public static Map<String, Object> searchByUsername(String username) throws UserNotFoundException {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Long> id = exec.submit(() -> {
            try {
                //String out = "{\"usernames\":[\"" + username +"\"], \"excludeBannedUsers\":false}";
                String user = username.replace(' ', '+'); // format properly for http requests

                Link info = new Link("https://api.roblox.com/users/get-by-username?username=" + user);

                String Id = info.data.get("Id").toString();

                return Long.valueOf(Id);

            } catch (IOException e) {
                String stringified = e.toString();

                if (stringified.contains("400")) // http response code 400
                    throw new UserNotFoundException("Failed to fetch user!");

                ErrorHandler.report(e);
            }
            
            return 1L;
        });

        Map<String, Object> requested = new HashMap<>();

        long Id = 1L;

        try {
            Id = id.get(5, TimeUnit.SECONDS);
            requested = getInformation(Id);
        } catch (InterruptedException | ExecutionException | TimeoutException | SocketTimeoutException e) {
            throw new UserNotFoundException("Failed to fetch user!");
        }
        return requested;
    }

    public static Map<String, Object> getInformation(long userId) throws SocketTimeoutException, UserNotFoundException {
        Map<String, Object> data = new HashMap<>();

        final String base = "https://friends.roblox.com/v1/users/" + userId;

        final String[] apiDomains = {
            "https://users.roblox.com/v1/users/" + userId,
            base + "/friends/count",
            base + "/followers/count",
            base + "/followings/count",
            "https://api.roblox.com/users/" + userId + "/onlinestatus/"
        };

        final String[][] toFilter = {
            new String[]{"externalAppDisplayName"},
            null,
            null,
            null,
            new String[]{"GameId", "LastLocation", "LocationType", "PlaceId", "VisitorId", "PresenceType", "UniverseId", "Visibility"}
        };

        int chosen = 5;

        try {
            AdvancedSettings advSettings = new AdvancedSettings();
            chosen = Integer.valueOf(advSettings.get("threadsToUse"));
        } catch (IOException iex) {
            ErrorHandler.report(iex);
        }

        // Start of multi-threaded data retrieval
        Stack<Integer> buffer = new Stack<>();

        final int maxThreads = chosen;

        ThreadPoolExecutor retrieve = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);

        int index = 0;

        for (final String domain : apiDomains){
            int ind = index;

            Future<Map<String, Object>> fetched = retrieve.submit(() -> {
                Map<String, Object> toReturn = new HashMap<>();

                try {
                    Link con = new Link(domain);

                    toReturn = con.filter(toFilter[ind]);

                    buffer.push(1); // fill buffer upon thread completion

                } catch (IOException e) {
                    int remainingBuffer = 5 - buffer.size();

                    for (int i = 0; i < remainingBuffer; i++)
                        buffer.push(1);
                    
                    return toReturn;
                }

                return toReturn;
            });

            retrieve.submit(() -> {
                try {
                    Map<String, Object> result = fetched.get(3, TimeUnit.SECONDS);
                    
                    if (result.size() < 1)
                        buffer.push(1);

                    data.putAll(result);
                } catch (ExecutionException exc) {
                    ErrorHandler.report(exc);
                } catch (InterruptedException | TimeoutException timeout) {}
            });

            index++;
        }

        data.put("image", getAppearance.retrieveImage(userId));

        while (buffer.size() < apiDomains.length) {
            try {
                Thread.sleep(2L);
            } catch (InterruptedException inter) {}
        }

        retrieve.shutdownNow();
        //End of multi-threaded data retrieval
        boolean valid = validateData(data);

        if (!valid)
            throw new UserNotFoundException("Failed to fetch user! Received " + (numData - data.size()) + " less keys");

        return data;
    }

}