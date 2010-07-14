package org.sonatype.nexus.rest;

import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.NexusNGArtifact;
import org.sonatype.nexus.rest.model.NexusNGArtifactHit;
import org.sonatype.nexus.rest.model.NexusNGArtifactLink;
import org.sonatype.nexus.rest.model.SearchNGResponse;
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

        // NG
        
        xstream.processAnnotations( SearchNGResponse.class );
        xstream.processAnnotations( NexusNGArtifact.class );
        xstream.processAnnotations( NexusNGArtifactHit.class );
        xstream.processAnnotations( NexusNGArtifactLink.class );

        xstream.registerLocalConverter( SearchNGResponse.class, "data", new AliasingListConverter(
            NexusNGArtifact.class, "artifact" ) );
        xstream.registerLocalConverter( NexusNGArtifact.class, "artifactHits", new AliasingListConverter(
            NexusNGArtifactHit.class, "artifactHit" ) );
        xstream.registerLocalConverter( NexusNGArtifactHit.class, "artifactLinks", new AliasingListConverter(
            NexusNGArtifactLink.class, "artifactLink" ) );
    }
}
