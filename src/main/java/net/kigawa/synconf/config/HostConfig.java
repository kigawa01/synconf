package net.kigawa.synconf.config;

import java.util.HashMap;
import java.util.Map;

public final class HostConfig
{
    private Map<String, String> repoPathToAbsolutePath;

    public HostConfig()
    {
        this.repoPathToAbsolutePath = new HashMap<>();
    }

    public Map<String, String> getRepoPathToAbsolutePath()
    {
        return repoPathToAbsolutePath;
    }

    public void setRepoPathToAbsolutePath(Map<String, String> repoPathToAbsolutePath)
    {
        this.repoPathToAbsolutePath = repoPathToAbsolutePath;
    }
}
