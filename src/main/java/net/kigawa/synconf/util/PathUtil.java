package net.kigawa.synconf.util;

import java.nio.file.Path;

public class PathUtil
{
    public static Path appendPath(Path path, String more)
    {
        return Path.of(path.toString(), more);
    }
}
