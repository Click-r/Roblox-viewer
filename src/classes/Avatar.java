package classes;

import java.awt.Color;
import java.awt.Image;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import classes.api.getAppearance;

/**A class which describes the contents of a given outfit or a {@link Player}'s currently worn apparel*/
public class Avatar {
    public static class Asset {
        public long id;
        public String name;
        public Image image;
    }

    private static Map<Integer, SimpleImmutableEntry<String, Color>> colourLookup;

    public String name; // max length is 99 characters
    public long id; // if the id is -1, that indicates that the appearance stored in the avatar is a user's current appearence
    public List<Asset> assets;
    public Map<String, SimpleImmutableEntry<String, Color>> bodycolours;
    public Image image;

    static {
        colourLookup = getAppearance.getColourIdInfo(); // initialize the lookup table for every instance of avatar
        colourLookup.put(0, new SimpleImmutableEntry<String, Color>("Unknown", new Color(0, 0, 0))); // placeholder colour
    }

    /**
     * <p>Initializes an <code>Avatar</code> instance that contains the contents of the outfit described by the outfit ID parameter.</p> 
     * @param outfitId
    */

    public Avatar(long outfitId) {
        JSONObject details = getAppearance.getOutfitDetails(outfitId);

        id = outfitId;
        name = details.getString("name");
        // outfit id & name

        setDataFields(details);
    }

    /**
     * <p>Initializes an <code>Avatar</code> instance that contains what the player described by the user ID parameter is wearing.</p> 
     * @param userId
    */

    public Avatar(Long userId) {
        JSONObject currentlyWearing = getAppearance.getCurrentlyWearing(userId);

        id = -1L;
        name = "Currently Wearing";

        setDataFields(currentlyWearing);
    }

    private void setDataFields(JSONObject details) {
        JSONArray assetList = details.getJSONArray("assets");
        assets = getAssets(assetList);
        // accessories worn in the outfit

        JSONObject bodyColoursData = details.getJSONObject("bodyColors");
        bodycolours = getColours(bodyColoursData);
        // colours of the outfit
    }

    /**
     * <p>Converts the colours described by the ColorId of each bodypart to RGB and the colour's name.</p>
     * @param rawColours
     * @return Bodyparts mapped to RGB colours.
    */

    public static Map<String, SimpleImmutableEntry<String, Color>> getColours(JSONObject rawColours) {
        Map<String, SimpleImmutableEntry<String, Color>> parts = new HashMap<>();

        for (String partName : rawColours.keySet()) {
            int colourId = rawColours.getInt(partName);
            partName = partName.replace("ColorId", "");

            parts.put(partName, colourLookup.get(colourId));
        }

        return parts;
    }

    /**
     * <p>Returns a list of assets (see {@link Asset}) used in the <code>Avatar</code> described by the given instance</p>
     * @param rawAssetList
     * @return List of assets
    */

    public static List<Asset> getAssets(JSONArray rawAssetList) {
        List<Asset> assetsParsed = new ArrayList<>();

        rawAssetList.forEach((assetDescription) -> {
            JSONObject desc = (JSONObject) assetDescription;

            Asset asset = new Asset();
            asset.id = desc.getLong("id");
            asset.name = desc.getString("name");

            assetsParsed.add(asset);
        });

        return assetsParsed;
    }

    /**
     * <p>Updates the given <code>Avatar</code> instance's image.</p>
    */

    public void setImage() {
        image = getAppearance.getOutfitThumbnail(id);
    }

    /**
     * <p>Sets the image field of a given <code>Avatar</code> instance to that of the passed in image</p>
     * @param image
    */

    public void setImage(Image image) {
        this.image = image;
    }
}
