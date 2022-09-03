package net.kigawa.synconf.config;

import net.kigawa.synconf.Synconf;
import net.kigawa.synconf.util.CommandUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Path;
import java.util.function.Supplier;

public class Configs
{

    public static <T> T loadConfig(Path path, Class<T> configClass, Supplier<T> defaultValue) throws IOException
    {
        var config = loadConfig(path, configClass);
        return config == null ? defaultValue.get() : config;
    }

    public static <T> T loadConfig(Path path, Class<T> configClass) throws IOException
    {
        var file = path.toFile();
        if (!file.exists()) {
            file.delete();
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        try (var reader = new BufferedReader(new FileReader(path.toFile()))) {
            return new Yaml().loadAs(reader, configClass);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static void saveConfig(Path path, Object config) throws Exception
    {
        CommandUtil.execCommand(Synconf.getInstance().logger, Synconf.getInstance().executorService, "git", "add", path.toString());


        try (var writer = new BufferedWriter(new FileWriter(path.toFile()));) {
            new Yaml().dump(config, writer);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}
