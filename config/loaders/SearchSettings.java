package loaders;

import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.*;

import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;

import loaders.base.*;

import ui.ErrorHandler;
import ui.SettingsMenu;

public class SearchSettings extends Setting {

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
        super(SettingId.SEARCH);
    }

    @Override
    public JPanel getSettingPanel(Rectangle bounds) {
        final Color highlighted = new Color(218, 218, 218);

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

        JPanel timezoneSetting = new JPanel();
        timezoneSetting.setBounds(idSetting.getX(), idSetting.getY() + idSetting.getHeight() + 20, idSetting.getWidth(), idSetting.getHeight() + 130);
        timezoneSetting.setLayout(null);
        timezoneSetting.setBackground(highlighted);
        timezoneSetting.setBorder(new TitledBorder(new EtchedBorder(), "Time Zone"));

        Set<String> valid = new HashSet<String>(Arrays.asList(TimeZone.getAvailableIDs()));
        valid.removeIf(zone -> zone.toUpperCase() != zone);

        String[] validZones = new String[valid.size()];
        byte ind = 0;

        for (String zone: valid) {
            validZones[ind] = zone;
            ind++;
        }

        Arrays.sort(validZones);

        boolean local = Boolean.valueOf(get("local"));

        JComboBox<String> pickZone = new JComboBox<String>(validZones);
        pickZone.setBounds(8, 20, 250, 30);
        pickZone.setEditable(false);
        pickZone.setMaximumRowCount(7);
        pickZone.setName("timezone");
        pickZone.setEnabled(!local);
        pickZone.setSelectedItem(get("timezone"));
        
        JCheckBox useLocal = new JCheckBox("Local", local); // sets whether it uses local timezone
        useLocal.setToolTipText("Tells the program whether to use local time or the selected time.");
        useLocal.setBounds(pickZone.getX() + pickZone.getWidth() + 20, pickZone.getY(), 60, 20);
        useLocal.setBackground(highlighted);
        useLocal.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                pickZone.setEnabled(ie.getStateChange() != ItemEvent.SELECTED);
            }
        });
        useLocal.setName("local");

        timezoneSetting.add(pickZone);
        timezoneSetting.add(useLocal);

        ActionListener actListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isModified();
            }
        };

        DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isModified();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isModified();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isModified();
            }
        };

        minInput.getDocument().addDocumentListener(docListener);
        maxInput.getDocument().addDocumentListener(docListener);
        pickZone.addActionListener(actListener);
        useLocal.addActionListener(actListener);

        panel.add(idSetting);
        panel.add(timezoneSetting);

        components.put(minInput.getName(), minInput);
        components.put(maxInput.getName(), maxInput);
        components.put(pickZone.getName(), pickZone);
        components.put(useLocal.getName(), useLocal);

        return panel;
    }

    @Override
    public boolean applyChanges() {
        boolean valid = true;

        String min_id = ((JTextComponent) components.get("min_id")).getText();
        String max_id = ((JTextComponent) components.get("max_id")).getText();

        if (isLongValid(min_id) && isLongValid(max_id)) {
            Long textA,textB;
            textA = Long.valueOf(min_id);
            textB = Long.valueOf(max_id);

            if ((textA > 0 && textB > 0) && (textA < textB)) {
                set("min_id", textA.toString());
                set("max_id", textB.toString());
            } else valid = false;
        } else valid = false;

        String local = ((JCheckBox) components.get("local")).isSelected() ? "true" : "false";
        String timezone = ((JComboBox<?>) components.get("timezone")).getSelectedItem().toString();

        set("local", local);
        set("timezone", timezone);

        try {
            saveToFile();

            return valid;
        } catch (IOException writingexc) {
            ErrorHandler.report(writingexc);
        }

        return false; // if it goes past the try-catch block we return invalid
    }

    @Override
    public void isModified() {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = getHash();
            
            List<String> strList = new ArrayList<String>();

            strList.add(String.valueOf(((JCheckBox) components.get("local")).isSelected()));
            strList.add(((JTextComponent) components.get("min_id")).getText());
            strList.add(((JTextComponent) components.get("max_id")).getText());
            strList.add(((JComboBox<?>) components.get("timezone")).getSelectedItem().toString());
            // can't wait to have to add more to this manually as settings expand e.e

            strList.sort((str1, str2) -> str1.length() - str2.length());

            strList.forEach((string) -> mDigest.update(string.getBytes()));

            byte[] output = mDigest.digest();

            boolean equal = MessageDigest.isEqual(hash, output);

            SettingsMenu.state.setSaveState(getId().toString(), equal);
            SettingsMenu.state.notifyUser();
        } catch (IOException | NoSuchAlgorithmException errs) {
            ErrorHandler.report(errs);
        }
    }
}
