package org.sonatype.nexus.proxy.maven.uid;

import org.apache.maven.index.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.uid.Attribute;
import org.sonatype.nexus.proxy.maven.MavenRepository;

/**
 * Boolean Attribute that returns true if UID represents a path in Maven repository, and path obeys Maven repository
 * layout and points to a Maven Repository Metadata (maven-metadata.xml) file.
 * 
 * @author cstamas
 */
public class IsMavenRepositoryMetadataAttribute
    implements Attribute<Boolean>
{
    @Override
    public Boolean getValueFor( RepositoryItemUid subject )
    {
        return subject.getRepository().getRepositoryKind().isFacetAvailable( MavenRepository.class )
            && M2ArtifactRecognizer.isMetadata( subject.getPath() )
            && !M2ArtifactRecognizer.isChecksum( subject.getPath() );
    }
}
