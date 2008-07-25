package org.sonatype.nexus.integrationtests.nexus133;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.thoughtworks.xstream.XStream;

public class Nexus133TargetCrudJsonTests
    extends AbstractNexusIntegrationTest
{
    
    // this is not a great use of a super class, but its really easy, and its only a test class.
    protected XStream xstream;
    protected MediaType mediaType;
    
    public Nexus133TargetCrudJsonTests()
    {
        xstream = XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
        this.mediaType = MediaType.APPLICATION_JSON;
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void createTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "createTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

        this.verifyTargetsConfig( responseResource );
    }

    
    public void readTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "readTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        // make sure it was added
        this.verifyTargetsConfig( responseResource );

        // update the Id
        resource.setId( responseResource.getId() );

        response = this.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not GET Repository Target: " + response.getStatus() );
        }
        
        // get the Resource object
        responseResource = this.getResourceFromResponse( response );

        Assert.assertEquals( resource.getContentClass(), responseResource.getContentClass() );
        Assert.assertEquals( resource.getName(), responseResource.getName() );
        Assert.assertEquals( resource.getPatterns(), responseResource.getPatterns() );

    }

    @Test
    public void udpateTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "udpateTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        // make sure it was added
        this.verifyTargetsConfig( responseResource );

        // update the Id
        resource.setId( responseResource.getId() );

        resource.setName( "udpateTestRenamed" );
        resource.setContentClass( "maven2" );
        patterns.clear();
        patterns.add( ".*new.*" );
        patterns.add( ".*patterns.*" );

        response = this.sendMessage( Method.PUT, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        responseResource = this.getResourceFromResponse( response );

        // make sure it was updated
        this.verifyTargetsConfig( responseResource );

    }

    @Test
    public void deleteTest()
        throws IOException
    {

        RepositoryTargetResource resource = new RepositoryTargetResource();

        // resource.setId( "createTest" );
        resource.setContentClass( "maven1" );
        resource.setName( "deleteTest" );

        List<String> patterns = new ArrayList<String>();
        patterns.add( ".*foo.*" );
        patterns.add( ".*bar.*" );
        resource.setPatterns( patterns );

        Response response = this.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not create Repository Target: " + response.getStatus() );
        }

        // get the Resource object
        RepositoryTargetResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertTrue( StringUtils.isNotEmpty( responseResource.getId() ) );

        // make sure it was added so we know if it was removed
        this.verifyTargetsConfig( responseResource );

        // use the new ID
        // response = this.deleteTarget( responseResource.getId() );

        response = this.sendMessage( Method.DELETE, responseResource );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not delete Repository Target: " + response.getStatus() );
        }

        this.verifyTargetsConfig( new ArrayList<RepositoryTargetResource>() );
    }

    private void verifyTargetsConfig( RepositoryTargetResource targetResource )
        throws IOException
    {
        ArrayList<RepositoryTargetResource> targetResources = new ArrayList<RepositoryTargetResource>();
        targetResources.add( targetResource );
        this.verifyTargetsConfig( targetResources );
    }

    @SuppressWarnings( "unchecked" )
    private void verifyTargetsConfig( List<RepositoryTargetResource> targetResources )
        throws IOException
    {
        // check the nexus.xml
        Configuration config = this.getNexusConfig();

        List<CRepositoryTarget> repoTargets = config.getRepositoryTargets();

        // TODO: we can't check the size unless we reset the config after each run...
        // check to see if the size matches
        // Assert.assertTrue( "Configuration had a different number: (" + repoTargets.size()
        // + ") of targets then expected: (" + targetResources.size() + ")",
        // repoTargets.size() == targetResources.size() );

        // look for the target by id

        for ( Iterator<RepositoryTargetResource> iter = targetResources.iterator(); iter.hasNext(); )
        {
            RepositoryTargetResource targetResource = iter.next();
            boolean found = false;

            for ( Iterator<CRepositoryTarget> iterInner = repoTargets.iterator(); iterInner.hasNext(); )
            {
                CRepositoryTarget repositoryTarget = iterInner.next();

                if ( targetResource.getId().equals( repositoryTarget.getId() ) )
                {
                    found = true;
                    Assert.assertEquals( targetResource.getId(), repositoryTarget.getId() );
                    Assert.assertEquals( targetResource.getContentClass(), repositoryTarget.getContentClass() );
                    Assert.assertEquals( targetResource.getName(), repositoryTarget.getName() );
                    Assert.assertEquals( targetResource.getPatterns(), repositoryTarget.getPatterns() );

                    break;
                }
                
            }
            
            if(!found)
            {
                
                Assert.fail("Target with ID: "+ targetResource.getId() +" could not be found in configuration.");
            }
        }
    }

    private Configuration getNexusConfig()
        throws IOException
    {

        URL configURL = new URL( this.getBaseNexusUrl() + "service/local/configs/current" );

        Reader fr = null;
        Configuration configuration = null;

        try
        {
            NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

            fr = new InputStreamReader( configURL.openStream() );

            // read again with interpolation
            configuration = reader.read( fr );

        }
        catch ( XmlPullParserException e )
        {
            Assert.fail( "could not parse nexus.xml: " + e.getMessage() );
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }
        return configuration;
    }

    private RepositoryTargetResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        System.out.println( " getResourceFromResponse: " + responseString );

        
        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        RepositoryTargetResourceResponse resourceResponse =
            (RepositoryTargetResourceResponse) representation.getPayload( new RepositoryTargetResourceResponse() );

        return resourceResponse.getData();

    }

    private Response sendMessage( Method method, RepositoryTargetResource resource )
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String repoTargetId = ( resource.getId() == null ) ? "?undefined" : "/" + resource.getId();

        String serviceURI = this.getBaseNexusUrl() + "service/local/repo_targets" + repoTargetId;
        System.out.println( "serviceURI: " + serviceURI );

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( method );

        RepositoryTargetResourceResponse requestResponse = new RepositoryTargetResourceResponse();
        requestResponse.setData( resource );

        // now set the payload
        representation.setPayload( requestResponse );
        request.setEntity( representation );
       

        Client client = new Client( Protocol.HTTP );

        return client.handle( request );
    }

}
