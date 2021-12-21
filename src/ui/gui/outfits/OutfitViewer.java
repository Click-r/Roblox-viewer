package ui.gui.outfits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import classes.Avatar;
import classes.Link;
import classes.Player;

import classes.api.getAppearance;

import java.awt.Image;

import java.io.IOException;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;

import main.Controller;

import ui.gui.err.ErrorHandler;

public class OutfitViewer extends JFrame {
    private static boolean displayingInfo = false;
    private static Player current;
    private static List<Avatar> outfits = new ArrayList<Avatar>();
    private static List<JPanel> cards = new ArrayList<JPanel>();
    private static Map<String, JComponent> outfitComponents = new HashMap<String, JComponent>();

    private static Image getEnlargedImage(int width) {
        String url = (String) current.image.getProperty("direct_url", null);

        if (url.startsWith("https://tr")) {
            String[] parts = url.split("/");
            parts[5] = parts[4] = "420"; // resize image in the direct link

            url = String.join("/", parts);
        } else {
            return current.image.getScaledInstance(width, width, Image.SCALE_SMOOTH);
        }

        try {
            Image toReturn = new Link(url, false).getImage();
            toReturn = toReturn.getScaledInstance(width, width, Image.SCALE_AREA_AVERAGING);

            return toReturn;
        } catch (IOException e) {
            ErrorHandler.report(e, current);
        }

        return null;
    }

