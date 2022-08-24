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
        var resultList = CommandUtil.execCommand("git", "add", "-u");
    }

    public void reflectToRepo()
    {

    }

    public void reflectToAbsolut(){

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
