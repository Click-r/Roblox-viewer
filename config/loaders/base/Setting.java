package loaders.base;

import java.awt.Rectangle;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

import javax.swing.JPanel;

public abstract class Setting {
    private static SettingId id;

    public static SettingId getId() {
        return id;
    }

    public static void setId(SettingId newId) {
        id = newId;
    }

    abstract public JPanel getSettingPanel(Rectangle bounds);

    public static Properties getConfig() throws IOException {
        String idStr = id.toString().toLowerCase();
        InputStream stream = ClassLoader.getSystemResourceAsStream("settings/" + idStr + "/" + idStr + ".properties");

        Properties property = new Properties();
        property.load(stream);

        return property;
    }

    // TODO: finish this off
}
