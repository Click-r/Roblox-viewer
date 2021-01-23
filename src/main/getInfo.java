package main;

import java.util.HashMap;

import java.util.Scanner;

import java.io.*;

import java.nio.charset.*;

import java.net.*;

import java.util.Stack;

import java.util.concurrent.*;

public class getInfo {

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
            Id = id.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return requested;
        }

        try {
            requested = getInformation(Id);
        } catch (SocketTimeoutException err) {
            System.out.println("Timed out");
        }

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
            base + "/followings/count"
        };

        boolean properlyParsed = true;

        // Start of multi-threaded data retrieval
        Stack<Integer> buffer = new Stack<Integer>();

        ThreadPoolExecutor retrieve = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        for (final String domain : apiDomains){
            retrieve.submit(() -> {
                try {
                    URL api = new URL(domain);
                    URLConnection apiConnection = api.openConnection();
                    apiConnection.setConnectTimeout(5000);
                    apiConnection.connect();
                    InputStream response = apiConnection.getInputStream();
                    String textResponse = "NaN";
            
                    try (Scanner scanner = new Scanner(response)) {
                        String responseBody = scanner.useDelimiter("\\A").next();
                        textResponse = responseBody;
                    }
            
                    HashMap<String,Object> subData = JSONtoHashtable.toHashtable(textResponse);

                    subData.forEach( 
                        (key,val) ->
                            {
                                String name = key;
                                if (subData.size() == 1){
                                    String[] urlName = domain.split("/");
                                    name = urlName[urlName.length - 2];
                                }
                                data.put(name, (String)val);
                            }
                        );
                            
                    buffer.push(1); // fill buffer upon thread completion
            
                    } catch (IOException e) {
                        int remainingBuffer = 5 - buffer.size();
                        for (int i = 0; i < remainingBuffer; i++)
                            buffer.push(1);
                        return;
                    }
                }
            );
        }

        while (buffer.size() < 5) {
            try {
                Thread.sleep(2L);
            } catch (InterruptedException inter) {}
        }

        retrieve.shutdownNow();
        //End of multi-threaded data retrieval

        properlyParsed &= data.size() == 10;

        if (properlyParsed) {
            String creationDate = data.get("created").split("T")[0];
            
            data.replace("created", creationDate.substring(1,creationDate.length()));
            data.remove("displayName");

            String beaned = data.get("isBanned");
            
            data.remove("isBanned");
            data.put("banned", beaned);
        }
        
        return data;
    }

}