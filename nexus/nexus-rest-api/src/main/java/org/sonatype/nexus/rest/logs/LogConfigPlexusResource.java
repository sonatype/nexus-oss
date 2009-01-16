/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.logs;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.log.SimpleLog4jConfig;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.LogConfigResource;
import org.sonatype.nexus.rest.model.LogConfigResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author juven
 */
@Component( role = PlexusResource.class, hint = "logConfig" )
public class LogConfigPlexusResource
    extends AbstractNexusPlexusResource
{
    public LogConfigPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new LogConfigResourceResponse();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:logconfig]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/log/config";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        LogConfigResourceResponse result = new LogConfigResourceResponse();

        try
        {
            SimpleLog4jConfig logConfig = getNexus().getLogConfig();

            LogConfigResource data = new LogConfigResource();

            data.setRootLogger( logConfig.getRootLogger() );

            data.setFileAppenderLocation( logConfig.getFileAppenderLocation() );

            data.setFileAppenderPattern( logConfig.getFileAppenderPattern() );

            result.setData( data );

            return result;
        }
        catch ( IOException e )
        {
            getLogger().warn( "Could not load log configuration!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        LogConfigResourceResponse requestResource = (LogConfigResourceResponse) payload;

        if ( requestResource == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
        }

        try
        {
            LogConfigResource data = requestResource.getData();

            SimpleLog4jConfig logConfig = new SimpleLog4jConfig(
                data.getRootLogger(),
                data.getFileAppenderLocation(),
                data.getFileAppenderPattern() );

            getNexus().setLogConfig( logConfig );

            LogConfigResourceResponse responseResource = new LogConfigResourceResponse();

            responseResource.setData( data );

            return responseResource;
        }
        catch ( IOException e )
        {
            getLogger().warn( "Could not set log configuration!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
    }

}
