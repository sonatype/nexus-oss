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
/**
 *
 */
package org.sonatype.nexus.repository.yum.internal.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.restlet.data.Method.GET;
import static org.restlet.data.Method.POST;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;
import static org.sonatype.nexus.repository.yum.internal.rest.RepositoryVersionAliasResource.RESOURCE_URI;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.sonatype.nexus.repository.yum.internal.config.YumPluginConfiguration;
import org.sonatype.nexus.repository.yum.internal.utils.AbstractYumNexusTestCase;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author BVoss
 */
public class RepositoryVersionAliasResourceTest
    extends AbstractYumNexusTestCase
{

    private static final String EXISTING_VERSION = "trunk";

    private static final String RELEASES = "releases";

    private static final String TRUNK_VERSION = "5.1.15-2";

    private static final String NOT_EXISTING_REPOSITORY = "blablup-repo";

    private static final String VERSION_TO_CREATE = "new-version";

    private static final String ALIAS_TO_CREATE = "alias-to-create";

    @Requirement( hint = "RepositoryVersionAliasResource" )
    private PlexusResource resource;

    @Inject
    private YumPluginConfiguration yumConfiguration;

    @Before
    public void loadYumConfig()
    {
        yumConfiguration.load();
    }

    @Test
    public void requestedAliasNotfound()
        throws Exception
    {
        assert404( new Request() );
    }

    @Test
    public void requestedAliasNotfoundNoParameters()
        throws Exception
    {
        assert404( createRequest( RELEASES, "bla" ) );
    }

    @Test
    public void requestedAliasReturnedVersionString()
        throws Exception
    {
        final Request request = createRequest( RELEASES, EXISTING_VERSION );
        final StringRepresentation version = (StringRepresentation) resource.get( null, request, null, null );
        Assert.assertEquals( TRUNK_VERSION, version.getText() );
    }

    @Test
    public void shouldReturn404ForInvalidRepository()
        throws Exception
    {
        assert404( createRequest( NOT_EXISTING_REPOSITORY, EXISTING_VERSION ) );
    }

    @Test
    public void shouldReturn404ForInvalidRepositoryRpm()
        throws Exception
    {
        assert404( createRequest( NOT_EXISTING_REPOSITORY, EXISTING_VERSION + ".rpm" ) );
    }

    @Test
    public void shouldRetrieveRestRequirements()
        throws Exception
    {
        assertThat( resource.getResourceUri(), is( RESOURCE_URI ) );
        assertThat( resource.getPayloadInstance(), nullValue() );
        assertThat( resource.getPayloadInstance( GET ), nullValue() );
        assertThat( resource.getPayloadInstance( POST ), instanceOf( String.class ) );
    }

    @Test
    public void shouldRejectEmptyPayload()
        throws Exception
    {
        check400ForPayload( null );
    }

    @Test
    public void shouldRejectObjectPayload()
        throws Exception
    {
        check400ForPayload( new Object() );
    }

    @Test
    public void shouldSetVersion()
        throws Exception
    {
        final Request request = createRequest( NOT_EXISTING_REPOSITORY, ALIAS_TO_CREATE );
        StringRepresentation result = (StringRepresentation) resource.post( null, request, null, VERSION_TO_CREATE );
        assertThat( result.getText(), is( VERSION_TO_CREATE ) );
        result = (StringRepresentation) resource.get( null, request, null, null );
        assertThat( result.getText(), is( VERSION_TO_CREATE ) );
    }

    private void check400ForPayload( Object payload )
    {
        try
        {
            resource.post( null, createRequest( NOT_EXISTING_REPOSITORY, RELEASES ), null, payload );
            Assert.fail();
        }
        catch ( ResourceException e )
        {
            assertThat( e.getStatus(), is( CLIENT_ERROR_BAD_REQUEST ) );
        }
    }

    private void assert404( final Request request )
    {
        try
        {
            resource.get( null, request, null, null );
            Assert.fail( ResourceException.class + " expected" );
        }
        catch ( ResourceException e )
        {
            Assert.assertEquals( Status.CLIENT_ERROR_NOT_FOUND, e.getStatus() );
        }
    }

    private Request createRequest( final String repoValue, final String aliasValue )
    {
        final Request request = new Request();
        request.getAttributes().put( RepositoryVersionAliasResource.REPOSITORY_ID_PARAM, repoValue );
        request.getAttributes().put( RepositoryVersionAliasResource.ALIAS_PARAM, aliasValue );
        return request;
    }

}
