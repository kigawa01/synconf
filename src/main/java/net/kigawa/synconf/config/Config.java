package net.kigawa.synconf.config;

public class Config
{
    long loopWait;
    int port;

    public Config()
    {
        loopWait = 60;
        port = 10000;
    }

    public long getLoopWait()
    {
        return loopWait;
    }

    public void setLoopWait(long loopWait)
    {
        this.loopWait = loopWait;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}
