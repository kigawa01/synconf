package net.kigawa.synconf;

import net.kigawa.kutil.kutil.KutilFile;
import net.kigawa.kutil.log.log.KLogger;
import net.kigawa.synconf.config.Config;
import net.kigawa.synconf.config.Configs;
import net.kigawa.synconf.config.HostConfig;
import net.kigawa.synconf.util.CommandUtil;
import net.kigawa.synconf.util.OsUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Synconf
{
    private static final String PROJECT_NAME = "synconf";
    private static Synconf synconf;
    public final ExecutorService executorService;
    public final Config config;
    public final KLogger logger;
    private final SocketConnector socketConnector;
    private boolean end = false;

    private Synconf() throws IOException
    {
        logger = new KLogger("synconf", null, Level.INFO, OsUtil.getLogPath().toFile());
        logger.enable();

        logger.info("start synconf");
        var configPath = Path.of("config.yml");
        config = Configs.loadConfig(configPath, Config.class);
        Configs.saveConfig(configPath, config);
        executorService = Executors.newCachedThreadPool();
        socketConnector = new SocketConnector(config.port(), logger, executorService);

        executorService.execute(this::setupSymbolicLink);
        executorService.execute(() -> {
            try {
                sync();
            } catch (Exception e) {
                logger.warning(e);
                end();
            }
        });

        try {
            var timeout = executorService.awaitTermination(5, TimeUnit.MINUTES);
            if (timeout) {
                logger.warning("services timeout");
                end();
            }
        } catch (InterruptedException e) {
            logger.warning(e);
            end();
        }
        if (isEnd()) return;

        executorService.execute(this::timer);
    }

    private void timer()
    {
        try {
            sync();
        } catch (Exception e) {
            logger.warning(e);
        }

        synchronized (this) {
            try {
                wait(1000 * 60 * config.loopWait());
            } catch (InterruptedException e) {
                logger.warning(e);
            }
            if (isEnd()) return;
        }
        executorService.execute(this::timer);
    }

    public void end()
    {
        synchronized (this) {
            end = true;
            notifyAll();
        }
        socketConnector.end();

        executorService.shutdown();
        try {
            var timeout = executorService.awaitTermination(5, TimeUnit.MINUTES);
            if (timeout) {
                logger.warning("services timeout");
            }
        } catch (InterruptedException e) {
            logger.warning(e);
        }
        logger.disable();
    }

    public void resetTimer()
    {
        synchronized (this) {
            notifyAll();
        }
    }

    public synchronized void sync() throws Exception
    {
        if (!CommandUtil.isCommandExist("git")) throw new Exception("command not found");
        CommandUtil.execCommand("git", "add", "-u");
        CommandUtil.execCommand("git", "commit", "-m", "update");
        CommandUtil.execCommand("git", "fetch");
        CommandUtil.execCommand("git", "marge");
        CommandUtil.execCommand("git", "checkout", "--ours");
        CommandUtil.execCommand("git", "add", "-u");
        CommandUtil.execCommand("git", "commit", "-m", "marge");
    }

    public void setupSymbolicLink()
    {
        logger.info("set up symbol link...");
        try {
            var hostname = InetAddress.getLocalHost().getHostName();
            var configPath = Path.of("hosts", hostname + ".yml");
            var config = Configs.loadConfig(configPath, HostConfig.class);
            Configs.saveConfig(configPath, config);

            var reposFolder = KutilFile.getRelativeFile("configs");
            for (var paths : config.repoPathToAbsolutePath().entrySet()) {
                var absoluteFile = KutilFile.getRelativeFile(paths.getValue());
                if (absoluteFile.exists()) continue;

                var repoFile = KutilFile.getFile(reposFolder, paths.getKey());

                Files.createSymbolicLink(repoFile.toPath(), absoluteFile.toPath());
            }
        } catch (IOException e) {
            logger.warning(e);
            end();
            return;
        }
        logger.info("set upped symbol link");
    }

    public synchronized boolean isEnd()
    {
        return end;
    }

    public static Synconf getInstance()
    {
        return synconf;
    }

    public static void main(String[] args)
    {
        try {
            synconf = new Synconf();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
