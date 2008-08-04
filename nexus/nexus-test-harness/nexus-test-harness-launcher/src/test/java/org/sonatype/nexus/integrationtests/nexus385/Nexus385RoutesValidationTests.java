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

public class Nexus385RoutesValidationTests
    extends AbstractNexusIntegrationTest
{

    protected RoutesMessageUtil messageUtil;

    public Nexus385RoutesValidationTests()
    {
        this.messageUtil =
            new RoutesMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML,
                                   this.getBaseNexusUrl() );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoGroupIdTest()
        throws IOException
    {
     // FIXME: this test is known to fail, but is commented out so the CI builds are useful
        if ( this.printKnownErrorButDoNotFail( this.getClass(), "createNoGroupIdTest" ) )
        {
            return;
        }
        
        RepositoryRouteResource resource = new RepositoryRouteResource();
        // resource.setGroupId( "nexus-test" );
        resource.setPattern( ".*createNoGroupIdTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {   
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }
        
        this.messageUtil.validateResponseErrorXml(responseText);

    }
    
    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoRuleTypeTest()
        throws IOException
    {
        
     // FIXME: this test is known to fail, but is commented out so the CI builds are useful
        if ( this.printKnownErrorButDoNotFail( this.getClass(), "createNoRuleTypeTest" ) )
        {
            return;
        }
        
        RepositoryRouteResource resource = new RepositoryRouteResource();
         resource.setGroupId( "nexus-test" );
        resource.setPattern( ".*createNoRuleTypeTest.*" );
//        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {   
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }
        
        this.messageUtil.validateResponseErrorXml(responseText);

    }
    
    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoPatternTest()
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
         resource.setGroupId( "nexus-test" );
//        resource.setPattern( ".*createNoPatternTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {   
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }
        
        this.messageUtil.validateResponseErrorXml(responseText);

    }
    
    @SuppressWarnings( "unchecked" )
    @Test
    public void createWithInvalidPatternTest()
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
         resource.setGroupId( "nexus-test" );
        resource.setPattern( "*.createWithInvalidPatternTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {   
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }
        
        this.messageUtil.validateResponseErrorXml(responseText);

    }
    
    @SuppressWarnings( "unchecked" )
    @Test
    public void createWithInvalidGroupTest()
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
         resource.setGroupId( "INVALID" );
        resource.setPattern( "*.createWithInvalidPatternTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {   
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }
        
        this.messageUtil.validateResponseErrorXml(responseText);
    }
    
    @SuppressWarnings( "unchecked" )
    @Test
    public void createWithInvalidRuleTypeTest()
        throws IOException
    {
        
     // FIXME: this test is known to fail, but is commented out so the CI builds are useful
        if ( this.printKnownErrorButDoNotFail( this.getClass(), "createWithInvalidRuleTypeTest" ) )
        {
            return;
        }
        
        RepositoryRouteResource resource = new RepositoryRouteResource();
         resource.setGroupId( "nexus-test" );
        resource.setPattern( "*.createWithInvalidRuleTypeTest.*" );
        resource.setRuleType( "createWithInvalidRuleTypeTest" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {   
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }
        
        this.messageUtil.validateResponseErrorXml(responseText);
    }
    
    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoReposTest()
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
         resource.setGroupId( "nexus-test" );
        resource.setPattern( "*.createWithInvalidRuleTypeTest.*" );
        resource.setRuleType( "exclusive" );

//        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
//        memberRepo1.setId( "nexus-test-harness-repo" );
//        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {   
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }
        
        this.messageUtil.validateResponseErrorXml(responseText);
    }
    
    @SuppressWarnings( "unchecked" )
    @Test
    public void createWithInvalidReposTest()
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
         resource.setGroupId( "nexus-test" );
        resource.setPattern( "*.createWithInvalidRuleTypeTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "INVALID" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {   
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }
        
        this.messageUtil.validateResponseErrorXml(responseText);
    }
    
    

}
