package net.kigawa.synconf.config;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record HostConfig(Map<String, String> repoPathToAbsolutePath)
{
    public HostConfig()
    {
        this(new HashMap<>());
    }

}
