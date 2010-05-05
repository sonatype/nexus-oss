package org.sonatype.nexus.integrationtests.nexus3045;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;
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
        repoUtil =
            new RepositoryMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML,
                                       getRepositoryTypeRegistry() );
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
        Assert.assertThat( getRepoIds( route.getRepositories() ), IsCollectionContaining.hasItem( "thirdparty" ) );
        Assert.assertThat( getRepoIds( route.getRepositories() ),
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
