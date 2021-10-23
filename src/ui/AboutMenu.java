package ui;

import java.util.HashMap;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.event.*;

import java.io.InputStream;

import main.Controller;

public class AboutMenu extends JFrame {

    private static HashMap<String, JComponent> comps = new HashMap<String, JComponent>();
    private static String currentlyLoadedLicense = null;

    @SuppressWarnings("static-access")
    public AboutMenu() {
        JFrame window = new JFrame(Controller.title + " - About");
        window.setResizable(false);
        window.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        window.setBounds(200, 200, 700, 700);
        window.setLayout(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBounds(0, 0, window.getWidth(), window.getHeight());
        mainPanel.setLayout(null);

        JPanel licenseView = new JPanel();
        licenseView.setBounds(mainPanel.getBounds());
        licenseView.setVisible(false);
        licenseView.setLayout(null);

        String defaultDesc = "This is the license that %s is licensed under. The license defines how, and in what manner, the software may be used.";

        JTextArea description = new JTextArea(3, 50);
        description.setBounds(-1, -1, window.getWidth() - 15, 50);
        description.setBackground(window.getBackground());
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setText(defaultDesc);
        description.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 1));

        JTextArea licenseText = new JTextArea(1024, 64);
        licenseText.setEditable(false);
        licenseText.setLineWrap(true);
        licenseText.setWrapStyleWord(true);

        JScrollPane licenseScroll = new JScrollPane(licenseText);
        licenseScroll.setBorder(null);
        licenseScroll.setBounds(0, description.getY() + description.getHeight() + 1, window.getWidth() - 17, 530);
        licenseScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        licenseText.setVisible(true);

        JButton backButton = new JButton("Back");
        backButton.setBounds(308, 595, 64, 30);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                description.setText(defaultDesc);
                licenseView.setVisible(false);

                comps.forEach((name, comp) -> {
                    comp.setVisible(true);
                    comp.setEnabled(true);
                });
            }
        });

        licenseView.add(description);
        licenseView.add(licenseScroll);
        licenseView.add(backButton);

        JTabbedPane options = new JTabbedPane(JTabbedPane.TOP);
        options.setBounds(0, 0, mainPanel.getWidth(), mainPanel.getHeight());

        mainPanel.add(options);
        
        JPanel projectInfo = new JPanel();
        projectInfo.setBounds(mainPanel.getBounds());
        projectInfo.setLayout(null);

        JTextArea information = new JTextArea(60, 51);
        information.setLineWrap(true);
        information.setWrapStyleWord(true);
        information.setEditable(false);
        information.setBackground(window.getBackground());
        information.setText("Coming soon.");

        JPanel licenseInfo = new JPanel();
        licenseInfo.setLayout(null);

        JButton rblxinfoviewerLicense = new JButton("RBLXInfoViewer");
        rblxinfoviewerLicense.setBounds(30, 20, 150, 50);
        rblxinfoviewerLicense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String viewing = rblxinfoviewerLicense.getText();

                if (currentlyLoadedLicense == null || !currentlyLoadedLicense.equals(viewing)) {
                    currentlyLoadedLicense = viewing;
                    licenseText.setText("");
                    
                    InputStream inp = AboutMenu.class.getResourceAsStream("assets/licenses/" + viewing + "_LICENSE.txt");
                    
                    try (Scanner reader = new Scanner(inp)) {
                        licenseText.append(reader.nextLine());

                        while (reader.hasNext()) {
                            licenseText.append("\n");
                            licenseText.append(reader.nextLine());
                        }
                    }

                    licenseText.setCaretPosition(0);
                }

                description.setText(String.format(defaultDesc, viewing));

                licenseView.setVisible(true);

                comps.forEach((name, comp) -> {
                    comp.setVisible(false);
                    comp.setEnabled(false);
                });
            }
        });

        comps.put("RBLXInfoViewer", rblxinfoviewerLicense);

        JButton jsonjavaLicense = new JButton("JSON-Java");
        jsonjavaLicense.setBounds(window.getWidth() - 200, 20, 150, 50);
        jsonjavaLicense.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String viewing = jsonjavaLicense.getText();

                if (currentlyLoadedLicense == null || !currentlyLoadedLicense.equals(viewing)) {
                    currentlyLoadedLicense = viewing;
                    licenseText.setText("");

                    InputStream inp = AboutMenu.class.getResourceAsStream("assets/licenses/" + viewing + "_LICENSE.txt");
                    
                    try (Scanner reader = new Scanner(inp)) {
                        licenseText.append(reader.nextLine());

                        while (reader.hasNext()) {
                            licenseText.append("\n");
                            licenseText.append(reader.nextLine());
                        }
                    }

                    licenseText.setCaretPosition(0);
                }

                description.setText(String.format(defaultDesc, viewing));

                licenseView.setVisible(true);
                
                comps.forEach((name, comp) -> {
                    comp.setVisible(false);
                    comp.setEnabled(false);
                });
            }
        }); // TODO: shorten this copy and paste code

        comps.put("JSON-Java", jsonjavaLicense);

        licenseInfo.add(rblxinfoviewerLicense);
        licenseInfo.add(jsonjavaLicense);
        licenseInfo.add(licenseView);

        options.addTab("Project", information);
        options.addTab("Licenses", licenseInfo);

        mainPanel.add(options);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                MainWindow.toolbar.onMenuExit("About");
                currentlyLoadedLicense = null;
            }
        });

        window.add(mainPanel);

        window.setVisible(true);
    }

}
