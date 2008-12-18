package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.mapping.RequestRepositoryMapper;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public abstract class AbstractGroupRepository
    extends AbstractRepository
    implements GroupRepository
{
    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private RequestRepositoryMapper requestRepositoryMapper;

    private List<String> memberRepoIds = new ArrayList<String>();

    @Override
    protected void doDeleteItem( RepositoryItemUid uid )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        throw new UnsupportedStorageOperationException( "Cannot modify repository group content" );
    }

    @Override
    protected Collection<StorageItem> doListItems( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        HashSet<String> names = new HashSet<String>();
        ArrayList<StorageItem> result = new ArrayList<StorageItem>();
        boolean found = false;
        try
        {
            addItems( names, result, getLocalStorage().listItems( uid ) );
            found = true;
        }
        catch ( ItemNotFoundException ignored )
        {
            // ignored
        }

        for ( Repository repo : getMemberRepositories() )
        {
            try
            {
                RepositoryItemUid memberUid = repo.createUid( uid.getPath() );

                ResourceStoreRequest req = new ResourceStoreRequest( memberUid, localOnly );
                req.setRequestContext( context );

                addItems( names, result, repo.list( req ) );

                found = true;
            }
            catch ( ItemNotFoundException e )
            {
                // ignored
            }
            catch ( IllegalOperationException e )
            {
                // ignored
            }
            catch ( StorageException e )
            {
                // ignored
            }
            catch ( AccessDeniedException e )
            {
                // ignored
            }
        }

        if ( !found )
        {
            throw new ItemNotFoundException( uid );
        }

        return result;
    }

    private static void addItems( HashSet<String> names, ArrayList<StorageItem> result,
        Collection<StorageItem> listItems )
    {
        for ( StorageItem item : listItems )
        {
            if ( names.add( item.getPath() ) )
            {
                result.add( item );
            }
        }
    }

    @Override
    protected StorageItem doRetrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        try
        {
            // local always wins
            return getLocalStorage().retrieveItem( uid );
        }
        catch ( ItemNotFoundException ignored )
        {
            // ignored
        }

        for ( Repository repo : getRequestRepositories( uid ) )
        {
            try
            {
                RepositoryItemUid memberUid = repo.createUid( uid.getPath() );

                StorageItem item = repo.retrieveItem( localOnly, memberUid, context );

                if ( item instanceof StorageCollectionItem )
                {
                    item = new DefaultStorageCollectionItem( this, uid.getPath(), true, false );
                }

                return item;
            }
            catch ( IllegalOperationException e )
            {
                // ignored
            }
            catch ( ItemNotFoundException e )
            {
                // ignored
            }
            catch ( StorageException e )
            {
                // ignored
            }
        }

        throw new ItemNotFoundException( uid );
    }

    public List<Repository> getMemberRepositories()
    {
        ArrayList<Repository> result = new ArrayList<Repository>();

        try
        {
            for ( String repoId : memberRepoIds )
            {
                Repository repo = repoRegistry.getRepository( repoId );
                result.add( repo );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            // XXX throw new StorageException( e );
        }

        return result;
    }

    protected List<Repository> getRequestRepositories( RepositoryItemUid uid )
        throws StorageException
    {
        List<Repository> members = getMemberRepositories();

        try
        {
            return requestRepositoryMapper.getMappedRepositories( repoRegistry, uid, members );
        }
        catch ( NoSuchResourceStoreException e )
        {
            throw new StorageException( e );
        }
    }

    public void setMemberRepositories( List<String> repositories )
    {
        memberRepoIds = new ArrayList<String>( repositories );
    }

    public void removeMemberRepository( String repositoryId )
    {
        memberRepoIds.remove( repositoryId );
    }

    public List<StorageItem> doRetrieveItems( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws StorageException
    {
        ArrayList<StorageItem> items = new ArrayList<StorageItem>();

        for ( Repository repository : getRequestRepositories( uid ) )
        {
            RepositoryItemUid muid = repository.createUid( uid.getPath() );

            try
            {
                items.add( repository.retrieveItem( localOnly, muid, context ) );
            }
            catch ( StorageException e )
            {
                throw e;
            }
            catch ( IllegalOperationException e )
            {
                getLogger().warn( "Member repository request failed", e );
            }
            catch ( ItemNotFoundException e )
            {
                // that's okay
            }
        }

        return items;
    }

}
