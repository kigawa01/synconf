package net.kigawa.synconf;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record Config(Map<String, String> repoPathToAbsolutePath)
{
    public Config()
    {
        this(new HashMap<>());
    }

    public static Config loadConfig(Path path) throws IOException
    {
        try (var reader = new BufferedReader(new FileReader(path.toFile()))) {
            return new Yaml().loadAs(reader, Config.class);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static void saveConfig(Path path, Config config) throws IOException
    {
        try (var writer = new BufferedWriter(new FileWriter(path.toFile()));) {
            new Yaml().dump(config, writer);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}
