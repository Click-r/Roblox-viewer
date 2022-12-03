package loaders.base;

import java.awt.Rectangle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Paths;

import java.util.AbstractMap.SimpleEntry;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import main.Controller;

import ui.gui.err.ErrorHandler;
import ui.gui.utilities.SettingsMenu;

public abstract class Setting {
    protected SettingId id;
    protected String path;
    protected Properties configFile;
    protected Map<String, SimpleEntry<JComponent, String>> components;

    public Setting(SettingId create) throws IOException {
        id = create;

        String use = create.toString().toLowerCase();
        path = "settings/" + use + "/" + use + ".properties";
        path = Controller.runningAsJar ? System.getProperty("user.dir") + "/" + path : path; // distinction between IDE and jar

        configFile = getConfig();
        components = new HashMap<String, SimpleEntry<JComponent, String>>();
    }

    public SettingId getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    private void resetInputsUI() {
        components.forEach((setting, pair) -> {
            String setValue = get(setting + "_DEFAULT");

            JComponent component = pair.getKey();

            if (component instanceof JTextComponent) {
                ((JTextComponent) component).setText(setValue);
            } else if (component instanceof JToggleButton) {
                ((JToggleButton) component).setSelected(setValue.equals("true"));
            } else if (component instanceof JComboBox) {
                JComboBox<?> toSet = (JComboBox<?>) component;

                if (setValue.matches("\\d+"))
                    toSet.setSelectedIndex(Integer.valueOf(setValue));
                else
                    toSet.setSelectedItem(setValue);
            } else {
                System.out.printf("You forgot to implement the default reset for type %s in order to reset input box of option %s!\n",
                                    component.getClass().getSuperclass().getSimpleName(), setting);
            }
        }); // not the prettiest code i've written
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

        resetInputsUI();
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

        List<String> strList = new ArrayList<>();

        config.forEach((name, value) -> {
            if (!((String)name).endsWith("_DEFAULT"))
                strList.add(name.toString() + value.toString());
        });

        Collections.sort(strList);

        strList.forEach((string) -> mDigest.update(string.getBytes()));

        byte[] output = mDigest.digest();

        return output;
    }

    public void isModified() {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = getHash();

            List<String> valList = new ArrayList<>();
            components.forEach((settingName, pair) -> valList.add(settingName + pair.getValue()));

            Collections.sort(valList);

            valList.forEach(string -> mDigest.update(string.getBytes()));
            byte[] output = mDigest.digest();

            boolean equal = MessageDigest.isEqual(hash, output);

            SettingsMenu.state.setSaveState(getId().toString(), equal);
            SettingsMenu.state.notifyUser();
        } catch (NoSuchAlgorithmException | IOException excs) {
            ErrorHandler.report(excs);
        }
    }

    public void set(String key, String value) {
        configFile.setProperty(key, value);
    }

    public String get(String key) {
        return configFile.getProperty(key);
    }

    abstract public JPanel getSettingPanel(Rectangle bounds);

    abstract public boolean applyChanges();
}
