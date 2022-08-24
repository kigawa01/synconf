package net.kigawa.synconf;

import net.kigawa.kutil.kutil.KutilFile;
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

    public void setupSymbolicLink1() throws IOException
    {
        try {
            var hostname = InetAddress.getLocalHost().getHostName();
            var config = Config.loadConfig(Path.of("hosts", hostname));

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
