package ui.gui.utilities;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import classes.Player;
import main.Controller;

public class SearchHistory extends JFrame {
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
        frame.setVisible(true);

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
        c.ipady = 0;
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
        delEntry.setCursor(buttonCursor);
        c.gridx = 1;
        c.gridy = 0;
        actionsPanel.add(delEntry, c);

        JButton searchEntry = new JButton("Search");
        searchEntry.setCursor(buttonCursor);
        c.gridx = 2;
        c.gridy = 0;
        actionsPanel.add(searchEntry, c);

        frame.pack();

        return frame;
    }

    public static void display() {
        build();
    }
}
