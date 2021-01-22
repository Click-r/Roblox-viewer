package main;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Stack;
import java.util.Random;

import classes.*;

public class displayWindow {

    public static JComponent createIOField(JComponent parentTo, String inpOutInfo, JComponent last, Color backG, boolean editable, int w, int h, String Default, HashMap<String, JTextComponent> appendTo){
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

    public static void updateVals(Player player, HashMap<String,JTextComponent> compMap) throws NullPointerException {
        compMap.forEach((name, comp) -> {
            name = name.toLowerCase();
            try {
                Field toGet = Player.class.getDeclaredField(name);
                toGet.setAccessible(true);
                comp.setText(format(toGet.get(player).toString()));
            } catch (NoSuchFieldException e) {} catch (IllegalAccessException err) {}
        });
    }

    public static long randomLong(long min, long max) {
        return min + (long) (Math.random() * (max - min));
    }

    final static String version = "0.1b";
    final static String title = "RBLXInfoViewer";
    final static String author = "Cli_ck";

    public static void main(String[] args) {

        JFrame frame = new JFrame(title + " v" + version);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Color infoSectionColor = new Color(218,218,218);

        JComponent lastTxt = null;

        int x,y;
        x = 800;
        y = 660;

        final int aX,aY;
        aX = 704;
        aY = 576; // for some reason these differ from the frame width and length but ok

        frame.setBounds(200,200, x, y);
        frame.setResizable(false);
        frame.setPreferredSize(new Dimension(x,y));

        JPanel info = new JPanel();
        info.setBounds(15, 15, 450, 300);
        info.setBackground(infoSectionColor);
        info.setLayout(null);
        info.setBorder(new TitledBorder(new EtchedBorder() , "General Info"));

        JPanel description = new JPanel();
        description.setBounds(15, 407, 600, 160);
        description.setBackground(infoSectionColor);
        description.setBorder(new TitledBorder(new EtchedBorder(), "Description"));

        JTextArea descriptionText = new JTextArea(8, 51);
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setEditable(false);
        descriptionText.setName("description");

        JScrollPane scroll = new JScrollPane(descriptionText);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        description.add(scroll);
        descriptionText.setVisible(true);

        HashMap<String, JTextComponent> comps = new HashMap<String, JTextComponent>();

        lastTxt = createIOField(info, "Name", lastTxt, infoSectionColor, true, 200, 25, "ROBLOX",comps);
        lastTxt = createIOField(info, "ID", lastTxt, infoSectionColor, false, 200, 25, "1",comps);
        lastTxt = createIOField(info, "Friends", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        lastTxt = createIOField(info, "Followings", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        lastTxt = createIOField(info, "Followers", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        lastTxt = createIOField(info, "Created", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        lastTxt = createIOField(info, "Banned", lastTxt, infoSectionColor, false, 200, 25, "",comps);
        comps.put(descriptionText.getName(), descriptionText);
        //status coming soon

        Random gen = new Random();
        int chosen = gen.nextInt(48);

        String startUser = "ROBLOX";

        if (chosen == 29) {
            startUser = author;
        }
    
        updateVals(new Player(startUser), comps);


        Stack<String> cmp = new Stack<String>();

        cmp.push(startUser);

        JButton search = new JButton();
        search.setText("Search");
        search.setBounds(aX/2, aY-5, 80, 45);
        search.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (search.isEnabled()) {

                    search.setEnabled(false);

                    String name = comps
                        .get("name")
                        .getText();
                    cmp.push(name);

                    String n1,n2;

                    n1 = cmp.pop().toLowerCase();
                    n2 = cmp.pop().toLowerCase();

                    if (!n1.equals(n2)) {

                        try {
                            updateVals(new Player(n1), comps);
                            cmp.push(n1);

                        } catch (NumberFormatException err) {
                            System.out.println("User named " + n1 + " does not exist!");
                            cmp.push(n2);
                        }

                    } else {
                        cmp.push(n2);
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
                    } catch (NumberFormatException err) {
                        System.out.println("Thread timed out");
                    }

                    randomize.setEnabled(true);
                }
            }
        });
        info.add(randomize);

        frame.add(info);
        frame.add(search);
        frame.add(description);

        frame.setLayout(null);

        frame.setVisible(true);
    }
}
