package loaders.base;

import java.awt.Rectangle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import main.Controller;

public abstract class Setting {
    protected static SettingId id;
    protected static String path;

    public static SettingId getId() {
        return id;
    }

    public static String getPath() {
        return path;
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

        String file = Controller.runningAsJar ? path : Setting.class.getClassLoader().getResource(path).getFile();
        // make sure it can run both in the IDE and jar file

        FileOutputStream save = new FileOutputStream(file);
        properties.store(save, "Reset to defaults");
    }

    public static Properties getConfig() throws IOException {
        InputStream stream = Controller.runningAsJar ? Paths.get(path).toUri().toURL().openStream() : Setting.class.getClassLoader().getResourceAsStream(path);
        // make sure it can run both in the IDE and jar file (again)

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
