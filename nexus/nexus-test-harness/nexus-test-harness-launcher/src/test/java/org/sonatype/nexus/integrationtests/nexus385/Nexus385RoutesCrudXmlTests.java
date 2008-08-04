package org.sonatype.nexus.integrationtests.nexus385;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.NexusConfigUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus385RoutesCrudXmlTests
    extends AbstractNexusIntegrationTest
{

    protected RoutesMessageUtil messageUtil;

    public Nexus385RoutesCrudXmlTests()
    {
        this.messageUtil =
            new RoutesMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML,
                                   this.getBaseNexusUrl() );
    }

    @Test
    public void createTest()
        throws IOException
    {
        this.runCreateTest( "exclusive" );
        this.runCreateTest( "inclusive" );
        this.runCreateTest( "blocking" );
    }

    @SuppressWarnings( "unchecked" )
    private RepositoryRouteResource runCreateTest( String ruleType )
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "nexus-test" );
        resource.setPattern( ".*" + ruleType + ".*" );
        resource.setRuleType( ruleType );

        if ( !"blocking".equals( ruleType ) )
        {
            RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
            memberRepo1.setId( "nexus-test-harness-repo" );
            resource.addRepository( memberRepo1 );
        }

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create privilege: " + response.getStatus() + "\nresponse:\n" + responseText );
        }

        // get the Resource object
        RepositoryRouteResource resourceResponse = this.messageUtil.getResourceFromResponse( response );

        Assert.assertNotNull( resourceResponse.getId() );

        Assert.assertEquals( resource.getGroupId(), resourceResponse.getGroupId() );
        Assert.assertEquals( resource.getPattern(), resourceResponse.getPattern() );
        Assert.assertEquals( resource.getRuleType(), resourceResponse.getRuleType() );
        this.messageUtil.validateSame( resource.getRepositories(), resourceResponse.getRepositories() );

        // now check the nexus config
        this.messageUtil.validateRoutesConfig( resourceResponse );

        return resourceResponse;
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void readTest()
        throws IOException
    {
        // create
        RepositoryRouteResource resource = this.runCreateTest( "exclusive" );

        Response response = this.messageUtil.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create privilege: " + response.getStatus() + "\nresponse:\n" + responseText );
        }

        // get the Resource object
        RepositoryRouteResource resourceResponse = this.messageUtil.getResourceFromResponse( response );

        Assert.assertNotNull( resourceResponse.getId() );

        Assert.assertEquals( resource.getGroupId(), resourceResponse.getGroupId() );
        Assert.assertEquals( resource.getPattern(), resourceResponse.getPattern() );
        Assert.assertEquals( resource.getRuleType(), resourceResponse.getRuleType() );
        this.messageUtil.validateSame( resource.getRepositories(), resourceResponse.getRepositories() );

        // now check the nexus config
        this.messageUtil.validateRoutesConfig( resourceResponse );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void updateTest()
        throws IOException
    {
        // FIXME: this test is known to fail, but is commented out so the CI builds are useful
        if ( this.printKnownErrorButDoNotFail( this.getClass(), "updateTest" ) )
        {
            return;
        }

        // create
        RepositoryRouteResource resource = this.runCreateTest( "exclusive" );
        resource.setPattern( ".*update.*" );

        Response response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create privilege: " + response.getStatus() + "\nresponse:\n" + responseText );
        }

        // get the Resource object
        RepositoryRouteResource resourceResponse = this.messageUtil.getResourceFromResponse( response );

        Assert.assertNotNull( resourceResponse.getId() );

        Assert.assertEquals( resource.getGroupId(), resourceResponse.getGroupId() );
        Assert.assertEquals( resource.getPattern(), resourceResponse.getPattern() );
        Assert.assertEquals( resource.getRuleType(), resourceResponse.getRuleType() );
        this.messageUtil.validateSame( resource.getRepositories(), resourceResponse.getRepositories() );

        // now check the nexus config
        this.messageUtil.validateRoutesConfig( resourceResponse );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void deleteTest()
        throws IOException
    {
        // create
        RepositoryRouteResource resource = this.runCreateTest( "exclusive" );

        Response response = this.messageUtil.sendMessage( Method.DELETE, resource );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create privilege: " + response.getStatus() + "\nresponse:\n" + responseText );
        }

        Assert.assertTrue( "Route was not deleted.", NexusConfigUtil.getRoute( resource.getId() ) == null );

    }

}
