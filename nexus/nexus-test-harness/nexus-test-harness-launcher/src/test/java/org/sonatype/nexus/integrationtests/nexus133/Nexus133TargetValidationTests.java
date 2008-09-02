package org.sonatype.nexus.integrationtests.nexus133;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

/**
 * Extra CRUD validation tests.
 */
public class Nexus133TargetValidationTests
    extends AbstractNexusIntegrationTest
{

    protected TargetMessageUtil messageUtil;

    public Nexus133TargetValidationTests()
    {
        this.messageUtil =
            new TargetMessageUtil(
                                   XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) ),
                                   MediaType.APPLICATION_JSON );
    }

    @Test
    public void noPatternsTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "noPatternsTest" );

        // List<String> patterns = new ArrayList<String>();
        // patterns.add( ".*foo.*" );
        // patterns.add( ".*bar.*" );
        // resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Target should not have been created: " + response.getStatus() + "\n" + responseText );
        }
        Assert.assertTrue( "Response text did not contain an error message. \nResponse Text:\n " + responseText,
                           responseText.startsWith( "{\"errors\":" ) );
    }

    @Test
    public void noNameTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( null );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Target should not have been created: " + response.getStatus() + "\n" + responseText );
        }
        Assert.assertTrue( "Response text did not contain an error message. \nResponse Text:\n " + responseText,
                           responseText.startsWith( "{\"errors\":" ) );
    }

    @Test
    public void invalidRegExTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "invalidRegExTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( "*.foo.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Target should not have been created: " + response.getStatus() + "\n" + responseText );
        }
        Assert.assertTrue( "Response text did not contain an error message. \nResponse Text:\n " + responseText,
                           responseText.startsWith( "{\"errors\":" ) );
    }

    @Test
    public void invalidContentClass()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "INVALID_CLASS" );
        resource.setName( "invalidContentClass" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Target should not have been created: " + response.getStatus() + "\n" + responseText );
        }
        Assert.assertTrue( "Response text did not contain an error message. \nResponse Text:\n " + responseText,
                           responseText.startsWith( "{\"errors\":" ) );
    }

    @Test
    public void duplicateTargetTest()
        throws IOException
    {
        RepositoryTargetResource resource = new RepositoryTargetResource();
        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "duplicateTargetTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        String id1 = responseResource.getId();

        // make sure it was updated
        this.messageUtil.verifyTargetsConfig( responseResource );

        // send again
        response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Target: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.messageUtil.getResourceFromResponse( response );
        String id2 = responseResource.getId();

        // make sure it was updated
        this.messageUtil.verifyTargetsConfig( responseResource );

        Assert.assertNotSame( id1, id2 );

    }

    @Test
    public void updateValidation()
        throws IOException
    {
        RepositoryTargetResource resource = new RepositoryTargetResource();
        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "updateValidation" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create user: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

        // verify config
        this.messageUtil.verifyTargetsConfig( responseResource );

        // update the Id
        resource.setId( responseResource.getId() );

        /*
         * NO Name
         */
        resource.setContentClass( "maven1" );
        resource.setName( null );
        patterns.clear();
        patterns.add( ".*new.*" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Target should not have been created: " + response.getStatus() );
        }
        String responseText = response.getEntity().getText();
        Assert.assertTrue("responseText does not contain an error message:\n"+ responseText, responseText.startsWith( "{\"errors\":" ) );

        /*
         * Invalid RegEx
         */

        resource.setContentClass( "maven1" );
        resource.setName( "updateValidation" );
        patterns.clear();
        patterns.add( "*.new.*" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Target should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );

        /*
         * NO Patterns
         */
        resource.setContentClass( "maven1" );
        resource.setName( "updateValidation" );
        patterns.clear();

        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Target should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );

        /*
         * NO Content Class
         */
        resource.setContentClass( null );
        resource.setName( "updateValidation" );
        patterns.clear();
        patterns.add( ".*new.*" );

        response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( response.getStatus().isSuccess() )
        {
            Assert.fail( "Target should not have been created: " + response.getStatus() );
        }
        Assert.assertTrue( response.getEntity().getText().startsWith( "{\"errors\":" ) );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void maven1ContentClassTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "maven1ContentClassTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

        this.messageUtil.verifyTargetsConfig( responseResource );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void maven2ContentClassTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven2" );
        resource.setName( "maven2ContentClassTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

        this.messageUtil.verifyTargetsConfig( responseResource );
    }

    @SuppressWarnings( "unchecked" )
    //@Test
    // eclipseContentClass is disabled for beta5!
    public void eclipseContentClassTest()
        throws IOException
    {
        // m2namespace
        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "eclipse-update-site" );
        resource.setName( "eclipseContentClassTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

        this.messageUtil.verifyTargetsConfig( responseResource );
    }

    @SuppressWarnings( "unchecked" )
    //@Test
    // m2NamespaceContentclass is disabled for beta5!
    public void m2NamespaceContentClassTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "m2namespace" );
        resource.setName( "m2NamespaceContentClassTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

        this.messageUtil.verifyTargetsConfig( responseResource );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createTestWithId()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // FIXME: This should be allowed
        // resource.setId( "createTestWithId" );
        resource.setContentClass( "maven1" );
        resource.setName( "createTestWithId" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.messageUtil.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        // FIXME: This should be allowed
        // Assert.assertEquals( resource.getId(), responseResource.getId() );
        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

        this.messageUtil.verifyTargetsConfig( responseResource );
    }

}
