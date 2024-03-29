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
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import classes.Avatar;
import classes.Player;
import classes.Avatar.Asset;
import classes.api.getAppearance;
import classes.Images;
import classes.Images.*;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;

import main.Controller;

import ui.gui.err.ErrorHandler;

public class OutfitViewer extends JFrame {
    private static JFrame window;
    private static boolean displayingInfo = false;
    private static Player current;
    private static List<Avatar> outfits = new ArrayList<>();
    private static List<JPanel> outfitCards = new ArrayList<>();
    private static List<JPanel> assetCards = new ArrayList<>();
    private static Map<String, JComponent> outfitComponents = new HashMap<>();
    private static Avatar viewing;
    private static Image reloadImg;
    public static ProgressBarManager progBar;

    public static class ProgressBarManager {
        private volatile JProgressBar bar;
        private volatile int progress = 0;
        private int maxVal = 0;

        public ProgressBarManager(JProgressBar progBar) {
            bar = progBar;

            progress = 0;
            bar.setValue(progress);
        }

        public void incProgress(int incBy) {
            progress += incBy;

            bar.setValue(progress);
        }

        public void setMax(int max) {
            maxVal = max;
            bar.setMaximum(maxVal);
        }

        public void finish() {
            bar.setVisible(false);
            bar.setValue(0);
        }
    }

    static {
        try {
            reloadImg = Images.getLocal(Local.REFRESH);
            reloadImg = reloadImg.getScaledInstance(20, 20, Image.SCALE_AREA_AVERAGING);
        } catch (IOException failedLoad) {
            ErrorHandler.report(failedLoad);
        }
    }

