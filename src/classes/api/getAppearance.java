package classes.api;

import java.awt.Image;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import classes.Link;

public class getAppearance {

    public static Image retrieveImage(long userId) {
        ExecutorService ret = Executors.newSingleThreadExecutor();

        Future<Image> img = ret.submit(() -> {
            String url = "https://web.roblox.com/Thumbs/Avatar.ashx?x=150&y=150&Format=Png&userid=" + userId;

            try {
                Link imageLink = new Link(url, false);
                
                return imageLink.getImage();
            } catch (IOException e) {}

            return null;
        });

        try {
            return img.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {}
        
        return null;
    }

    public static Map<String, Long> getOutfits(long userId) {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Map<String, Long> data = new HashMap<String, Long>();

        Future<JSONObject> jsonData = exec.submit(() -> {
            String url = "https://avatar.roblox.com/v1/users/" + userId + "/outfits?page=1&itemsPerPage=64";

            try {
                Link info = new Link(url, false);

                return info.getRawJson(false);
            } catch (IOException e) {}

            return null;
        });

        try {
            JSONObject jsobj = jsonData.get(5, TimeUnit.SECONDS);
            JSONArray outfits = jsobj.getJSONArray("data");

            outfits.forEach((outfitData) -> {
                JSONObject processed = (JSONObject) outfitData;

                data.put(processed.getString("name"), processed.getLong("id"));
            });
        } catch (InterruptedException | ExecutionException | TimeoutException e) {}

        return data;
    }
}
