package ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Desktop;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;

import java.net.URI;
import java.net.URISyntaxException;

import java.io.IOException;

import javax.swing.*;

import classes.Player;

@SuppressWarnings("serial")

public class ErrorHandler extends JFrame {
    private static JFrame window;
    private static JTextArea writeTo;

    private static void build() {
        window = new JFrame(displayWindow.title + " error report");
        window.setDefaultCloseOperation(EXIT_ON_CLOSE);

        final int x,y;
        x = 500;
        y = 400;

        window.setBounds(200, 200, x, y);
        window.setResizable(false);
        window.setPreferredSize(new Dimension(x, y));

        // message
        JPanel textinfo = new JPanel();
        textinfo.setBounds(16, 5, 450, 75);
        textinfo.setLayout(null);
                
        JTextPane msg = new JTextPane();
        msg.setEditable(false);
        msg.setText("Uh oh! It appears " + displayWindow.title + " has encountered a critical error. Below you will find a stack trace of the error, please take some time and report the issue on github (preferably copying and pasting the stack trace and providing what you were doing beforehand), if possible.");
        msg.setBounds(0, 0, 450, 75);
        
        textinfo.add(msg);
        textinfo.setVisible(true);

        // stack trace
        JPanel stackTrace = new JPanel();
        stackTrace.setBounds(-7, 81, 500, 232);

        JTextArea traceBox = new JTextArea(14, 40);
        traceBox.setLineWrap(true);
        traceBox.setWrapStyleWord(true);
        traceBox.setEditable(false);

        JScrollPane scroll = new JScrollPane(traceBox);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        stackTrace.add(scroll);
        writeTo = traceBox;
        traceBox.setVisible(true);

        // buttons
        JPanel buttons = new JPanel();
        buttons.setBounds(0, 320, 400, 40);
        buttons.setLayout(null);
        
        int center = (int) buttons.getBounds().getCenterX();
        
        JButton reportButton = new JButton();
        reportButton.setText("Report");
        reportButton.setBounds(center - 60, 0, 100, 40);
        reportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    final URI link = new URI("https://github.com/Click-r/Roblox-viewer/issues/new");
        
                    Desktop dsk = Desktop.getDesktop();
        
                    if (Desktop.isDesktopSupported() && dsk.isSupported(Desktop.Action.BROWSE))
                        dsk.browse(link);
                } catch (IOException | URISyntaxException exc) {}
            }
        });
        
        JButton copyButton = new JButton();
        copyButton.setText("Copy");
        copyButton.setBounds(center + 60, 0, 100, 40);
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringSelection trace = new StringSelection(traceBox.getText());
        
                Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
        
                board.setContents(trace, null);
            }
        });
                
        buttons.add(reportButton);
        buttons.add(copyButton);
                
        window.add(stackTrace);
        window.add(textinfo);
        window.add(buttons);
        
        window.setLayout(null);
    }

    private static void display() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                build();

                window.setVisible(true);
            }
        });
    }

    public static void report(Exception error, Player... additional) {
        String additionalMsg = (additional.length == 1) ? ("UID: " + additional[0].id + "\nName: " + additional[0].name + "\n\n") : "";
        String errMessage = additionalMsg + error + "\n\n";

        for (StackTraceElement e: error.getStackTrace()) {
            errMessage += (e + "\n");
        }

        display();

        writeTo.setText(errMessage);
    }
}
