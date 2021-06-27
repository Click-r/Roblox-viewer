package main;

import java.net.URISyntaxException;
import java.nio.file.*;

import ui.*;

public class Controller {
    public final static String version = "0.7b";
    public final static String title = "RBLXInfoViewer";
    public final static String author = "Cli_ck";

    public final static boolean runningAsJar = Controller.class.getResource("").getProtocol().equals("jar");

    private static void initFiles() {
        String current = System.getProperty("user.dir");
        String target = current + "\\settings";

        Path exists = Paths.get(target);

        if (Files.isDirectory(exists) || !runningAsJar) {
            System.out.println("Settings directory exists.");
        } else {
            Path settingsDir = Paths.get(target);
            ClassLoader controllerLoader = Controller.class.getClassLoader();

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

            } catch (Exception iex) {
                ErrorHandler.report(iex);
            }
        }
    }

    public static void main(String[] args) throws URISyntaxException {
        initFiles();

        MainWindow mw = new MainWindow();
        mw.display();
    }
}
