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
package org.sonatype.nexus.integrationtests.nexus3045;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.RoutesMessageUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus3045GroupRemovalIT
    extends AbstractNexusIntegrationTest
{

    private RoutesMessageUtil routesUtil;

    private RepositoryMessageUtil repoUtil;

    private GroupMessageUtil groupUtil;

    public Nexus3045GroupRemovalIT()
        throws ComponentLookupException
    {
        super();
        routesUtil = new RoutesMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
        repoUtil = new RepositoryMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
        groupUtil = new GroupMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
    }

    private static final String GROUP_ROUTE_ID = "297bf5de34f";

    private static final String REPO_ROUTE_ID = "29574c59c49";

    @Test
    public void removeGroup()
        throws IOException
    {
        Assert.assertNotNull( routesUtil.getRoute( GROUP_ROUTE_ID ) );

        RepositoryGroupResource resource = this.groupUtil.getGroup( "public" );
        Response response = this.groupUtil.sendMessage( Method.DELETE, resource );
        Assert.assertTrue( response.getStatus().isSuccess() );

        Assert.assertEquals( 404, routesUtil.getRouteResponse( GROUP_ROUTE_ID ).getStatus().getCode() );
    }

    @Test
    public void removeRepository()
        throws IOException
    {
        Assert.assertNotNull( routesUtil.getRoute( REPO_ROUTE_ID ) );

        RepositoryBaseResource resource = this.repoUtil.getRepository( "releases" );
        Response response = this.repoUtil.sendMessage( Method.DELETE, resource );
        Assert.assertTrue( response.getStatus().isSuccess() );

        RepositoryRouteResource route = routesUtil.getRoute( REPO_ROUTE_ID );
        Assert.assertNotNull( route );
        MatcherAssert.assertThat( getRepoIds( route.getRepositories() ), IsCollectionContaining.hasItem( "thirdparty" ) );
        MatcherAssert.assertThat( getRepoIds( route.getRepositories() ),
            CoreMatchers.not( IsCollectionContaining.hasItem( "releases" ) ) );
    }

    private List<String> getRepoIds( List<RepositoryRouteMemberRepository> repositories )
    {
        List<String> repoIds = new ArrayList<String>();
        for ( RepositoryRouteMemberRepository repo : repositories )
        {
            repoIds.add( repo.getId() );
        }
        return repoIds;
    }
}
