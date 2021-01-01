import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.*;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Stack;
import java.util.Random;

public class displayWindow {

    public static Color lerpColor(Color current, Color goal, int percent){
        float p = (float)percent/100;
        int r,g,b;
        r = (int)((float)(goal.getRed() - current.getRed()) * p);
        g = (int)((float)(goal.getBlue() - current.getBlue()) * p);
        b = (int)((float)(goal.getGreen() - current.getGreen()) * p);
        return new Color((int)(current.getRed() + r),(int)(current.getBlue() + g),(int)(current.getGreen() + b));
    } //fun function don't mind this

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

    public static void updateVals(HashMap<String,String> dataMap, HashMap<String,JTextComponent> compMap) throws NullPointerException {
        if (dataMap.size() == 9) {
            dataMap.forEach((key,pair) -> {
                key = key.toLowerCase();
                try {
                    String formatted = format(pair); // properly display line breaks

                    if (formatted.contains("\""))
                        formatted = formatted.substring(1, formatted.length()-1);

                    compMap.get(key).setText(formatted);
                } catch (NullPointerException e) {}
            });
        }
    }

    final static String version = "0.1a";
    final static String title = "RBLXInfoViewer";

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
        aY = 576; //idk why these differ from the frame width and length but ok

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
        //description.setLayout(null);
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
        //status and description to be put somewhere else

        updateVals(getInfo.searchByUsername("ROBLOX"), comps);

        Stack<String> cmp = new Stack<String>();
        cmp.push("ROBLOX");

        JButton search = new JButton();
        search.setText("Search");
        //search.setSize(4, 4);
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
                            HashMap<String, String> returned = getInfo.searchByUsername(n1);
                            updateVals(returned, comps);
                            cmp.push(n1);

                        } catch (NullPointerException err) {
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

        JTextField id = (JTextField)comps.get("id");

        JButton randomize = new JButton();
        randomize.setText("Random ID");
        randomize.setBounds(id.getBounds().x + id.getBounds().width + 4, id.getBounds().y, 95, id.getBounds().height);
        randomize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (randomize.isEnabled()){

                    randomize.setEnabled(false);

                    Random gen = new Random();
                    int newId = gen.nextInt(2_000_000_000);
                    // when the total number of roblox users surpasses the 32 bit signed integer limit i'll need to start using the long data type
                    
                    try {
                        HashMap<String, String> returned = getInfo.getInformation(newId);
                        updateVals(returned, comps);
                    } catch (SocketTimeoutException err) {
                        System.out.println("Timed out");
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