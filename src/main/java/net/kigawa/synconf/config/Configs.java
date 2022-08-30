package net.kigawa.synconf.config;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Path;

public class Configs
{
    public static <T> T loadConfig(Path path, Class<T> configClass) throws IOException
    {
        try (var reader = new BufferedReader(new FileReader(path.toFile()))) {
            return new Yaml().loadAs(reader, configClass);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static void saveConfig(Path path, Object config) throws IOException
    {
        try (var writer = new BufferedWriter(new FileWriter(path.toFile()));) {
            new Yaml().dump(config, writer);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}
