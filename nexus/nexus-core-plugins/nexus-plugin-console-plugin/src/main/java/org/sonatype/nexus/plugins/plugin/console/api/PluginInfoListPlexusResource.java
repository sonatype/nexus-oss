/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.plugin.console.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.plugin.console.PluginConsoleManager;
import org.sonatype.nexus.plugins.plugin.console.api.dto.DocumentationLinkDTO;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoListResponseDTO;
import org.sonatype.nexus.plugins.plugin.console.api.dto.RestInfoDTO;
import org.sonatype.nexus.plugins.plugin.console.model.DocumentationLink;
import org.sonatype.nexus.plugins.plugin.console.model.PluginInfo;
import org.sonatype.nexus.plugins.plugin.console.model.RestInfo;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

import com.thoughtworks.xstream.XStream;

/**
 * Resource publishing Nexus plugin details.
 */
@Component( role = PlexusResource.class, hint = "PluginInfoListPlexusResource" )
@Path( "/plugin_console/plugin_infos" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class PluginInfoListPlexusResource
    extends AbstractNexusPlexusResource
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
        xstream.processAnnotations( RestInfoDTO.class );

        xstream.registerLocalConverter( PluginInfoListResponseDTO.class, "data", new AliasingListConverter(
            PluginInfoDTO.class, "pluginInfo" ) );

        xstream.registerLocalConverter( PluginInfoDTO.class, "restInfos", new AliasingListConverter( RestInfoDTO.class,
            "restInfo" ) );
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

    /**
     * Returns the list of known Nexus plugins with details describing them.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = PluginInfoListResponseDTO.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PluginInfoListResponseDTO result = new PluginInfoListResponseDTO();

        for ( PluginInfo pluginInfo : pluginConsoleManager.listPluginInfo() )
        {
            result.addPluginInfo( nexusToRestModel( request, pluginInfo ) );
        }

        return result;
    }

    private PluginInfoDTO nexusToRestModel( Request request, PluginInfo pluginInfo )
    {
        PluginInfoDTO result = new PluginInfoDTO();

        result.setName( pluginInfo.getName() );
        result.setStatus( pluginInfo.getStatus() );
        result.setVersion( pluginInfo.getVersion() );
        result.setDescription( pluginInfo.getDescription() );
        result.setSite( pluginInfo.getSite() );

        List<DocumentationLinkDTO> docUrls = new ArrayList<DocumentationLinkDTO>();
        for ( DocumentationLink doc : pluginInfo.getDocumentation() )
        {
            DocumentationLinkDTO docLink = new DocumentationLinkDTO();
            docLink.setLabel( doc.getLabel() );
            docLink.setUrl( createRootReference( request, doc.getUrl() + "/docs/index.html" ).getTargetRef().toString() );
            docUrls.add( docLink );
        }
        result.setDocumentation( docUrls );

        result.setScmVersion( StringUtils.isEmpty( pluginInfo.getScmVersion() ) ? "N/A" : pluginInfo.getScmVersion() );
        result.setScmTimestamp( StringUtils.isEmpty( pluginInfo.getScmTimestamp() ) ? "N/A"
            : pluginInfo.getScmTimestamp() );
        result.setFailureReason( pluginInfo.getFailureReason() );

        for ( RestInfo restInfo : pluginInfo.getRestInfos() )
        {
            RestInfoDTO restInfoDTO = new RestInfoDTO();
            restInfoDTO.setURI( restInfo.getUri() );
            result.addRestInfo( restInfoDTO );
        }

        return result;
    }
}
