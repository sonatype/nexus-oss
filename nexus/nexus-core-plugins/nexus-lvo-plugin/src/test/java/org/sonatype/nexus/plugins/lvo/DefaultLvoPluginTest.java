/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.plugins.lvo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.nexus.plugins.lvo.config.LvoPluginConfiguration;
import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 *
 */
public class DefaultLvoPluginTest
{

    @Mock
    private LvoPluginConfiguration cfg;

    private final Map<String, DiscoveryStrategy> strategies = Maps.newHashMap();

    private DefaultLvoPlugin underTest;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks( this );
        when( cfg.isEnabled() ).thenReturn( true );

        underTest = new DefaultLvoPlugin( cfg, strategies );
    }

    @Test
    public void testGetVersionTouchesStrategy()
        throws NoSuchStrategyException, IOException, NoSuchKeyException, NoSuchRepositoryException
    {
        final Function<DiscoveryResponse, DiscoveryResponse> func = new Function<DiscoveryResponse, DiscoveryResponse>()
        {
            @Override
            public DiscoveryResponse apply( @Nullable final DiscoveryResponse input )
            {
                return input;
            }
        };

        final CLvoKey key = mockOut( func );

        DiscoveryResponse response = underTest.getLatestVersionForKey( "test" );
        DiscoveryRequest request = response.getRequest();
        assertThat( request.getKey(), is( "test" ) );
        assertThat( request.getLvoKey(), is( key ) );

        verify( strategies.get( "test" ) ).discoverLatestVersion( request );
    }

    private CLvoKey mockOut( final Function<DiscoveryResponse, DiscoveryResponse> func )
        throws NoSuchKeyException, NoSuchRepositoryException, IOException
    {
        final CLvoKey key = mock( CLvoKey.class );
        DiscoveryStrategy strategy = mock( DiscoveryStrategy.class );
        strategies.put( "test", strategy );

        when( cfg.getLvoKey( "test" ) ).thenReturn( key );
        when( key.getStrategy() ).thenReturn( "test" );
        when( strategy.discoverLatestVersion( any( DiscoveryRequest.class ) ) ).then( new Answer<Object>()
        {
            @Override
            public Object answer( final InvocationOnMock invocationOnMock )
                throws Throwable
            {
                DiscoveryRequest request = (DiscoveryRequest) invocationOnMock.getArguments()[0];
                return func.apply( new DiscoveryResponse( request ) );
            }
        } );
        return key;
    }

    @Test
    public void testQueryVersionNotNewer()
        throws NoSuchStrategyException, IOException, NoSuchKeyException, NoSuchRepositoryException
    {
        mockOut( new Function<DiscoveryResponse, DiscoveryResponse>()
        {
            @Override
            public DiscoveryResponse apply( @Nullable final DiscoveryResponse input )
            {
                input.setSuccessful( true );
                input.setVersion( "1" );
                input.setUrl( "http://some.url" );
                return spy( input );
            }
        } );

        DiscoveryResponse response = underTest.queryLatestVersionForKey( "test", "2" );
        verify( response, atLeastOnce() ).isSuccessful();
        verify( response ).getVersion();
        verify( response ).setSuccessful( false );

        assertThat( response.isSuccessful(), is( false ) );
    }

    @Test
    public void testQueryVersionNewer()
        throws NoSuchStrategyException, IOException, NoSuchKeyException, NoSuchRepositoryException
    {
        mockOut( new Function<DiscoveryResponse, DiscoveryResponse>()
        {
            @Override
            public DiscoveryResponse apply( @Nullable final DiscoveryResponse input )
            {
                input.setSuccessful( true );
                input.setVersion( "3" );
                input.setUrl( "http://some.url" );
                return spy( input );
            }
        } );

        DiscoveryResponse response = underTest.queryLatestVersionForKey( "test", "2" );
        verify( response, atLeastOnce() ).isSuccessful();
        verify( response ).getVersion();

        assertThat( response.isSuccessful(), is( true ) );
    }

}
