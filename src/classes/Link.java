package classes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

import org.json.*;

/** Establishes a connection and allows interaction with ROBLOX API endpoints. */

public class Link {
    public Map<String, Object> data;
    public String method;
    
    private HttpURLConnection connection;
    private byte[] payload;

    /**
     * Retrieves data from a website via the GET method.
     * @param link
     * @throws IOException
    */

    public Link(String link) throws IOException {
        this.method = "GET";
        URL site = new URL(link);

        this.connection = (HttpURLConnection) site.openConnection();
        this.connection.setConnectTimeout(5000);

        this.data = getData(link, this.method);
    }

    /**
     * Sends a JSON payload to a website and attempts to retrieve data via the POST method.
     * @param link
     * @param payload - must be of type <code>byte[]</code> and encoded in <code>UTF-8</code>.
     * @throws IOException
    */

    public Link(String link, String payload) throws IOException {
        this.method = "POST";
        this.payload = payload.getBytes(StandardCharsets.UTF_8);
        URL site = new URL(link);

        this.connection = (HttpURLConnection) site.openConnection();
        this.connection.setRequestMethod(this.method);
        this.connection.setDoOutput(true);
        this.connection.setFixedLengthStreamingMode(this.payload.length);
        this.connection.setRequestProperty("Content-type", "application/json; charset=UTF-8");

        this.data = getData(link, this.method);
    }

    private Map<String, Object> getData(String link, String method) throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();

        this.connection.connect();

        if (method.equals("POST")) {
            OutputStream os = this.connection.getOutputStream();
            os.write(this.payload);
        }

        InputStreamReader response = new InputStreamReader(this.connection.getInputStream(), StandardCharsets.UTF_8);
        String textResponse = "NaN";

        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            textResponse = responseBody;
        }

        JSONObject json = new JSONObject(textResponse);
        Map<String,Object> subData = json.toMap();

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

    /**
     * <p>Removes any unwanted key-value pairs from the present <code>Link</code> instance's <code>HashMap</code> using the key.</p>
     * @param unwantedKeys - may be set to <code>null</code> to signfy no keys should be filtered.
     * @return the current instance's filtered <code>HashMap</code>.
    */

    public Map<String, Object> filter(String[] unwantedKeys) {
        if (unwantedKeys == null)
            return this.data;

        for (String key: unwantedKeys)
            this.data.remove(key);

        return this.data;
    }
}
