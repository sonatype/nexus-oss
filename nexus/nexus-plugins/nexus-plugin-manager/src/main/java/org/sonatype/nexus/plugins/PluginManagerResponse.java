package org.sonatype.nexus.plugins;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.util.StringUtils;

public class PluginManagerResponse
{
    private RequestResult result;

    private List<PluginResponse> processedPlugins;

    public PluginManagerResponse()
    {
        result = RequestResult.COMPLETED;
    }

    public PluginManagerResponse( RequestResult result )
    {
        this();

        setResultForced( result );
    }

    public boolean isSuccessful()
    {
        return getResult().equals( RequestResult.COMPLETED );
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
            setResult( RequestResult.PARTIAL );
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

    // ==

    public String formatAsString( boolean detailed )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "Plugin manager action execution was: " ).append( getResult().toString() ).append( "\n" );
        sb.append( "\n" );
        sb.append( "Following plugins were processed:\n" );

        for ( PluginResponse response : getProcessedPluginResponses() )
        {
            sb.append( "... " ).append( response.getPluginCoordinates().toString() ).append( " :: " )
                .append( response.isSuccesful() ? "Activated" : "FAILED!" ).append( "\n" );

            if ( !response.isSuccesful() )
            {
                sb.append( "       Reason:" ).append( response.getThrowable().getLocalizedMessage() );

                if ( detailed )
                {
                    StringWriter sw = new StringWriter();

                    response.getThrowable().printStackTrace( new PrintWriter( sw ) );

                    sb.append( "\nStack trace:\n" ).append( sw.toString() );
                }
            }

            if ( detailed && response.getPluginDescriptor() != null )
            {
                PluginDescriptor pluginDescriptor = response.getPluginDescriptor();

                sb.append( "       Detailed report about the plugin \"" ).append(
                                                                                  pluginDescriptor
                                                                                      .getPluginCoordinates()
                                                                                      .toString() ).append( "\":\n\n" );

                sb.append( "         Source: \"" ).append( pluginDescriptor.getSource() ).append( "\":\n" );

                sb.append( "         Plugin defined these components:\n" );

                for ( ComponentDescriptor<?> component : pluginDescriptor.getComponents() )
                {
                    sb.append( "         * FQN of Type \"" ).append( component.getRole() );

                    if ( StringUtils.isNotBlank( component.getRoleHint() ) )
                    {
                        sb.append( "\", named as \"" ).append( component.getRoleHint() );
                    }

                    sb.append( "\", with implementation \"" ).append( component.getImplementation() ).append( "\"\n" );
                }

                if ( !pluginDescriptor.getPluginRepositoryTypes().isEmpty() )
                {
                    sb.append( "\n" );
                    sb.append( "         Plugin defined these custom Repository Types:\n" );

                    for ( PluginRepositoryType repoType : pluginDescriptor.getPluginRepositoryTypes().values() )
                    {
                        sb.append( "         * FQN of Type \"" + repoType.getComponentContract()
                            + "\", to be published at path \"" + repoType.getPathPrefix() + "\"\n" );
                    }
                }

                if ( !pluginDescriptor.getPluginStaticResourceModels().isEmpty() )
                {
                    sb.append( "\n" );
                    sb.append( "         Plugin contributed these static resources:\n" );

                    for ( PluginStaticResourceModel model : pluginDescriptor.getPluginStaticResourceModels() )
                    {
                        sb.append( "         * Resource path \"" + model.getResourcePath()
                            + "\", to be published at path \"" + model.getPublishedPath() + "\", content type \""
                            + model.getContentType() + "\"\n" );
                    }
                }

                sb.append( "\n" );
            }
        }

        return sb.toString();
    }
}
