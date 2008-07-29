package org.sonatype.nexus.integrationtests.nexus233;

import java.io.IOException;
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
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeListResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeStatusResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class PrivilegesMessageUtil
{

    private XStream xstream;

    private MediaType mediaType;

    private String baseNexusUrl;

    public PrivilegesMessageUtil( XStream xstream, MediaType mediaType, String baseNexusUrl )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
        this.baseNexusUrl = baseNexusUrl;
    }

    public Response sendMessage( Method method, PrivilegeBaseResource resource )
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String privName = ( method == Method.POST ) ? "" : "/" + resource.getName();

        String serviceURI = this.baseNexusUrl + "service/local/privileges" + privName;
        System.out.println( "serviceURI: " + serviceURI );

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( method );

        PrivilegeResourceRequest requestResponse = new PrivilegeResourceRequest();
        requestResponse.setData( resource );

        // now set the payload
        representation.setPayload( requestResponse );
        System.out.println( method.getName() + ": "+ representation.getText() );
        request.setEntity( representation );

        Client client = new Client( Protocol.HTTP );

        return client.handle( request );
    }

    public PrivilegeBaseStatusResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        PrivilegeStatusResourceResponse resourceResponse =
            (PrivilegeStatusResourceResponse) representation.getPayload( new PrivilegeStatusResourceResponse() );

        return (PrivilegeBaseStatusResource) resourceResponse.getData();

    }
    
    public List<PrivilegeBaseStatusResource> getResourceListFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
    
        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
    
        PrivilegeListResourceResponse resourceResponse =
            (PrivilegeListResourceResponse) representation.getPayload( new PrivilegeListResourceResponse() );
    
        return resourceResponse.getData();
    }

    public void verifyPrivilegeConfig( PrivilegeBaseStatusResource privilegeResource )
        throws IOException
    {
        ArrayList<PrivilegeBaseStatusResource> targetResources = new ArrayList<PrivilegeBaseStatusResource>();
        targetResources.add( privilegeResource );
        this.verifyPrivilegeConfig( targetResources );
    }

    @SuppressWarnings( "unchecked" )
    public void verifyPrivilegeConfig( List<PrivilegeBaseStatusResource> privilegeResources )
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

        for ( Iterator<PrivilegeBaseStatusResource> iter = privilegeResources.iterator(); iter.hasNext(); )
        {
            PrivilegeBaseStatusResource targetResource = iter.next();
            boolean found = false;

            for ( Iterator<CRepositoryTarget> iterInner = repoTargets.iterator(); iterInner.hasNext(); )
            {
                CRepositoryTarget repositoryTarget = iterInner.next();

                if ( targetResource.getId().equals( repositoryTarget.getId() ) )
                {
                    found = true;
//                    Assert.assertEquals( targetResource.getId(), repositoryTarget.getId() );
//                    Assert.assertEquals( targetResource.getContentClass(), repositoryTarget.getContentClass() );
//                    Assert.assertEquals( targetResource.getName(), repositoryTarget.getName() );
//                    Assert.assertEquals( targetResource.getPatterns(), repositoryTarget.getPatterns() );

                    break;
                }

            }

            if ( !found )
            {

                Assert.fail( "Target with ID: " + targetResource.getId() + " could not be found in configuration." );
            }
        }
    }

    private Configuration getNexusConfig()
        throws IOException
    {

        URL configURL = new URL( this.baseNexusUrl + "service/local/configs/current" );

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

}
