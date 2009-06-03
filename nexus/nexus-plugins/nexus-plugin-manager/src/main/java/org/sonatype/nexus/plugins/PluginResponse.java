package org.sonatype.nexus.plugins;

public class PluginResponse
{
    private final PluginCoordinates pluginCoordinates;

    private Throwable throwable;

    public PluginResponse( PluginCoordinates pluginCoordinates )
    {
        this.pluginCoordinates = pluginCoordinates;
    }

    public PluginCoordinates getPluginCoordinates()
    {
        return pluginCoordinates;
    }

    public boolean isSuccesful()
    {
        return throwable == null;
    }

    public Throwable getThrowable()
    {
        return throwable;
    }

    public void setThrowable( Throwable throwable )
    {
        this.throwable = throwable;
    }
}
