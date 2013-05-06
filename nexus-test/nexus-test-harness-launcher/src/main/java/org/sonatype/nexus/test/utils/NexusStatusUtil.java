/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.nexus.test.booter.Jetty8NexusBooter;
import org.sonatype.nexus.test.booter.NexusBooter;

/**
 * Simple util class
 */
public class NexusStatusUtil
{
    protected static Logger log = LoggerFactory.getLogger( NexusStatusUtil.class );

    private final NexusBooter nexusBooter;

    public NexusStatusUtil( final int port )
        throws Exception
    {
        nexusBooter =
            new Jetty8NexusBooter( new File( TestProperties.getAll().get( "nexus.base.dir" ) ).getAbsoluteFile(), port );
    }

    public boolean isNexusRESTStarted()
        throws NexusIllegalStateException
    {
        final String statusURI = AbstractNexusIntegrationTest.nexusBaseUrl + RequestFacade.SERVICE_LOCAL + "status";
        // by not using test context we are only checking anonymously - this may not be a good idea, not sure
        org.apache.commons.httpclient.HttpMethod method = null;
        try
        {
            try
            {
                method = RequestFacade.executeHTTPClientMethod( new GetMethod( statusURI ), false );
            }
            catch ( HttpException ex )
            {
                throw new NexusIllegalStateException( "Problem executing status request: ", ex );
            }
            catch ( IOException ex )
            {
                throw new NexusIllegalStateException( "Problem executing status request: ", ex );
            }

            final int statusCode = method.getStatusCode();
            // 200 if anonymous access is enabled
            // 401 if nexus is running but anonymous access is disabled
            if ( statusCode == 401 )
            {
                return true;
            }
            else if ( statusCode != 200 )
            {
                log.debug( "Status check returned status " + statusCode );
                return false;
            }

            String entityText;
            try
            {
                entityText = method.getResponseBodyAsString();
            }
            catch ( IOException e )
            {
                throw new NexusIllegalStateException( "Unable to retrieve nexus status body", e );
            }

            StatusResourceResponse status =
                (StatusResourceResponse) XStreamFactory.getXmlXStream().fromXML( entityText );
            if ( !SystemState.STARTED.toString().equals( status.getData().getState() ) )
            {
                log.debug( "Status check returned system state " + status.getData().getState() );
                return false;
            }

            return true;
        }
        finally
        {
            if ( method != null )
            {
                method.releaseConnection(); // request facade does this but just making sure
            }
        }
    }

    /**
     * Get Nexus Status, failing if the request response is not successfully returned.
     *
     * @return the status resource
     * @throws NexusIllegalStateException
     */
    public StatusResourceResponse getNexusStatus()
        throws NexusIllegalStateException
    {
        try
        {
            String entityText = RequestFacade.doGetForText( "service/local/status" );
            StatusResourceResponse status =
                (StatusResourceResponse) XStreamFactory.getXmlXStream().fromXML( entityText );
            return status;
        }
        catch ( IOException ex )
        {
            throw new NexusIllegalStateException( "Could not get nexus status", ex );
        }
    }

    public void start( final String testId )
        throws Exception
    {
        if ( isNexusApplicationPortOpen() )
        {
            throw new NexusIllegalStateException( "Port is in use: " + AbstractNexusIntegrationTest.nexusApplicationPort );
        }

        nexusBooter.startNexus( testId );
    }

    public void stop()
        throws Exception
    {
        nexusBooter.stopNexus();
    }

    public boolean isNexusRunning()
    {
        if ( !isNexusApplicationPortOpen() )
        {
            return false;
        }

        try
        {
            return isNexusRESTStarted();
        }
        catch ( NexusIllegalStateException e )
        {
            log.debug( "Problem accessing nexus", e );
        }
        return false;

    }

    private boolean isNexusApplicationPortOpen()
    {
        return isPortOpen( AbstractNexusIntegrationTest.nexusApplicationPort,
            "AbstractNexusIntegrationTest.nexusApplicationPort" );
    }

    /**
     * @param port the port to check for being open
     * @param portName the name of the port we are checking
     * @return true if port is open, false if not
     */
    private boolean isPortOpen( final int port, final String portName )
    {
        Socket sock = null;
        try
        {
            sock = new Socket( "localhost", port );
            return true;
        }
        catch ( UnknownHostException e1 )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( portName + "(" + port + ") is not open: " + e1.getMessage() );
            }
        }
        catch ( IOException e1 )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( portName + "(" + port + ") is not open: " + e1.getMessage() );
            }
        }
        finally
        {
            if ( sock != null )
            {
                try
                {
                    sock.close();
                }
                catch ( IOException e )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Problem closing socket to " + portName + "(" + port + ") : " + e.getMessage() );
                    }
                }
            }
        }
        return false;
    }

    public boolean isNexusStopped()
        throws NexusIllegalStateException
    {
        return !isNexusRunning();
    }
}
