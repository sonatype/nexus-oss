package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginManagerResponse
{
    private RequestResult result;

    private List<PluginResponse> processedPlugins;

    public PluginManagerResponse()
    {
        result = RequestResult.COMPLETELY_EXECUTED;
    }

    public PluginManagerResponse( RequestResult result )
    {
        this();

        setResultForced( result );
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
        if ( this.result == null )
        {
            setResultForced( result );
        }
        else if ( result.compareTo( this.result ) > 0 )
        {
            // is worse then now
            this.result = result;
        }
    }

    public void setResultForced( RequestResult result )
    {
        this.result = result;
    }

    public void addPluginResponse( PluginResponse response )
    {
        getProcessedPluginResponses().add( response );

        if ( !response.isSuccesful() )
        {
            setResult( RequestResult.PARTIALLY_EXECUTED );
        }
    }

    public List<PluginCoordinates> getProcessedPluginCoordinates()
    {
        List<PluginResponse> responses = getProcessedPluginResponses();

        ArrayList<PluginCoordinates> result = new ArrayList<PluginCoordinates>( responses.size() );

        for ( PluginResponse response : responses )
        {
            result.add( response.getPluginCoordinates() );
        }

        return Collections.unmodifiableList( result );
    }

    public List<PluginResponse> getProcessedPluginResponses()
    {
        if ( processedPlugins == null )
        {
            processedPlugins = new ArrayList<PluginResponse>();
        }

        return processedPlugins;
    }
}
