package ui.gui.utilities;

import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.*;
import java.awt.Color;

import java.io.IOException;

import java.util.BitSet;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import main.Controller;

import misc.ExecuteCreateShortcut;

import ui.gui.err.ErrorHandler;
import ui.gui.main.MainWindow;

import loaders.*;
import loaders.base.*;

public class SettingsMenu extends JFrame {
    private static JFrame self;
    private static JTextPane saveStatus;
    
    public static SettingValidity state;

    public static class SettingValidity {
        LinkedHashMap<String, BitSet> states = new LinkedHashMap<>();

        public SettingValidity(LinkedHashMap<String, Setting> settingHashMap) {
            for (String name : settingHashMap.keySet()) {
                BitSet defaultBitSet = new BitSet(2);
                defaultBitSet.set(0, 2, true);
                /*
                1st bit: true if saved, false if unsaved
                2nd bit: true if setting configuration is valid, false if invalid
                */

                states.put(name, defaultBitSet);
            }
        }

        public void setSaveState(String settingName, boolean isSaved) {
            states.get(settingName).set(0, isSaved);
        }

        public void setValidityState(String settingName, boolean isValid) {
            states.get(settingName).set(1, isValid);
        }

        public void notifyUser() {
            BitSet finalState = new BitSet(2);
            finalState.set(0, 2, true);

            states.forEach((name, bitset) -> finalState.and(bitset));

            String title = String.format("%s - Settings%s", Controller.title, (finalState.cardinality() < 2) ? "*" : ""); // cardinality is how many bits are set
            self.setTitle(title);

            if (finalState.get(1))
                saveStatus.setText(finalState.get(0) ? "Good to go!" : "Unsaved changes!");
            else
                saveStatus.setText("Invalid configuration!");
        }
    }

    @SuppressWarnings("static-access")
    public SettingsMenu() {
        LinkedHashMap<String, Setting> setters = new LinkedHashMap<>();

        JFrame window = new JFrame(Controller.title + " - Settings");
        window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final int x = 450;
        final int y = x + 40;

        window.setBounds(200, 400, x, y);
        window.setResizable(false);
        window.setPreferredSize(new Dimension(x, y));

        final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

        JPanel primary = new JPanel();
        primary.setBounds(0, 0, x, y - 140);
        primary.setLayout(null);
        primary.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));

        JTabbedPane tabbed = new JTabbedPane(JTabbedPane.LEFT);
        tabbed.setBounds(0, 0, primary.getWidth(), primary.getHeight());

        try {
            SearchSettings searchSettings = new SearchSettings();
            setters.put("Search", searchSettings);

            DisplaySettings displaySettings = new DisplaySettings();
            setters.put("Display", displaySettings);

            AdvancedSettings advancedSettings = new AdvancedSettings();
            setters.put("Advanced", advancedSettings);

            setters.forEach((name, settingOption) -> tabbed.addTab(name, settingOption.getSettingPanel(primary.getBounds())));
        } catch (IOException io) {
            ErrorHandler.report(io);
        }

        state = new SettingValidity(setters);

        tabbed.setVisible(true);
        primary.add(tabbed);

        JButton resetDefault = new JButton();
        resetDefault.setBounds(x/6 - 25, y - 137, 90, 40);
        resetDefault.setCursor(handCursor);
        resetDefault.setText("Reset all");
        resetDefault.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setters.forEach((name, setter) -> {
                    try {
                        int idx = tabbed.indexOfTab(name);
                        setter.resetToDefaults();

                        state.setSaveState(name, true);
                        state.setValidityState(name, true);

                        tabbed.setForegroundAt(idx, tabbed.getForeground());
                    } catch (IOException resetErr) {
                        ErrorHandler.report(resetErr);
                    }
                });

                state.notifyUser();
            }
        });

        JButton reset = new JButton();
        reset.setSize(resetDefault.getSize());
        reset.setCursor(handCursor);
        reset.setLocation(resetDefault.getX() + resetDefault.getWidth() + 30, resetDefault.getY());
        reset.setText("Reset");
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int idx = tabbed.getSelectedIndex();

                    String current = tabbed.getTitleAt(idx);

                    Setting toReset = setters.get(current);
                    toReset.resetToDefaults();

                    state.setSaveState(current, true);
                    state.setValidityState(current, true);

                    tabbed.setForegroundAt(idx, tabbed.getForeground());

                    state.notifyUser();
                } catch (IOException resErr) {
                    ErrorHandler.report(resErr);
                }
            }
        });

        JButton apply = new JButton();
        apply.setSize(resetDefault.getSize());
        apply.setCursor(handCursor);
        apply.setLocation(reset.getX() + reset.getWidth() + 30, reset.getY());
        apply.setText("Apply");
        apply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean allSaved = true;

                final Color invalidColor = new Color(227, 57, 57);

                for (Setting setter : setters.values()) {
                    boolean isConfigValid = setter.applyChanges();
                    allSaved &= isConfigValid;

                    String tabName = setter.getId().toString();
                    int idx = tabbed.indexOfTab(tabName);

                    if (!isConfigValid) {
                        tabbed.setForegroundAt(idx, invalidColor);

                        state.setValidityState(tabName, false);
                    } else if (tabbed.getForegroundAt(idx).equals(invalidColor)) {
                        tabbed.setForegroundAt(idx, tabbed.getForeground());

                        state.setValidityState(tabName, true);
                    }

                    state.setSaveState(tabName, allSaved);
                }

                state.notifyUser();
            }
        });

        JButton shortcutCreate = new JButton();
        shortcutCreate.setSize(resetDefault.getSize());
        shortcutCreate.setCursor(handCursor);
        shortcutCreate.setLocation(resetDefault.getX(), resetDefault.getY() + resetDefault.getHeight() + 4);
        shortcutCreate.setText("Shortcut");
        shortcutCreate.setToolTipText("Creates a desktop shortcut to the jar file.");
        shortcutCreate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ExecuteCreateShortcut.RunScript();
            }
        });

        JTextPane desc = new JTextPane();
        desc.setEditable(false);
        desc.setText("Good to go!");
        desc.setBounds(-1, y - 55, 110, 16);
        desc.setBackground(window.getBackground());
        desc.setFont(new Font(desc.getFont().getName(), Font.PLAIN, 10));
        desc.setHighlighter(null);
        desc.getCaret().deinstall(desc);
        
        saveStatus = desc;

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                MainWindow.toolbar.onMenuExit("Settings");
            }
        });

        window.add(primary);
        window.add(resetDefault);
        window.add(reset);
        window.add(apply);
        window.add(shortcutCreate);
        window.add(desc);

        window.setLayout(null);

        window.setVisible(true);

        self = window;
    }
}
