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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.ClientInfo;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * This is a "remote" strategy, uses HTTP GET for information fetch from the remoteUrl. Note: this class uses Restlet
 * Client implementation to do it. Note: this implementation will follow redirects, up to 3 times.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "http-get" )
public class HttpGetDiscoveryStrategy
    extends AbstractRemoteDiscoveryStrategy
{
    @Requirement
    private GlobalRestApiSettings restApiSettings;

    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException,
            IOException
    {
        DiscoveryResponse dr = new DiscoveryResponse( request );

        // handle
        RequestResult response = handleRequest( getRemoteUrl( request ) );

        if ( response != null )
        {
            try
            {
                BufferedReader reader = new BufferedReader( new InputStreamReader( response.getInputStream() ) );
    
                dr.setVersion( reader.readLine() );
    
                dr.setSuccessful( true );
            }
            finally
            {
                response.close();
            }
        }

        return dr;
    }

    protected Request getRestRequest( String url )
    {
        Request rr = new Request( Method.GET, url );

        rr.setReferrerRef( restApiSettings.getBaseUrl() );

        ClientInfo ci = new ClientInfo();

        ci.setAgent( formatUserAgent() );

        rr.setClientInfo( ci );

        return rr;
    }

}
