package classes.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import classes.Player;

import main.Controller;

import ui.gui.utilities.SearchHistory;

public class Cacher {
    final static boolean runningAsJar = Controller.runningAsJar;
    private static Path cacheDir;

    public static final int maxEntries = 5;
    public static Deque<File> entries = new LinkedBlockingDeque<>(maxEntries);

    public Cacher() {

        if (cacheDir == null) {
            String current = System.getProperty("user.dir");
            Path dirPath = Paths.get(current + "\\cache");

            if (Files.isDirectory(dirPath) && runningAsJar) {
                cacheDir = dirPath;

                File[] playerFiles = Paths.get(dirPath + "\\players").toFile().listFiles();
                Arrays.sort(playerFiles, (file1, file2) -> {
                    String fileStr1 = ((File) file1).getName().replaceAll("[^\\d]", ""); // clear string name of alphabetical characters
                    Date fileDate1 = new Date(Long.valueOf(fileStr1));

                    String fileStr2 = ((File) file2).getName().replaceAll("[^\\d]", "");
                    Date fileDate2 = new Date(Long.valueOf(fileStr2));

                    return fileDate1.compareTo(fileDate2);
                }); // this sorts the files into ascending order based on time created

                for (int idx = playerFiles.length - 1; idx >= 0; idx--) {
                    File file = playerFiles[idx];

                    if (file.isFile() && file.getName().endsWith(".plyr")) { // safety checks
                        boolean accepted = entries.offerLast(file); // offer last because we're iterating backwards

                        if (!accepted) {
                            boolean deleted = file.delete();

                            if (!deleted)
                                System.out.println("Couldn't delete " + file.getName());
                        }
                    }
                }
            }
        }
    }

    public void writePlayerObject(Player plr) {
        String saveAs = "Player_" + System.currentTimeMillis();

        if (runningAsJar) {
            String name = cacheDir + "\\players\\" + saveAs + ".plyr";

            try (ObjectOutputStream objOutput = new ObjectOutputStream(new FileOutputStream(name))) {
                objOutput.writeObject(plr);
                
                File toAdd = new File(name);
                boolean success = entries.offerFirst(toAdd);

                if (!success) {
                    File removed = entries.removeLast();

                    if (!removed.delete()) {
                        System.out.println("Couldn't delete " + removed.getName());
                        return;
                    } else {
                        SearchHistory.lastPlayerRemoved();
                    }
                    
                    entries.offerFirst(toAdd);
                }

                SearchHistory.playerAdded(plr);
            } catch (IOException excp) {
                excp.printStackTrace();
            }
        }
    }

    public Object readPlayerObject(String fileName) {
        if (runningAsJar) {
            try (ObjectInputStream objInput = new ObjectInputStream(new FileInputStream(cacheDir + "\\players\\" + fileName))) {
                return objInput.readObject();
            } catch (IOException | ClassNotFoundException excp) {
                excp.printStackTrace();
            }
        }

        return null;
    }

    public boolean removePlayerObject(Player toRemove) {
        if (runningAsJar) {
            String name = toRemove.name;

            Path path = Paths.get(cacheDir + "\\players\\" + name + ".plyr");
            File playerFile = path.toFile();

            if (playerFile.exists() && playerFile.isFile()) {
                if (entries.remove(playerFile))
                    return playerFile.delete();
            }
        }

        return false;
    }
}
