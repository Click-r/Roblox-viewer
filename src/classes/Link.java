package classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import main.*;

public class Link {
    public HashMap<String, Object> data;
    public String method;
    
    private HttpURLConnection connection;
    private byte[] payload;

    public Link(String link) throws IOException {
        this.method = "GET";
        URL site = new URL(link);

        this.connection = (HttpURLConnection) site.openConnection();
        this.connection.setConnectTimeout(5000);

        this.data = getData(link, this.method);
    }

    public Link(String link, byte[] payload) throws IOException {
        this.method = "POST";
        this.payload = payload;
        URL site = new URL(link);

        this.connection = (HttpURLConnection) site.openConnection();
        this.connection.setRequestMethod(this.method);
        this.connection.setDoOutput(true);
        this.connection.setFixedLengthStreamingMode(payload.length);
        this.connection.setRequestProperty("Content-type", "application/json; charset=UTF-8");

        this.data = getData(link, this.method);
    }

    private HashMap<String, Object> getData(String link, String method) throws IOException {
        HashMap<String, Object> data = new HashMap<String, Object>();

        this.connection.connect();

        if (method.equals("POST")) {
            OutputStream os = this.connection.getOutputStream();
            os.write(this.payload);
        }

        InputStream response = this.connection.getInputStream();
        String textResponse = "NaN";

        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            textResponse = responseBody;
        }

        HashMap<String,Object> subData = JSONtoHashtable.toHashtable(textResponse);

        subData.forEach((key,val) -> {
            String name = key;

            if (subData.size() == 1){
                String[] urlName = link.split("/");
                name = urlName[urlName.length - 2];
            }  

            data.put(name, val);
        });
        
        return data;
    }

    public HashMap<String, Object> filter(String[] unwantedKeys) {
        if (unwantedKeys == null)
            return this.data;

        for (String key: unwantedKeys)
            this.data.remove(key);

        return this.data;
    }
}
