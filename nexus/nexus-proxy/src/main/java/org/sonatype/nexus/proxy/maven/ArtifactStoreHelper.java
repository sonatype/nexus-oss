package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

public class ArtifactStoreHelper
    implements ArtifactStore
{
    private final MavenRepository repository;

    private final GavCalculator gavCalculator;

    public ArtifactStoreHelper( MavenRepository repo, GavCalculator gavCalculator )
    {
        super();

        this.repository = repo;

        this.gavCalculator = gavCalculator;
    }

    public StorageFileItem retrieveArtifact( String groupId, String artifactId, String version, String classifier )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        Gav gav = new Gav( groupId, artifactId, version, classifier, "jar", null, null, null, RepositoryPolicy.SNAPSHOT
            .equals( repository.getRepositoryPolicy() ), false, null );

        RepositoryItemUid uid = new RepositoryItemUid( repository, gavCalculator.gavToPath( gav ) );

        StorageItem item = repository.retrieveItem( false, uid );

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            return (StorageFileItem) item;
        }
        else
        {
            throw new StorageException( "The Artifact retrieval returned non-file, path:" + uid.getPath() );
        }
    }

    public StorageFileItem retrieveArtifactPom( String groupId, String artifactId, String version )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        Gav gav = new Gav( groupId, artifactId, version, null, "pom", null, null, null, RepositoryPolicy.SNAPSHOT
            .equals( repository.getRepositoryPolicy() ), false, null );

        RepositoryItemUid uid = new RepositoryItemUid( repository, gavCalculator.gavToPath( gav ) );

        StorageItem item = repository.retrieveItem( false, uid );

        if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            return (StorageFileItem) item;
        }
        else
        {
            throw new StorageException( "The POM retrieval returned non-file, path:" + uid.getPath() );
        }
    }
}
