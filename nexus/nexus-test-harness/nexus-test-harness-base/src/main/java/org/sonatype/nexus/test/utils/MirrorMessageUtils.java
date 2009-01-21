package org.sonatype.nexus.test.utils;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceRequest;
import org.sonatype.nexus.rest.model.MirrorResourceResponse;
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

    public MirrorResource createMirror( String repositoryId, MirrorResource mirror )
        throws IOException
    {
        Response response = this.sendMessage( Method.POST, repositoryId, mirror );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create mirror: " + response.getStatus() + ":\n" + responseText );
        }

        // get the Resource object
        MirrorResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null
        Assert.assertNotNull( "Mirror ID shouldn't be null: " + response.getEntity().getText(), responseResource.getId() );
        mirror.setId( responseResource.getId() );

        Assert.assertEquals( mirror.getUrl(), responseResource.getUrl() );
        Assert.assertEquals( mirror.getId(), responseResource.getId() );

        return mirror;
    }
    
    public MirrorResource updateMirror( String repositoryId, MirrorResource mirror )
        throws IOException
    {
        Response response = this.sendMessage( Method.PUT, repositoryId, mirror );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not update mirror: " + response.getStatus() + "\n" + responseText );
        }

        // get the Resource object
        MirrorResource responseResource = this.getResourceFromResponse( response );

        // make sure the id != null

        Assert.assertEquals( mirror.getId(), responseResource.getId() );
        Assert.assertEquals( mirror.getUrl(), responseResource.getUrl() );

        return responseResource;
    }
    
    public Response sendMessage( Method method, String repositoryId, MirrorResource resource )
        throws IOException
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String id = ( method == Method.POST ) ? "" : "/" + resource.getId();

        String serviceURI = "service/local/repository_mirrors/" + repositoryId + id;

        MirrorResourceRequest request = new MirrorResourceRequest();
        request.setData( resource );

        // now set the payload
        representation.setPayload( request );

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }
    
    public MirrorResource getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        // this
        MirrorResourceResponse resourceResponse = (MirrorResourceResponse) representation
            .getPayload( new MirrorResourceResponse() );

        return resourceResponse.getData();
    }
}
