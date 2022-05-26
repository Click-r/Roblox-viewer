package classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.*;

import java.awt.Image;
import java.awt.image.BufferedImage;

import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.*;

import loaders.AdvancedSettings;

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

    public Link(String link, boolean... shouldInit) throws IOException {
        AdvancedSettings advSettings = new AdvancedSettings();
        int timeout = Integer.valueOf(advSettings.get("connectionTimeout"));

        this.method = "GET";
        URL site = new URL(link);

        this.connection = (HttpURLConnection) site.openConnection();
        this.connection.setConnectTimeout(timeout);

        initialize(link, shouldInit);
    }

    /**
     * Sends a JSON payload to a website and attempts to retrieve data via the POST method.
     * @param link
     * @param payload - must be of type <code>byte[]</code> and encoded in <code>UTF-8</code>.
     * @throws IOException
    */

    public Link(String link, String payload, boolean... shouldInit) throws IOException {
        this.method = "POST";
        this.payload = payload.getBytes(StandardCharsets.UTF_8);
        URL site = new URL(link);

        this.connection = (HttpURLConnection) site.openConnection();
        this.connection.setRequestMethod(this.method);
        this.connection.setDoOutput(true);
        this.connection.setFixedLengthStreamingMode(this.payload.length);
        this.connection.setRequestProperty("Content-type", "application/json; charset=UTF-8");

        initialize(link, shouldInit);
    }

    /**
     * <p>Reads and returns the image contained on the url described by a given <code>Link</code> instance.</p>
     * @return image
     * @throws IOException
    */

    public Image getImage() throws IOException {
        this.connection.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);
        this.connection.connect();

        InputStream siteInput = this.connection.getInputStream();

        String Etag = connection.getHeaderField("ETag"); // error tag? not sure

        Hashtable<String, String> imgProperties = new Hashtable<>();
        imgProperties.put("direct_url", connection.getURL().toString());

        if (Etag != null) { // only exists if the image data is placeholder due to some reason
            Etag = Etag.substring(1, Etag.length() - 1); // get rid of quotation marks

            imgProperties.put("error", Etag);
        }
        
        BufferedImage toReturn = (BufferedImage) ImageIO.read(siteInput);
        toReturn = new BufferedImage(toReturn.getColorModel(), toReturn.getRaster(), toReturn.isAlphaPremultiplied(), imgProperties);

        return toReturn;
    }

    /**
     * <p>Returns the raw JSON content of the site described by a given <code>Link</code> instance.</p>
     * @param hasConnected - whether the client has already connected to the site.
     * @return JSON content
     * @throws IOException
    */

    public JSONObject getRawJson(boolean hasConnected) throws IOException {
        String textResponse = getRawResponse(hasConnected);
        
        return new JSONObject(textResponse);
    }

    public String getRawResponse(boolean hasConnected) throws IOException {
        if (!hasConnected)
            this.connection.connect();

        String textResponse = null;

        try (Scanner scanner = new Scanner(this.connection.getInputStream(), "UTF-8")) {
            String responseBody = scanner.useDelimiter("\\A").next();
            textResponse = responseBody;
        }

        return textResponse;
    }

    private Map<String, Object> getData(String link, String method) throws IOException {
        Map<String, Object> data = new HashMap<>();

        boolean isPOST = method.equals("POST");

        if (isPOST) {
            this.connection.connect();

            OutputStream os = this.connection.getOutputStream();
            os.write(this.payload);
        }

        JSONObject json = getRawJson(isPOST);
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

    private void initialize(String link, boolean... should) throws IOException {
        if (should.length == 0 || should[0])
            this.data = getData(link, this.method);
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

    public void setRequestProperty(String key, String val) {
        this.connection.setRequestProperty(key, val);
    }
}
