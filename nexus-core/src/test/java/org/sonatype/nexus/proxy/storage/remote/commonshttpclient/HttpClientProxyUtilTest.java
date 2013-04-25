/*
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
package org.sonatype.nexus.proxy.storage.remote.commonshttpclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;

public class HttpClientProxyUtilTest
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final RemoteConnectionSettings remoteConnectionSettings = new RemoteConnectionSettings()
    {
        @Override
        public void setUserAgentCustomizationString( String userAgentCustomizationString )
        {
        }

        @Override
        public void setRetrievalRetryCount( int retrievalRetryCount )
        {
        }

        @Override
        public void setQueryString( String queryString )
        {
        }

        @Override
        public void setConnectionTimeout( int connectionTimeout )
        {
        }

        @Override
        public String getUserAgentCustomizationString()
        {
            return null;
        }

        @Override
        public int getRetrievalRetryCount()
        {
            return 3;
        }

        @Override
        public String getQueryString()
        {
            return null;
        }

        @Override
        public int getConnectionTimeout()
        {
            return 12000;
        }
    };

    private final RemoteProxySettings remoteProxySettings = new RemoteProxySettings()
    {
        @Override
        public void setProxyAuthentication( RemoteAuthenticationSettings proxyAuthentication )
        {
        }

        @Override
        public void setPort( int port )
        {
        }

        @Override
        public void setNonProxyHosts( Set<String> nonProxyHosts )
        {
        }

        @Override
        public void setHostname( String hostname )
        {
        }

        @Override
        public boolean isEnabled()
        {
            return false;
        }

        @Override
        public RemoteAuthenticationSettings getProxyAuthentication()
        {
            return null;
        }

        @Override
        public int getPort()
        {
            return 0;
        }

        @Override
        public Set<String> getNonProxyHosts()
        {
            return null;
        }

        @Override
        public String getHostname()
        {
            return null;
        }
    };

    @Test
    public void testHttpClientProxyUtilUseDoesNotModifyContext()
        throws Exception
    {
        final DefaultRemoteStorageContext ctx = new DefaultRemoteStorageContext( null );
        ctx.setRemoteConnectionSettings( remoteConnectionSettings );
        ctx.setRemoteProxySettings( remoteProxySettings );

        final HttpClient httpClient = new HttpClient();

        // get last changed
        int lastChanged = ctx.getGeneration();

        // 1st invocation, should modify it
        HttpClientProxyUtil.applyProxyToHttpClient( httpClient, ctx, logger );
        assertThat( ctx.getGeneration(), greaterThan( lastChanged ) );

        // get last changed
        lastChanged = ctx.getGeneration();

        // now 2nd invocation, should not change
        HttpClientProxyUtil.applyProxyToHttpClient( httpClient, ctx, logger );
        assertThat( ctx.getGeneration(), equalTo( lastChanged ) );
    }

    @Test
    public void testHttpClientProxyUtilUseDoesNotModifyContextUsingDeprecatedMethod()
        throws Exception
    {
        final DefaultRemoteStorageContext ctx = new DefaultRemoteStorageContext( null );
        ctx.setRemoteConnectionSettings( remoteConnectionSettings );
        ctx.setRemoteProxySettings( remoteProxySettings );

        final HttpClient httpClient = new HttpClient();

        // get last changed
        long lastChanged = ctx.getLastChanged();
        // making sure we will have subtraction result grater than 0
        Thread.sleep( 15 );

        // 1st invocation, should modify it
        HttpClientProxyUtil.applyProxyToHttpClient( httpClient, ctx, logger );
        assertThat( ctx.getLastChanged(), greaterThan( lastChanged ) );

        // get last changed
        lastChanged = ctx.getLastChanged();
        // making sure we will have subtraction result grater than 0
        Thread.sleep( 15 );

        // now 2nd invocation, should not change
        HttpClientProxyUtil.applyProxyToHttpClient( httpClient, ctx, logger );
        assertThat( ctx.getLastChanged(), equalTo( lastChanged ) );
    }
}
