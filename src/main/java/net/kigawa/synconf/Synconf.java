package net.kigawa.synconf;

import net.kigawa.synconf.util.CommandUtil;

import java.io.IOException;

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
    }

    public static Synconf getInstance()
    {
        return synconf;
    }

    public static void main(String[] args)
    {
        synconf = new Synconf();
    }
}
