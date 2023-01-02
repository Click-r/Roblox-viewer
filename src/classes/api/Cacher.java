package classes.api;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import classes.Player;

import main.Controller;

public class Cacher {
    final static boolean runningAsJar = Controller.runningAsJar;
    private static Path cacheDir;

    public Cacher() {
        if (cacheDir == null) {
            String current = System.getProperty("user.dir");
            Path dirPath = Paths.get(current + "\\cache");

            if (Files.isDirectory(dirPath) && runningAsJar)
                cacheDir = dirPath;
        }
    }

    public void writePlayerObject(Player plr) {
        String saveAs = "Player_" + System.currentTimeMillis();

        if (runningAsJar) {
            try (ObjectOutputStream objOutput = new ObjectOutputStream(new FileOutputStream(cacheDir + "\\players\\" + saveAs + ".plyr"))) {
                objOutput.writeObject(plr);
            } catch (IOException excp) {
                excp.printStackTrace();
            }
        }
    }

    public Object readPlayerObject(String fileName) {
        if (runningAsJar) {
            try (ObjectInputStream objInput = new ObjectInputStream(new FileInputStream(cacheDir + "\\players\\" + fileName + ".plyr"))) {
                return objInput.readObject();
            } catch (IOException | ClassNotFoundException excp) {
                excp.printStackTrace();
            }
        }

        return null;
    }

}
