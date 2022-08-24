package net.kigawa.synconf;

import net.kigawa.kutil.kutil.KutilFile;
import net.kigawa.synconf.config.Config;
import net.kigawa.synconf.config.Configs;
import net.kigawa.synconf.config.HostConfig;
import net.kigawa.synconf.util.CommandUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Synconf
{
    private static final String PROJECT_NAME = "synconf";
    private static Synconf synconf;

    private Synconf() throws IOException
    {
        setupSymbolicLink();
        var configPath = Path.of("config.yml");
        var config = Configs.loadConfig(configPath, Config.class);
        Configs.saveConfig(configPath, config);

        while (true) {
            timer();
            try {
                Thread.sleep(1000 * 60 * config.loopWait());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void timer()
    {
        try {
            sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sync() throws Exception
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

    public void setupSymbolicLink() throws IOException
    {
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
        } catch (UnknownHostException e) {
            throw new IOException(e);
        }
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
