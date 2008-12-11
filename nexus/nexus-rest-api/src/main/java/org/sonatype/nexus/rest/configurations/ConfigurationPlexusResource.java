/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.configurations;

import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.global.GlobalConfigurationPlexusResource;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * A resource that is able to retrieve configurations as stream.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "configuration" )
public class ConfigurationPlexusResource
    extends AbstractNexusPlexusResource
{
    /** The config key used in URI and request attributes */
    public static final String CONFIG_NAME_KEY = "configName";

    /** Name denoting current Nexus configuration */
    public static final String CURRENT_CONFIG_NAME = "current";

    /** Name denoting default Nexus configuration */
    public static final String DEFAULT_CONFIG_NAME = "default";

    @Override
    public Object getPayloadInstance()
    {
        // this is RO resource, and have no payload, it streams the file to client
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/configs/{" + CONFIG_NAME_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/configs/*", "authcBasic,perms[nexus:configuration]" );
    }

    @Override
    public List<Variant> getVariants()
    {
        List<Variant> result = super.getVariants();

        result.clear();

        result.add( new Variant( MediaType.APPLICATION_XML ) );

        return result;
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String configurationName = request
            .getAttributes().get( GlobalConfigurationPlexusResource.CONFIG_NAME_KEY ).toString();

        try
        {
            NexusStreamResponse result;

            if ( DEFAULT_CONFIG_NAME.equals( configurationName ) )
            {
                result = getNexus().getDefaultConfigurationAsStream();
            }
            else if ( CURRENT_CONFIG_NAME.equals( configurationName ) )
            {
                result = getNexus().getConfigurationAsStream();
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No configuration named '"
                    + configurationName + "' found!" );
            }

            return new InputStreamRepresentation( MediaType.valueOf( result.getMimeType() ), result.getInputStream() );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "IOException during configuration retrieval!", e );
        }
    }

}
