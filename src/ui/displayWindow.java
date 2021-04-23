package ui;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.*;
import java.awt.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import java.lang.reflect.Field;

import java.util.HashMap;

import classes.*;

import main.Controller;

public class displayWindow{
    private static String lastModifed;
    private static Player last;

    private static JTextComponent createIOField(JComponent parentTo, String inpOutInfo, JComponent last, Color backG, boolean editable, int w, int h, String Default, HashMap<String, JTextComponent> appendTo){
        JTextPane ioDISP = new JTextPane();
        ioDISP.setText(inpOutInfo + ":");
        if (last == null)
            ioDISP.setBounds(4, 15, 67, 25);
        else
            ioDISP.setBounds(4, last.getY() + 10 + 20, 67, 25);
        ioDISP.setEditable(false);
        ioDISP.setBackground(backG);
        ioDISP.setOpaque(true);

        JTextField ioF = new JTextField();
        ioF.setColumns(1);
        ioF.setBounds(ioDISP.getWidth() + 4, ioDISP.getY(), w, h);
        ioF.setEditable(editable);
        ioF.setHorizontalAlignment(JTextField.LEFT);
        ioF.setText(Default);
        ioF.setName(inpOutInfo);

        if (editable) 
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
        
        parentTo.add(ioDISP);
        parentTo.add(ioF);

        appendTo.put(inpOutInfo.toLowerCase(), ioF);

        return ioDISP;
    }

    private static String format(String input) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try (PrintStream printStream = new PrintStream(stream, true, "utf-8")) {
            printStream.format(input);
        } catch (UnsupportedEncodingException uns) {
            ErrorHandler.report(uns);
        }

