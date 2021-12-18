package ui.gui.outfits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import classes.Avatar;
import classes.Player;

import classes.api.getAppearance;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;

import main.Controller;

public class OutfitViewer extends JFrame {
    private static boolean displayingInfo = false;
    private static Player current;
    private static List<Avatar> outfits = new ArrayList<Avatar>();
    private static Map<String, JComponent> outfitComponents = new HashMap<String, JComponent>();


    private static JFrame build() {
        JFrame frame = new JFrame(Controller.title + " - Outfit Viewer");
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final Color bgcolour = new Color(238, 238, 238);
        
        int x,y;
        x = 800;
        y = 700;

        /*final int aX,aY;
        aX = 704;
        aY = 616;*/

        frame.setBounds(200, 200, x, y);
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(x,y));

        frame.setLayout(null);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                displayingInfo = false;
                outfits.clear();
                
                System.gc();
            }
        });

        JPanel outfitPanel = new JPanel();
        outfitPanel.setBounds(270, 0, x - 285, y);
        outfitPanel.setBackground(bgcolour);
        outfitPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));
        outfitPanel.setLayout(null);

        JTextPane outfitsTitle = new JTextPane();
        outfitsTitle.setEditable(false);
        outfitsTitle.setBackground(bgcolour);
        outfitsTitle.setBounds(2, 2, 270, 50);
        outfitsTitle.setText(String.format("%s's outfits (%d)", current.name, outfits.size()));
        outfitsTitle.setFont(new Font(outfitsTitle.getFont().getFontName(), outfitsTitle.getFont().getStyle(), 25));

        outfitComponents.put("title", outfitsTitle);

        outfitPanel.add(outfitsTitle);

        // TODO: make the card things for the outfits

        frame.add(outfitPanel);

        return frame;
    }

    public static void search(long userId) {
        Map<String, Long> outfitList = getAppearance.getOutfits(userId);

        long[] ids = new long[outfitList.size()];

        int i = 0;

        for (long id : outfitList.values()) {
            ids[i] = id;
            i++;
        }

        outfits = getAppearance.multiGetOutfits(ids);
    }

    public static void display(Player user) {
        current = user;

        if (displayingInfo) {
            search(user.id);

            ((JTextComponent) outfitComponents.get("title")).setText(String.format("%s's outfits (%d)", user.name, outfits.size()));

            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                search(user.id);
                displayingInfo = true;

                build(); // TODO: make gui
            }
        });
    }
}
