package ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import main.Controller;

public class SettingsMenu extends JFrame {
    @SuppressWarnings("static-access")

    public SettingsMenu() {
        JFrame window = new JFrame(Controller.title + " - Settings (Coming soon)");
        window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final int x = 450;
        final int y = x;

        window.setBounds(200, 400, x, y);
        window.setResizable(false);
        window.setPreferredSize(new Dimension(x, y));

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                MainWindow.toolbar.onSettingsExit();
            }
        });

        window.setLayout(null);

        window.setVisible(true);
    }
}