    private static JPanel generateOutfitCard(Avatar outfit, boolean setimage) {
        if (setimage)
            outfit.setImage();

        StringBuilder name = new StringBuilder(outfit.name);
        name.setLength(40);

        if (outfit.name.length() > 40)
            name.append("...");

        JPanel card = new JPanel();
        card.setSize(160, 230);
        card.setBackground(new Color(218, 218, 218));
        card.setLayout(null);
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));

        JTextArea outfitName = new JTextArea();
        outfitName.setBounds(0, 0, card.getWidth(), 32);
        outfitName.setLineWrap(true);
        outfitName.setWrapStyleWord(true);
        outfitName.setEditable(false);
        outfitName.setText(name.toString());
        outfitName.setBackground(new Color(225, 225, 225));
        outfitName.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(0, 0, 0)));

        Image scaled = outfit.outfitThumbnail.getScaledInstance(card.getWidth() - 2, card.getWidth() - 2, Image.SCALE_AREA_AVERAGING);

        JLabel outfitImage = new JLabel(new ImageIcon(scaled));
        outfitImage.setBounds(outfitName.getX() + 1, outfitName.getY() + outfitName.getHeight(), card.getWidth() - 2, card.getWidth() - 2);

        JButton viewDetails = new JButton("Further details");
        viewDetails.setBounds(outfitName.getX(), outfitName.getY() + outfitName.getHeight() + 159, card.getWidth(), 39);

        card.add(outfitName);
        card.add(viewDetails);
        card.add(outfitImage);

        return card;
    }

    private static List<JPanel> generateCards() {
        long[] ids = new long[outfits.size()];
        for (int i = 0; i < outfits.size(); i++)
            ids[i] = outfits.get(i).id;

        Image[] outfitImages = getAppearance.batchGetOutfitThumbnails(ids);
        for (int j = 0; j < outfits.size(); j++)
            outfits.get(j).setImage(outfitImages[j]);

        for (Avatar outfit : outfits)
            cards.add(generateOutfitCard(outfit, false));
        
        return cards;
    }

    private static void updateCards() {
        JPanel appendTo = (JPanel) outfitComponents.get("outfitPanel");
        appendTo.removeAll();
        cards.clear();
        
        cards = generateCards();

        int xDistBetweenCards = 4;
        int yDistBetweenCards = 6;
        int cardsPerRow = 3;

        int rows = (cards.size() / cardsPerRow);
        if ((cards.size() % cardsPerRow) > 0)
            rows++;

        appendTo.setPreferredSize(new Dimension(
            appendTo.getWidth(), // the width is already initialized in the build() method
            rows * (yDistBetweenCards + 230) // adjust size based on how many outfits there are
        ));
        
        for (int i = 0; i < cards.size(); i++) {
            int yCoordinate = 2 + (i / cardsPerRow) * (yDistBetweenCards + 230);
            int xCoordinate = 4 + (i % cardsPerRow) * (xDistBetweenCards + 160);

            JPanel card = cards.get(i);
            card.setLocation(xCoordinate, yCoordinate);

            appendTo.add(card);
        }

        appendTo.repaint();
    }

    private static JFrame build() {
        JFrame frame = new JFrame(Controller.title + " - Outfit Viewer");
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final Color bgcolour = new Color(238, 238, 238);
        
        int x,y;
        x = 800;
        y = 700;

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
                cards.clear();
                
                System.gc();
            }
        });

        JPanel infoPanel = new JPanel();
        infoPanel.setBounds(0, 0, 270, y);
        infoPanel.setBackground(bgcolour);
        infoPanel.setLayout(null);

        JTextPane infoTitle = new JTextPane();
        infoTitle.setEditable(false);
        infoTitle.setBackground(bgcolour);
        infoTitle.setBounds(2, 2, infoPanel.getWidth(), 51);
        infoTitle.setText("Details");
        infoTitle.setFont(new Font(infoTitle.getFont().getFontName(), infoTitle.getFont().getStyle(), 25));
        infoTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0)));

        JPanel imageContainer = new JPanel();
        imageContainer.setBounds(infoTitle.getX() - 2, infoTitle.getY() + infoTitle.getHeight(), infoPanel.getWidth(), infoPanel.getWidth());
        imageContainer.setBackground(new Color(218, 218, 218));
        imageContainer.setLayout(null);

        ImageIcon icon = new ImageIcon(getEnlargedImage(infoPanel.getWidth()));
        JLabel enlargedImage = new JLabel(icon);
        enlargedImage.setSize(imageContainer.getSize());

        imageContainer.add(enlargedImage);


        infoPanel.add(imageContainer);
        infoPanel.add(infoTitle);

        JPanel outfitPanel = new JPanel();
        outfitPanel.setBounds(270, 0, x - 285, y);
        outfitPanel.setBackground(bgcolour);
        outfitPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));
        outfitPanel.setLayout(null);

        JTextPane outfitsTitle = new JTextPane();
        outfitsTitle.setEditable(false);
        outfitsTitle.setBackground(bgcolour);
        outfitsTitle.setBounds(2, 2, 550, 50);
        outfitsTitle.setText(String.format("%s's outfits (%d)", current.name, outfits.size()));
        outfitsTitle.setFont(new Font(outfitsTitle.getFont().getFontName(), outfitsTitle.getFont().getStyle(), 25));

        outfitComponents.put("title", outfitsTitle);

        int yDistBetweenCards = 6;

        JPanel outfitView = new JPanel();
        outfitView.setLayout(null);
        outfitView.setBounds(outfitsTitle.getX() - 1, outfitsTitle.getY() + outfitsTitle.getHeight(), outfitPanel.getWidth(), 17 * (yDistBetweenCards + 230));
        outfitComponents.put("outfitPanel", outfitView);

        JScrollPane outfitScroll = new JScrollPane(outfitView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outfitScroll.setBounds(outfitView.getX(), outfitView.getY(), outfitView.getWidth(), outfitPanel.getHeight() - 90);
        outfitScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0, 0, 0)));
        outfitScroll.getVerticalScrollBar().setUnitIncrement(16);

        updateCards();

        outfitPanel.add(outfitsTitle);
        outfitPanel.add(outfitScroll);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                outfitScroll.getVerticalScrollBar().setValue(0); // scroll to the very top
            }
        });

        frame.add(infoPanel);
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
            updateCards();

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
