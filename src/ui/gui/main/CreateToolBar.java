package ui.gui.main;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import ui.gui.utilities.AboutMenu;
import ui.gui.utilities.SearchHistory;
import ui.gui.utilities.SettingsMenu;

import java.util.HashMap;

public class CreateToolBar {
    public HashMap<String, JButton> compDict = new HashMap<>();
    public JToolBar bar;

    public CreateToolBar(JFrame target) {
        JToolBar toolbar = new JToolBar();

        JButton settings = new JButton("Settings");
        JButton about = new JButton("About");
        JButton searchHistory = new JButton("Search History");

        settings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SettingsMenu();

                settings.setEnabled(false);
            }
        });

        about.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutMenu();

                about.setEnabled(false);
            }
        });

        searchHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SearchHistory.display();

                // TODO: do this one properly
            }
        });

        settings.setName("Settings");
        about.setName("About");
        searchHistory.setName("Search History");

        compDict.put(settings.getName(), settings);
        compDict.put(about.getName(), about);
        compDict.put(searchHistory.getName(), searchHistory);

        toolbar.add(settings);
        toolbar.addSeparator();
        toolbar.add(about);
        toolbar.addSeparator();
        toolbar.add(searchHistory);
        toolbar.setBounds(0, 0, target.getWidth(), 40);

        bar = toolbar;

        target.add(toolbar);
    }
}
