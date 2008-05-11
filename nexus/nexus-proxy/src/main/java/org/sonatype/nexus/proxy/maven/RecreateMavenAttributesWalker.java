package org.sonatype.nexus.proxy.maven;

import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.RecreateAttributesWalker;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class RecreateMavenAttributesWalker
    extends RecreateAttributesWalker
{
    private boolean shouldFixChecksums;

    public RecreateMavenAttributesWalker( Repository repository, Logger logger, Map<String, String> initialData )
    {
        super( repository, logger, initialData );

        this.shouldFixChecksums = RepositoryType.HOSTED.equals( getRepository().getRepositoryType() );
    }

    @Override
    protected void processFileItem( StorageFileItem item )
    {
        super.processFileItem( item );

        try
        {
            if ( shouldFixChecksums && !item.isVirtual() && !item.getName().endsWith( ".sha1" )
                && !item.getName().endsWith( ".md5" ) )
            {
                storeHashIfNotExists( item, "sha1", item.getAttributes().get(
                    DigestCalculatingInspector.DIGEST_SHA1_KEY ) );

                storeHashIfNotExists( item, "md5", item.getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY ) );
            }
        }
        catch ( RepositoryNotAvailableException e )
        {
            getLogger().info( "Cannot fix hashes!", e );
        }
        catch ( StorageException e )
        {
            getLogger().info( "Cannot fix hashes!", e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            getLogger().info( "Cannot fix hashes!", e );
        }
    }

    protected void storeHashIfNotExists( StorageFileItem file, String ext, String hash )
        throws StorageException,
            UnsupportedStorageOperationException,
            RepositoryNotAvailableException
    {
        RepositoryItemUid hashUid = new RepositoryItemUid( file.getRepositoryItemUid().getRepository(), file
            .getRepositoryItemUid().getPath()
            + "." + ext );

        Repository repository = file.getRepositoryItemUid().getRepository();

        if ( !repository.getLocalStorage().containsItem( hashUid ) )
        {
            DefaultStorageFileItem hashItem = new DefaultStorageFileItem(
                repository,
                hashUid.getPath(),
                true,
                true,
                new StringContentLocator( hash ) );

            repository.storeItem( hashItem );

            repository.removeFromNotFoundCache( hashUid.getPath() );
        }

    }
}
