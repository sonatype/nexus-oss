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
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.plexus.jetty.Jetty7;

/**
 * Simple util class
 */
public class NexusStatusUtil
{
    protected static Logger log = LoggerFactory.getLogger( NexusStatusUtil.class );

    private final File bundleBasedir;

    private final Jetty7 jetty7;

    public NexusStatusUtil( final int port )
        throws Exception
    {
        this.bundleBasedir = new File( TestProperties.getAll().get( "nexus.base.dir" ) ).getAbsoluteFile();

        tamperJettyProperties( bundleBasedir, port );

        Map<String, String> ctx = new HashMap<String, String>();
        ctx.put( "bundleBasedir", bundleBasedir.getAbsolutePath() );

        this.jetty7 = new Jetty7( new File( bundleBasedir, "conf/jetty.xml" ), ctx );
    }

    private void tamperJettyProperties( final File basedir, final int port )
        throws Exception
    {
        File jettyProperties = new File( basedir, "conf/jetty.properties" );

        if ( !jettyProperties.isFile() )
        {
            throw new FileNotFoundException( "Jetty properties not found at " + jettyProperties.getAbsolutePath() );
        }

        Properties p = new Properties();
        InputStream in = new FileInputStream( jettyProperties );
        p.load( in );
        IOUtil.close( in );

        p.setProperty( "application-port", String.valueOf( port ) );

        OutputStream out = new FileOutputStream( jettyProperties );
        p.store( out, "NexusStatusUtil" );
        IOUtil.close( out );
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
                debug( "Status check returned status " + statusCode );
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
                debug( "Status check returned system state " + status.getData().getState() );
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

    public void start( String testId )
        throws Exception
    {
        if ( isNexusApplicationPortOpen() )
        {
            throw new NexusIllegalStateException( "Ports in use!!!" );
        }

        jetty7.startJetty();
    }

    public void stop()
        throws Exception
    {
        jetty7.stopJetty();
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
     * This is a hack because due to magic log4j property reloading we lose normal log output sent to logger
     * 
     * @param msg the msg to log at debug level
     */
    private void debug( final String msg )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "NexusStatusUtil.debug() " + msg );
        }
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
            sock = new Socket( "localhost", AbstractNexusIntegrationTest.nexusApplicationPort );
            return true;
        }
        catch ( UnknownHostException e1 )
        {
            if ( log.isDebugEnabled() )
            {
                debug( portName + "(" + port + ") is not open: " + e1.getMessage() );
            }
        }
        catch ( IOException e1 )
        {
            if ( log.isDebugEnabled() )
            {
                debug( portName + "(" + port + ") is not open: " + e1.getMessage() );
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
                        debug( "Problem closing socket to " + portName + "(" + port + ") : " + e.getMessage() );
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
