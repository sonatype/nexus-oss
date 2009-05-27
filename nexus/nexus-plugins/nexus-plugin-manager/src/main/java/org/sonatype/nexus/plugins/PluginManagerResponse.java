package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.List;

public class PluginManagerResponse
{
    private RequestResult result;

    private List<PluginResponse> processedPlugins;

    public PluginManagerResponse( RequestResult result )
    {
        setResult( result );
    }

    public boolean isSuccessful()
    {
        return getResult().equals( RequestResult.COMPLETELY_EXECUTED );
    }

    public RequestResult getResult()
    {
        return result;
    }

    public void setResult( RequestResult result )
    {
        this.result = result;
    }

    public List<PluginResponse> getProcessedPluginCoordinates()
    {
        if ( processedPlugins == null )
        {
            processedPlugins = new ArrayList<PluginResponse>();
        }

        return processedPlugins;
    }
}
