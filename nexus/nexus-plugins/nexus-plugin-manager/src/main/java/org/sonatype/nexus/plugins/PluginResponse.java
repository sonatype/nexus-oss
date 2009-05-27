package org.sonatype.nexus.plugins;

public class PluginResponse
{
    private PluginCoordinates pluginCoordinates;

    private boolean succesful;

    private Throwable throwable;

    public PluginCoordinates getPluginCoordinates()
    {
        return pluginCoordinates;
    }

    public void setPluginCoordinates( PluginCoordinates pluginCoordinates )
    {
        this.pluginCoordinates = pluginCoordinates;
    }

    public boolean isSuccesful()
    {
        return succesful;
    }

    public void setSuccesful( boolean succesful )
    {
        this.succesful = succesful;
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
