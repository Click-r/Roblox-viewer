package classes;

import javax.imageio.ImageIO;

import java.awt.Image;

import java.io.IOException;
import java.io.InputStream;

import java.util.EnumMap;

public class Images {
    public static enum Local {
        WARNING,
        REFRESH
    }
    private static EnumMap<Local, String> pathMap = new EnumMap<>(Local.class);

    public static enum Placeholder {
        MODERATED,
        MISSING,
        FAILED_LOAD
    }
    private static EnumMap<Placeholder, String> hashMapping = new EnumMap<>(Placeholder.class);

    static {
        pathMap.put(Local.WARNING, "/ui/assets/warning.png");
        pathMap.put(Local.REFRESH, "/ui/assets/reload.png");

        hashMapping.put(Placeholder.MODERATED, "b561617d22628c1d01dd10f02e80c384");
        hashMapping.put(Placeholder.MISSING, "894dca84231352d56ec346174a3c0cf9");
        hashMapping.put(Placeholder.FAILED_LOAD, "5228e2fd54377f39e87d3c25a58dd018");
    }

    public static String linkFromHash(String hash) {
        int state = 31;

        for (int i = 0; i < 32; i++)
            state ^= hash.codePointAt(i); // xor
        
        return "https://t" + (state % 8) + ".rbxcdn.com/" + hash;
    }

    public static Image fetchImage(String url) throws IOException {
        try {
            Link imgLink = new Link(url, false);

            return imgLink.getImage();
        } catch (IOException e) {
            System.out.println(url);
            
            for (int retries = 0; retries < 3; retries++) {
                System.out.printf("Image fetch attempt %d...\n", retries + 1);

                try {
                    Link retry = new Link(url, false);

                    return retry.getImage();
                } catch (IOException ioe) {};
            }

            String link = linkFromHash(hashMapping.get(Placeholder.FAILED_LOAD));
            Image replacement = new Link(link, false).getImage(); // retrieve it manually so it doesn't get stuck in recursion

            return replacement;
        }
    }

    public static Image getLocal(Local img) throws IOException {
        String path = pathMap.get(img);

        InputStream stream = Images.class.getResourceAsStream(path);
        Image toReturn = ImageIO.read(stream);

        return toReturn;
    }

    public static Image getPlaceholder(Placeholder img) throws IOException {
        String link = linkFromHash(hashMapping.get(img));

        Image toReturn = fetchImage(link);

        return toReturn;
    }
}
