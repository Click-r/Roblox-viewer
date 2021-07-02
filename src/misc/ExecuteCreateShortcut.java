package misc;

// import java.io.BufferedReader;
import java.io.IOException;
// import java.io.InputStreamReader;

import main.Controller;

import ui.ErrorHandler;

public class ExecuteCreateShortcut {
    public static void RunScript() {
        if (Controller.runningAsJar) {

            String scriptPath = System.getProperty("user.dir") + "/misc/CreateShortcut.vbs";

            Runtime runtime = Runtime.getRuntime();

            try {
                Process cmd = runtime.exec("cscript " + scriptPath);

                // BufferedReader output = new BufferedReader(new InputStreamReader(cmd.getInputStream()));

                // new Thread(new Runnable() {
                //     String line = null;

                //     public void run() {
                //         try {
                //             while ((line = output.readLine()) != null) {
                //                 System.out.println(line);
                //             }
                //         } catch (IOException ie) {
                //             ie.printStackTrace();
                //         }
                //     }
                // }).start();

                // ^^ those are for debugging purposes

                cmd.waitFor();
            } catch (IOException err) {
                ErrorHandler.report(err);
            } catch (InterruptedException inter) {}

            // TODO: add osx, linux, unix, etc. support (shouldn't be too hard since all of those create symlinks using ln -s)
        }
    }
}
