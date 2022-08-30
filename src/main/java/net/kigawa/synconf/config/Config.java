package net.kigawa.synconf.config;

public record Config(long loopWait, int port)
{
    public Config()
    {
        this(60,100000);
    }
}
