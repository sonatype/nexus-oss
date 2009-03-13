package org.sonatype.nexus.proxy.maven;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryRequest;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public abstract class AbstractMavenGroupRepository
    extends AbstractGroupRepository
    implements MavenGroupRepository
{
    /**
     * Metadata manager.
     */
    @Requirement
    private MetadataManager metadataManager;

    /**
     * The artifact packaging mapper.
     */
    @Requirement
    private ArtifactPackagingMapper artifactPackagingMapper;

    private ArtifactStoreHelper artifactStoreHelper;

    private RepositoryKind repositoryKind;

    public RepositoryKind getRepositoryKind()
    {
        if ( repositoryKind == null )
        {
            repositoryKind = new DefaultRepositoryKind( GroupRepository.class, Arrays
                .asList( new Class<?>[] { MavenGroupRepository.class } ) );
        }
        return repositoryKind;
    }

    public ArtifactPackagingMapper getArtifactPackagingMapper()
    {
        return artifactPackagingMapper;
    }

    public ArtifactStoreHelper getArtifactStoreHelper()
    {
        if ( artifactStoreHelper == null )
        {
            artifactStoreHelper = new ArtifactStoreHelper( this );
        }

        return artifactStoreHelper;
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    public boolean recreateMavenMetadata( ResourceStoreRequest request )
    {
        return false;
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return RepositoryPolicy.MIXED;
    }

    public void setRepositoryPolicy( RepositoryPolicy repositoryPolicy )
    {
        throw new UnsupportedOperationException(
            "Setting repository policy on a Maven group repository is not possible!" );
    }

    public void storeItemWithChecksums( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().storeItemWithChecksums( request, is, userAttributes );
    }

    public void storeItemWithChecksums( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            StorageException
    {
        getArtifactStoreHelper().storeItemWithChecksums( item );
    }

    public void deleteItemWithChecksums( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            ItemNotFoundException,
            IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        getArtifactStoreHelper().deleteItemWithChecksums( request );
    }

    public void deleteItemWithChecksums( RepositoryRequest request )
        throws UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        getArtifactStoreHelper().deleteItemWithChecksums( request );
    }

}
