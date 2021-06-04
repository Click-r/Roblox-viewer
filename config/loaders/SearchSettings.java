package loaders;

import java.awt.Rectangle;
import java.awt.Color;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import loaders.base.*;

import ui.ErrorHandler;

public class SearchSettings extends Setting {
    private static Properties configFile;
    private static Map<String, JComponent> components = new HashMap<String, JComponent>();

    private boolean isLongValid(String numberString) {
        if (numberString.matches("\\d+")) {
            try {
                Long.valueOf(numberString);
                return true;
            } catch (NumberFormatException tooLarge) {
                System.out.println("Number is too large!");
            }
        }
        
        return false;
    }

    public SearchSettings() throws IOException {
        id = SettingId.SEARCH;

        configFile = getConfig();
    }

    @Override
    public void set(String key, String value) {
        configFile.setProperty(key, value);
    }

    @Override
    public String get(String key) {
        return configFile.getProperty(key);
    }

    @Override
    public JPanel getSettingPanel(Rectangle bounds) {
        final Color highlighted = new Color(218,218,218);

        JPanel panel = new JPanel();
        panel.setBounds(bounds);
        panel.setLayout(null);

        JPanel idSetting = new JPanel();
        idSetting.setBounds(4, 5, 350, 60);
        idSetting.setLayout(null);
        idSetting.setBorder(new TitledBorder(new EtchedBorder(), "ID"));
        idSetting.setBackground(highlighted);

        JTextPane minText = new JTextPane();
        minText.setBounds(5, idSetting.getHeight()/2 - 10, 25, 20);
        minText.setText("Min:");
        minText.setBackground(highlighted);
        minText.setEditable(false);

        JTextField minInput = new JTextField();
        minInput.setBounds(minText.getX() + minText.getWidth() + 1, minText.getY() + 3, 110, minText.getHeight());
        minInput.setColumns(1);
        minInput.setEditable(true);
        minInput.setText(get("min_id"));
        minInput.setName("min_id");

        JTextPane maxText = new JTextPane();
        maxText.setBounds(minInput.getX() + minInput.getWidth() + 20, minText.getY(), 26, 20);
        maxText.setText("Max:");
        maxText.setBackground(highlighted);
        maxText.setEditable(false);

        JTextField maxInput = new JTextField();
        maxInput.setBounds(maxText.getX() + maxText.getWidth() + 1, maxText.getY() + 3, 110, maxText.getHeight());
        maxInput.setColumns(1);
        maxInput.setEditable(true);
        maxInput.setText(get("max_id"));
        maxInput.setName("max_id");

        idSetting.add(minText);
        idSetting.add(minInput);
        idSetting.add(maxText);
        idSetting.add(maxInput);

        panel.add(idSetting);

        components.put(minInput.getName(), minInput);
        components.put(maxInput.getName(), maxInput);

        return panel;
    }

    @Override
    public Map<String, JComponent> getComponents() {
        return components;
    }

    @Override
    public void applyChanges(Map<String, JComponent> setterComponents) {
        String min_id = ((JTextComponent) setterComponents.get("min_id")).getText();
        String max_id = ((JTextComponent) setterComponents.get("max_id")).getText();

        if (isLongValid(min_id) && isLongValid(max_id)) {
            Long textA,textB;
            textA = Long.valueOf(min_id);
            textB = Long.valueOf(max_id);

            if ((textA > 0 && textB > 0) && (textA < textB)) {
                set("min_id", textA.toString());
                set("max_id", textB.toString());
            }
        }

        try {
            FileOutputStream save = new FileOutputStream(ClassLoader.getSystemResource("settings/search/search.properties").getFile());
            configFile.store(save, "Changed values");
        } catch (IOException writingexc) {
            ErrorHandler.report(writingexc);
        }

        //TODO: if there is a more efficient way of applying the settings, then implement it
    }
    
}
