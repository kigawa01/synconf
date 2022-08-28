package net.kigawa.synconf.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OsUtil
{
    public static final String OS_NAME = System.getProperty("os.name");

    public static String createBinaryFilename(String command)
    {
        return getOs().binaryFilenameCreator.create(command);
    }

    public static Path getVariablePath()
    {
        return switch (getOs()) {
            case Linux -> Paths.get("/var");
            default -> throw new UnsupportedOperationException(getOs() + " is not supported");
        };
    }

    public static Path getLogPath()
    {
        return PathUtil.appendPath(getVariablePath(), "log");
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
        Windows("windows", command -> command + ".exe"),
        Linux("linux", command -> command),
        ;

        public final String name;
        public final BinaryFilenameCreator binaryFilenameCreator;

        OsType(String name, BinaryFilenameCreator binaryFilenameCreator)
        {
            this.name = name;
            this.binaryFilenameCreator = binaryFilenameCreator;
        }

        public boolean isMatch(String name)
        {
            return this.name.toLowerCase().contains(name.toLowerCase());
        }
    }

    public interface BinaryFilenameCreator
    {
        String create(String command);
    }
}