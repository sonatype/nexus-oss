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
package org.sonatype.nexus.plugins.ithelper.log;

import java.util.Date;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "LogHelperResource" )
public class LogHelperPlexusResource
    extends AbstractPlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "anon" );
    }

    @Override
    public String getResourceUri()
    {
        return "/loghelper";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        String loggerName = form.getFirstValue( "loggerName" );
        String level = form.getFirstValue( "level" );
        String message = form.getFirstValue( "message" );
        String exceptionType = form.getFirstValue( "exceptionType" );
        String exceptionMessage = form.getFirstValue( "exceptionMessage" );

        if ( message == null )
        {
            message = "A log message at " + new Date();
        }

        Throwable exception = null;
        if ( exceptionType != null || exceptionMessage != null )
        {
            if ( exceptionMessage == null )
            {
                exceptionMessage = "An exception thrown at " + new Date();
            }
            exception = new Exception( exceptionMessage );
            if ( exceptionType != null )
            {
                try
                {
                    exception =
                        (Throwable) Class.forName( exceptionType ).getConstructor( String.class ).newInstance(
                            exceptionMessage );
                }
                catch ( Exception e )
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        Logger logger;
        if ( loggerName == null )
        {
            logger = LoggerFactory.getLogger( this.getClass() );
        }
        else
        {
            logger = LoggerFactory.getLogger( loggerName );
        }

        if ( level == null )
        {
            level = "INFO";
        }
        if ( level.equalsIgnoreCase( "trace" ) )
        {
            if ( exception == null )
            {
                logger.trace( message );
            }
            else
            {
                logger.trace( message, exception );
            }
        }
        else if ( level.equalsIgnoreCase( "debug" ) )
        {
            if ( exception == null )
            {
                logger.debug( message );
            }
            else
            {
                logger.debug( message, exception );
            }
        }
        else if ( level.equalsIgnoreCase( "warn" ) )
        {
            if ( exception == null )
            {
                logger.warn( message );
            }
            else
            {
                logger.warn( message, exception );
            }
        }
        else if ( level.equalsIgnoreCase( "error" ) )
        {
            if ( exception == null )
            {
                logger.error( message );
            }
            else
            {
                logger.error( message, exception );
            }
        }
        else
        {
            if ( exception == null )
            {
                logger.info( message );
            }
            else
            {
                logger.info( message, exception );
            }
        }

        response.setStatus( Status.SUCCESS_OK );
        return "OK";
    }

}