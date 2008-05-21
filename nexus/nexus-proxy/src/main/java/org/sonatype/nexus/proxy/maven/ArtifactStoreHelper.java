package org.sonatype.nexus.proxy.maven;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class ArtifactStoreHelper
    implements ArtifactStore
{
    private final MavenRepository repository;

    public ArtifactStoreHelper( MavenRepository repo )
    {
        super();

        this.repository = repo;
    }

    public StorageFileItem retrieveArtifactPom( GAVRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        Gav gav = new Gav(
            gavRequest.getGroupId(),
            gavRequest.getArtifactId(),
            gavRequest.getVersion(),
            null,
            "pom",
            null,
            null,
            null,
            RepositoryPolicy.SNAPSHOT.equals( repository.getRepositoryPolicy() ),
            false,
            null );

        RepositoryItemUid uid = new RepositoryItemUid( repository, repository.getGavCalculator().gavToPath( gav ) );

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

    public StorageFileItem retrieveArtifact( GAVRequest gavRequest )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        // TODO: packaging2extension mapping, now we default to JAR
        Gav gav = new Gav( gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), gavRequest
            .getClassifier(), "jar", null, null, null, RepositoryPolicy.SNAPSHOT.equals( repository
            .getRepositoryPolicy() ), false, null );

        RepositoryItemUid uid = new RepositoryItemUid( repository, repository.getGavCalculator().gavToPath( gav ) );

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

    public void storeArtifactPom( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void storeArtifact( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void storeArtifactWithGeneratedPom( GAVRequest gavRequest, InputStream is )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void deleteArtifact( GAVRequest gavRequest, boolean withAllSubordinates )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public Collection<Gav> listArtifacts( GAVRequest gavRequest )
    {
        return Collections.emptyList();
    }

}
