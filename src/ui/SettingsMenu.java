package ui;

import java.awt.Dimension;
import java.awt.event.*;
import java.awt.Color;

import java.io.IOException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import main.Controller;

import loaders.*;

public class SettingsMenu extends JFrame {
    @SuppressWarnings("static-access")

    public SettingsMenu() {
        HashMap<String, loaders.base.Setting> setters = new HashMap<String, loaders.base.Setting>();

        JFrame window = new JFrame(Controller.title + " - Settings (Coming soon)");
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
            setters.put("SearchSettings", searchSettings);
            JPanel searchOptions = searchSettings.getSettingPanel(primary.getBounds());

            tabbed.addTab(searchSettings.getId().toString(), searchOptions);
        } catch (IOException io) {
            ErrorHandler.report(io);
        }

        tabbed.setVisible(true);

        primary.add(tabbed);

        int combined = primary.getY() + primary.getHeight();

        JButton resetDefault = new JButton();
        resetDefault.setBounds(
            x/6 - 25,
            combined + ((y - combined) / 2) - 40,
            90, 
            40
        );
        resetDefault.setText("Reset all");
        resetDefault.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setters.forEach((name, setter) -> {
                    try {
                        setter.resetToDefaults();
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

        JButton apply = new JButton();
        apply.setSize(resetDefault.getSize());
        apply.setLocation(reset.getX() + reset.getWidth() + 30, reset.getY());
        apply.setText("Apply");
        apply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setters.forEach((name, setter) -> setter.applyChanges(setter.getComponents()));
            }
        });

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

        window.setLayout(null);

        window.setVisible(true);
    }
}
