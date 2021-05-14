package ui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import java.util.HashMap;

public class CreateToolBar {
    public HashMap<String, JButton> compDict = new HashMap<String, JButton>();
    public JToolBar bar;

    public CreateToolBar(JFrame target) {
        JToolBar toolbar = new JToolBar();

        JButton settings = new JButton("Settings");

        settings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SettingsMenu();

                settings.setEnabled(false);
            }
        });

        settings.setName("Settings");

        compDict.put(settings.getName(), settings);

        toolbar.add(settings);
        toolbar.setBounds(0, 0, target.getWidth(), 40);

        bar = toolbar;

        target.add(toolbar);
    }
}
