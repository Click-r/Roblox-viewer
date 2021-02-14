package main;

import java.util.HashMap;

import java.util.Scanner;

import java.io.*;

import java.nio.charset.*;

import java.net.*;

import java.util.Stack;

import java.util.concurrent.*;

import classes.Link;

public class getInfo {

    final static int numData = classes.Player.class.getDeclaredFields().length;

    @SuppressWarnings("unchecked")

    public static HashMap<String, String> searchByUsername(String username){
        final Charset UTF_8 = StandardCharsets.UTF_8;
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Long> id = exec.submit(() -> {
            try {
                URL apiEndpoint = new URL("https://users.roblox.com/v1/usernames/users");

                URLConnection apiConnect = apiEndpoint.openConnection();
                HttpURLConnection httpCon = (HttpURLConnection)apiConnect;

                byte[] out = ("{\"usernames\":[\"" + username +"\"], \"excludeBannedUsers\":false}").getBytes(UTF_8);
                int len = out.length;

                httpCon.setRequestMethod("POST");
                httpCon.setDoOutput(true);
                httpCon.setFixedLengthStreamingMode(len);
                httpCon.setRequestProperty("Content-type", "application/json; charset=UTF-8");
                httpCon.connect();

                try (OutputStream os = httpCon.getOutputStream()) {
                    os.write(out);
                }

                InputStream response = httpCon.getInputStream();
                String txtResponse = "NaN";

                try (Scanner scanner = new Scanner(response)){
                    txtResponse = scanner.useDelimiter("\\A").next();
                }

                return Long.valueOf((String)((HashMap<String, Object>)JSONtoHashtable.toHashtable(txtResponse).get("data")).get("id"));

            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return 1L;
        });

        HashMap<String, String> requested = new HashMap<String, String>();

        long Id = 1L;

        try {
            Id = id.get(5, TimeUnit.SECONDS);
            requested = getInformation(Id);
        } catch (InterruptedException | ExecutionException | TimeoutException | SocketTimeoutException e) {}

        return requested;
    }

    public static HashMap<String, String> getInformation(long userId) throws SocketTimeoutException {

        HashMap<String, String> data = new HashMap<String, String>();

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

            Future<HashMap<String, String>> fetched = retrieve.submit(() -> {
                HashMap<String, String> toReturn = new HashMap<String, String>();

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
                    HashMap<String, String> result = (HashMap<String, String>) fetched.get(3, TimeUnit.SECONDS);
                    
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
            String creationDate = data.get("created").split("T")[0];
            
            data.replace("created", creationDate.substring(1,creationDate.length()));

            String beaned = data.get("isBanned");
            
            data.remove("isBanned");
            data.put("banned", beaned);

            String online = data.get("IsOnline");

            data.remove("IsOnline");
            data.put("online", online);

            String lastOnline = data.get("LastOnline").split("T")[0];

            data.remove("LastOnline");
            data.put("lastonline", lastOnline);
        }

        return data;
    }

}