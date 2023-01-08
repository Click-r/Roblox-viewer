package ui.gui.utilities;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import java.io.File;

import java.util.Map;
import java.util.Deque;
import java.util.HashMap;

import classes.Player;
import classes.api.Cacher;

import main.Controller;

public class SearchHistory extends JFrame {
    private static boolean displaying = false;
    private static Map<String, JComponent> components = new HashMap<>();
    private static Player[] playerEntries = new Player[Cacher.maxEntries];
    private static JPanel selectedEntry = null;

    private static Color alternatingColor(int n) {
        if (n % 2 == 0)
            return new Color(245, 245, 245);
        else
            return new Color(238, 238, 238);
    }

    private static JPanel createUserEntry(Player user, int idx) {
        Color normalCol = alternatingColor(idx);

        JPanel toCreate = new JPanel(new GridBagLayout());
        toCreate.setBackground(normalCol);
        toCreate.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedEntry != null) {
                    String indexText = ((JLabel) selectedEntry.getComponent(0)).getText();
                    indexText = indexText.replace(".", "");

                    int idxPrevious = Integer.valueOf(indexText);
                    selectedEntry.setBackground(alternatingColor(idxPrevious));
                } else {
                    ((JButton) components.get("delEntry")).setEnabled(true);
                    ((JButton) components.get("searchEntry")).setEnabled(true);
                }
                
                selectedEntry = toCreate;
                selectedEntry.setBackground(new Color(154, 184, 211));

                JTextArea infoPreviewArea = (JTextArea) components.get("infoBox");

                String text = 
                String.format("Name:%s\nID:%d\nDisplay Name:%s\nFriends:%d\nFollowings:%d\nFollowers:%d\nCreated:%s\nBanned:%s\nLast Online:%s\n\nDescription:%s",
                          user.name, user.id, user.dispname, user.friends, user.followings, user.followers, user.created, user.banned, user.lastonline, user.description);
                
