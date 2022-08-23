package net.kigawa.synconf;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OS_Util
{
    public static final String OS_NAME = System.getProperty("os.name");

    public static Path getVariablePath()
    {
        return switch (getOs()) {
            case Linux -> Paths.get("/var");
            default -> throw new UnsupportedOperationException(getOs() + " is not supported");
        };
    }

    public static OsType getOs()
    {
        for (var osType : OsType.values()) {
            if (osType.isMatch(OS_NAME)) return osType;
        }
        throw new UnsupportedOperationException(getOs() + " is not supported");
    }

    public enum OsType
    {
        Windows("windows"),
        Linux("linux"),
        ;

        private final String name;

        OsType(String name)
        {
            this.name = name;
        }

        public boolean isMatch(String name)
        {
            return this.name.toLowerCase().contains(name.toLowerCase());
        }
    }
}