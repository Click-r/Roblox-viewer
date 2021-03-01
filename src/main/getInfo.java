package main;

import java.util.HashMap;

import java.io.*;

import java.nio.charset.*;

import java.net.*;

import java.util.Stack;

import java.util.concurrent.*;

import classes.Link;

public class getInfo {

    final static int numData = classes.Player.class.getDeclaredFields().length;

    @SuppressWarnings("unchecked")

    public static HashMap<String, Object> searchByUsername(String username){
        final Charset UTF_8 = StandardCharsets.UTF_8;
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Long> id = exec.submit(() -> {
            try {
                byte[] out = ("{\"usernames\":[\"" + username +"\"], \"excludeBannedUsers\":false}").getBytes(UTF_8);

                Link info = new Link("https://users.roblox.com/v1/usernames/users", out);
                HashMap<String, Object> data = (HashMap<String, Object>) info.data.get("usernames");
                String Id = (String) data.get("id");

                return Long.valueOf(Id);

            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return 1L;
        });

        HashMap<String, Object> requested = new HashMap<String, Object>();

        long Id = 1L;

        try {
            Id = id.get(5, TimeUnit.SECONDS);
            requested = getInformation(Id);
        } catch (InterruptedException | ExecutionException | TimeoutException | SocketTimeoutException e) {
            e.printStackTrace();
        }
        return requested;
    }

    public static HashMap<String, Object> getInformation(long userId) throws SocketTimeoutException {

        HashMap<String, Object> data = new HashMap<String, Object>();

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
            new String[]{"displayName"},
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

            Future<HashMap<String, Object>> fetched = retrieve.submit(() -> {
                HashMap<String, Object> toReturn = new HashMap<String, Object>();

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
                    HashMap<String, Object> result = (HashMap<String, Object>) fetched.get(3, TimeUnit.SECONDS);
                    
                    if (result.size() < 1)
                        buffer.push(1);

                    data.putAll(result);
                } catch (TimeoutException|InterruptedException|ExecutionException timeout) {}
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
            creationDate = creationDate.split("T")[0];
            
            data.replace("created", creationDate.substring(1,creationDate.length()));

            String beaned = (String) data.get("isBanned");
            
            data.remove("isBanned");
            data.put("banned", beaned);

            String online = (String) data.get("IsOnline");

            data.remove("IsOnline");
            data.put("online", online);

            String lastOnline = (String) data.get("LastOnline");
            lastOnline = lastOnline.split("T")[0];

            data.remove("LastOnline");
            data.put("lastonline", lastOnline);
        }

        return data;
    }

}