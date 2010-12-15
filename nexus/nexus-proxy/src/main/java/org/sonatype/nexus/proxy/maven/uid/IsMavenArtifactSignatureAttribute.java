package org.sonatype.nexus.proxy.maven.uid;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.uid.Attribute;
import org.sonatype.nexus.proxy.maven.MavenRepository;

/**
 * Boolean Attribute that returns true if UID represents a path in Maven repository, and path obeys Maven repository
 * layout and is a path pointing to Maven signature file.
 * 
 * @author cstamas
 */
public class IsMavenArtifactSignatureAttribute
    implements Attribute<Boolean>
{
    @Override
    public Boolean getValueFor( RepositoryItemUid subject )
    {
        return subject.getRepository().getRepositoryKind().isFacetAvailable( MavenRepository.class )
            && pathIsValidGav( subject.getRepository().adaptToFacet( MavenRepository.class ), subject.getPath() );
    }

    protected boolean pathIsValidGav( MavenRepository repository, String path )
    {
        try
        {
            Gav gav = repository.getGavCalculator().pathToGav( path );

            return gav != null && !gav.isHash() && gav.isSignature();
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            return false;
        }
    }
}
