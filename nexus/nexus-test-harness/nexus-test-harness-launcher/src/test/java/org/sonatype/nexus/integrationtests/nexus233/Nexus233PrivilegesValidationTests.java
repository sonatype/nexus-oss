package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus233PrivilegesValidationTests
    extends AbstractNexusIntegrationTest
{

    protected PrivilegesMessageUtil messageUtil;

    public Nexus233PrivilegesValidationTests()
    {
        this.messageUtil =
            new PrivilegesMessageUtil( XStreamInitializer.initialize( new XStream() ), MediaType.APPLICATION_XML,
                                       this.getBaseNexusUrl() );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createWithInvalidMethodTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "INVALID" );
        resource.setMethod( methods );
        resource.setName( "createWithInvalidMethodTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }
        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoMethodTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        // methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "createNoMethodTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoNameTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        // resource.setName( "createNoMethodTest" );
        resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoTypeTest()
        throws IOException
    {

        // FIXME: this test is known to fail, but is commented out so the CI builds are useful
        if ( this.printKnownErrorButDoNotFail( this.getClass(), "createNoTypeTest" ) )
        {
            return;
        }

        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "createNoTypeTest" );
        // resource.setType( "repositoryTarget" );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createNoRepoTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        resource.setMethod( methods );
        resource.setName( "createNoRepoTest" );
        resource.setType( "repositoryTarget" );
        // resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createWithInvalidAndValidMethodsTest()
        throws IOException
    {
        PrivilegeTargetResource resource = new PrivilegeTargetResource();

        List methods = new ArrayList<String>();
        methods.add( "read" );
        methods.add( "INVALID" );
        resource.setMethod( methods );
        resource.setName( "createWithInvalidAndValidMethodsTest" );
        resource.setType( "repositoryTarget" );
        // resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

        Assert.assertNull( SecurityConfigUtil.getCRepoTargetPrivilegeByName( "createWithInvalidAndValidMethodsTest - (read)" ) );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createApplicationResource()
        throws IOException
    {
        // FIXME: this test is known to fail, but is commented out so the CI builds are useful
        if ( this.printKnownErrorButDoNotFail( this.getClass(), "createApplicationResource" ) )
        {
            return;
        }

        PrivilegeTargetResource resource = new PrivilegeTargetResource();
        resource.addMethod( "read" );
        resource.setName( "createApplicationResource" );
        resource.setType( "application" );
        resource.setRepositoryTargetId( "testTarget" );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Privilege should not have been created: " + response.getStatus() + "\nreponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );
    }

    @Test
    public void readInvalidIdTest()
        throws IOException
    {

        Response response = this.messageUtil.sendMessage( Method.GET, null, "INVALID" );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 404 )
        {
            Assert.fail( "A 404 should have been returned: " + response.getStatus() + "\nreponse:\n" + responseText );
        }

    }

    @Test
    public void deleteInvalidIdTest()
        throws IOException
    {

        Response response = this.messageUtil.sendMessage( Method.DELETE, null, "INVALID" );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().getCode() != 404 )
        {
            Assert.fail( "A 404 should have been returned: " + response.getStatus() + "\nreponse:\n" + responseText );
        }

    }

}
