package net.kigawa.synconf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class CommandUtil
{
    public static boolean isCommandExist(String command)
    {
        for (String folderPath : System.getenv("Path").split(";")) {
            var folder = new File(folderPath);
            if (!folder.isDirectory()) continue;

            File[] files = folder.listFiles();
            if (files == null) continue;

            for (File file : files) {
                if (!file.isFile() || !file.canExecute()) continue;

                return file.getName().equals(OsUtil.createBinaryFilename(command));
            }
        }
        return false;
    }

    public static List<String> execCommand(String... commands) throws Exception
    {
        var list = new LinkedList<String>();

        try {
            Process process = Runtime.getRuntime().exec(commands);

            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (process.isAlive()) {
                    list.add(reader.readLine());
                }
            }

            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            throw new Exception(e);
        }

        return list;
    }
}
