package loaders.base;

import java.awt.Rectangle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class Setting {
    protected static SettingId id;

    public static SettingId getId() {
        return id;
    }

    public static void resetToDefaults() throws IOException {
        Properties properties = getConfig();

        final String suffix = "_DEFAULT";

        properties.forEach((key, val) -> {
            if (key.toString().endsWith(suffix))
                return;
            
            String defaultKey = key + suffix;
            
            if (properties.containsKey(defaultKey)) {
                String defValue = properties.getProperty(defaultKey);

                properties.setProperty( (String) key, defValue);
            }
        });

        FileOutputStream save = new FileOutputStream(ClassLoader.getSystemResource("settings/" + id + "/" + id + ".properties").getFile());
        properties.store(save, "Reset to defaults");
    }

    public static Properties getConfig() throws IOException {
        InputStream stream = ClassLoader.getSystemResourceAsStream("settings/" + id + "/" + id + ".properties");

        Properties property = new Properties();
        property.load(stream);

        return property;
    }

    abstract public JPanel getSettingPanel(Rectangle bounds);

    abstract public void set(String key, String value);

    abstract public String get(String key);

    abstract public Map<String, JComponent> getComponents();

    abstract public void applyChanges(Map<String, JComponent> setterComponents);
}
