package org.sonatype.nexus.integrationtests.nexus383;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class SearchMessageUtil
{

    private String baseNexusUrl;

    public SearchMessageUtil( String baseNexusUrl )
    {
        super();
        this.baseNexusUrl = baseNexusUrl;
    }

    @SuppressWarnings( "unchecked" )
    public List<NexusArtifact> searchFor( String query )
        throws Exception
    {
        String serviceURI = this.baseNexusUrl + "service/local/data_index?q=" + query;
        System.out.println( "serviceURI: " + serviceURI );

        URL serviceURL = new URL( serviceURI );

        InputStream is = serviceURL.openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int readChar = -1;
        while ( ( readChar = is.read() ) != -1 )
        {
            out.write( readChar );
        }

        String responseText = out.toString();
        System.out.println( "responseText: \n" + responseText );
        XStream xstream = new XStream();
        XStreamInitializer.initialize( xstream );
        XStreamRepresentation representation =
            new XStreamRepresentation( xstream, responseText, MediaType.APPLICATION_XML );

        SearchResponse searchResponde = (SearchResponse) representation.getPayload( new SearchResponse() );

        return searchResponde.getData();
    }

    public NexusArtifact searchForSHA1( String sha1 )
        throws Exception
    {
        String serviceURI = this.baseNexusUrl + "service/local/identify/sha1/" + sha1;
        System.out.println( "serviceURI: " + serviceURI );

        URL serviceURL = new URL( serviceURI );

        InputStream is = serviceURL.openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int readChar = -1;
        while ( ( readChar = is.read() ) != -1 )
        {
            out.write( readChar );
        }

        String responseText = out.toString();
        if ( StringUtils.isEmpty( responseText ) )
        {
            return null;
        }
        System.out.println( "responseText: \n" + responseText );
        XStream xstream = new XStream();
        XStreamInitializer.initialize( xstream );
        // FIXME check with toby if I need to use this.
        // XStreamRepresentation representation = new XStreamRepresentation(
        // xstream, responseText, MediaType.APPLICATION_XML);
        //
        // SearchResponse searchResponde = (SearchResponse) representation
        // .getPayload(new SearchResponse());
        //
        // return searchResponde.getData();
        return (NexusArtifact) xstream.fromXML( responseText );
    }

}
