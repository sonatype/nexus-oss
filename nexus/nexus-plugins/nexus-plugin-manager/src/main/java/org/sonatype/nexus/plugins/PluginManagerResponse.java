package org.sonatype.nexus.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.plugin.metadata.GAVCoordinate;

public class PluginManagerResponse
{
    private final GAVCoordinate originator;

    private List<PluginResponse> processedPlugins;

    public PluginManagerResponse( GAVCoordinate originator )
    {
        this.originator = originator;
    }

    public GAVCoordinate getOriginator()
    {
        return originator;
    }

    public boolean isSuccessful()
    {
        for ( PluginResponse pluginResponse : getProcessedPluginResponses() )
        {
            if ( !pluginResponse.isSuccessful() )
            {
                return false;
            }
        }

        return true;
    }

    public void addPluginResponse( PluginResponse response )
    {
        getProcessedPluginResponses().add( response );
    }

    public void addPluginManagerResponse( PluginManagerResponse response )
    {
        for ( PluginResponse pluginResponse : response.getProcessedPluginResponses() )
        {
            addPluginResponse( pluginResponse );
        }
    }

    public List<GAVCoordinate> getProcessedPluginCoordinates()
    {
        List<PluginResponse> responses = getProcessedPluginResponses();

        ArrayList<GAVCoordinate> result = new ArrayList<GAVCoordinate>( responses.size() );

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

    // ==

    public String formatAsString( boolean detailed )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "Plugin manager action against plugin \"" ).append( getOriginator().toCompositeForm() )
            .append( "\" execution was: " ).append( isSuccessful() ? "SUCCESFUL" : "NOT SUCCESSFUL" ).append( "\n" );

        sb.append( "Following plugins were processed:\n" );

        for ( PluginResponse response : getProcessedPluginResponses() )
        {
            sb.append( response.formatAsString( detailed ) );
        }

        return sb.toString();
    }
}
