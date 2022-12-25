package ui.gui.utilities;

import java.awt.Dimension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.Calendar;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import main.Controller;

public class DebugConsole extends JFrame {
    
    private static class LogStream extends ByteArrayOutputStream {
        private JTextArea receive;

        private int prev = 0;
        private String toAppend = "";

        public LogStream(JTextArea writeTo) {
            super();
            receive = writeTo;
        }

        @Override
        public void write(int b) {
            super.write(b);

            receive.append(Character.toString((char) b));
        }

        
        @Override
        public void write(byte[] b, int off, int len) {
            super.write(b, off, len);

            if (toAppend.isEmpty()) {
                Calendar time = Calendar.getInstance();
                String timeString = String.format("[%d:%d:%d] ",
                                                  time.get(Calendar.HOUR_OF_DAY),
                                                  time.get(Calendar.MINUTE),
                                                  time.get(Calendar.SECOND));
                
                toAppend += timeString;
            }

            for (int i = prev; i < count; i++)
                toAppend += (char) buf[i];
            
            if ((char) b[len - 1] == '\n') {
                receive.append(toAppend);
                toAppend = "";
            }

            prev = count;
        }
    }

    private static LogStream receiving;

    private static JFrame build() {
        JFrame frame = new JFrame(Controller.title + " - Debug Console");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        final Color bgcolour = new Color(238, 238, 238);

        frame.setBounds(200, 200, 800, 700);
        frame.setPreferredSize(new Dimension(800, 700));
        frame.setMinimumSize(new Dimension(640, 590));
        frame.setResizable(true);
        frame.setBackground(bgcolour);
        frame.setLayout(new GridBagLayout());
        frame.setVisible(true);

        GridBagConstraints c = new GridBagConstraints();

        JPanel textPanel = new JPanel(new GridBagLayout());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(0, 0, 2, 0);
        c.weightx = 1;
        c.weighty = 1;
        c.ipady = 500;

        frame.add(textPanel, c);

        JTextArea log = new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBorder(new LineBorder(new Color(0, 0, 0), 1));
        receiving = new LogStream(log);

        JScrollPane logScroll = new JScrollPane(log);
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(1, 2, 1, 2);
        c.weightx = 1;
        c.weighty = 1;
        c.ipady = 450;

        textPanel.add(logScroll, c);

        JLabel inpDisplayText = new JLabel("Input:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.ipady = 0;
        textPanel.add(inpDisplayText, c);

        JTextField commandLine = new JTextField();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 0;
        c.insets = new Insets(1, 2, 1, 2);

        textPanel.add(commandLine, c);

        c = new GridBagConstraints();

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        c.gridy = 1;
        c.ipady = 15;
        c.weightx = 1;
        c.weighty = 0;

        frame.add(buttonPanel, c);

        JButton clearButton, exportButton, closeButton;
        Cursor cursor = new Cursor(Cursor.HAND_CURSOR);

        clearButton = new JButton("Clear");
        clearButton.setCursor(cursor);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;

        buttonPanel.add(clearButton, c);

        exportButton = new JButton("Export");
        exportButton.setCursor(cursor);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(0, 5, 0, 0);

        buttonPanel.add(exportButton, c);

        closeButton = new JButton("Close");
        closeButton.setCursor(cursor);
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(0, 5, 0, 0);

        buttonPanel.add(closeButton, c);

        frame.pack();

        return frame;
    }

    public static void display() {
        build();

        System.setOut(new PrintStream(receiving));
        //System.out.println("Hello Worldddddddddddddddddddddddddddddddddd!");
    }
}
