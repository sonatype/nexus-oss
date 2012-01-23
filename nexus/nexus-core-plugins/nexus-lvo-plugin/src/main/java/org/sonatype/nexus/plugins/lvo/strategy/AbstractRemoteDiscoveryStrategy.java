/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.error.reporting.NexusProxyServerConfigurator;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;

public abstract class AbstractRemoteDiscoveryStrategy
    extends AbstractDiscoveryStrategy
{

    @Requirement
    private NexusConfiguration nexusConfig;

    /**
     * For plexus injection.
     */
    protected AbstractRemoteDiscoveryStrategy()
    {
    }

    AbstractRemoteDiscoveryStrategy( final NexusConfiguration nexusConfig )
    {
        this.nexusConfig = nexusConfig;
    }

    /**
     * Format's the user agent string for remote discoveries, if needed. TODO: this method is a copy+paste of the one in
     * AbstractRemoteRepositoryStorage, fix this
     */
    protected String formatUserAgent()
    {
        SystemStatus status = getNexus().getSystemStatus();

        StringBuffer userAgentPlatformInfo = new StringBuffer( "Nexus/" )
            .append( status.getVersion() ).append( " (" ).append( status.getEditionShort() ).append( "; " ).append(
                System.getProperty( "os.name" ) ).append( "; " ).append( System.getProperty( "os.version" ) ).append(
                "; " ).append( System.getProperty( "os.arch" ) ).append( "; " ).append(
                System.getProperty( "java.version" ) ).append( ") " ).append( "LVOPlugin/1.0" );

        return userAgentPlatformInfo.toString();
    }

    protected HttpGetDiscoveryStrategy.RequestResult handleRequest( String url )
    {
        HttpClient client = new HttpClient();

        new NexusProxyServerConfigurator(
            nexusConfig.getGlobalRemoteStorageContext(), getLogger()
        ).applyToClient( client );

        GetMethod method = new GetMethod( url );

        HttpGetDiscoveryStrategy.RequestResult result = null;

        try
        {
            int status = client.executeMethod( method );

            if ( HttpStatus.SC_OK == status )
            {
                result = new HttpGetDiscoveryStrategy.RequestResult( method );
            }
        }
        catch ( IOException e )
        {
            getLogger().debug( "Error retrieving lvo data", e );
        }

        return result;
    }

    protected String getRemoteUrl( DiscoveryRequest request )
    {
        return request.getLvoKey().getRemoteUrl();
    }

    protected static final class RequestResult
    {

        private InputStream is;

        private GetMethod method;

        public RequestResult( GetMethod method )
            throws IOException
        {
            this.is = method.getResponseBodyAsStream();
            this.method = method;
        }

        public InputStream getInputStream()
        {
            return this.is;
        }

        public void close()
        {
            IOUtil.close( is );

            if ( this.method != null )
            {
                this.method.releaseConnection();
            }
        }
    }
}
