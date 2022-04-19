package loaders;

import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import loaders.base.*;

import ui.gui.err.ErrorHandler;
import ui.gui.utilities.SettingsMenu;

public class AdvancedSettings extends Setting {

    public AdvancedSettings() throws IOException {
        super(SettingId.ADVANCED);
    }

    @Override
    public JPanel getSettingPanel(Rectangle bounds) {
        final Color highlighted = new Color(218, 218, 218);

        JPanel panel = new JPanel();
        panel.setBounds(bounds);
        panel.setLayout(null);

        JPanel ioPanel = new JPanel();
        ioPanel.setBounds(4, 5, 250, 90);
        ioPanel.setLayout(null);
        ioPanel.setBorder(new TitledBorder(new EtchedBorder(), "I/O"));
        ioPanel.setBackground(highlighted);

        JTextPane timeoutText = new JTextPane();
        timeoutText.setBounds(4, 14, 50, 16);
        timeoutText.setText("Timeout:");
        timeoutText.setEditable(false);
        timeoutText.setToolTipText("Option for setting the duration after which the web request is invalidated.");
        timeoutText.setBackground(highlighted);

        JTextField timeoutInput = new JTextField();
        timeoutInput.setBounds(timeoutText.getX() + timeoutText.getWidth() + 5, timeoutText.getY() + 4, 40, 17);
        timeoutInput.setColumns(1);
        timeoutInput.setEditable(true);
        timeoutInput.setToolTipText("<html> <p> Must be in the range 0 - 32767.<br>Time unit is milliseconds (i.e 5000 milliseconds = 5 seconds). </p> </html>"); // thanks, java
        timeoutInput.setText(get("connectionTimeout"));
        timeoutInput.setName("connectionTimeout");

        JTextPane threadText = new JTextPane();
        threadText.setBounds(timeoutText.getX(), timeoutText.getY() + timeoutText.getHeight() + 7, 50, 16);
        threadText.setText("Threads:");
        threadText.setEditable(false);
        threadText.setToolTipText("Option for setting how much threads the threadpool should be allowed to use while getting the data.");
        threadText.setBackground(highlighted);

        JTextField threadInput = new JTextField();
        threadInput.setBounds(threadText.getX() + threadText.getWidth() + 5, threadText.getY() + 4, 40, 17);
        threadInput.setColumns(1);
        threadInput.setEditable(true);
        threadInput.setToolTipText("Must be in the range 0 - 127."); // i don't see any reason as to why anyone would need more than 127 threads for this
        threadInput.setText(get("threadsToUse"));
        threadInput.setName("threadsToUse");

        boolean showPing = Boolean.valueOf(get("displayPing"));

        JCheckBox pingInput = new JCheckBox("Show Ping*", showPing);
        pingInput.setBounds(timeoutInput.getX() + timeoutInput.getWidth() + 15, timeoutInput.getY(), 90, 20);
        pingInput.setBackground(highlighted);
        pingInput.setName("displayPing");

        JTextPane pingRestartReminder = new JTextPane();
        pingRestartReminder.setBounds(threadText.getX(), threadText.getY() + threadText.getHeight() + 5, 150, 18);
        pingRestartReminder.setEditable(false);
        pingRestartReminder.setText("*Restart required");
        pingRestartReminder.setForeground(new Color(237, 19, 19));
        pingRestartReminder.setBackground(highlighted);

        ioPanel.add(timeoutText);
        ioPanel.add(timeoutInput);
        ioPanel.add(threadText);
        ioPanel.add(threadInput);
        ioPanel.add(pingInput);
        ioPanel.add(pingRestartReminder);

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

        timeoutInput.getDocument().addDocumentListener(docListen);
        threadInput.getDocument().addDocumentListener(docListen);

        pingInput.addActionListener(actListener);

        panel.add(ioPanel);

        components.put("connectionTimeout", timeoutInput);
        components.put("threadsToUse", threadInput);
        components.put("displayPing", pingInput);
        // TODO: also add option to see the log (changing the vm launch flags)

        return panel;
    }

    @Override
    public boolean applyChanges() {
        boolean valid = true;

        String timeoutValue = ((JTextField) components.get("connectionTimeout")).getText();
        String threadValue = ((JTextField) components.get("threadsToUse")).getText();
        String pingValue = ((JCheckBox) components.get("displayPing")).isSelected() ? "true" : "false";

        if (timeoutValue.matches("[0-9]+") && threadValue.matches("[0-9]+")) {
            try {
                short timeoutNumVal = Short.valueOf(timeoutValue);
                byte threadNumVal = Byte.valueOf(threadValue);

                if (timeoutNumVal > 0 && threadNumVal > 0) {
                    set("connectionTimeout", timeoutValue);
                    set("threadsToUse", threadValue);
                } else valid = false;
            } catch (NumberFormatException conversion) {
                valid = false;
            }
        } else valid = false;

        set("displayPing", pingValue);

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

            String timeoutValue = ((JTextField) components.get("connectionTimeout")).getText();
            String threadValue = ((JTextField) components.get("threadsToUse")).getText();
            String pingValue = ((JCheckBox) components.get("displayPing")).isSelected() ? "true" : "false";

            List<String> valList = new ArrayList<>();

            valList.add(timeoutValue);
            valList.add(threadValue);
            valList.add(pingValue);

            valList.sort((str1, str2) -> str1.length() - str2.length());

            valList.forEach(string -> mDigest.update(string.getBytes()));

            byte[] output = mDigest.digest();

            boolean equal = MessageDigest.isEqual(hash, output);

            SettingsMenu.state.setSaveState(getId().toString(), equal);
            SettingsMenu.state.notifyUser();
        } catch (NoSuchAlgorithmException | IOException excs) {
            ErrorHandler.report(excs);
        }
    }
    
}
