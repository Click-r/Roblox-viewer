package ui.gui.main;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.Cursor;
import java.awt.Image;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.lang.reflect.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import classes.*;
import classes.api.getAppearance;
import classes.Images;
import classes.Images.*;

import loaders.*;
import loaders.Themes.*;

import main.Controller;

import ui.gui.err.ErrorHandler;
import ui.gui.outfits.OutfitViewer;

public class MainWindow {
    private static String lastModifed;
    private static Player last;
    private static JButton searchKey;
    private static HashMap<String, JTextComponent> components = new HashMap<>();
    private static boolean showingError = false;
    private static JLabel avatarDisplay;

    public static ToolBarManager toolbar;

    public static class ToolBarManager {
        private static HashMap<String, JButton> buttons;

        public ToolBarManager(JFrame target) {
            CreateToolBar tBar = new CreateToolBar(target);

            buttons = tBar.compDict;
        }

        public static void onMenuExit(String buttonName) {
            JButton settingsButton = buttons.get(buttonName);

            settingsButton.setEnabled(true);
        }
    }

    enum ErrorType {
        PLAYER,
        IMAGE
    };

    private JTextComponent createIOField(JComponent parentTo, String inpOutInfo, JComponent last, Color backG, boolean editable, int w, int h, String Default, HashMap<String, JTextComponent> appendTo, Map<String, Color> palette){
        final Color text = palette.get("text");
        
        JTextPane ioDISP = new JTextPane(); // TODO: change this to JLabel w/ text
        ioDISP.setText(inpOutInfo + ":");
        if (last == null)
            ioDISP.setBounds(4, 15, 67, 25);
        else
            ioDISP.setBounds(4, last.getY() + 10 + 20, 67, 25);
        ioDISP.setEditable(false);
        ioDISP.setBackground(backG);
        ioDISP.setOpaque(true);
        ioDISP.setForeground(text);

        JTextField ioF = new JTextField();
        ioF.setBorder(null);
        ioF.setColumns(1);
        ioF.setBounds(ioDISP.getWidth() + 4, ioDISP.getY(), w, h);
        ioF.setEditable(editable);
        ioF.setHorizontalAlignment(JTextField.LEFT);
        ioF.setBackground(palette.get("background"));
        ioF.setForeground(text);
        Color back = ioF.getBackground();
        ioF.setCaretColor(new Color(255 - back.getRed(), 255 - back.getGreen(), 255 - back.getBlue())); // invert background colour
        ioF.setText(Default);
        ioF.setName(inpOutInfo);

        if (editable) {
            ioF.setBackground(palette.get("amplified"));

            ioF.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    lastModifed = ioF.getName().toLowerCase();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    lastModifed = ioF.getName().toLowerCase();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    lastModifed = ioF.getName().toLowerCase();
                }

                // sets lastModified to the textbox that last changed
            });

            ioF.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                        searchKey.doClick();
                        // assuming the search will always be there could be error prone, but i don't think it's of high priority at the moment
                }
            });
        }
        
        parentTo.add(ioDISP);
        parentTo.add(ioF);

        appendTo.put(inpOutInfo.toLowerCase(), ioF);

        return ioDISP;
    }

    private static void updateVals(Player player, HashMap<String, JTextComponent> compMap) {
        System.gc(); // free up unneeded, occupied memory
        last = player;

        compMap.forEach((name, comp) -> {
            if (comp.getName() != null) { // exclude error message box, which has no name
                name = name.toLowerCase();

                try {
                    Field toGet = Player.class.getDeclaredField(name);
                    toGet.setAccessible(true);
                    comp.setText(String.format(toGet.get(player).toString().replace("%", "%%")));
                    // format string to print escape characters properly and to escape printf formatting
                } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {
                    ErrorHandler.report(e, player);
                }
            }
        });

        if (avatarDisplay != null) {
            try {
                ImageIcon img = new ImageIcon(player.image);
                
                avatarDisplay.setIcon(img);
            } catch (NullPointerException noImageFound) {
                System.out.println("Avatar image not found");

                try {
                    Image replacement = Images.getPlaceholder(Placeholder.FAILED_LOAD);

                    avatarDisplay.setIcon(new ImageIcon(replacement));
                    player.image = replacement;
                } catch (IOException failed) {
                    failed.printStackTrace();
                }
            }
        }

        compMap.computeIfPresent("delay", (key, comp) -> {
            long delay = player.getDelay();
            comp.setText(String.valueOf(delay) + "ms");

            return comp;
        });
    }

    private long randomLong(long min, long max) {
        return min + (long) (Math.random() * (max - min));
    }

    private static void presentError(ErrorType type, String input, Exception... exception) {
        JTextPane msgBox = (JTextPane) components.get("errMsg");

        showingError = true;
        msgBox.getParent().setVisible(true);

        String msg = "";
        switch (type) {
            case PLAYER:
                String datatype = lastModifed.toLowerCase().equals("name") ? "name " : "id "; // determines if it's id or name based on lastModified
                String message = "Failed fetching user with " + datatype + input;

                if (exception.length == 1) {
                    Exception error = exception[0];
                    
                    String errMsg = error.getMessage();

                    if (errMsg != null && errMsg.endsWith("Code: 429"))
                        message = "Too many requests! Slow down!";
                }
                
                msg = message;
                break;
            case IMAGE:
                final String deletedImageContentCdn = "b561617d22628c1d01dd10f02e80c384"; // usually when image data was manually removed
                final String missingImageContentCdn = "894dca84231352d56ec346174a3c0cf9"; // the content provider link for the usual missing content image
                final String failedImageLoadCdn = "5228e2fd54377f39e87d3c25a58dd018"; // usually when the content delivery network could not provide the image

                HashMap<String, String> errorLookup = new HashMap<>();
                errorLookup.put(deletedImageContentCdn, "Image data was moderated.");
                errorLookup.put(missingImageContentCdn, "Image data not found.");
                errorLookup.put(failedImageLoadCdn, "Image failed to load, please try again.");

                String res = errorLookup.getOrDefault(input, input);

                msg = res;
                break;
        }
        msgBox.setText(msg);

        if (msg.length() > 34)
            msgBox.setLocation(40, 3);
        else
            msgBox.setLocation(40, 11);
    }

    public static void externalPlayerSearch(Number id) {
        lastModifed = "id";
        showingError = false;

        SwingWorker<Void, Void> searcher = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Player searchingFor = new Player(id);
                updateVals(searchingFor, components);

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    presentError(ErrorType.PLAYER, id.toString());
                } finally {
                    components.get("errMsg").getParent().setVisible(showingError); // the error box itself
                }
            }
        };

        searcher.execute();
    }

    private JFrame build(Palette palette) {
        JFrame frame = new JFrame(Controller.title + " v" + Controller.version);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final HashMap<String, Color> paletteCols = palette.colourPalette;

        final Color infoSectionColor = paletteCols.get("info");
        final Color textColor = paletteCols.get("text");
        final Color backgroundColor = paletteCols.get("background");
        final Color amplifiedColor = paletteCols.get("amplified");
        final Color errCol = paletteCols.get("error");
        final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

        boolean showPing = true;

        try {
            AdvancedSettings advSettings = new AdvancedSettings();
            showPing = Boolean.valueOf(advSettings.get("displayPing"));
        } catch (IOException ioex) {
            ErrorHandler.report(ioex);
        }

        JTextComponent lastTxt = null;

        int x,y;
        x = 800;
        y = 650;

        final int aX,aY;
        aX = x - 96;
        aY = y - 84; // these differ because components are moved from the top-left corner

        frame.setBounds(200,200, x, y);
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(x,y));
        frame.getContentPane().setBackground(backgroundColor);

        // general info
        JPanel info = new JPanel();
        info.setBounds(15, 55, 450, 300);
        info.setBackground(infoSectionColor);
        info.setLayout(null);

        TitledBorder infoBorder = new TitledBorder(new EtchedBorder(), "General Info");
        infoBorder.setTitleColor(textColor);

        info.setBorder(infoBorder);

        // description
        JPanel description = new JPanel();
        description.setBounds(15, 375, 710, 160);
        description.setBackground(infoSectionColor);

        TitledBorder descBorder = new TitledBorder(new EtchedBorder(), "Description");
        descBorder.setTitleColor(textColor);

        description.setBorder(descBorder);

        JTextArea descriptionText = new JTextArea(8, 62);
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setEditable(false);
        descriptionText.setBackground(amplifiedColor);
        descriptionText.setForeground(textColor);
        descriptionText.setName("description");

        JScrollPane descScroll = new JScrollPane(descriptionText);
        descScroll.setBorder(null);
        descScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        description.add(descScroll);

        JPanel error = new JPanel();
        error.setBounds(aX/2 - 336, aY-5, 260, 45);
        error.setBackground(errCol);
        error.setLayout(null);
        error.setBorder(BorderFactory.createLineBorder(new Color(255, 0, 0), 1));
        error.setVisible(false);

        JTextPane errorMsg = new JTextPane();
        errorMsg.setBounds(40, 11, 200, 45);
        errorMsg.setText("User not found.");
        errorMsg.setForeground(textColor);
        errorMsg.setOpaque(false);
        errorMsg.setEditable(false);
        errorMsg.setHighlighter(null);
        errorMsg.getCaret().deinstall(errorMsg); // fixes weird background formatting bug

        components.put("errMsg", errorMsg);

        error.add(errorMsg);

        Color darkerBg = new Color(infoSectionColor.getRed() - 25,  infoSectionColor.getGreen() - 25, infoSectionColor.getBlue() - 25);

        // avatar
        JPanel imageSection = new JPanel();
        imageSection.setBounds(info.getX() + info.getWidth() + 30, info.getY(), 230, info.getHeight());
        imageSection.setBackground(infoSectionColor);
        imageSection.setName("image");
        imageSection.setLayout(null);

        try {
            final Image scaled = Images.getLocal(Local.WARNING).getScaledInstance(35, 35, Image.SCALE_AREA_AVERAGING);

            JLabel warn = new JLabel(new ImageIcon(scaled));
            warn.setVisible(true);
            warn.setOpaque(false);
            warn.setBounds(2, 2, 40, 40);

            error.add(warn);
        } catch (IOException ioe) {
            ErrorHandler.report(ioe);
        }

        JLabel av = new JLabel();
        av.setBounds(40, 20, 150, 150);
        av.setOpaque(true);
        av.setBackground(darkerBg);
        av.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));
        av.addPropertyChangeListener(new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String changed = evt.getPropertyName();

                if (changed.equals("icon")) {
                    ImageIcon newIcon = (ImageIcon) evt.getNewValue();
                    Image newImg = newIcon.getImage();

                    Object errCode = newImg.getProperty("error", av);

                    if (errCode != Image.UndefinedProperty) // undefined property is returned when the property does not exist
                        presentError(ErrorType.IMAGE, errCode.toString());
                }
            }
        });

        avatarDisplay = av;

        JPanel subPanel = new JPanel();
        subPanel.setBounds(40, 19 + av.getHeight(), 150, 30);
        subPanel.setBackground(new Color(darkerBg.getRed() + 10, darkerBg.getGreen() + 10, darkerBg.getBlue() + 10));
        subPanel.setLayout(null);
        subPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));

        JTextPane onlineText = new JTextPane();
        onlineText.setBounds(1, 4, 58, 25);
        onlineText.setText("Is online:");
        onlineText.setBackground(subPanel.getBackground());
        onlineText.setEditable(false);
        onlineText.setForeground(textColor);

        JTextPane isOnline = new JTextPane();
        isOnline.setBounds(onlineText.getWidth(), 4, 30, 25);
        isOnline.setBackground(subPanel.getBackground());
        isOnline.setEditable(false);
        isOnline.setName("online"); // this looks a bit of out of place
        isOnline.setForeground(textColor);
        
        JButton reload = new JButton("Reload Image");
        reload.setBounds(subPanel.getX(), subPanel.getY() + subPanel.getHeight() + 8, subPanel.getWidth(), 25);
        reload.setCursor(handCursor);
        reload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reload.setEnabled(false);
                showingError = false;

                Image reloadedImg = getAppearance.retrieveImage(last.id);

                av.setIcon(new ImageIcon(reloadedImg));
                last.image = reloadedImg; // update player image

                error.setVisible(showingError);

                reload.setEnabled(true);
            }
        });

        JProgressBar outfitProgressBar = new JProgressBar();
        outfitProgressBar.setBounds(reload.getX(), reload.getY() + reload.getHeight() + 6, reload.getWidth(), reload.getHeight());
        outfitProgressBar.setStringPainted(true);
        outfitProgressBar.setVisible(false);
        outfitProgressBar.setEnabled(false);
        // TODO: figure out something for dark theme

        JButton openOutfits = new JButton("View Outfits");
        openOutfits.setBounds(outfitProgressBar.getBounds());
        openOutfits.setCursor(handCursor);
        openOutfits.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openOutfits.setEnabled(false);
                openOutfits.setVisible(false);

                outfitProgressBar.setEnabled(true);
                outfitProgressBar.setVisible(true);

                OutfitViewer.progBar = new OutfitViewer.ProgressBarManager(outfitProgressBar);
                OutfitViewer.display(last);
            }
        });

        outfitProgressBar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                outfitProgressBar.setVisible(false);
                outfitProgressBar.setEnabled(false);

                openOutfits.setVisible(true);
                openOutfits.setEnabled(true); // re-enables openOutfits after the progress bar has reached completion
            }
        });

        subPanel.add(onlineText);
        subPanel.add(isOnline);

        imageSection.add(subPanel);
        imageSection.add(av);
        imageSection.add(reload);
        imageSection.add(outfitProgressBar);
        imageSection.add(openOutfits);

        toolbar = new ToolBarManager(frame);

        HashMap<String, JTextComponent> comps = new HashMap<>();

        lastTxt = createIOField(info, "Name", lastTxt, infoSectionColor, true, 200, 25, "ROBLOX", comps, paletteCols);
        lastTxt = createIOField(info, "ID", lastTxt, infoSectionColor, true, 200, 25, "1", comps, paletteCols);
        lastTxt = createIOField(info, "DispName", lastTxt, infoSectionColor, false, 200, 25, "", comps, paletteCols);
        lastTxt = createIOField(info, "Friends", lastTxt, infoSectionColor, false, 200, 25, "", comps, paletteCols);
        lastTxt = createIOField(info, "Followings", lastTxt, infoSectionColor, false, 200, 25, "", comps, paletteCols);
        lastTxt = createIOField(info, "Followers", lastTxt, infoSectionColor, false, 200, 25, "", comps, paletteCols);
        lastTxt = createIOField(info, "Created", lastTxt, infoSectionColor, false, 200, 25, "", comps, paletteCols);
        lastTxt = createIOField(info, "Banned", lastTxt, infoSectionColor, false, 200, 25, "", comps, paletteCols);
        lastTxt = createIOField(info, "LastOnline", lastTxt, infoSectionColor, false, 200, 25, "", comps, paletteCols);
        comps.put(descriptionText.getName(), descriptionText);
        comps.put(isOnline.getName(), isOnline);

        JTextPane pingText = new JTextPane();
        JTextPane pingVal = new JTextPane();

        if (showPing) {
            StyleContext context = new StyleContext();
            StyledDocument document = new DefaultStyledDocument(context);
            Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
            StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);

            pingText = new JTextPane();
            pingText.setBounds(aX - 18, aY + 25, 35, 18);
            pingText.setEditable(false);
            pingText.setText("Ping:");
            pingText.setBackground(backgroundColor);
            pingText.setForeground(textColor);

            pingVal = new JTextPane(document);
            pingVal.setBounds(pingText.getX() + pingText.getWidth(), pingText.getY(), 60, 16);
            pingVal.setEditable(false);
            pingVal.setBackground(backgroundColor);
            pingVal.setForeground(textColor);
            pingVal.setName("delay");

            frame.add(pingText);
            frame.add(pingVal);
            comps.put(pingVal.getName(), pingVal);
        }

        components.putAll(comps);

        long chosen = randomLong(1L, 48L);

        String startUser = "ROBLOX";

        try {
            DisplaySettings dispSettings = new DisplaySettings();
            startUser = dispSettings.get("start_user");

        } catch (IOException ioexcept) {
            ErrorHandler.report(ioexcept);
        }

        if (chosen == 29L) 
            startUser = Controller.author;
        
        try {
            Player start = new Player(startUser);

            updateVals(start, comps);
        } catch (UserNotFoundException uException) {
            uException.printStackTrace();

            try {
                Player newStart = new Player(1L);

                updateVals(newStart, comps);
            } catch (UserNotFoundException | NullPointerException notFound) {
                notFound.printStackTrace();
            } finally {
                lastModifed = "name"; // set it to name for the present error method to use
                presentError(ErrorType.PLAYER, startUser, uException);

                lastModifed = null; // set it back to its default value
            }
        }

        JButton search = new JButton();
        search.setText("Search");
        search.setBounds(aX/2, aY-5, 80, 45);
        search.setCursor(handCursor);
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (search.isEnabled()) {
                    search.setEnabled(false);

                    String input = comps.get(lastModifed.toLowerCase()).getText();

                    try {
                        if (lastModifed.equals("name")) {
                            String name = comps.get("name").getText();

                            if (!last.name.equals(name)) {
                                showingError = false; // order matters here and in the corresponding else block: when the player image updates, the image could error
                                updateVals(new Player(input), comps);
                            }
                        } else {
                            long id = Long.valueOf(comps.get("id").getText()); // NumberFormatException can be thrown here

                            if (last.id != id) {
                                showingError = false;
                                updateVals(new Player(Long.valueOf(input)), comps);
                            }
                        }

                        openOutfits.setEnabled(!last.banned);
                    } catch (UserNotFoundException | NumberFormatException err) {
                        presentError(ErrorType.PLAYER, input, err);
                    } finally {
                        error.setVisible(showingError);
                        search.setEnabled(true);

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() { descScroll.getVerticalScrollBar().setValue(0); } // scroll desc to the very top
                        });
                    }
                }
            }
        });

        searchKey = search;

        JTextField id = (JTextField) comps.get("id");

        JButton randomize = new JButton();
        randomize.setText("Random ID");
        randomize.setBounds(id.getBounds().x + id.getBounds().width + 4, id.getBounds().y, 95, id.getBounds().height);
        randomize.setCursor(handCursor);
        randomize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (randomize.isEnabled()){
                    randomize.setEnabled(false);
                    showingError = false;

                    long min, max, newId;
                    min = 1L;
                    max = 3_000_000_000L;
                    newId = min;

                    try {
                        SearchSettings srch = new SearchSettings();

                        min = Long.valueOf(srch.get("min_id")).longValue();
                        max = Long.valueOf(srch.get("max_id")).longValue();

                        newId = randomLong(min, max);

                        updateVals(new Player(newId), comps);
                        
                        openOutfits.setEnabled(!last.banned);
                    } catch (UserNotFoundException err) {
                        presentError(ErrorType.PLAYER, String.valueOf(newId), err);
                    } catch (IOException ioexc) {
                        ErrorHandler.report(ioexc);
                    } finally {
                        error.setVisible(showingError); // presentError sets showingError (global private variable) to true if there is an error
                        randomize.setEnabled(true);

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() { descScroll.getVerticalScrollBar().setValue(0); } // scroll desc to the very top, again
                        });
                    }
                }
            }
        });

        if (palette.colour == Themes.DARK) {
            LineBorder border = new LineBorder(new Color(0, 0, 0), 1);

            search.setBackground(amplifiedColor);
            search.setContentAreaFilled(false);
            search.setBorder(border);
            search.setForeground(textColor);

            randomize.setBackground(amplifiedColor);
            randomize.setContentAreaFilled(false);
            randomize.setBorder(border);
            randomize.setForeground(textColor);

            reload.setBackground(amplifiedColor);
            reload.setContentAreaFilled(false);
            reload.setBorder(border);
            reload.setForeground(textColor);

            openOutfits.setBackground(amplifiedColor);
            openOutfits.setContentAreaFilled(false);
            openOutfits.setBorder(border);
            openOutfits.setForeground(textColor);
        }

        info.add(randomize);

        frame.add(info);
        frame.add(search);
        frame.add(description);
        frame.add(error);
        frame.add(imageSection);

        frame.setLayout(null);

        return frame;
    }

    public void display() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Palette selected = new Palette(Themes.LIGHT);

                try {
                    DisplaySettings dispSettings = new DisplaySettings();

                    int themeSelected = Integer.valueOf(dispSettings.get("current_theme"));
                    Themes theme = (themeSelected == 0) ? Themes.LIGHT : Themes.DARK;

                    selected = new Palette(theme);
                } catch (IOException e) {
                    ErrorHandler.report(e);
                }

                JFrame show = build(selected);

                show.setVisible(true);
            }
        });
    }
}

/*
Here once lay a small, overlooked text.
Ere its removal, it showed one's status.
It was fun and unassuming, not momentous
Until its removal, upon ROBLOX's behest.

RIP user statuses
*/
