package classes;

import java.awt.Color;
import java.awt.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import classes.api.getAppearance;

public class Avatar {
    public static class Asset {
        public long id;
        public String name;
    }

    public String name; // max length is 99 characters
    public long id;
    public List<Asset> assets = new ArrayList<Asset>();
    public Map<String, Color> bodycolours = new HashMap<String, Color>();
    public Image outfitThumbnail;

    {
        Color defaultColor = new Color(0, 0, 0);

        bodycolours.put("headColorId", defaultColor);
        bodycolours.put("torsoColorId", defaultColor);
        bodycolours.put("rightArmColorId", defaultColor);
        bodycolours.put("leftArmColorId", defaultColor);
        bodycolours.put("rightLegColorId", defaultColor);
        bodycolours.put("leftLegColorId", defaultColor);
    }

    public Avatar(long outfitId) {
        JSONObject details = getAppearance.getOutfitDetails(outfitId);

        id = outfitId;
        name = details.getString("name");
        // outfit id & name

        JSONArray assetList = details.getJSONArray("assets");
        assetList.forEach((assetDescription) -> {
            JSONObject desc = (JSONObject) assetDescription;

            Asset asset = new Asset();
            asset.id = desc.getLong("id");
            asset.name = desc.getString("name");

            assets.add(asset);
        });
        // accessories worn in the outfit

        /*
        JSONObject bodyColours = details.getJSONObject("bodyColors");
        TODO: Decode body colour ids into rgb 
        */
    }

    public void setImage() {
        outfitThumbnail = getAppearance.getOutfitThumbnail(id);
    }

    public void setImage(Image image) {
        outfitThumbnail = image;
    } // in case of batch gets
}
