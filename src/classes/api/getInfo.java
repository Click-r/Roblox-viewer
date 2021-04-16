package classes.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Calendar;
import java.util.concurrent.*;

import java.io.*;
import java.net.*;

import java.time.*;

import classes.Link;

import ui.ErrorHandler;

public class getInfo {

    private static String dateLocalTime(String time) {
        String abbrev = Calendar
          .getInstance()
          .getTimeZone()
          .getDisplayName(false, java.util.TimeZone.SHORT);

        ZoneId zId = Calendar
          .getInstance()
          .getTimeZone()
          .toZoneId();

        String local = ZonedDateTime
          .parse(time)
          .toInstant()
          .atZone(zId)
          .toString()
          .split("\\.")[0];
        local = local.replaceAll("T", " @ ");
        

        return local + " " + abbrev;
    }

    final static int numData = classes.Player.class.getDeclaredFields().length;

    @SuppressWarnings("unchecked")

    public static Map<String, Object> searchByUsername(String username){
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Long> id = exec.submit(() -> {
            try {
                String out = "{\"usernames\":[\"" + username +"\"], \"excludeBannedUsers\":false}";
                Link info = new Link("https://users.roblox.com/v1/usernames/users", out);

                List<?> arrData = (List<?>) info.data.get("usernames");
                Map<String, Object> data = (Map<String, Object>) arrData.get(0);

                String Id = data.get("id").toString();

                return Long.valueOf(Id);

            } catch (IOException e) {
                ErrorHandler.report(e);
            }
            
            return 1L;
        });

        Map<String, Object> requested = new HashMap<String, Object>();

        long Id = 1L;

        try {
            Id = id.get(5, TimeUnit.SECONDS);
            requested = getInformation(Id);
        } catch (InterruptedException | ExecutionException | TimeoutException | SocketTimeoutException e) {
            e.printStackTrace();
        }
        return requested;
    }

    public static Map<String, Object> getInformation(long userId) throws SocketTimeoutException {

        Map<String, Object> data = new HashMap<String, Object>();

        final String base = "https://friends.roblox.com/v1/users/" + userId;

        final String[] apiDomains = {
            "https://users.roblox.com/v1/users/" + userId,
            "https://users.roblox.com/v1/users/" + userId + "/status/ ",
            base + "/friends/count",
            base + "/followers/count",
            base + "/followings/count",
            "https://api.roblox.com/users/" + userId + "/onlinestatus/"
        };

        final String[][] toFilter = {
            null,
            null,
            null,
            null,
            null,
            new String[]{"GameId", "LastLocation", "LocationType", "PlaceId", "VisitorId", "PresenceType", "UniverseId"}
        };

        boolean properlyParsed = true;

        // Start of multi-threaded data retrieval
        Stack<Integer> buffer = new Stack<Integer>();

        final int maxThreads = 5;

        ThreadPoolExecutor retrieve = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);

        int index = 0;

        for (final String domain : apiDomains){
            int ind = index;

            Future<Map<String, Object>> fetched = retrieve.submit(() -> {
                Map<String, Object> toReturn = new HashMap<String, Object>();

                try {
                    Link con = new Link(domain);

                    toReturn = con.filter(toFilter[ind]);

                    buffer.push(1); // fill buffer upon thread completion

                } catch (IOException e) {
                    int remainingBuffer = 5 - buffer.size();

                    assert remainingBuffer >= 0;

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
                } catch (InterruptedException|TimeoutException timeout) {}
            }
            );

            index++;
        }

        while (buffer.size() < apiDomains.length) {
            try {
                Thread.sleep(2L);
            } catch (InterruptedException inter) {}
        }

        retrieve.shutdownNow();
        //End of multi-threaded data retrieval

        properlyParsed &= (data.size() == numData);

        if (properlyParsed) {
            String creationDate = (String) data.get("created");
            creationDate = dateLocalTime(creationDate);
            
            data.replace("created", creationDate);

            boolean beaned = (boolean) data.get("isBanned");
            
            data.remove("isBanned");
            data.put("banned", beaned);

            boolean online = (boolean) data.get("IsOnline");

            data.remove("IsOnline");
            data.put("online", online);

            String lastOnline = (String) data.get("LastOnline");
            lastOnline = dateLocalTime(lastOnline);

            data.remove("LastOnline");
            data.put("lastonline", lastOnline);

            long id = Long.valueOf(data.get("id").toString());
            data.replace("id", id);

            String dispname = (String) data.get("displayName");
            data.remove("displayName");
            data.put("dispname", dispname);
        }

        return data;
    }

}