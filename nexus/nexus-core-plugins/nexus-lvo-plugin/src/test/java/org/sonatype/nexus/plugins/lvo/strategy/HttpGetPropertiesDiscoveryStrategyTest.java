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
package org.sonatype.nexus.plugins.lvo.strategy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.google.common.base.Throwables;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * Tests for HttpGetPropertiesDiscoveryStrategyTest.
 */
public class HttpGetPropertiesDiscoveryStrategyTest
{

    @Test
    public void translateProperties()
        throws IOException, NoSuchRepositoryException
    {
        HttpGetPropertiesDiscoveryStrategy underTest = new HttpGetPropertiesDiscoveryStrategy()
        {
            @Override
            protected RequestResult handleRequest( final String url )
            {
                try
                {
                    GetMethod method = mock( GetMethod.class );
                    when( method.getResponseBodyAsStream() )
                        .thenReturn(
                            new ByteArrayInputStream(
                                "test.version=2.0\ntest.url=http://some.url\n".getBytes() )
                        );
                    return new RequestResult( method );
                }
                catch ( IOException e )
                {
                    Throwables.propagate( e );
                }
                return null;
            }
        };

        final DiscoveryRequest request = new DiscoveryRequest( "test", mock( CLvoKey.class ) );
        DiscoveryResponse response = underTest.discoverLatestVersion( request );

        assertThat( response.getVersion(), is( "2.0" ) );
        assertThat( response.isSuccessful(), is( true ) );
    }

    @Test
    public void translatePropertiesFail()
        throws IOException, NoSuchRepositoryException
    {
        HttpGetPropertiesDiscoveryStrategy underTest = new HttpGetPropertiesDiscoveryStrategy()
        {
            @Override
            protected RequestResult handleRequest( final String url )
            {
                return null;
            }
        };

        final DiscoveryRequest request = new DiscoveryRequest( "test", mock( CLvoKey.class ) );
        DiscoveryResponse response = underTest.discoverLatestVersion( request );

        assertThat( response.getVersion(), nullValue() );
        assertThat( response.isSuccessful(), is( false ) );
    }

}