        return stream.toString();
    }

    private static void updateVals(Player player, HashMap<String,JTextComponent> compMap) {
        compMap.forEach((name, comp) -> {
            name = name.toLowerCase();
            try {
                Field toGet = Player.class.getDeclaredField(name);
                toGet.setAccessible(true);
                comp.setText(format(toGet.get(player).toString()));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                ErrorHandler.report(e, player);
            }
        });

        last = player;
    }

    private static long randomLong(long min, long max) {
        return min + (long) (Math.random() * (max - min));
    }

    private static void presentError(JTextPane msgBox, String input) {
        msgBox.getParent().setVisible(true);

        String datatype = lastModifed.toLowerCase().equals("name") ? "name " : "id "; // determines if it's id or name based on lastModified
        final String message = "Failed fetching user with " + datatype + input;

        if (message.length() > 34)
            msgBox.setLocation(40, 3);
        else
            msgBox.setLocation(40, 11);
        
        msgBox.setText("Failed fetching user with " + datatype + input);
    }

    private static JFrame build() {
        JFrame frame = new JFrame(Controller.title + " v" + Controller.version);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Color infoSectionColor = new Color(218,218,218);

        JTextComponent lastTxt = null;

        int x,y;
        x = 800;
        y = 660;

        final int aX,aY;
        aX = 704;
        aY = 576; // for some reason these differ from the frame width and length but ok

        frame.setBounds(200,200, x, y);
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(x,y));

        // general info
        JPanel info = new JPanel();
        info.setBounds(15, 15, 450, 300);
        info.setBackground(infoSectionColor);
        info.setLayout(null);
        info.setBorder(new TitledBorder(new EtchedBorder(), "General Info"));

        // description
        JPanel description = new JPanel();
        description.setBounds(15, 407, 600, 160);
        description.setBackground(infoSectionColor);
        description.setBorder(new TitledBorder(new EtchedBorder(), "Description"));

        JTextArea descriptionText = new JTextArea(8, 51);
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setEditable(false);
        descriptionText.setName("description");

        JScrollPane descScroll = new JScrollPane(descriptionText);
        descScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        description.add(descScroll);
        descriptionText.setVisible(true);

        // status
        JPanel status = new JPanel();
        status.setBounds(15, description.getY() - 86, 600, 80);
        status.setBackground(infoSectionColor);
        status.setBorder(new TitledBorder(new EtchedBorder(), "Status"));

        JTextArea statusText = new JTextArea(3,51);
        statusText.setLineWrap(true);
        statusText.setWrapStyleWord(true);
        statusText.setEditable(false);
        statusText.setName("status");
        
        JScrollPane statScroll = new JScrollPane(statusText);
        statScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        status.add(statScroll);
        statusText.setVisible(true);

        HashMap<String, JTextComponent> comps = new HashMap<String, JTextComponent>();

        lastTxt = createIOField(info, "Name", lastTxt, infoSectionColor, true, 200, 25, "ROBLOX",comps);
        lastTxt = createIOField(info, "ID", lastTxt, infoSectionColor, true, 200, 25, "1",comps);
        lastTxt = createIOField(info, "DispName", lastTxt, infoSectionColor, false, 200, 25, "", comps);
        lastTxt = createIOField(info, "Friends", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        lastTxt = createIOField(info, "Followings", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        lastTxt = createIOField(info, "Followers", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        lastTxt = createIOField(info, "Created", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        lastTxt = createIOField(info, "Banned", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        lastTxt = createIOField(info, "LastOnline", lastTxt, infoSectionColor, false, 200, 25, "", comps);
        comps.put(descriptionText.getName(), descriptionText);
        comps.put(statusText.getName(), statusText);

        long chosen = randomLong(1L, 48L);

        String startUser = "ROBLOX";

        if (chosen == 29L) 
            startUser = Controller.author;
        
        try {
            Player start = new Player(startUser);

            updateVals(start, comps);
        } catch (UserNotFoundException uException) {}

        final Color errCol = new Color(252, 163, 150);

        JPanel error = new JPanel();
        error.setBounds(aX/2 - 336, aY-5, 260, 45);
        error.setBackground(errCol);
        error.setLayout(null);
        error.setBorder(BorderFactory.createLineBorder(new Color(255, 0, 0), 1));
        error.setVisible(false);

        JTextPane errorMsg = new JTextPane();
        errorMsg.setBounds(40, 11, 200, 45);
        errorMsg.setText("User not found.");
        errorMsg.setOpaque(false);
        errorMsg.setEditable(false);
        errorMsg.setHighlighter(null);
        errorMsg.getCaret().deinstall(errorMsg); // fixes weird background formatting bug

        try {
            final Image scaled = ErrorHandler.getWarningImg().getImage().getScaledInstance(35, 35, Image.SCALE_AREA_AVERAGING);
            // TODO: make controller distribute the images

            JLabel warn = new JLabel(new ImageIcon(scaled));
            warn.setVisible(true);
            warn.setOpaque(false);
            warn.setBounds(2, 2, 40, 40);

            error.add(warn);
        } catch (IOException ioe) {
            ErrorHandler.report(ioe);
        }

        error.add(errorMsg);

        JButton search = new JButton();
        search.setText("Search");
        search.setBounds(aX/2, aY-5, 80, 45);
        search.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (search.isEnabled()) {

                    search.setEnabled(false);

                    boolean same = false;

                    if (lastModifed.equals("name")) {
                        String name = comps
                          .get("name")
                          .getText();

                        same = last.name.equals(name);
                    } else {
                        try {
                            long id = Long.valueOf(comps.get("id").getText());

                            same = last.id.equals(id);
                        } catch (NumberFormatException nException) {
                            presentError(errorMsg, comps.get("id").getText());
                            
                            same = true; // so it doesn't look it up
                            // if it looks it up then the other piece of code below this would error too
                        }
                    }

                    if (!same) {
                        String input = comps.get(lastModifed.toLowerCase()).getText();
                        
                        try {
                            if (lastModifed.equals("name"))
                                updateVals(new Player(input), comps);
                            else 
                                updateVals(new Player(Long.valueOf(input)), comps);
                            
                            error.setVisible(false);
                        } catch (UserNotFoundException err) {
                            presentError(errorMsg, input);
                        }

                    }
                    
                    search.setEnabled(true);

                }
            }
        });

        JTextField id = (JTextField) comps.get("id");

        JButton randomize = new JButton();
        randomize.setText("Random ID");
        randomize.setBounds(id.getBounds().x + id.getBounds().width + 4, id.getBounds().y, 95, id.getBounds().height);
        randomize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (randomize.isEnabled()){
                    randomize.setEnabled(false);

                    long newId = randomLong(1L, 2_300_000_000L);
                    
                    try {
                        updateVals(new Player(newId), comps);

                        error.setVisible(false);
                    } catch (UserNotFoundException err) {
                        presentError(errorMsg, String.valueOf(newId));
                    }

                    randomize.setEnabled(true);
                }
            }
        });

        info.add(randomize);

        frame.add(info);
        frame.add(search);
        frame.add(description);
        frame.add(status);
        frame.add(error);

        frame.setLayout(null);

        return frame;
    }

    public static void display() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame show = build();

                show.setVisible(true);
            }
        });
    }
}
