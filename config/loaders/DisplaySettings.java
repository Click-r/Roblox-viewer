package loaders;

import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.*;

import java.io.IOException;

import java.nio.charset.Charset;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.*;
import javax.swing.event.*;

import loaders.base.*;

import ui.ErrorHandler;
import ui.SettingsMenu;

public class DisplaySettings extends Setting {

    public DisplaySettings() throws IOException {
        super(SettingId.DISPLAY);
    }

    @Override
    public JPanel getSettingPanel(Rectangle bounds) {
        final Color highlighted = new Color(218, 218, 218);

        JPanel dispPanel = new JPanel();
        dispPanel.setBounds(bounds);
        dispPanel.setLayout(null);

        JPanel themeSetting = new JPanel();
        themeSetting.setBounds(4, 5, 180, 90);
        themeSetting.setLayout(null);
        themeSetting.setBorder(new TitledBorder(new EtchedBorder(), "Theme"));
        themeSetting.setBackground(highlighted);

        JComboBox<String> themeSelection = new JComboBox<String>(new String[]{"Light", "Dark (beta)"});
        themeSelection.setBounds(8, 15, 130, 25);
        themeSelection.setEditable(false);
        themeSelection.setMaximumRowCount(2);
        themeSelection.setSelectedIndex(Integer.valueOf(get("current_theme")));
        themeSelection.setName("current_theme");

        JTextPane restartReminder = new JTextPane();
        restartReminder.setBounds(themeSelection.getX() - 2, themeSelection.getY() + themeSelection.getHeight() + 5, 150, 18);
        restartReminder.setEditable(false);
        restartReminder.setText("Restart required");
        restartReminder.setForeground(new Color(237, 19, 19));
        restartReminder.setBackground(highlighted);

        JPanel userDisplaySetting = new JPanel();
        userDisplaySetting.setLocation(themeSetting.getX(), themeSetting.getY() + themeSetting.getHeight() + 5);
        userDisplaySetting.setSize(300, themeSetting.getHeight());
        userDisplaySetting.setBorder(new TitledBorder(new EtchedBorder(), "User"));
        userDisplaySetting.setBackground(highlighted);
        userDisplaySetting.setLayout(null);

        JTextPane startUserText = new JTextPane();
        startUserText.setBounds(8, 15, 65, 16);
        startUserText.setText("Start user:");
        startUserText.setEditable(false);
        startUserText.setBackground(highlighted);

        JTextField usernameInput = new JTextField();
        usernameInput.setBounds(startUserText.getX() + startUserText.getWidth() + 5, startUserText.getY() + 3, 160, 20);
        usernameInput.setColumns(1);
        usernameInput.setEditable(true);
        usernameInput.setToolTipText("Must be a username");
        usernameInput.setText(get("start_user"));
        usernameInput.setName("start_user");

        themeSetting.add(themeSelection);
        themeSetting.add(restartReminder);

        userDisplaySetting.add(startUserText);
        userDisplaySetting.add(usernameInput);

        DocumentListener docListen = new DocumentListener(){
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

        ActionListener actListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                isModified();
            }
        };

        usernameInput.getDocument().addDocumentListener(docListen);
        themeSelection.addActionListener(actListener);

        dispPanel.add(themeSetting);
        dispPanel.add(userDisplaySetting);

        components.put("current_theme", themeSelection);
        components.put("start_user", usernameInput);

        return dispPanel;
    }


    @Override
    public boolean applyChanges() {
        boolean valid = true;

        String newStartUser = ((JTextField) components.get("start_user")).getText();

        if (Charset.forName("US-ASCII").newEncoder().canEncode(newStartUser)) { // make sure it is an ascii name
            set("start_user", newStartUser);
        } else valid = false;

        String themeNum = String.valueOf(((JComboBox<?>) components.get("current_theme")).getSelectedIndex());

        if (themeNum.matches("[0-1]") && themeNum.length() == 1) {
            set("current_theme", themeNum);
        } else {
            System.out.println("what");
            valid = false;
        }

        try {
            saveToFile();

            return valid;
        } catch (IOException writingexc) {
            ErrorHandler.report(writingexc);
        }

        return false;
    }

    @Override
    public void isModified() {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = getHash();

            List<String> strList = new ArrayList<String>();

            strList.add(String.valueOf(((JComboBox<?>) components.get("current_theme")).getSelectedIndex()));
            strList.add(((JTextField) components.get("start_user")).getText());

            strList.sort((str1, str2) -> str1.length() - str2.length());

            strList.forEach((string) -> mDigest.update(string.getBytes()));

            byte[] output = mDigest.digest();

            boolean equal = MessageDigest.isEqual(hash, output);
            
            SettingsMenu.state.setSaveState(getId().toString(), equal);
            SettingsMenu.state.notifyUser();
        } catch (NoSuchAlgorithmException | IOException exc) {
            ErrorHandler.report(exc);
        }
    }
    
}
