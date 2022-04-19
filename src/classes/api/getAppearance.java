package classes.api;

import java.awt.Image;
import java.awt.Color;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import classes.Avatar;
import classes.Link;

import loaders.AdvancedSettings;

import ui.gui.err.ErrorHandler;

public class getAppearance {

    public static Image retrieveImage(long userId) {
        HashMap<String, String> errorStates = new HashMap<>();
        errorStates.put("Error", "https://t5.rbxcdn.com/5228e2fd54377f39e87d3c25a58dd018");
        errorStates.put("InReview", "https://t5.rbxcdn.com/5228e2fd54377f39e87d3c25a58dd018");
        errorStates.put("Pending", "https://t5.rbxcdn.com/5228e2fd54377f39e87d3c25a58dd018");
        errorStates.put("Blocked", "https://t3.rbxcdn.com/894dca84231352d56ec346174a3c0cf9");
        errorStates.put("TemporarilyUnavailable", "https://t4.rbxcdn.com/b561617d22628c1d01dd10f02e80c384");

        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Image> img = exec.submit(() -> {
            String url = "https://thumbnails.roblox.com/v1/users/avatar?userIds=" + userId + "&size=150x150&format=Png&isCircular=false";

            try {
                Link imageLink = new Link(url, false);
                JSONObject returned = imageLink.getRawJson(false).getJSONArray("data").getJSONObject(0);
                String state = returned.getString("state");
                System.out.println(state);

                if (state.equals("Completed"))
                    return new Link(returned.getString("imageUrl"), false).getImage();
                else
                    return new Link(errorStates.get(state), false).getImage();
            } catch (IOException e) {
                int retries = 0;

                do {
                    System.out.println(url);
                    System.out.printf("Image fetch attempt %d...\n", retries);

                    try {
                        Link retryLink = new Link(url, false);
                        JSONObject returned = retryLink.getRawJson(false).getJSONArray("data").getJSONObject(0);
                        String state = returned.getString("state");

                        if (state.equals("Completed"))
                            return new Link(returned.getString("imageUrl"), false).getImage();
                    } catch (IOException io) {}

                    retries++;
                } while (retries < 2);

                Link imageLink = new Link("https://t5.rbxcdn.com/5228e2fd54377f39e87d3c25a58dd018", false);

                return imageLink.getImage();
            }
        });

        try {
            return img.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {}
        
        return null;
    }

    public static JSONObject getCurrentlyWearing(long userId) {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<JSONObject> jsonData = exec.submit(() -> {
            String url = "https://avatar.roblox.com/v1/users/" + userId + "/avatar";

            try {
                Link info = new Link(url, false);

                return info.getRawJson(false);
            } catch (IOException e) {}

            return null;
        });

        try {
            return jsonData.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {}
        catch (ExecutionException execExc) {
            ErrorHandler.report(execExc);
        }

        return null;
    }

    public static Map<String, Long> getOutfits(long userId) {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Map<String, Long> data = new HashMap<>();

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
        } catch (InterruptedException | TimeoutException e) {}
        catch (ExecutionException execExc) {
            ErrorHandler.report(execExc);
        }

        return data;
    }

    public static JSONObject getOutfitDetails(long outfitId) {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<JSONObject> jsonData = exec.submit(() -> {
            String url = "https://avatar.roblox.com/v1/outfits/" + outfitId + "/details";

            try {
                Link info = new Link(url, false);

                return info.getRawJson(false);
            } catch (IOException e) {
                e.printStackTrace();

                Map<String, Object> data = new HashMap<>();
                data.put("id", outfitId);
                data.put("name", "");
                data.put("assets", new JSONArray());

                Map<String, Integer> colourMap = new HashMap<>();
                colourMap.put("headColorId", 0);
                colourMap.put("torsoColorId", 0);
                colourMap.put("rightArmColorId", 0);
                colourMap.put("leftArmColorId", 0);
                colourMap.put("rightLegColorId", 0);
                colourMap.put("leftLegColorId", 0);

                data.put("bodyColors", new JSONObject(colourMap));

                JSONObject placeholderData = new JSONObject(data);
                
                return placeholderData;
            }
        });

        try {
            return jsonData.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {}
        catch (ExecutionException execExc) {
            ErrorHandler.report(execExc);
        }

        return null;
    }

    public static Image getOutfitThumbnail(long outfitId) {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Image> outfitImage = exec.submit(() -> {
            String url = "https://thumbnails.roblox.com/v1/users/outfits?userOutfitIds=" + outfitId + "&size=150x150&format=Png&isCircular=false";

            try {
                Link dataLink = new Link(url, false);

                JSONObject returned = dataLink.getRawJson(false);
                JSONObject imageData = returned.getJSONArray("data").getJSONObject(0);

                String imgUrl = "";

                String state = imageData.getString("state");
                if (state.equals("Blocked") || state.equals("Error") )
                    imgUrl = "https://t4.rbxcdn.com/b561617d22628c1d01dd10f02e80c384";
                else if (state.equals("InReview") || state.equals("Pending"))
                    imgUrl = "https://t5.rbxcdn.com/5228e2fd54377f39e87d3c25a58dd018";
                else
                    imgUrl = imageData.getString("imageUrl");

                Link imageLink = new Link(imgUrl, false);

                return imageLink.getImage();
            } catch (IOException e) {}

            return null;
        });

        try {
            return outfitImage.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {}
        catch (ExecutionException execExc) {
            ErrorHandler.report(execExc);
        }

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

                for (int i = 0; i < outfits.length(); i++) {
                    JSONObject currentobj = outfits.getJSONObject(i);

                    final String state = currentobj.getString("state");

                    String setTo = "";

                    if (state.equals("Blocked") || state.equals("Error") )
                        setTo = "https://t4.rbxcdn.com/b561617d22628c1d01dd10f02e80c384";
                    else if (state.equals("InReview") || state.equals("Pending"))
                        setTo = "https://t5.rbxcdn.com/5228e2fd54377f39e87d3c25a58dd018";
                    else
                        setTo = currentobj.getString("imageUrl");
                    
                    imageUrls[i] = setTo;
                }
            } catch (IOException e) {}

            return imageUrls;
        });

        String[] urls = new String[outfitIds.length];

        try {
            urls = imageLinks.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        Image[] imgs = new Image[outfitIds.length];

        int chosen = 5;

        try {
            AdvancedSettings advSettings = new AdvancedSettings();
            chosen = Integer.valueOf(advSettings.get("threadsToUse"));
        } catch (IOException iex) {
            ErrorHandler.report(iex);
        }

        Stack<Integer> buffer = new Stack<>();

        final int maxThreads = chosen;

        ThreadPoolExecutor downloadImages = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);

        int index = 0;

        for (final String imgUrl : urls) {
            int ind = index;

            Future<Image> fetchImg = downloadImages.submit(() -> {
                try {
                    Link con = new Link(imgUrl, false);

                    Image toReturn = con.getImage();
                    buffer.push(1);

                    return toReturn;
                } catch (FileNotFoundException notfound) { // for some reason the file could be missing in 150x150 resolution
                    System.out.println("150x150 image file not found, trying 420x420");

                    String newUrl = "";

                    String[] parts = imgUrl.split("/");
                    parts[5] = parts[4] = "420";

                    newUrl = String.join("/", parts);

                    Image toReturn = new Link(newUrl, false).getImage();
                    buffer.push(1);

                    return toReturn.getScaledInstance(150, 150, Image.SCALE_AREA_AVERAGING);
                } catch (IOException e) {
                    System.out.println("Failed to fetch outfit image " + imgUrl + "\nPutting in placeholder image.");

                    Image replacement = new Link("https://t5.rbxcdn.com/5228e2fd54377f39e87d3c25a58dd018", false).getImage();
                    buffer.push(1);

                    return replacement;
                }
            });

            downloadImages.submit(() -> {
                try {
                    Image result = fetchImg.get(5, TimeUnit.SECONDS);

                    imgs[ind] = result;
                } catch (ExecutionException exc) {
                    ErrorHandler.report(exc);
                } catch (InterruptedException | TimeoutException timeout) {}
            });

            index++;
        }

        while (buffer.size() < outfitIds.length) {
            try {
                Thread.sleep(2L);
            } catch (InterruptedException inter) {}
        }

        downloadImages.shutdownNow();

        if (imgs.length != outfitIds.length)
            System.out.println("uh oh");
        
        return imgs;
    }

