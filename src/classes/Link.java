package classes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Scanner;

import main.*;

public class Link {
    public HashMap<String, String> data;

    public Link(String link) throws IOException {
        this.data = getData(link);
    }

    private HashMap<String, String> getData(String link) throws IOException {
        HashMap<String, String> data = new HashMap<String, String>();

        URL api = new URL(link);
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

        subData.forEach( (key,val) -> {
            String name = key;
            if (subData.size() == 1){
                String[] urlName = link.split("/");
                name = urlName[urlName.length - 2];
            }
            data.put(name, (String)val);
        });
        
        return data;
    }

    public HashMap<String, String> filter(String[] unwantedKeys) {
        if (unwantedKeys == null)
            return this.data;

        for (String key: unwantedKeys)
            this.data.remove(key);

        return this.data;
    }
}
