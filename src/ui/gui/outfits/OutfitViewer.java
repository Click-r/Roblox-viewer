package ui.gui.outfits;

import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import classes.api.getAppearance;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.awt.Color;

import main.Controller;

public class OutfitViewer extends JFrame {
    private static boolean displayingInfo = false;

    private static JFrame build() {
        JFrame frame = new JFrame(Controller.title + " - Outfit Viewer");
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        int x,y;
        x = 800;
        y = 700;

        final int aX,aY;
        aX = 704;
        aY = 616;

        frame.setBounds(200, 200, x, y);
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(x,y));

        frame.setLayout(null);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                displayingInfo = false;
            }
        });

        return frame;
    }

    public static void search(long userId) {
        Map<String, Long> outfits = getAppearance.getOutfits(userId);

        outfits.forEach((name, id) -> System.out.println(name + "  :  " + id));
    }

    public static void display(long userId) {
        if (displayingInfo) {
            search(userId);
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                search(userId);
                displayingInfo = true;

                build();
            }
        });
    }
}
