package net.kigawa.synconf.util;

import net.kigawa.kutil.kutil.KutilString;
import net.kigawa.kutil.log.log.KLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandUtil
{
    public static List<String> execCommand(KLogger logger, ExecutorService executorService, String... commands) throws Exception
    {
        var list = new LinkedList<String>();

        try {
            Process process = Runtime.getRuntime().exec(commands);

            executorService.execute(() -> {
                logger.info("> open command out: " + KutilString.insertSymbol(" ", List.of(commands)));
                try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    while (process.isAlive()) {
                        var line = reader.readLine();
                        if (line == null) break;
                        logger.info(line);
                    }
                } catch (IOException e) {
                    logger.warning(e);
                }
                logger.info("> close command out");
            });

            process.waitFor(5, TimeUnit.MINUTES);
            process.destroy();
        } catch (IOException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            throw new Exception(e);
        }

        return list;
    }
}
