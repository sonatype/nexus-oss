package org.sonatype.nexus.rest;

import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

import com.thoughtworks.xstream.XStream;

public class XStreamInitializer
{
    public static void init( XStream xstream )
    {
        xstream.processAnnotations( SearchResponse.class );
        
        xstream.registerLocalConverter( SearchResponse.class, "data", new AliasingListConverter( NexusArtifact.class,
            "artifact" ) );
    }
}
