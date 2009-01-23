package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListRequest;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.rest.model.MirrorStatusResource;
import org.sonatype.nexus.rest.model.MirrorStatusResourceListResponse;
import org.sonatype.nexus.util.StringDigester;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class MirrorMessageUtils
{
    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( MirrorMessageUtils.class );

    public MirrorMessageUtils( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }
    
    public MirrorResourceListResponse getMirrors( String repositoryId )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = "service/local/repository_mirrors/" + repositoryId;

        Response response = RequestFacade.sendMessage( serviceURI, Method.GET, representation );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not get mirrors: " + response.getStatus() + ":\n" + responseText );
        }

        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        MirrorResourceListResponse resourceResponse = (MirrorResourceListResponse) representation
            .getPayload( new MirrorResourceListResponse() );

        Assert.assertNotNull( "Resource Response shouldn't be null", resourceResponse );
        
        for ( MirrorResource resource : ( List<MirrorResource> ) resourceResponse.getData() )
        {
            Assert.assertNotNull( "Id shouldn't be null", resource.getId() );
        }
        
        return resourceResponse;
    }

    public MirrorResourceListResponse setMirrors( String repositoryId, MirrorResourceListRequest resourceRequest )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = "service/local/repository_mirrors/" + repositoryId;

        // now set the payload
        representation.setPayload( resourceRequest );

        Response response = RequestFacade.sendMessage( serviceURI, Method.POST, representation );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not set mirrors: " + response.getStatus() + ":\n" + responseText );
        }

        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        MirrorResourceListResponse resourceResponse = (MirrorResourceListResponse) representation
            .getPayload( new MirrorResourceListResponse() );

        Assert.assertNotNull( "Resource Response shouldn't be null", resourceResponse );
        
        for ( MirrorResource resource : ( List<MirrorResource> ) resourceResponse.getData() )
        {
            Assert.assertNotNull( "Id shouldn't be null", resource.getId() );
        }
        
        for ( int i = 0 ; i < resourceResponse.getData().size() ; i++ )
        {
            Assert.assertEquals( ( ( MirrorResource ) resourceResponse.getData().get( i ) ).getUrl(), ( ( MirrorResource ) resourceRequest.getData().get( i ) ).getUrl() ); 
        }
        
        return resourceResponse;
    }
    
    public MirrorStatusResourceListResponse getMirrorsStatus( String repositoryId )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = "service/local/repository_mirrors_status/" + repositoryId;

        Response response = RequestFacade.sendMessage( serviceURI, Method.GET, representation );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not get mirrors status: " + response.getStatus() + ":\n" + responseText );
        }

        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        MirrorStatusResourceListResponse resourceResponse = (MirrorStatusResourceListResponse) representation
            .getPayload( new MirrorStatusResourceListResponse() );

        Assert.assertNotNull( "Resource Response shouldn't be null", resourceResponse );
        
        for ( MirrorStatusResource resource : ( List<MirrorStatusResource> ) resourceResponse.getData() )
        {
            Assert.assertNotNull( "Id shouldn't be null", resource.getId() );
        }
        
        return resourceResponse;
    }
    
    public MirrorResourceListResponse getPredefinedMirrors( String repositoryId )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = "service/local/repository_predefined_mirrors/" + repositoryId;

        Response response = RequestFacade.sendMessage( serviceURI, Method.GET, representation );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not get predefined mirrors: " + response.getStatus() + ":\n" + responseText );
        }

        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        MirrorResourceListResponse resourceResponse = (MirrorResourceListResponse) representation
            .getPayload( new MirrorResourceListResponse() );

        Assert.assertNotNull( "Resource Response shouldn't be null", resourceResponse );
        
        for ( MirrorResource resource : ( List<MirrorResource> ) resourceResponse.getData() )
        {
            Assert.assertNotNull( "URL shouldn't be null", resource.getUrl() );
            Assert.assertEquals( StringDigester.getSha1Digest( resource.getUrl() ), resource.getId() );
        }
        
        return resourceResponse;
    }
}
