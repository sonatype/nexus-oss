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

/**
 * A very simple RawTransport that uses Restlet under the hud.
 * 
 * @author cstamas
 */
public class RestletRawTransport
    implements RawTransport
{
    private Client client;

    private String repositoryRoot;

    /**
     * Creates a default instance of RestletRawTransport using the HTTP protocol.
     * 
     * @param ulr the repo root
     */
    public RestletRawTransport( String url )
    {
        this( url, Protocol.HTTP );
    }

    /**
     * Creates an instance of RestletRawRTransport using the supplied protocol. The invoker must care about connector
     * availability.
     * 
     * @param url the repo root
     * @param protocol
     */
    public RestletRawTransport( String url, Protocol protocol )
    {
        this( url, new Client( protocol ) );
    }

    /**
     * Creates an instance of RestletRawTransport using the supplied Restlet Client.
     * 
     * @param url the repo root
     * @param client
     */
    public RestletRawTransport( String url, Client client )
    {
        this.client = client;

        this.repositoryRoot = url;
    }

    public byte[] readRawData( String path )
        throws IOException
    {
        while ( path.startsWith( "/" ) )
        {
            path = path.substring( 1 );
        }

        Request rr = createRequest( Method.GET, path );

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

    public void writeRawData( String path, byte[] data )
        throws IOException
    {
        while ( path.startsWith( "/" ) )
        {
            path = path.substring( 1 );
        }

        Request rr = createRequest( Method.PUT, path );

        ByteArrayRepresentation entity = new ByteArrayRepresentation( MediaType.APPLICATION_XML, data );

        rr.setEntity( entity );

        Response response = client.handle( rr );

        if ( !response.getStatus().isSuccess() )
        {
            throw new IOException( "The response was not successful: " + response.getStatus() );
        }
    }

    protected Request createRequest( Method method, String path )
    {
        Request request = new Request( method, new Reference( repositoryRoot, path ) );

        ClientInfo ci = new ClientInfo();

        ci.setAgent( "NexusRM/1.0.0" );

        ci.setAcceptedMediaTypes( Collections.singletonList( new Preference<MediaType>( MediaType.APPLICATION_XML ) ) );

        request.setClientInfo( ci );

        return request;
    }

}
