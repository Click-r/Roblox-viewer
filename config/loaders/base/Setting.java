package loaders.base;

import java.awt.Rectangle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Paths;

import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import main.Controller;

public abstract class Setting {
    protected SettingId id;
    protected String path;
    protected Properties configFile;
    protected Map<String, JComponent> components;

    public Setting(SettingId create) throws IOException {
        id = create;

        String use = create.toString().toLowerCase();
        path = "settings/" + use + "/" + use + ".properties";
        path = Controller.runningAsJar ? System.getProperty("user.dir") + "/" + path : path; // distinction between IDE and jar

        configFile = getConfig();
        components = new HashMap<String, JComponent>();
    }

    public SettingId getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void resetToDefaults() throws IOException {
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

    public void saveToFile() throws IOException {
        String file = Controller.runningAsJar ? path : Setting.class.getClassLoader().getResource(path).getFile();

        FileOutputStream save = new FileOutputStream(file);
        configFile.store(save, "Changed values");
    }

    public Properties getConfig() throws IOException {
        InputStream stream = Controller.runningAsJar ? Paths.get(path).toUri().toURL().openStream() : Setting.class.getClassLoader().getResourceAsStream(path);
        // make sure it can run both in the IDE and jar file (again)

        Properties property = new Properties();
        property.load(stream);

        return property;
    }

    public byte[] getHash() throws IOException, NoSuchAlgorithmException {
        Properties config = getConfig();

        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");

        List<String> strList = new ArrayList<String>();

        config.forEach((name, value) -> {
            if (!((String)name).endsWith("_DEFAULT"))
                strList.add(value.toString());
        });

        strList.sort((str1, str2) -> str1.length() - str2.length());

        strList.forEach((string) -> mDigest.update(string.getBytes()));

        byte[] output = mDigest.digest();

        return output;
    }

    public void set(String key, String value) {
        configFile.setProperty(key, value);
    }

    public String get(String key) {
        return configFile.getProperty(key);
    }

    abstract public JPanel getSettingPanel(Rectangle bounds);

    abstract public boolean applyChanges();

    abstract public void isModified();
}
