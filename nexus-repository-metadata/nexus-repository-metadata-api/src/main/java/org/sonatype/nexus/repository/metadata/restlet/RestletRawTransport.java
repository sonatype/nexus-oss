package org.sonatype.nexus.repository.metadata.restlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.restlet.Client;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.repository.metadata.RawTransport;
import org.sonatype.nexus.repository.metadata.RawTransportRequest;

/**
 * A very simple RawTransport that uses Restlet under the hud.
 * 
 * @author cstamas
 */
public class RestletRawTransport
    implements RawTransport
{
    private Client client;

    /**
     * Creates a default instance of RestletRawTransport using the HTTP protocol.
     */
    public RestletRawTransport()
    {
        this( Protocol.HTTP );
    }

    /**
     * Creates an instance of RestletRawRTransport using the supplied protocol. The invoker must care about connector
     * availability.
     * 
     * @param protocol
     */
    public RestletRawTransport( Protocol protocol )
    {
        this( new Client( protocol ) );
    }

    /**
     * Creates an instance of RestletRawTransport using the supplied Restlet Client.
     * 
     * @param client
     */
    public RestletRawTransport( Client client )
    {
        this.client = client;
    }

    public byte[] readRawData( RawTransportRequest request )
        throws IOException
    {
        while ( request.getPath().startsWith( "/" ) )
        {
            request.setPath( request.getPath().substring( 1 ) );
        }

        Request rr = createRequest( Method.GET, request );

        Response response = client.handle( rr );

        if ( response.getStatus().isSuccess() && response.isEntityAvailable() )
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            response.getEntity().write( baos );

            return baos.toByteArray();
        }
        else if ( response.getStatus().equals( Status.CLIENT_ERROR_NOT_FOUND ) )
        {
            return null;
        }
        else
        {
            throw new IOException( "The response was not successful: " + response.getStatus() );
        }
    }

    protected Request createRequest( Method method, RawTransportRequest req )
    {
        Request request = new Request( method, new Reference( req.getUrl(), req.getPath() ) );

        ClientInfo ci = new ClientInfo();

        ci.setAgent( "NexusRM/1.0.0" );

        ci.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_XML ) ) );

        request.setClientInfo( ci );

        return request;
    }

}
