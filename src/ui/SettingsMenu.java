package ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.awt.Color;

import java.io.IOException;

import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import main.Controller;

import loaders.*;
import loaders.base.*;

public class SettingsMenu extends JFrame {
    private static JFrame self;
    private static JTextPane saveStatus;

    @SuppressWarnings("static-access")

    public SettingsMenu() {
        HashMap<String, Setting> setters = new HashMap<String, Setting>();

        JFrame window = new JFrame(Controller.title + " - Settings");
        window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final int x = 450;
        final int y = x + 40;

        window.setBounds(200, 400, x, y);
        window.setResizable(false);
        window.setPreferredSize(new Dimension(x, y));

        JPanel primary = new JPanel();
        primary.setBounds(0, 0, x, y - 100);
        primary.setLayout(null);
        primary.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));

        JTabbedPane tabbed = new JTabbedPane(JTabbedPane.LEFT);
        tabbed.setBounds(0, 0, primary.getWidth(), primary.getHeight());

        try {
            SearchSettings searchSettings = new SearchSettings();
            setters.put("Search", searchSettings);
            JPanel searchOptions = searchSettings.getSettingPanel(primary.getBounds());

            tabbed.addTab(searchSettings.getId().toString(), searchOptions);
        } catch (IOException io) {
            ErrorHandler.report(io);
        }

        tabbed.setVisible(true);

        primary.add(tabbed);

        JButton resetDefault = new JButton();
        resetDefault.setBounds(
            x/6 - 25,
            y - 97,
            90, 
            40
        );
        resetDefault.setText("Reset all");
        resetDefault.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setters.forEach((name, setter) -> {
                    try {
                        setter.resetToDefaults();
                        saveNotify(true);
                    } catch (IOException resetErr) {
                        ErrorHandler.report(resetErr);
                    }
                });
            }
        });

        JButton reset = new JButton();
        reset.setSize(resetDefault.getSize());
        reset.setLocation(resetDefault.getX() + resetDefault.getWidth() + 30, resetDefault.getY());
        reset.setText("Reset");
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String current = tabbed.getTitleAt(tabbed.getSelectedIndex());
                    Setting toReset = setters.get(current);
                    toReset.resetToDefaults();
                    saveNotify(true);
                } catch (IOException resErr) {
                    ErrorHandler.report(resErr);
                }
            }
        });

        JButton apply = new JButton();
        apply.setSize(resetDefault.getSize());
        apply.setLocation(reset.getX() + reset.getWidth() + 30, reset.getY());
        apply.setText("Apply");
        apply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setters.forEach((name, setter) -> setter.applyChanges(setter.getComponents()));
                saveNotify(true);
            }
        });

        JTextPane desc = new JTextPane();
        desc.setEditable(false);
        desc.setText("Good to go!");
        desc.setBounds(-1, y - 55, 95, 16);
        desc.setBackground(window.getBackground());
        desc.setFont(new Font(desc.getFont().getName(), Font.PLAIN, 10));
        desc.setHighlighter(null);
        desc.getCaret().deinstall(desc);
        
        saveStatus = desc;

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                MainWindow.toolbar.onSettingsExit();
            }
        });

        window.add(primary);
        window.add(resetDefault);
        window.add(reset);
        window.add(apply);
        window.add(desc);

        window.setLayout(null);

        window.setVisible(true);

        self = window;
    }

    public static void saveNotify(boolean saved) {
        if (saved) {
            self.setTitle(Controller.title + " - Settings");
            saveStatus.setText("Good to go!");
        } else {
            self.setTitle(Controller.title + " - Settings*");
            saveStatus.setText("Unsaved changes!");
        }
    }
}
