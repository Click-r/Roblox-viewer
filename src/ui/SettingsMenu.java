package ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import main.Controller;

import loaders.*;

public class SettingsMenu extends JFrame {
    @SuppressWarnings("static-access")

    public SettingsMenu() {
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

        JTabbedPane tabbed = new JTabbedPane(JTabbedPane.LEFT);
        tabbed.setBounds(0, 0, primary.getWidth(), primary.getHeight());

        try {
            SearchSettings searchSettings = new SearchSettings();
            JPanel searchOptions = searchSettings.getSettingPanel(primary.getBounds());

            tabbed.addTab(searchSettings.getId().toString(), searchOptions);
        } catch (IOException io) {
            ErrorHandler.report(io);
        }

        tabbed.setVisible(true);

        primary.add(tabbed);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                MainWindow.toolbar.onSettingsExit();
            }
        });

        window.add(primary);

        window.setLayout(null);

        window.setVisible(true);
    }
}
