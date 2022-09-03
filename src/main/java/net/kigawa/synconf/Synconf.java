package net.kigawa.synconf;

import net.kigawa.kutil.kutil.KutilFile;
import net.kigawa.kutil.log.log.KLogger;
import net.kigawa.kutil.log.log.fomatter.KFormatter;
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
import java.util.logging.ConsoleHandler;
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
        synconf = this;
        logger = new KLogger("synconf", null, Level.INFO, KutilFile.getFile(OsUtil.getLogPath().toFile(), "synconf"));
        logger.enable();
        for (var handler : KLogger.getLogger("").getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setFormatter(new KFormatter());
            }
        }

        logger.info("start synconf");
        var configPath = Path.of("config.yml");
        executorService = Executors.newCachedThreadPool();
        config = Configs.loadConfig(configPath, Config.class, Config::new);
        try {
            Configs.saveConfig(configPath, config);
        } catch (Exception e) {
            logger.warning(e);
            end();
        }

        executorService.execute(this::setupSymbolicLink);
        executorService.execute(this::timer);

        socketConnector = new SocketConnector(config.getPort(), logger, executorService);
        if (isEnd()) return;

        while (true) {
            logger.info("check is end");
            synchronized (this) {
                if (isEnd()) {
                    logger.info("stop synconf");
                    break;
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger.warning(e);
                }
            }
        }
        socketConnector.end();

        executorService.shutdown();
        try {
            logger.info("wait services");
            var timeout = !executorService.awaitTermination(5, TimeUnit.MINUTES);
            if (timeout) {
                logger.warning("services timeout");
            }
        } catch (InterruptedException e) {
            logger.warning(e);
        }
        logger.info("disable logger");
        logger.disable();
    }

    private void timer()
    {
        logger.info("run scheduled task");
        try {
            sync();
        } catch (Exception e) {
            logger.warning(e);
        }

        synchronized (this) {
            try {
                wait(1000 * 60 * config.getLoopWait());
            } catch (InterruptedException e) {
                logger.warning(e);
            }
            if (isEnd()) {
                logger.info("stop timer");
                return;
            }
        }
        timer();
    }

    public void end()
    {
        synchronized (this) {
            end = true;
            notifyAll();
        }
    }

    public void resetTimer()
    {
        synchronized (this) {
            notifyAll();
        }
    }

    public synchronized void sync() throws Exception
    {
        logger.info("sync start...");
        CommandUtil.execCommand(logger, executorService, "git", "add", "-u");
        CommandUtil.execCommand(logger, executorService, "git", "commit", "-m", "update");
        CommandUtil.execCommand(logger, executorService, "git", "fetch");
        CommandUtil.execCommand(logger, executorService, "git", "merge");
        CommandUtil.execCommand(logger, executorService, "git", "checkout", "--ours");
        CommandUtil.execCommand(logger, executorService, "git", "add", "-u");
        CommandUtil.execCommand(logger, executorService, "git", "commit", "-m", "merge");
        CommandUtil.execCommand(logger, executorService, "git", "push");
        logger.info("sync end");
    }

    public void setupSymbolicLink()
    {
        logger.info("set up symbol link...");
        try {
            var hostname = InetAddress.getLocalHost().getHostName();
            var configPath = Path.of("hosts", hostname + ".yml");
            var config = Configs.loadConfig(configPath, HostConfig.class, HostConfig::new);
            try {
                Configs.saveConfig(configPath, config);
            } catch (Exception e) {
                logger.warning(e);
                end();
            }

            var reposFolder = KutilFile.getRelativeFile("configs");
            for (var paths : config.getRepoPathToAbsolutePath().entrySet()) {
                var absoluteFile = KutilFile.getRelativeFile(paths.getValue());
                if (absoluteFile.exists()) continue;

                var repoFile = KutilFile.getFile(reposFolder, paths.getKey());
                repoFile.mkdirs();

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
            new Synconf();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