    public static List<Avatar> multiGetOutfits(long[] ids) {
        int chosen = 5;

        try {
            AdvancedSettings advSettings = new AdvancedSettings();
            chosen = Integer.valueOf(advSettings.get("threadsToUse"));
        } catch (IOException iex) {
            ErrorHandler.report(iex);
        }

        final int maxThreads = chosen;

        ThreadPoolExecutor retrieve = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);
        List<Avatar> outfits = new ArrayList<>();

        for (long outfitId : ids) {
            Future<Avatar> getDetails = retrieve.submit(() -> {
                return new Avatar(outfitId);
            });

            retrieve.submit(() -> {
                try {
                    Avatar result = getDetails.get(5, TimeUnit.SECONDS);
                    outfits.add(result);
                } catch (ExecutionException exc) {
                    ErrorHandler.report(exc);
                } catch (InterruptedException | TimeoutException timeout) {}
            });
        }

        do {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {}
        } while (outfits.size() != ids.length);

        retrieve.shutdownNow();

        return outfits;
    }

    public static Map<Integer, SimpleImmutableEntry<String, Color>> getColourIdInfo() {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<Map<Integer, SimpleImmutableEntry<String, Color>>> lookup = exec.submit(() -> {
            String url = "https://avatar.roblox.com/v1/avatar-rules";
            Map<Integer, SimpleImmutableEntry<String, Color>> coloursTable = new HashMap<>();

            try {
                Link data = new Link(url, false);

                JSONObject jsonData = data.getRawJson(false);
                JSONArray bodyColoursPalette = jsonData.getJSONArray("bodyColorsPalette");

                bodyColoursPalette.forEach((jsobj) -> {
                    JSONObject entry = (JSONObject) jsobj;

                    int colourId = entry.getInt("brickColorId");

                    String hexColour = entry.getString("hexColor").substring(1);
                    int rgbInt = Integer.parseUnsignedInt(hexColour, 16);
                    Color parsed = new Color(rgbInt);

                    String colourName = entry.getString("name");

                    SimpleImmutableEntry<String, Color> pair = new SimpleImmutableEntry<>(colourName, parsed);
                    coloursTable.put(colourId, pair);
                });
            } catch (IOException e) {}

            return coloursTable;
        });

        try {
            Map<Integer, SimpleImmutableEntry<String, Color>> result = lookup.get(5, TimeUnit.SECONDS);

            return result;
        } catch (InterruptedException | TimeoutException e) {}
        catch (ExecutionException execExc) {
            ErrorHandler.report(execExc);
        }

        return null;
    }

    public static Image[] batchGetAssetThumbnails(long[] assetIds) {
        if (assetIds.length == 0)
            return new Image[]{};

        ExecutorService exec = Executors.newSingleThreadExecutor();

        Future<String[]> imageLinks = exec.submit(() -> {
            String[] imageUrls = new String[assetIds.length];

            long[][] fragments = new long[][]{assetIds};
            
            if (assetIds.length > 20) {
                fragments = new long[][]{
                    Arrays.copyOfRange(assetIds, 0, 20),
                    Arrays.copyOfRange(assetIds, 20, assetIds.length)
                };
            } // split up the assetIds, since you can't batch-get more than 20 image urls with one request

            int i = 0; // keeps track of the index for imageUrls

            for (long[] idsList : fragments) {
                String stringList = Arrays.toString(idsList);
                stringList = stringList
                  .substring(1, stringList.length() - 1)
                  .replace(" ", "");
        
                String url = "https://thumbnails.roblox.com/v1/assets?assetIds=" + stringList + "&size=110x110&format=Png&isCircular=false";

                try {
                    Link data = new Link(url, false);

                    JSONObject assetsData = data.getRawJson(false);
                    JSONArray dataArray = assetsData.getJSONArray("data");

                    for (int k = 0; k < dataArray.length(); k++) {
                        JSONObject currentobj = dataArray.getJSONObject(k);

                        final String state = currentobj.getString("state");

                        String setTo = "";

                        if (state.equals("Blocked") || state.equals("Error") )
                            setTo = "https://t4.rbxcdn.com/b561617d22628c1d01dd10f02e80c384";
                        else if (state.equals("InReview") || state.equals("Pending"))
                            setTo = "https://t5.rbxcdn.com/5228e2fd54377f39e87d3c25a58dd018";
                        else
                            setTo = currentobj.getString("imageUrl");
                        
                        imageUrls[i] = setTo;

                        i++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return imageUrls;
        });

        String[] urls = new String[assetIds.length];

        try {
            urls = imageLinks.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        Image[] imgs = new Image[assetIds.length];

        int chosen = 5;

        try {
            AdvancedSettings advSettings = new AdvancedSettings();
            chosen = Integer.valueOf(advSettings.get("threadsToUse"));
        } catch (IOException iex) {
            ErrorHandler.report(iex);
        }

        Stack<Integer> buffer = new Stack<>();

        final int maxThreads = chosen;

        ThreadPoolExecutor downloadImages = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);

        int index = 0;

        for (final String imgUrl : urls) {
            int ind = index;

            Future<Image> fetchImg = downloadImages.submit(() -> {
                try {
                    Link con = new Link(imgUrl, false);

                    Image toReturn = con.getImage();
                    buffer.push(1);

                    return toReturn;
                } catch (IOException e) {
                    System.out.println("Failed to fetch asset image " + imgUrl + "\nPutting in placeholder image.");
                    
                    Image replacement = new Link("https://t5.rbxcdn.com/5228e2fd54377f39e87d3c25a58dd018", false).getImage();
                    buffer.push(1);

                    return replacement;
                }
            });

            downloadImages.submit(() -> {
                try {
                    Image result = fetchImg.get(5, TimeUnit.SECONDS);

                    imgs[ind] = result;
                } catch (ExecutionException exc) {
                    ErrorHandler.report(exc);
                } catch (InterruptedException | TimeoutException timeout) {}
            });

            index++;
        }

        while (buffer.size() < assetIds.length) {
            try {
                Thread.sleep(2L);
            } catch (InterruptedException inter) {}
        }

        downloadImages.shutdownNow();

        if (imgs.length != assetIds.length)
            System.out.println("uh oh (x2)");
        
        return imgs;
    }
}
