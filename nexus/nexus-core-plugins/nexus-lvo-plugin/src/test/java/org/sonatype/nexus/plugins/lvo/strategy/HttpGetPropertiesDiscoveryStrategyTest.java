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
