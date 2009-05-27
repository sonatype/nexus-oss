package org.sonatype.nexus.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.access.NexusItemAuthorizer;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = IndexArtifactFilter.class )
public class DefaultIndexArtifactFilter
    extends AbstractLogEnabled
    implements IndexArtifactFilter
{

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private NexusItemAuthorizer nexusItemAuthorizer;

    public Collection<ArtifactInfo> filterArtifactInfos( Collection<ArtifactInfo> artifactInfos )
    {
        if ( artifactInfos == null )
        {
            return null;
        }
        List<ArtifactInfo> result = new ArrayList<ArtifactInfo>( artifactInfos.size() );
        for ( ArtifactInfo artifactInfo : artifactInfos )
        {
            if ( this.filterArtifactInfo( artifactInfo ) )
            {
                result.add( artifactInfo );
            }
        }
        return result;
    }

    public boolean filterArtifactInfo( ArtifactInfo artifactInfo )
    {
        try
        {
            Repository repository = this.repositoryRegistry.getRepository( artifactInfo.repository );

            if ( MavenRepository.class.isAssignableFrom( repository.getClass() ) )
            {
                MavenRepository mr = (MavenRepository) repository;

                Gav gav = new Gav(
                    artifactInfo.groupId,
                    artifactInfo.artifactId,
                    artifactInfo.version,
                    artifactInfo.classifier,
                    mr.getArtifactPackagingMapper().getExtensionForPackaging( artifactInfo.packaging ),
                    null,
                    null,
                    null,
                    VersionUtils.isSnapshot( artifactInfo.version ),
                    false,
                    null,
                    false,
                    null );

                ResourceStoreRequest req = new ResourceStoreRequest( mr.getGavCalculator().gavToPath( gav ) );

                return this.nexusItemAuthorizer.authorizePath( mr, req, Action.read );
            }
            else
            {
                // we are only filtering maven artifacts
                return true;
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            this.getLogger().warn(
                "Repository not found for artifact: " + artifactInfo.groupId + ":" + artifactInfo.artifactId + ":"
                    + artifactInfo.version + " in repository: " + artifactInfo.repository,
                e );

            // artifact does not exist, filter it out
            return false;
        }
    }
}
