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
package org.sonatype.nexus.testsuite.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.client.core.exception.NexusClientAccessForbiddenException;
import org.sonatype.nexus.client.core.exception.NexusClientBadRequestException;
import org.sonatype.nexus.client.core.exception.NexusClientException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.ArtifactResolveResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.security.rest.model.UserListResourceResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ClientErrorHandlingIT
    extends ClientITSupport
{

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public ClientErrorHandlingIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void convert400WithoutErrorMessage()
    {
        final JerseyNexusClient client = (JerseyNexusClient) client();
        try
        {
            client.serviceResource( "artifact/maven/resolve" ).get( ArtifactResolveResourceResponse.class );
        }
        catch ( UniformInterfaceException e )
        {
            final NexusClientException converted = client.convertIfKnown( e );
            assertThat( converted, is( instanceOf( NexusClientBadRequestException.class ) ) );
        }
    }

    @Test
    public void convertAlwaysReturnsAnException()
    {
        final JerseyNexusClient client = (JerseyNexusClient) client();
        try
        {
            client.serviceResource( "artifact/maven/resolve" ).get( ArtifactResolveResourceResponse.class );
        }
        catch ( UniformInterfaceException e )
        {
            final NexusClientException converted = client.convert( e );
            assertThat( converted, is( notNullValue() ) );
        }
    }

    @Test
    public void entityIsNotConsumed()
    {
        final JerseyNexusClient client = (JerseyNexusClient) client();
        try
        {
            client.serviceResource( "artifact/maven/resolve" ).get( ArtifactResolveResourceResponse.class );
        }
        catch ( UniformInterfaceException e )
        {
            client.convertIfKnown( e );
            assertThat( e.getResponse().hasEntity(), is( true ) );
            assertThat( e.getResponse().getEntity( String.class ), is( notNullValue() ) );
        }
    }

    @Test
    public void convert404()
    {
        final JerseyNexusClient client = (JerseyNexusClient) client();
        try
        {
            client.serviceResource( "repositories/foo" ).get( RepositoryResourceResponse.class );
        }
        catch ( UniformInterfaceException e )
        {
            final NexusClientException converted = client.convertIfKnown( e );
            assertThat( converted, is( instanceOf( NexusClientNotFoundException.class ) ) );

            // do it again so we ensure we consumed and such connection is available
            try
            {
                client.serviceResource( "repositories/foo" ).get( RepositoryResourceResponse.class );
            }
            catch ( UniformInterfaceException e1 )
            {
                final NexusClientException converted1 = client.convertIfKnown( e );
                assertThat( converted1, is( instanceOf( NexusClientNotFoundException.class ) ) );
            }
        }
    }

    @Test
    public void convert403()
    {
        final JerseyNexusClient client = (JerseyNexusClient) createNexusClient(
            nexus(), "deployment", "deployment123"
        );
        try
        {
            client.serviceResource( "users" ).get( UserListResourceResponse.class );
        }
        catch ( UniformInterfaceException e )
        {
            final NexusClientException converted = client.convertIfKnown( e );
            assertThat( converted, is( instanceOf( NexusClientAccessForbiddenException.class ) ) );

            // do it again so we ensure we consumed and such connection is available
            try
            {
                client.serviceResource( "users" ).get( UserListResourceResponse.class );
            }
            catch ( UniformInterfaceException e1 )
            {
                final NexusClientException converted1 = client.convertIfKnown( e );
                assertThat( converted1, is( instanceOf( NexusClientAccessForbiddenException.class ) ) );
            }
        }
    }

}
