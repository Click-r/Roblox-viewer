package main;

import java.util.HashMap;

import java.util.Scanner;

import java.io.*;

import java.nio.charset.*;

import java.net.*;

import java.util.Stack;

//import java.util.Random;
//import java.util.List;
//import java.util.ArrayList;

public class getInfo {

    @SuppressWarnings("unchecked")

    public static HashMap<String, String> searchByUsername(String username){
        Stack<Long> id = new Stack<Long>();
        final Charset UTF_8 = StandardCharsets.UTF_8;

        Thread toRun = new Thread() {
            @Override
            public void run() {
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

                    id.push(Long.valueOf((String)((HashMap<String, Object>)JSONtoHashtable.toHashtable(txtResponse).get("data")).get("id")));

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("User most likely doesn't exist, check the name which you have entered or check your internet connection.");
                }
            }
        };

        toRun.run();

        HashMap<String, String> requested = new HashMap<String, String>();

        if (id.size() == 1) {
            long Id = id.pop();
            try {
                requested = getInformation(Id);
            } catch (SocketTimeoutException err) {
                System.out.println("Timed out");
            }
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
        Stack<Runnable> tasks = new Stack<Runnable>();
        Stack<Thread> usable = new Stack<Thread>();
        Stack<Integer> buffer = new Stack<Integer>();

        for (final String domain : apiDomains){

            Runnable task = new Runnable() {
                @Override
                public void run() {
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
                        usable.push(new Thread()); // push another thread back onto stack to perform next activity (if any)
        
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {}
                }
            };

            tasks.push(task);
        }

        final int proc = Runtime.getRuntime().availableProcessors();
        int maxUtilize = (proc > 5) ? 5 : proc; // see how much threads are able to be utilised

        for (int i = 0; i < maxUtilize; i++) { // populate stack with usable threads
            Thread completeTask = new Thread();
            usable.push(completeTask);
        }

        long started = System.currentTimeMillis();
        Thread executeOn = new Thread();

        while (buffer.size() != 5) { // start running threads
            if (tasks.size() != 0 && !usable.isEmpty()) {
                Runnable current = tasks.pop();
                usable.pop();

                executeOn = new Thread() {
                    public void run() {
                        current.run();
                    }
                };

                executeOn.start(); // execute the runnable
            }

            if (System.currentTimeMillis() - started >= 1000){
                properlyParsed = false;
                executeOn.interrupt();
                System.out.println("interrupted");
                // if the thread hangs for some reason then interrupt execution so the program doesn't get stuck
                break;
            }
                
        }
        //End of multi-threaded data retrieval

        if (properlyParsed){
            String creationDate = data.get("created").split("T")[0];
            
            data.replace("created", creationDate.substring(1,creationDate.length()));
            data.remove("displayName");

            String beaned = data.get("isBanned");
            
            data.remove("isBanned");
            data.put("Banned", beaned);
        }
        
        return data;
    }

}