package org.sonatype.nexus.plugins.plugin.console.api;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.plugin.console.PluginConsoleManager;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoListResponseDTO;
import org.sonatype.nexus.plugins.plugin.console.model.PluginInfo;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

import com.thoughtworks.xstream.XStream;

/**
 * @author juven
 */
@Component( role = PlexusResource.class, hint = "PluginInfoListPlexusResource" )
public class PluginInfoListPlexusResource
    extends AbstractPlexusResource
{
    @Requirement
    private PluginConsoleManager pluginConsoleManager;

    public PluginInfoListPlexusResource()
    {
        this.setReadable( true );
        this.setModifiable( false );
    }

    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );

        xstream.processAnnotations( PluginInfoDTO.class );
        xstream.processAnnotations( PluginInfoListResponseDTO.class );

        xstream.registerLocalConverter( PluginInfoListResponseDTO.class, "data", new AliasingListConverter(
            PluginInfoDTO.class,
            "pluginInfo" ) );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/plugin_console/plugin_infos";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:pluginconsoleplugininfos]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PluginInfoListResponseDTO result = new PluginInfoListResponseDTO();

        for ( PluginInfo pluginInfo : pluginConsoleManager.listPluginInfo() )
        {
            result.addPluginInfo( nexusToRestModel( pluginInfo ) );
        }

        return result;
    }

    private PluginInfoDTO nexusToRestModel( PluginInfo pluginInfo )
    {
        PluginInfoDTO result = new PluginInfoDTO();

        result.setName( pluginInfo.getName() );
        result.setStatus( pluginInfo.getStatus() );
        result.setVersion( pluginInfo.getVersion() );
        result.setDescription( pluginInfo.getDescription() );
        result.setSite( pluginInfo.getSite() );
        result.setScmVersion( StringUtils.isEmpty( pluginInfo.getScmVersion() ) ? "N/A" : pluginInfo.getScmVersion() );
        result.setScmTimestamp( StringUtils.isEmpty( pluginInfo.getScmTimestamp() ) ? "N/A" : pluginInfo
            .getScmTimestamp() );
        result.setFailureReason( pluginInfo.getFailureReason() );

        return result;
    }

}
