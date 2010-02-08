/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.logs;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
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
@Path( LogConfigPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class LogConfigPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String RESOURCE_URI = "/log/config";
    
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
        return RESOURCE_URI;
    }

    /**
     * Get the logging configuration.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = LogConfigResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        LogConfigResourceResponse result = new LogConfigResourceResponse();

        try
        {
            SimpleLog4jConfig logConfig = (SimpleLog4jConfig) getNexus().getLogConfig();

            LogConfigResource data = new LogConfigResource();

            data.setRootLoggerLevel( parseRootLoggerLevel( logConfig ) );

            data.setRootLoggerAppenders( parseRootLoggerAppenders( logConfig ) );

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

    /**
     * Update the logging configuration.
     */
    @Override
    @PUT
    @ResourceMethodSignature( input = LogConfigResourceResponse.class, output = LogConfigResourceResponse.class )
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

            String rootLogger = data.getRootLoggerLevel() + ", " + data.getRootLoggerAppenders();

            SimpleLog4jConfig logConfig =
                new SimpleLog4jConfig( rootLogger, data.getFileAppenderLocation(), data.getFileAppenderPattern() );

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

    private String parseRootLoggerLevel( SimpleLog4jConfig config )
    {
        String rootLogger = config.getRootLogger();

        int splitIndex = rootLogger.indexOf( "," );

        return rootLogger.substring( 0, splitIndex ).trim();
    }

    private String parseRootLoggerAppenders( SimpleLog4jConfig config )
    {
        String rootLogger = config.getRootLogger();

        int splitIndex = rootLogger.indexOf( "," );

        return rootLogger.substring( splitIndex + 1 ).trim();
    }
}
