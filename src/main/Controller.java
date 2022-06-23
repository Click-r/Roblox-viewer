package main;

import java.net.URISyntaxException;

import java.nio.file.*;

import java.io.IOException;

import ui.gui.err.ErrorHandler;
import ui.gui.main.MainWindow;

public class Controller {
    public final static String version = "1.3a";
    public final static String title = "RBLXInfoViewer";
    public final static String author = "Cli_ck";

    public final static boolean runningAsJar = Controller.class.getResource("").getProtocol().equals("jar");

    private static void initFiles() {
        ClassLoader controllerLoader = Controller.class.getClassLoader();

        String current = System.getProperty("user.dir");
        String target = current + "\\settings";

        Path settingsDir = Paths.get(target);

        if (Files.isDirectory(settingsDir) || !runningAsJar) {
            System.out.println("Settings directory exists.");
        } else {
            try {
                Files.createDirectory(settingsDir); // creates initial settings directory

                Path searchDir = Paths.get(settingsDir + "\\search");
                Files.createDirectory(searchDir);
                Files.copy(controllerLoader.getResourceAsStream("settings/search/search.properties"), Paths.get(searchDir + "\\search.properties"));
                // search directory and search properties file

                Path displayDir = Paths.get(settingsDir + "\\display");
                Files.createDirectory(displayDir);
                Files.copy(controllerLoader.getResourceAsStream("settings/display/display.properties"), Paths.get(displayDir + "\\display.properties"));
                // display directory and display properties file

                Path advancedDir = Paths.get(settingsDir + "\\advanced");
                Files.createDirectory(advancedDir);
                Files.copy(controllerLoader.getResourceAsStream("settings/advanced/advanced.properties"), Paths.get(advancedDir + "\\advanced.properties"));
                // advanced directory and advanced properties file

            } catch (IOException iex) {
                ErrorHandler.report(iex);
            }
        } // deals with the settings directory

        target = current + "\\misc";

        Path misc = Paths.get(target);

        if (Files.isDirectory(misc) || !runningAsJar) {
            System.out.println("Misc directory exists.");
        } else {
            try {
                Files.createDirectory(misc);
                Files.copy(controllerLoader.getResourceAsStream("misc/CreateShortcut.vbs"), Paths.get(target + "\\CreateShortcut.vbs"));
            } catch (IOException iex) {
                ErrorHandler.report(iex);
            }
        } // deals with misc directory

        // TODO: clean this up sometime
    }

    public static void main(String[] args) throws URISyntaxException {
        System.setProperty("file.encoding", "UTF-8");

        initFiles();

        MainWindow mw = new MainWindow();
        mw.display();
    }
}
