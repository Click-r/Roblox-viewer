package ui.gui.utilities;

import java.util.HashMap;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import java.awt.Desktop;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;

import java.io.IOException;
import java.io.InputStream;

import java.net.URI;
import java.net.URISyntaxException;

import main.Controller;

import ui.gui.main.MainWindow;

public class AboutMenu extends JFrame {

    private static HashMap<String, JButton> comps = new HashMap<>();
    private static String currentlyLoadedLicense = null;

    private static ActionListener constructListener(String viewing, JPanel licensePanel, JTextArea licenseDisplay, JTextArea licenseDescription) {
        String defaultDesc = "This is the license that %s is licensed under. The license defines how, and in what manner, the software may be used.";

        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentlyLoadedLicense == null || !currentlyLoadedLicense.equals(viewing)) {
                    currentlyLoadedLicense = viewing;
                    licenseDisplay.setText("");
                    
                    InputStream inp = AboutMenu.class.getResourceAsStream("/ui/assets/licenses/" + viewing + "_LICENSE.txt");
                    
                    try (Scanner reader = new Scanner(inp)) {
                        while (reader.hasNext())
                            licenseDisplay.append(reader.nextLine() + "\n");
                    }

                    licenseDisplay.setCaretPosition(0);
                }

                licenseDescription.setText(String.format(defaultDesc, viewing));

                licensePanel.setVisible(true);