                infoPreviewArea.setText(text);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedEntry != toCreate) {
                    if (idx % 2 == 0)
                        toCreate.setBackground(new Color(147, 177, 204));
                    else
                        toCreate.setBackground(new Color(154, 184, 211));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedEntry != toCreate) {
                    toCreate.setBackground(normalCol);
                }
            }
        });

        GridBagConstraints c = new GridBagConstraints();

        JLabel index = new JLabel((idx + 1) + ".");
        index.setName("idx");
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        toCreate.add(index, c, 0);

        JLabel name = new JLabel(user.name);
        c.gridx = 1;
        toCreate.add(name, c, 1);

        JLabel id = new JLabel(String.valueOf(user.id));
        c.gridx = 2;
        toCreate.add(id, c, 2);

        toCreate.validate();

        return toCreate;
    }

    public static void lastPlayerRemoved() {
        if (displaying) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JPanel prevSearchesPanel = (JPanel) components.get("prevSearches");

                    if (prevSearchesPanel.getComponent(0) == selectedEntry) {
                        selectedEntry = null;
                        ((JTextArea) components.get("infoBox")).setText("");

                        ((JButton) components.get("delEntry")).setEnabled(false);
                        ((JButton) components.get("searchEntry")).setEnabled(false);
                    }

                    playerEntries[0] = null;
                    prevSearchesPanel.remove(0);
        
                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx = 0;
                    c.weightx = 1;
                    c.weighty = 0;
                    c.anchor = GridBagConstraints.NORTH;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.gridwidth = GridBagConstraints.REMAINDER;
                    c.gridheight = 1;
        
                    for (int i = 1; i <= playerEntries.length - 1; i++) {
                        playerEntries[i - 1] = playerEntries[i];
                        c.gridy = i - 1;

                        JPanel entry = (JPanel) prevSearchesPanel.getComponent(i - 1);
                        boolean isSelected = (entry == selectedEntry);

                        prevSearchesPanel.remove(entry);
                        
                        if (isSelected) 
                            entry.setBackground(new Color(154, 184, 211));
                        else
                            entry.setBackground(alternatingColor(i - 1));
                        ((JLabel) entry.getComponent(0)).setText(i + ".");

                        prevSearchesPanel.add(entry, c, i - 1);
                    }
        
                    prevSearchesPanel.revalidate();
                }
            });
        }
    }

    public static void playerAdded(Player added) {
        if (displaying) {
            int insertAt = Cacher.entries.size() - 1;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JPanel prevSearchesPanel = (JPanel) components.get("prevSearches");
                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx = 0;
                    c.gridy = insertAt;
                    c.weightx = 1;
                    c.weighty = 0;
                    c.anchor = GridBagConstraints.NORTH;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.gridwidth = GridBagConstraints.REMAINDER;
                    c.gridheight = 1;
        
                    playerEntries[insertAt] = added;
                    prevSearchesPanel.add(createUserEntry(added, insertAt), c, insertAt);
        
                    prevSearchesPanel.revalidate();
                }
            });
        }
    }

    private static JFrame build() {
        JFrame frame = new JFrame(Controller.title + " - Search History");
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final Color bgcolour = new Color(238, 238, 238);

        frame.setBounds(200, 200, 700, 500);
        frame.setPreferredSize(new Dimension(700, 500));
        frame.setMinimumSize(new Dimension(700, 500));
        frame.setResizable(true);
        frame.setBackground(bgcolour);
        frame.setLayout(new GridBagLayout());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                displaying = true;
            }

            @Override
            public void windowClosed(WindowEvent e) {
                displaying = false;
            }
        });

        GridBagConstraints c = new GridBagConstraints();

        JPanel historyPanel = new JPanel(new GridBagLayout());
        historyPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 0, 0)));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = GridBagConstraints.REMAINDER;
        frame.add(historyPanel, c);

        JPanel labelsPanel = new JPanel(new GridBagLayout());
        labelsPanel.setBackground(bgcolour);
        labelsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0)));
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        historyPanel.add(labelsPanel, c);

        JLabel noLabel = new JLabel("No.");
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 1;
        labelsPanel.add(noLabel, c);

        JLabel nameLabel = new JLabel("Name");
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 1;
        labelsPanel.add(nameLabel, c);

        JLabel idLabel = new JLabel("ID");
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridwidth = 1;
        labelsPanel.add(idLabel, c);

        JPanel searchesPanel = new JPanel(new GridBagLayout());
        searchesPanel.setBackground(new Color(245, 245, 245));
        components.put("prevSearches", searchesPanel);

        JPanel lastEntry = new JPanel(new GridBagLayout());
        lastEntry.setBackground(searchesPanel.getBackground());
        c.anchor = GridBagConstraints.SOUTH;
        c.gridx = 0;
        c.gridy = Cacher.maxEntries;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        searchesPanel.add(lastEntry, c, -1); // end component

        JScrollPane searchesScroll = new JScrollPane(searchesPanel);
        searchesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        searchesScroll.setBorder(null);
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        historyPanel.add(searchesScroll, c);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.VERTICAL;
        c.ipady = 0;
        c.ipadx = 0;
        frame.add(infoPanel, c);

        JPanel sortConfig = new JPanel(new GridBagLayout());
        sortConfig.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0)));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 0;
        infoPanel.add(sortConfig, c);

        String[] sortOptions = new String[]{"No.", "Name", "ID"};
        String[] orderOptions = new String[]{"Ascending", "Descending"};

        JLabel sortBy = new JLabel("Sort by:");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 4, 0);
        sortConfig.add(sortBy, c);

        JComboBox<String> sortSelect = new JComboBox<>(sortOptions);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        sortConfig.add(sortSelect, c);

        JLabel orderBy = new JLabel("Order:");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 0);
        sortConfig.add(orderBy, c);

        JComboBox<String> orderSelect = new JComboBox<>(orderOptions);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        sortConfig.add(orderSelect, c);

        JPanel cachedInfoPreview = new JPanel(new GridBagLayout());
        cachedInfoPreview.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0)));
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.VERTICAL;
        infoPanel.add(cachedInfoPreview, c);

        JLabel infoText = new JLabel("User Information (Cached at {date}):");
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        cachedInfoPreview.add(infoText, c);

        JTextArea infoPreview = new JTextArea(Player.class.getFields().length - 1, 28); // subtract 1 from player fields because image is transient
        infoPreview.setLineWrap(true);
        infoPreview.setWrapStyleWord(true);
        infoPreview.setEditable(false);
        components.put("infoBox", infoPreview);

        JScrollPane infoScroll = new JScrollPane(infoPreview);
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScroll.setBorder(null);
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.gridwidth = GridBagConstraints.REMAINDER;
        cachedInfoPreview.add(infoScroll, c);

        JPanel actionsPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.5;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(actionsPanel, c);

        final Cursor buttonCursor = new Cursor(Cursor.HAND_CURSOR);

        JButton clearHist = new JButton("Clear History");
        clearHist.setCursor(buttonCursor);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.ipady = 5;
        c.insets = new Insets(2, 2, 2, 2);
        actionsPanel.add(clearHist, c);

        JButton delEntry = new JButton("Delete Entry");
        delEntry.setEnabled(false);
        delEntry.setCursor(buttonCursor);
        c.gridx = 1;
        c.gridy = 0;
        actionsPanel.add(delEntry, c);
        components.put("delEntry", delEntry);

        JButton searchEntry = new JButton("Search");
        searchEntry.setEnabled(false);
        searchEntry.setCursor(buttonCursor);
        c.gridx = 2;
        c.gridy = 0;
        actionsPanel.add(searchEntry, c);
        components.put("searchEntry", searchEntry);

        frame.pack();

        return frame;
    }

    public static void display() {
        if (Controller.runningAsJar) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = build();

                    Deque<File> cacheEntries = Cacher.entries;
                    Cacher cache = new Cacher();
        
                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx = 0;
                    c.weightx = 1;
                    c.weighty = 0;
                    c.anchor = GridBagConstraints.NORTH;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.gridwidth = GridBagConstraints.REMAINDER;
                    c.gridheight = 1;
        
                    int idx = 0;
                    for (File plrFile : cacheEntries) {
                        c.gridy = idx;
        
                        Player player = (Player) cache.readPlayerObject(plrFile.getName());
                        JPanel entry = createUserEntry(player, idx);
        
                        components.get("prevSearches").add(entry, c, idx);
                        playerEntries[idx] = player;
        
                        idx++;
                    }
        
                    frame.pack();
                    frame.setVisible(true);
                }
            });
        }
    }
}
