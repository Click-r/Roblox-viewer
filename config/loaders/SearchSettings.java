package loaders;

import java.awt.Rectangle;
import java.awt.Color;

import java.io.IOException;

import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import loaders.base.*;

public class SearchSettings extends Setting {
    private static Properties configFile;

    public SearchSettings() throws IOException {
        setId(SettingId.SEARCH);

        configFile = getConfig();
    }

    public static void set(String key, String value) {
        configFile.setProperty(key, value);
    }

    public static String get(String key) {
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
        minInput.setName("Minimum");

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
        maxInput.setName("Maximum");

        idSetting.add(minText);
        idSetting.add(minInput);
        idSetting.add(maxText);
        idSetting.add(maxInput);

        panel.add(idSetting);

        return panel;
    }
    
}
