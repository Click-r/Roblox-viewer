package loaders;

import java.util.HashMap;
import java.awt.Color;

public enum Themes {
    LIGHT {
        public String toString() {
            return "Light";
        }
    },
    DARK {
        public String toString() {
            return "Dark";
        }
    };

    public static class Palette {
        public HashMap<String, Color> colourPalette = new HashMap<>();
        public Themes colour;

        public Palette(Themes selectedEnum) {
            colour = selectedEnum;

            switch (selectedEnum) {
                case LIGHT:
                    colourPalette.put("background", new Color(238, 238, 238));
                    colourPalette.put("info", new Color(218, 218, 218));
                    colourPalette.put("text", new Color(51, 51, 51));
                    colourPalette.put("amplified", new Color(255, 255, 255));
                    colourPalette.put("error", new Color(252, 163, 150));
                    break;
                case DARK:
                    colourPalette.put("background", new Color(79, 79, 79));
                    colourPalette.put("info", new Color(53, 53, 53));
                    colourPalette.put("text", new Color(171, 171, 171));
                    colourPalette.put("amplified", new Color(60, 60, 60));
                    colourPalette.put("error", new Color(168, 63, 47));
                    break;
            }
        }
    }
}