                comps.forEach((name, comp) -> {
                    comp.setVisible(false);
                    comp.setEnabled(false);
                });
            }
        };
    }

    @SuppressWarnings("static-access")
    public AboutMenu() {
        final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

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

        JTextArea description = new JTextArea(3, 50);
        description.setBounds(-1, -1, window.getWidth() - 16, 50);
        description.setBackground(window.getBackground());
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
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
        backButton.setCursor(handCursor);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                description.setText("");
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

        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(null);

        int paraWidth = window.getWidth() - 50;

        JTextPane title = new JTextPane();
        title.setBounds(5, 0, window.getWidth() - 50, 35);
        title.setEditable(false);
        title.setBackground(window.getBackground());
        title.setText("General Information");
        title.setFont(new Font(title.getFont().getFontName(), title.getFont().getStyle(), 25));

        JTextPane introduction = new JTextPane();
        introduction.setBounds(5, title.getHeight() + 4, paraWidth, 100);
        introduction.setBackground(window.getBackground());
        introduction.setEditable(false);
        introduction.setText(
            "Hello! Thank you for using RBLXInfoViewer. I hope you find it useful for whatever purpose it may serve. " +
            "This program was initially designed to improve my Java knowledge. However, it quickly evolved into something " +
            "much more. Although the principal objective of this program - presenting data from the ROBLOX API endpoints - " +
            "is simple, showing said data conveniently, in a user-friendly way, requires more thought. That, along with " +
            "ensuring ease of accessibility, is why I decided to tackle such a project."
        );

        JTextPane contributingAndMisc = new JTextPane();
        contributingAndMisc.setBounds(5, introduction.getY() + introduction.getHeight() + 6, paraWidth, 180);
        contributingAndMisc.setBackground(window.getBackground());
        contributingAndMisc.setEditable(false);
        contributingAndMisc.setText(
            "Now that we're on the same page, let's move on to how you can get involved. As you may know, this project " +
            "is open-source, meaning that anyone (with a GitHub account) can contribute to it. If you have a GitHub account, " +
            "you can submit pull requests to implement features. Likewise, you can also create issues, which can range from " +
            "feature requests to bug reports. If you do decide to create a GitHub account, or already have one, and would " +
            "like to support the project, then I'd greatly appreciate it if you gave it a star :)\n\nIf you don't want to " +
            "create a GitHub account, and would still like to make your voice heard, then you are welcome to contact me on " +
            "ROBLOX through messages: my username is Cli_ck. To deter spam, I have my privacy settings set in a way that " +
            "requires you to follow me, to message me.\n\nNow that we've established that: let's move on " +
            "to the frequently asked questions."
        );

        JTextPane faqTitle = new JTextPane();
        faqTitle.setBounds(5, contributingAndMisc.getY() + contributingAndMisc.getHeight() + 4, paraWidth, 35);
        faqTitle.setBackground(window.getBackground());
        faqTitle.setEditable(false);
        faqTitle.setText("FAQ");
        faqTitle.setFont(new Font(faqTitle.getFont().getFontName(), faqTitle.getFont().getStyle(), 25));

        JPanel faqPanel = new JPanel();
        faqPanel.setLayout(null);
        faqPanel.setBounds(-1, faqTitle.getY() + faqTitle.getHeight() + 4, paraWidth + 23 + 11, 270);

        JScrollPane faqScroll = new JScrollPane(faqPanel ,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        faqScroll.getVerticalScrollBar().setUnitIncrement(16);
        faqScroll.setBounds(faqPanel.getBounds());

        JTextPane speedQuestion = new JTextPane();
        speedQuestion.setBounds(6, 0, paraWidth, 25);
        speedQuestion.setBackground(window.getBackground());
        speedQuestion.setEditable(false);
        speedQuestion.setText("What can I do to speed up the search?");
        speedQuestion.setFont(new Font(speedQuestion.getFont().getFontName(), speedQuestion.getFont().getStyle(), 17));

        JTextPane speedAnswer = new JTextPane();
        speedAnswer.setBounds(6, speedQuestion.getY() + speedQuestion.getHeight() + 3, paraWidth, 220);
        speedAnswer.setBackground(window.getBackground());
        speedAnswer.setEditable(false);
        speedAnswer.setText(
            "How fast a request completes depends on a plethora of factors: your internet connection, the " +
            "stability of ROBLOX's web servers, distance to ROBLOX's web servers, the number of redirects, etc. " +
            "Imagine making all of those requests, one after the other, while accounting for the variety of " +
            "factors that I listed above - that'd take forever. That is why RBLXInfoViewer utilizes multithreading " +
            "to hasten the look-up.\n\nIf you haven't heard of multithreading before, let me explain: your processor " +
            "(CPU) likely runs many tasks concurrently on its different cores, divided into threads. Multithreaded " +
            "applications delegate tasks to different threads to make sure those tasks compute simultaneously, " +
            "allowing other threads to continue execution. As you can imagine, this helps make a lot of web " +
            "requests at the same time.\n\nSo, it uses multithreading, which is dependant on how many threads the " +
            "machine has. RBLXInfoViewer, by default, limits itself to 5 threads, which carry out the requests. " +
            "However, you can change that in the settings tab, advanced section."
        );

        JTextPane autoUpdateQ = new JTextPane();
        autoUpdateQ.setBounds(6, speedAnswer.getY() + speedAnswer.getHeight() + 4, paraWidth, 25);
        autoUpdateQ.setBackground(window.getBackground());
        autoUpdateQ.setEditable(false);
        autoUpdateQ.setText("Does this auto-update?");
        autoUpdateQ.setFont(new Font(autoUpdateQ.getFont().getFontName(), autoUpdateQ.getFont().getStyle(), 17));

        JTextPane autoUpdateA = new JTextPane();
        autoUpdateA.setBounds(6, autoUpdateQ.getY() + autoUpdateQ.getHeight() + 3, paraWidth, 70);
        autoUpdateA.setBackground(window.getBackground());
        autoUpdateA.setEditable(false);
        autoUpdateA.setText(
            "There is currently no auto-update feature. I'd have to make significant changes to how the " +
            "program works for that to be possible. I have thought about it and, given enough time, may " +
            "eventually make it - however, currently, it's not very high on the list of priorities. " +
            "Nevertheless, you can still stay updated on new releases and commits by putting this project " +
            "on your GitHub watch list."
        );

        JTextPane sourceQuestion = new JTextPane();
        sourceQuestion.setBounds(6, autoUpdateA.getY() + autoUpdateA.getHeight() + 4, paraWidth, 25);
        sourceQuestion.setBackground(window.getBackground());
        sourceQuestion.setEditable(false);
        sourceQuestion.setText("Where can I find the source code?");
        sourceQuestion.setFont(new Font(sourceQuestion.getFont().getFontName(), sourceQuestion.getFont().getStyle(), 17));

        JButton sourceAnswer = new JButton("Here.");
        sourceAnswer.setCursor(handCursor);
        sourceAnswer.setBounds(9, sourceQuestion.getY() + sourceQuestion.getHeight() + 3, 65, 35);
        sourceAnswer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    URI link = new URI("https://github.com/Click-r/Roblox-viewer");

                    Desktop dsk = Desktop.getDesktop();
            
                    if (Desktop.isDesktopSupported() && dsk.isSupported(Desktop.Action.BROWSE))
                        dsk.browse(link);
                } catch (IOException | URISyntaxException exc) {}
            }
        });

        JTextPane plansQuestion = new JTextPane();
        plansQuestion.setBounds(6, sourceAnswer.getY() + sourceAnswer.getHeight(), paraWidth, 25);
        plansQuestion.setBackground(window.getBackground());
        plansQuestion.setEditable(false);
        plansQuestion.setText("What features should I expect in the future?");
        plansQuestion.setFont(new Font(plansQuestion.getFont().getFontName(), plansQuestion.getFont().getStyle(), 17));

        JTextPane plansAnswer = new JTextPane();
        plansAnswer.setBounds(6, plansQuestion.getY() + plansQuestion.getHeight() + 3, paraWidth, 120);
        plansAnswer.setBackground(window.getBackground());
        plansAnswer.setEditable(false);
        plansAnswer.setText(
            "All the features I plan to implement can be found in the notes of the latest release on the GitHub " +
            "page. I usually start and work on new features whenever I happen to have a good deal of free time " +
            "- my academic life takes precedence over my hobby projects and online presence.\n\nAsides from all " +
            "the features I have lined up in the release notes, I also have some that I haven't yet disclosed. " +
            "That is because I'm either still trying to figure out how the feature would work or because I think " +
            "it would require too much unnecessary effort."
        );

        JTextPane limitsQuestion = new JTextPane();
        limitsQuestion.setBounds(6, plansAnswer.getY() + plansAnswer.getHeight() + 4, paraWidth, 25);
        limitsQuestion.setBackground(window.getBackground());
        limitsQuestion.setEditable(false);
        limitsQuestion.setText("Does this have any limitations?");
        limitsQuestion.setFont(new Font(limitsQuestion.getFont().getFontName(), limitsQuestion.getFont().getStyle(), 17));

        JTextPane limitsAnswer = new JTextPane();
        limitsAnswer.setBounds(6, limitsQuestion.getY() + limitsQuestion.getHeight() + 3, paraWidth, 90);
        limitsAnswer.setBackground(window.getBackground());
        limitsAnswer.setEditable(false);
        limitsAnswer.setText(
            "That depends on what you define as a limitation. If you consider not being able to fetch data from endpoints " +
            "instantly a limitation, then yes, that is indeed a limitation. For the most part, it has the same limitations " +
            "that the API endpoints themselves possess. For example, the API endpoints may return identical dates for account " +
            "creation and last online for some users. In other instances, select users appear to have missing data on the " +
            "endpoints. These are all rather bizarre, yet there's sadly no way to mitigate such inconsistencies."
        );

        JTextPane simplisticQ = new JTextPane();
        simplisticQ.setBounds(6, limitsAnswer.getY() + limitsAnswer.getHeight() + 4, paraWidth, 25);
        simplisticQ.setBackground(window.getBackground());
        simplisticQ.setEditable(false);
        simplisticQ.setText("Why is all the GUI so plain?");
        simplisticQ.setFont(new Font(simplisticQ.getFont().getFontName(), simplisticQ.getFont().getStyle(), 17));

        JTextPane simplisticA = new JTextPane();
        simplisticA.setBounds(6, simplisticQ.getY() + simplisticQ.getHeight() + 3, paraWidth, 30);
        simplisticA.setBackground(window.getBackground());
        simplisticA.setEditable(false);
        simplisticA.setText("I don't like writing GUIs.");

        JTextPane discordQuestion = new JTextPane();
        discordQuestion.setBounds(6, simplisticA.getY() + simplisticA.getHeight() + 4, paraWidth, 25);
        discordQuestion.setBackground(window.getBackground());
        discordQuestion.setEditable(false);
        discordQuestion.setText("Do you have a Discord server where I can track RBLXInfoViewer's progress?");
        discordQuestion.setFont(new Font(discordQuestion.getFont().getFontName(), discordQuestion.getFont().getStyle(), 17));

        JTextPane discordAnswer = new JTextPane();
        discordAnswer.setBounds(6, discordQuestion.getY() + discordQuestion.getHeight() + 3, paraWidth, 60);
        discordAnswer.setBackground(window.getBackground());
        discordAnswer.setEditable(false);
        discordAnswer.setText(
            "I do not. I would be willing to make one if enough people were to show interest, but, currently, it wouldn't " +
            "serve much of a purpose."
        );

        faqPanel.add(speedQuestion);
        faqPanel.add(speedAnswer);
        faqPanel.add(autoUpdateQ);
        faqPanel.add(autoUpdateA);
        faqPanel.add(sourceQuestion);
        faqPanel.add(sourceAnswer);
        faqPanel.add(plansQuestion);
        faqPanel.add(plansAnswer);
        faqPanel.add(limitsQuestion);
        faqPanel.add(limitsAnswer);
        faqPanel.add(simplisticQ);
        faqPanel.add(simplisticA);
        faqPanel.add(discordQuestion);
        faqPanel.add(discordAnswer);

        aboutPanel.add(title);
        aboutPanel.add(introduction);
        aboutPanel.add(contributingAndMisc);
        aboutPanel.add(faqTitle);
        aboutPanel.add(faqScroll);

        faqPanel.setPreferredSize(new Dimension(faqPanel.getWidth(), faqPanel.getHeight() + 700));

        JPanel licenseInfo = new JPanel();
        licenseInfo.setLayout(null);

        JButton rblxinfoviewerLicense = new JButton("RBLXInfoViewer");
        rblxinfoviewerLicense.setCursor(handCursor);
        rblxinfoviewerLicense.setBounds(30, 20, 150, 50);

        JButton jsonjavaLicense = new JButton("JSON-Java");
        jsonjavaLicense.setCursor(handCursor);
        jsonjavaLicense.setBounds(window.getWidth() - 200, 20, 150, 50);

        comps.put("RBLXInfoViewer", rblxinfoviewerLicense);
        comps.put("JSON-Java", jsonjavaLicense);

        comps.forEach((name, button) -> button.addActionListener(constructListener(name, licenseView, licenseText, description)));

        licenseInfo.add(rblxinfoviewerLicense);
        licenseInfo.add(jsonjavaLicense);
        licenseInfo.add(licenseView);

        options.addTab("Project", aboutPanel);
        options.addTab("Licenses", licenseInfo);

        mainPanel.add(options);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                faqScroll.getVerticalScrollBar().setValue(0);
            }
        });

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
