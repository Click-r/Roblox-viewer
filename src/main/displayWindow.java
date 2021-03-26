package main;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.*;

import java.lang.reflect.Field;

import java.util.HashMap;

import classes.*;

public class displayWindow {
    final static String version = "0.4";
    final static String title = "RBLXInfoViewer";
    final static String author = "Cli_ck";

    static String lastModifed;
    static Player last;

    public static JTextComponent createIOField(JComponent parentTo, String inpOutInfo, JComponent last, Color backG, boolean editable, int w, int h, String Default, HashMap<String, JTextComponent> appendTo){
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
            });
        
        parentTo.add(ioDISP);
        parentTo.add(ioF);

        appendTo.put(inpOutInfo.toLowerCase(), ioF);

        return ioDISP;
    }

    public static String format(String input) {
        return input
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\'", "\'")
            .replace("\\\"", "\"");
    }

    public static void updateVals(Player player, HashMap<String,JTextComponent> compMap) {
        compMap.forEach((name, comp) -> {
            name = name.toLowerCase();
            try {
                Field toGet = Player.class.getDeclaredField(name);
                toGet.setAccessible(true);
                comp.setText(format(toGet.get(player).toString()));
            } catch (NoSuchFieldException | IllegalAccessException e) {}
        });

        last = player;
    }

    public static long randomLong(long min, long max) {
        return min + (long) (Math.random() * (max - min));
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame(title + " v" + version);
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
        info.setBorder(new TitledBorder(new EtchedBorder() , "General Info"));

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

        if (chosen == 29L) {
            startUser = author;
        }
    
        updateVals(new Player(startUser), comps);

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
                        long id = Long.valueOf(comps.get("id").getText());

                        same = last.id.equals(id);
                    }

                    if (!same) {
                        String input = comps.get(lastModifed.toLowerCase()).getText();
                        
                        try {
                            if (lastModifed.equals("name"))
                                updateVals(new Player(input), comps);
                            else 
                                updateVals(new Player(Long.valueOf(input)), comps);
                        } catch (NumberFormatException | NullPointerException err) {
                            System.out.println("API endpoints failed to return user with name/id " + input);
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
                    } catch (NumberFormatException | NullPointerException err) {
                        System.out.println("API endpoints failed to return user with ID of " + newId);
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

        frame.setLayout(null);

        frame.setVisible(true);
    }
}
