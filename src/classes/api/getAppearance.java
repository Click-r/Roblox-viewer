package classes.api;

import java.awt.Image;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import classes.Link;

import loaders.AdvancedSettings;

import ui.gui.err.ErrorHandler;

public class getAppearance {

    public static Image retrieveImage(long userId) {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Image> img = exec.submit(() -> {
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

    public static JSONObject getOutfitDetails(long outfitId) {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<JSONObject> jsonData = exec.submit(() -> {
            String url = "https://avatar.roblox.com/v1/outfits/" + outfitId + "/details";

            try {
                Link info = new Link(url, false);

                return info.getRawJson(false);
            } catch (IOException e) {}

            return null;
        });

        try {
            return jsonData.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {}

        return null;
    }

    public static Image getOutfitThumbnail(long outfitId) {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Image> outfitImage = exec.submit(() -> {
            String url = "https://thumbnails.roblox.com/v1/users/outfits?userOutfitIds=" + outfitId + "&size=150x150&format=Png&isCircular=false";

            try {
                Link imageLink = new Link(url, false);

                return imageLink.getImage();
            } catch (IOException e) {}

            return null;
        });

        try {
            return outfitImage.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {}

        return null;
    }

    public static Image[] batchGetOutfitThumbnails(long[] outfitIds) {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<String[]> imageLinks = exec.submit(() -> {
            String outfitIdStrings = Arrays.toString(outfitIds);
            outfitIdStrings = outfitIdStrings
              .substring(1, outfitIdStrings.length() - 1)
              .replace(" ", "");

            String[] imageUrls = new String[outfitIds.length];
            String url = "https://thumbnails.roblox.com/v1/users/outfits?userOutfitIds=" + outfitIdStrings + "&size=150x150&format=Png&isCircular=false";

            try {
                Link data = new Link(url, false);

                JSONObject outfitData = data.getRawJson(false);

                JSONArray outfits = outfitData.getJSONArray("data");
                outfits.forEach((jsobj) -> {
                    JSONObject info = (JSONObject) jsobj;

                    imageUrls[info.getInt("targetId") - 1] = info.getString("imageUrl");
                });
            } catch (IOException e) {}

            return imageUrls;
        });

        String[] urls = new String[outfitIds.length];

        try {
            urls = imageLinks.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {}

        Image[] imgs = new Image[outfitIds.length];

        int chosen = 5;

        try {
            AdvancedSettings advSettings = new AdvancedSettings();
            chosen = Integer.valueOf(advSettings.get("threadsToUse"));
        } catch (IOException iex) {
            ErrorHandler.report(iex);
        }

        Stack<Integer> buffer = new Stack<Integer>();

        final int maxThreads = chosen;

        ThreadPoolExecutor downloadImages = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);

        int index = 0;

        for (final String imgUrl : urls) {
            int ind = index;

            Future<Image> fetchImg = downloadImages.submit(() -> {
                try {
                    Link con = new Link(imgUrl, false);

                    buffer.push(1);

                    return con.getImage();
                } catch (IOException e) {
                    int remainingBuffer = 5 - buffer.size();

                    for (int i = 0; i < remainingBuffer; i++)
                        buffer.push(1);
                    
                    return null;
                }
            });

            downloadImages.submit(() -> {
                try {
                    Image result = fetchImg.get(5, TimeUnit.SECONDS);

                    imgs[ind] = result;
                } catch (ExecutionException exc) {
                    ErrorHandler.report(exc);
                } catch (InterruptedException|TimeoutException timeout) {}
            });

            index++;
        }

        while (buffer.size() < outfitIds.length) {
            try {
                Thread.sleep(2L);
            } catch (InterruptedException inter) {}
        }

        if (imgs.length == outfitIds.length)
            System.out.println("we're good to go");
        
        return imgs;
    }
}