    private static void resetScrollbar(String scrollbarName) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ((JScrollPane) outfitComponents.get(scrollbarName)).getVerticalScrollBar().setValue(0);
            }
        });
    }

    private static String getMaxFitString(String unaltered, FontMetrics fontmetrics, int width, int rows) { // accounts for word wrapping with line wrapping
        String[] tokens = unaltered.split(" ");

        String toReturn = "";

        int row = 1;
        int spaceUsed = 0;

        final int spaceChr = fontmetrics.charWidth(' ');
        final int ellipsis = fontmetrics.stringWidth("...");

        int idx = 0;
        for (String token : tokens) {
            int size = fontmetrics.stringWidth(token) + spaceChr;

            if (spaceUsed + size < width) {
                String toAdd = (idx == 0) ? token : " " + token;

                toReturn += toAdd;
                spaceUsed += size;
            } else {
                if (size / width == 0) { // check the token isn't longer than 1 row
                    if (row < rows) {
                        toReturn += " " + token;

                        row++;
                        spaceUsed = size;
                    } else {
                        if (spaceUsed + ellipsis > width) {
                            int prevTokenLen = (" " + tokens[idx - 1]).length();
                            toReturn = toReturn.substring(0, toReturn.length() - prevTokenLen); // truncate to penultimate token
                        }
                        toReturn += "...";
                        // much more trivial when truncating to last token instead of trimming current token to size and then truncating
                        break;
                    }
                } else {
                    int rowsNeeded = size / width;

                    String toAdd = (idx == 0) ? token : " " + token;

                    if (row + rowsNeeded <= rows) {
                        int leftover = size - rowsNeeded * width;

                        toReturn += toAdd;
                        row += rowsNeeded;

                        spaceUsed = leftover;
                    } else {
                        int rowsLeft = (idx == 0) ? rows : rows - row;

                        int usableSpace = rowsLeft * (width - ellipsis);

                        int substrIdx = 0;
                        while (substrIdx <= toAdd.length() && fontmetrics.stringWidth(toAdd.substring(0, substrIdx)) < usableSpace - 2 * ellipsis)
                            substrIdx++;
                        // we give the ellipsis some leeway to make sure it still fits even if unicode characters are encountered

                        toReturn += toAdd.substring(0, substrIdx) + "...";
                        break;
                    }
                }
            }

            idx++;
        }

        return toReturn;
    }

    private static void updateNameId() {
        String id = "N/A";
        if (viewing.id != -1)
            id = String.valueOf(viewing.id);

        ((JTextArea) outfitComponents.get("outfitFullName")).setText(String.format("Outfit name: %s", viewing.name));
        ((JTextArea) outfitComponents.get("outfitID")).setText(String.format("Outfit ID: %s", id));
    }

    private static Image getEnlargedImage(int width) {
        String url = (String) viewing.image.getProperty("direct_url", null);

        if (url.startsWith("https://tr")) {
            String[] parts = url.split("/");
            parts[5] = parts[4] = "420"; // resize image in the direct link

            url = String.join("/", parts);
        } else {
            return viewing.image.getScaledInstance(width, width, Image.SCALE_SMOOTH); // return the error image upscaled
        }

        try {
            Image toReturn = Images.fetchImage(url);
            toReturn = toReturn.getScaledInstance(width, width, Image.SCALE_AREA_AVERAGING);

            return toReturn;
        } catch (IOException e) {}

        return null;
    }

    private static JPanel generateOutfitCard(Avatar outfit, boolean setimage) {
        if (setimage || outfit.image == null)
            outfit.setImage();
        
        if (outfit.name.isEmpty()) { // if internal server error occured
            long outfitId = outfit.id;

            Map<String, Long> outfitList = getAppearance.getOutfits(current.id);

            outfitList.forEach((name, id) -> {
                if (id == outfitId) {
                    outfit.name = name;
                    return;
                }
            });
        }

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

        Image scaled = outfit.image.getScaledInstance(card.getWidth() - 2, card.getWidth() - 2, Image.SCALE_AREA_AVERAGING);

        JLayeredPane imageSection = new JLayeredPane();
        imageSection.setBounds(outfitName.getX() + 1, outfitName.getY() + outfitName.getHeight(), card.getWidth() - 2, card.getWidth() - 2);
        imageSection.setLayout(null);

        JLabel outfitImage = new JLabel(new ImageIcon(scaled));
        outfitImage.setBounds(0, 0, imageSection.getWidth(), imageSection.getHeight());

        JButton reloadOutfit = new JButton(new ImageIcon(reloadImg));
        reloadOutfit.setSize(30, 30);
        reloadOutfit.setBackground(new Color(235, 235, 235));
        reloadOutfit.setLocation(outfitImage.getWidth() - reloadOutfit.getWidth() - 5, 5);
        reloadOutfit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reloadOutfit.setToolTipText("Reloads the outfit.");
        reloadOutfit.setVisible(false);
        reloadOutfit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int outfitIdx = outfits.indexOf(outfit);

                Avatar reloaded = new Avatar(outfit.id);
                reloaded.setImage(); // 1st reload

                outfits.set(outfitIdx, reloaded);

                JPanel outfitPanel = (JPanel) outfitComponents.get("outfitPanel");
                
                int cardIdx = outfitCards.indexOf(card);
                JPanel newCard = generateOutfitCard(reloaded, true); // 2nd reload - reloading the image a second time if it fails to load on the first

                Point oldPos = card.getLocation();
                newCard.setLocation(oldPos);

                outfitPanel.remove(card);
                outfitCards.set(cardIdx, newCard);
                outfitPanel.add(newCard);
                outfitPanel.repaint();

                if (viewing.equals(outfit))
                    updateViewing(reloaded);
            }
        });

        imageSection.add(reloadOutfit, 0);
        imageSection.add(outfitImage, 1);

        MouseAdapter reloadEnabler = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                reloadOutfit.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                reloadOutfit.setVisible(false);
            }
        };

        outfitImage.addMouseListener(reloadEnabler);
        reloadOutfit.addMouseListener(reloadEnabler);

        JButton viewDetails = new JButton("Further details");
        viewDetails.setBounds(outfitName.getX(), outfitName.getY() + outfitName.getHeight() + 159, card.getWidth(), 39);
        viewDetails.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewDetails.addActionListener(new ActionListener() { // view details of outfit
            @Override
            public void actionPerformed(ActionEvent e) {
                updateViewing(outfit);
            }
        });

        card.add(outfitName);
        card.add(viewDetails);
        card.add(imageSection);

        return card;
    }

    private static JPanel generateAssetCard(Asset asset) {
        JPanel card = new JPanel();
        card.setSize(115, 150);
        card.setLayout(null);
        card.setBackground(new Color(218, 218, 218));
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));

        JTextArea assetName = new JTextArea();
        assetName.setBounds(0, 0, card.getWidth(), 36);
        assetName.setLineWrap(true);
        assetName.setWrapStyleWord(true);
        assetName.setEditable(false);
        assetName.setBackground(new Color(225, 225, 225));
        assetName.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(0, 0, 0)));
        String toDisplay = getMaxFitString(asset.name, assetName.getFontMetrics(assetName.getFont()), card.getWidth(), 2);
        if (toDisplay.endsWith("..."))
            assetName.setToolTipText(asset.name);
        
        assetName.setText(toDisplay);

        JButton clickableImage = new JButton(new ImageIcon(asset.image.getScaledInstance(113, 113, Image.SCALE_AREA_AVERAGING)));
        clickableImage.setBounds(1, assetName.getY() + assetName.getHeight(), 113, 113);
        clickableImage.setBorder(null);
        clickableImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clickableImage.setContentAreaFilled(false);
        clickableImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    URI link = new URI("https://www.roblox.com/catalog/" + asset.id + "/");

                    Desktop dsk = Desktop.getDesktop();
            
                    if (Desktop.isDesktopSupported() && dsk.isSupported(Desktop.Action.BROWSE))
                        dsk.browse(link);
                } catch (IOException | URISyntaxException exc) {
                    System.out.printf("Couldn't open catalog page for asset ID: %d\n", asset.id);
                }
            }
        });

        card.add(assetName);
        card.add(clickableImage);

        return card;
    }

    private static List<JPanel> generateAssetCards() {
        long[] ids = new long[viewing.assets.size()];
        for (int i = 0; i < viewing.assets.size(); i++)
            ids[i] = viewing.assets.get(i).id;
        
        Image[] assetImages = getAppearance.batchGetAssetThumbnails(ids);
        for (int j = 0; j < viewing.assets.size(); j++)
            viewing.assets.get(j).image = assetImages[j];
        
        for (Asset accessory : viewing.assets)
            assetCards.add(generateAssetCard(accessory));
        
        return assetCards;
    }

    private static void updateAssetCards() {
        JPanel appendTo = (JPanel) outfitComponents.get("assetBoxes");
        appendTo.removeAll();
        assetCards.clear();

        assetCards = generateAssetCards();

        int xDistBetweenCards = 11;
        int yDistBetweenCards = 2;
        int cardsPerRow = 2;

        int rows = assetCards.size() / cardsPerRow;
        if ((assetCards.size() % cardsPerRow) > 0)
            rows++;

        int increaseBy = rows * (yDistBetweenCards + 150) + 43;
        appendTo.setSize(new Dimension(appendTo.getWidth(), increaseBy));

        JPanel container = (JPanel) outfitComponents.get("colourAssetContainer");
        container.setPreferredSize(new Dimension(container.getWidth(), 468 + 2 + increaseBy));

        JPanel assetContainer = (JPanel) outfitComponents.get("assetSection");
        assetContainer.setSize(new Dimension(assetContainer.getWidth(), 44 + increaseBy));

        for (int i = 0; i < assetCards.size(); i++) {
            int yCoordinate = 2 + (i / cardsPerRow) * (yDistBetweenCards + 150);
            int xCoordinate = 6 + (i % cardsPerRow) * (xDistBetweenCards + 115);

            JPanel card = assetCards.get(i);
            card.setLocation(xCoordinate, yCoordinate);

            appendTo.add(card);
        }

        appendTo.repaint();
    }

    private static void setOutfitImages() {
        long[] ids = new long[outfits.size()];
        for (int i = 0; i < outfits.size(); i++)
            ids[i] = outfits.get(i).id;
        
        Image[] outfitImages = getAppearance.batchGetOutfitThumbnails(ids, progBar::incProgress);
        for (int j = 0; j < outfits.size(); j++)
            outfits.get(j).setImage(outfitImages[j]);
    }

    private static List<JPanel> generateOutfitCards() {
        for (Avatar outfit : outfits)
            outfitCards.add(generateOutfitCard(outfit, false));
        
        return outfitCards;
    }

    private static void updateOutfitCards() {
        JPanel appendTo = (JPanel) outfitComponents.get("outfitPanel");
        appendTo.removeAll();
        outfitCards.clear();
        
        outfitCards = generateOutfitCards();

        int xDistBetweenCards = 4;
        int yDistBetweenCards = 6;
        int cardsPerRow = 3;

        int rows = outfitCards.size() / cardsPerRow;
        if ((outfitCards.size() % cardsPerRow) > 0)
            rows++;

        appendTo.setPreferredSize(new Dimension(
            appendTo.getWidth(), // the width is already initialized in the build() method
            rows * (yDistBetweenCards + 230) // adjust size based on how many outfits there are
        ));
        
        for (int i = 0; i < outfitCards.size(); i++) {
            int yCoordinate = 2 + (i / cardsPerRow) * (yDistBetweenCards + 230);
            int xCoordinate = 4 + (i % cardsPerRow) * (xDistBetweenCards + 160);

            JPanel card = outfitCards.get(i);
            card.setLocation(xCoordinate, yCoordinate);

            appendTo.add(card);
        }

        appendTo.repaint();
    }

    private static void updateColours() {
        viewing.bodycolours.forEach((bodyPart, colourInfo) -> outfitComponents.get(bodyPart).setBackground(colourInfo.getValue()));
    }

    private static void updateViewing(Avatar outfit) {
        viewing = outfit;

        ((JLabel) outfitComponents.get("largeImg")).setIcon(new ImageIcon(getEnlargedImage(270)));
        
        updateAssetCards();
        updateColours();
        updateNameId();

        resetScrollbar("infoScrollbar");
    }

    private static JFrame build() {
        JFrame frame = new JFrame(Controller.title + " - Outfit Viewer");
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        window = frame;

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
                outfitCards.clear();
                assetCards.clear();

                window = null;
                System.gc();
            }
        });

        //------------------------------------------------------Details section------------------------------------------------------//
        JPanel infoPanel = new JPanel();
        infoPanel.setBounds(0, 0, 270, y);
        infoPanel.setBackground(bgcolour);
        infoPanel.setLayout(null);

        JTextPane infoTitle = new JTextPane();
        infoTitle.setEditable(false);
        infoTitle.setBackground(bgcolour);
        infoTitle.setBounds(2, 2, infoPanel.getWidth() - 2, 51);
        infoTitle.setText("Details");
        infoTitle.setFont(new Font(infoTitle.getFont().getFontName(), infoTitle.getFont().getStyle(), 25));
        infoTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0, 0, 0)));

        JPanel imageContainer = new JPanel();
        imageContainer.setBounds(infoTitle.getX() - 2, infoTitle.getY() + infoTitle.getHeight() - 1, infoPanel.getWidth(), infoPanel.getWidth());
        imageContainer.setBackground(new Color(218, 218, 218));
        imageContainer.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0, 0, 0)));
        imageContainer.setLayout(null);

        ImageIcon icon = new ImageIcon(getEnlargedImage(infoPanel.getWidth()));
        JLabel enlargedImage = new JLabel(icon);
        enlargedImage.setSize(imageContainer.getSize());
        outfitComponents.put("largeImg", enlargedImage);

        imageContainer.add(enlargedImage);

        JPanel colourAssetContainer = new JPanel();
        colourAssetContainer.setBounds(
            0,
            imageContainer.getY() + imageContainer.getHeight(),
            infoPanel.getWidth(),
            infoPanel.getHeight() - (imageContainer.getY() + imageContainer.getHeight())
        );
        colourAssetContainer.setLayout(null);
        outfitComponents.put("colourAssetContainer", colourAssetContainer);

        JScrollPane infoScrollBar = new JScrollPane(colourAssetContainer, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScrollBar.setLocation(colourAssetContainer.getLocation());
        infoScrollBar.setSize(new Dimension(colourAssetContainer.getWidth(), colourAssetContainer.getHeight() - 38));
        infoScrollBar.getVerticalScrollBar().setUnitIncrement(16);
        infoScrollBar.setBorder(null);
        outfitComponents.put("infoScrollbar", infoScrollBar);

        //--------------------------------------------------------Body colours--------------------------------------------------------//
        JTextPane colourSubTitle = new JTextPane();
        colourSubTitle.setEditable(false);
        colourSubTitle.setBackground(bgcolour);
        colourSubTitle.setBounds(2, 0, infoScrollBar.getWidth() - 17, 40);
        colourSubTitle.setText("Colours");
        colourSubTitle.setFont(new Font(colourSubTitle.getFont().getFontName(), colourSubTitle.getFont().getStyle(), 22));

        JLayeredPane colourDisplay = new JLayeredPane();
        colourDisplay.setBounds(
            0,
            colourSubTitle.getY() + colourSubTitle.getHeight() + 1,
            infoScrollBar.getWidth() - 17,
            colourAssetContainer.getHeight() - (colourSubTitle.getY() + colourSubTitle.getHeight() + 1)
        );
        colourDisplay.setLayout(null);

        int halfway = (colourDisplay.getWidth() / 2);

        JPanel colourInfoBox = new JPanel();
        colourInfoBox.setBounds(0, 0, 100, 45);
        colourInfoBox.setVisible(false);
        colourInfoBox.setBackground(new Color(235, 235, 235));
        colourInfoBox.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));
        colourInfoBox.setLayout(null);

        JTextPane colName = new JTextPane();
        colName.setEditable(false);
        colName.setBackground(colourInfoBox.getBackground());
        colName.setBounds(1, 1, colourInfoBox.getWidth() - 2, 19);
        colName.setFont(new Font(colName.getFont().getFontName(), colName.getFont().getStyle(), 9));
        colName.setText("test");

        StyledDocument doc = colName.getStyledDocument();
        SimpleAttributeSet setAttrCenter = new SimpleAttributeSet();
        StyleConstants.setAlignment(setAttrCenter, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), setAttrCenter, false); // center text

        JTextPane rgbVal = new JTextPane();
        rgbVal.setEditable(false);
        rgbVal.setBackground(colourInfoBox.getBackground());
        rgbVal.setBounds(1, colName.getHeight() + 5, colourInfoBox.getWidth() - 2, 19);
        rgbVal.setFont(new Font(rgbVal.getFont().getFontName(), rgbVal.getFont().getStyle(), 9));
        rgbVal.setText("test");
        
        doc = rgbVal.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), setAttrCenter, false);

        colourInfoBox.add(colName);
        colourInfoBox.add(rgbVal);

        MouseMotionListener displayInfoBox = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point srcLocation = e.getComponent().getLocation();
                Point newPos = new Point(
                    e.getX() + (int) srcLocation.getX() + 1,
                    e.getY() + (int) srcLocation.getY() + 1
                );

                if (newPos.x + colourInfoBox.getWidth() - colourDisplay.getWidth() >= 0)
                    newPos.translate((int) -colourInfoBox.getWidth() - 1, -1);

                colourInfoBox.setLocation(newPos);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                this.mouseDragged(e);
            }
        };

        MouseAdapter signalEnter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                String bodypartName = e.getComponent().getName();
                String colourName = viewing.bodycolours.get(bodypartName).getKey();
                Color partColour = e.getComponent().getBackground();
                String rgbRep = String.format("RGB(%d, %d, %d)", partColour.getRed(), partColour.getGreen(), partColour.getBlue());

                colName.setText(colourName);
                rgbVal.setText(rgbRep);

                colourInfoBox.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                colourInfoBox.setVisible(false);
            }
        };

        JPanel bodyPanel = new JPanel();
        bodyPanel.setBounds(0, 0, colourDisplay.getWidth(), colourDisplay.getHeight());
        bodyPanel.setLayout(null);

        JPanel head = new JPanel();
        head.setBounds(halfway - 27, 8, 54, 54);
        head.setBackground(new Color(0, 0, 0));
        head.addMouseMotionListener(displayInfoBox);
        head.addMouseListener(signalEnter);
        head.setName("head");

        JPanel torso = new JPanel();
        torso.setBounds(halfway - 49, head.getY() + head.getHeight() + 2, 98, 110);
        torso.setBackground(new Color(0, 0, 0));
        torso.addMouseMotionListener(displayInfoBox);
        torso.addMouseListener(signalEnter);
        torso.setName("torso");

        JPanel leftArm = new JPanel();
        leftArm.setBounds(torso.getX() - 2 - 47, torso.getY(), 47, torso.getHeight());
        leftArm.setBackground(new Color(0, 0, 0));
        leftArm.addMouseMotionListener(displayInfoBox);
        leftArm.addMouseListener(signalEnter);
        leftArm.setName("leftArm");

        JPanel rightArm = new JPanel();
        rightArm.setBounds(torso.getX() + torso.getWidth() + 2, torso.getY(), 47, torso.getHeight());
        rightArm.setBackground(new Color(0, 0, 0));
        rightArm.addMouseMotionListener(displayInfoBox);
        rightArm.addMouseListener(signalEnter);
        rightArm.setName("rightArm");

        JPanel leftLeg = new JPanel();
        leftLeg.setBounds(torso.getX(), torso.getY() + torso.getHeight() + 2, 48, torso.getHeight());
        leftLeg.setBackground(new Color(0, 0, 0));
        leftLeg.addMouseMotionListener(displayInfoBox);
        leftLeg.addMouseListener(signalEnter);
        leftLeg.setName("leftLeg");

        JPanel rightLeg = new JPanel();
        rightLeg.setBounds(leftLeg.getX() + leftLeg.getWidth() + 2, leftLeg.getY(), 48, torso.getHeight());
        rightLeg.setBackground(new Color(0, 0, 0));
        rightLeg.addMouseMotionListener(displayInfoBox);
        rightLeg.addMouseListener(signalEnter);
        rightLeg.setName("rightLeg");

        bodyPanel.add(head);
        bodyPanel.add(torso);
        bodyPanel.add(leftArm);
        bodyPanel.add(rightArm);
        bodyPanel.add(leftLeg);
        bodyPanel.add(rightLeg);

        MouseMotionListener colourInfoListener = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                this.mouseMoved(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                colourInfoBox.setVisible(true);

                Point entry = e.getComponent().getLocation();
                entry.translate(e.getX(), e.getY());

                JComponent comp = (JComponent) bodyPanel.getComponentAt(entry);

                if (comp.getName() != null)
                    comp.getMouseMotionListeners()[0].mouseMoved(e); // body part panels are guaranteed to have 1 mouse motion listener
                else
                    colourInfoBox.setVisible(false);
            }
        };

        colourInfoBox.addMouseMotionListener(colourInfoListener);
        colName.addMouseMotionListener(colourInfoListener);
        rgbVal.addMouseMotionListener(colourInfoListener);

        outfitComponents.put("head", head);
        outfitComponents.put("torso", torso);
        outfitComponents.put("leftArm", leftArm);
        outfitComponents.put("rightArm", rightArm);
        outfitComponents.put("leftLeg", leftLeg);
        outfitComponents.put("rightLeg", rightLeg);

        colourDisplay.add(bodyPanel, 1);
        colourDisplay.add(colourInfoBox, 0);

        //---------------------------------------------------------Name & ID---------------------------------------------------------//
        JPanel nameIdSection = new JPanel();
        nameIdSection.setLayout(null);
        nameIdSection.setBounds(colourDisplay.getX(), colourDisplay.getY() + colourDisplay.getHeight(), colourDisplay.getWidth(), 90);

        JTextArea outfitFullName = new JTextArea();
        outfitFullName.setEditable(false);
        outfitFullName.setBackground(bgcolour);
        outfitFullName.setWrapStyleWord(true);
        outfitFullName.setLineWrap(true);
        outfitFullName.setText("Outfit name: Currently Wearing");
        outfitFullName.setBounds(2, 2, colourAssetContainer.getWidth() - 19, 45);
        outfitComponents.put("outfitFullName", outfitFullName);

        JTextArea outfitID = new JTextArea();
        outfitID.setEditable(false);
        outfitID.setBackground(bgcolour);
        outfitID.setText("Outfit ID: N/A");
        outfitID.setBounds(outfitFullName.getX(), outfitFullName.getY() + outfitFullName.getHeight() + 10, outfitFullName.getWidth(), 20);
        outfitComponents.put("outfitID", outfitID);

        nameIdSection.add(outfitFullName);
        nameIdSection.add(outfitID);

        //----------------------------------------------------------Assets----------------------------------------------------------//
        JPanel assetSection = new JPanel();
        assetSection.setLayout(null);
        assetSection.setBounds(0, nameIdSection.getY() + nameIdSection.getHeight(), colourDisplay.getWidth(), 409);
        assetSection.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0, 0, 0)));
        outfitComponents.put("assetSection", assetSection);
        
        JTextPane assetSubTitle = new JTextPane();
        assetSubTitle.setEditable(false);
        assetSubTitle.setBackground(bgcolour);
        assetSubTitle.setBounds(2, 2, colourSubTitle.getWidth() - 2, 40);
        assetSubTitle.setText("Assets");
        assetSubTitle.setFont(new Font(assetSubTitle.getFont().getFontName(), assetSubTitle.getFont().getStyle(), 22));

        JPanel assetBoxes = new JPanel();
        assetBoxes.setLayout(null);
        assetBoxes.setBounds(0, assetSubTitle.getY() + assetSubTitle.getHeight(), assetSection.getWidth(), assetSection.getHeight() - assetSubTitle.getHeight());
        outfitComponents.put("assetBoxes", assetBoxes);

        assetSection.add(assetSubTitle);
        assetSection.add(assetBoxes);

        colourAssetContainer.add(colourSubTitle);
        colourAssetContainer.add(colourDisplay);
        colourAssetContainer.add(nameIdSection);
        colourAssetContainer.add(assetSection);

        infoPanel.add(infoTitle);
        infoPanel.add(imageContainer);
        infoPanel.add(infoScrollBar);

        updateColours();
        updateAssetCards();

        //----------------------------------------------------Outfit cards section----------------------------------------------------//
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
        outfitComponents.put("outfitScrollbar", outfitScroll);

        updateOutfitCards();

        outfitPanel.add(outfitsTitle);
        outfitPanel.add(outfitScroll);

        resetScrollbar("outfitScrollbar");

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
        
        progBar.setMax(outfitList.size() * 2); // gather the outfits + images for outfits = 2 * #outfits

        outfits = getAppearance.multiGetOutfits(ids, progBar::incProgress);
        setOutfitImages();
    }

    public static void display(Player user) {
        current = user;
        System.gc();

        SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() { // executes these on a background thread, asynchronously
            @Override
            protected Void doInBackground() throws Exception {
                if (displayingInfo) {
                    viewing = user.getAppearance();
        
                    search(user.id);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ((JLabel) outfitComponents.get("largeImg")).setIcon(new ImageIcon(getEnlargedImage(270)));

                            updateOutfitCards();
                            updateAssetCards();
                            updateColours();
                            updateNameId();
                
                            ((JTextComponent) outfitComponents.get("title")).setText(String.format("%s's outfits (%d)", user.name, outfits.size()));
                
                            resetScrollbar("infoScrollbar");
                            resetScrollbar("outfitScrollbar");
                
                            window.requestFocus();
                        }
                    });

                    return null;
                }

                displayingInfo = true;

                viewing = user.getAppearance();
                search(user.id);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        build();
                        resetScrollbar("infoScrollbar");
                    }
                });
                
                return null;
            }

            @Override
            public void done() {
                progBar.finish();
            }
        };

        swingWorker.execute();
    }
}
