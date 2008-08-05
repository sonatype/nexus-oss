package org.sonatype.nexus.test.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class TargetMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    public TargetMessageUtil( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public Response sendMessage( Method method, RepositoryTargetResource resource ) throws IOException
    {
       
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String repoTargetId = ( resource.getId() == null ) ? "?undefined" : "/" + resource.getId();

        String serviceURI = "service/local/repo_targets" + repoTargetId;
        
        RepositoryTargetResourceResponse requestResponse = new RepositoryTargetResourceResponse();
        requestResponse.setData( resource );
     // now set the payload
        representation.setPayload( requestResponse );
        
        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    @SuppressWarnings( "unchecked" )
    public List<RepositoryTargetListResource> getList()
        throws IOException
    {

        String responseText = RequestFacade.doGetRequest( "service/local/repo_targets" ).getEntity().getText();
        System.out.println( "responseText: \n" + responseText );

        XStreamRepresentation representation =
            new XStreamRepresentation( new XStream(), responseText, MediaType.APPLICATION_XML );

        RepositoryTargetListResourceResponse resourceResponse =
            (RepositoryTargetListResourceResponse) representation.getPayload( new RepositoryTargetListResourceResponse() );

        return resourceResponse.getData();

    }

    public RepositoryTargetResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        System.out.println( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        RepositoryTargetResourceResponse resourceResponse =
            (RepositoryTargetResourceResponse) representation.getPayload( new RepositoryTargetResourceResponse() );

        return resourceResponse.getData();

    }

    public void verifyTargetsConfig( RepositoryTargetResource targetResource )
        throws IOException
    {
        ArrayList<RepositoryTargetResource> targetResources = new ArrayList<RepositoryTargetResource>();
        targetResources.add( targetResource );
        this.verifyTargetsConfig( targetResources );
    }

    @SuppressWarnings( "unchecked" )
    public void verifyTargetsConfig( List<RepositoryTargetResource> targetResources )
        throws IOException
    {
        // check the nexus.xml
        Configuration config = NexusConfigUtil.getNexusConfig();

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

            if ( !found )
            {

                Assert.fail( "Target with ID: " + targetResource.getId() + " could not be found in configuration." );
            }
        }
    }

    public void verifyCompleteTargetsConfig( List<RepositoryTargetListResource> targets ) throws IOException
    {
        // check the nexus.xml
        Configuration config = NexusConfigUtil.getNexusConfig();

        List<CRepositoryTarget> repoTargets = config.getRepositoryTargets();
        // check to see if the size matches
        Assert.assertTrue( "Configuration had a different number: (" + repoTargets.size()
            + ") of targets then expected: (" + targets.size() + ")", repoTargets.size() == targets.size() );

        // look for the target by id

        for ( Iterator<RepositoryTargetListResource> iter = targets.iterator(); iter.hasNext(); )
        {
            RepositoryTargetListResource targetResource = iter.next();
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

                    break;
                }

            }

            if ( !found )
            {

                Assert.fail( "Target with ID: " + targetResource.getId() + " could not be found in configuration." );
            }
        }

    }

    

}
