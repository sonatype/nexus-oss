package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.registry.UnknownContentClass;

public class DefaultGroupRepository
    extends DefaultRepository
    implements GroupRepository, EventListener
{
    private static final UnknownContentClass UNKNOWN_CONTENT_CLASS = new UnknownContentClass();

    private final List<Repository> memberRepositories = new ArrayList<Repository>();

    private ContentClass contentClass = UNKNOWN_CONTENT_CLASS;

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public List<Repository> getMemberRepositories()
    {
        return Collections.unmodifiableList( memberRepositories );
    }

    public void addMemberRepositories( List<Repository> repositories )
        throws InvalidGroupingException
    {
        if ( contentClass != UNKNOWN_CONTENT_CLASS )
        {
            // test for compatibility of all of them at once if we already have members and thus, contentClass
            for ( Repository repository : repositories )
            {
                if ( !contentClass.isCompatible( repository.getRepositoryContentClass() ) )
                {
                    throw new InvalidGroupingException( contentClass, repository.getRepositoryContentClass() );
                }
            }
        }
        else
        {
            // we are empty
            // test that they all have compatible content classes
            // note: the isCompatible() is commutative but not associative, so one way is enough to be checked
            for ( int i = 0; i < repositories.size() - 1; i++ )
            {
                Repository r1 = repositories.get( i );

                for ( Repository r2 : repositories.subList( i, repositories.size() - 1 ) )
                {
                    if ( !r1.getRepositoryContentClass().isCompatible( r2.getRepositoryContentClass() ) )
                    {
                        throw new InvalidGroupingException( r1.getRepositoryContentClass(), r2
                            .getRepositoryContentClass() );
                    }
                }
            }
        }

        // add it as batch
        if ( memberRepositories.size() == 0 )
        {
            // group was empty
            // TODO: and how does this fit with the fact the ContentClass is not associative?
            this.contentClass = repositories.get( 0 ).getRepositoryContentClass();

            memberRepositories.addAll( repositories );
        }
        else
        {
            for ( Repository repository : repositories )
            {
                // add it to list only if not already added
                if ( !memberRepositories.contains( repository ) )
                {
                    memberRepositories.add( repository );
                }
            }

            // TODO: should we consider as error repeated addition to group?
        }
    }

    public void addMemberRepository( Repository repository )
        throws InvalidGroupingException
    {
        if ( memberRepositories.size() == 0 )
        {
            // group was empty
            this.contentClass = repository.getRepositoryContentClass();

            memberRepositories.add( repository );
        }
        else if ( !contentClass.isCompatible( repository.getRepositoryContentClass() ) )
        {
            // invalid grouping, not compatible
            throw new InvalidGroupingException( contentClass, repository.getRepositoryContentClass() );
        }
        else
        {
            // add it to list only if not already added
            if ( !memberRepositories.contains( repository ) )
            {
                memberRepositories.add( repository );
            }

            // TODO: should we consider as error repeated addition to group?
        }
    }

    public void removeMemberRepository( Repository repository )
        throws NoSuchRepositoryException,
            InvalidGroupingException
    {
        if ( !memberRepositories.remove( repository ) )
        {
            throw new NoSuchRepositoryException( repository.getId() );
        }

        if ( memberRepositories.size() == 0 )
        {
            contentClass = UNKNOWN_CONTENT_CLASS;
        }
    }

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        List<StorageItem> resultItems = new ArrayList<StorageItem>();

        boolean wasAccessDeniedException = false;

        for ( Repository repository : memberRepositories )
        {
            try
            {
                if ( checkConditions( repository, request, Action.read ) )
                {
                    resultItems.add( repository.retrieveItem( request ) );
                }
            }
            catch ( NoSuchResourceStoreException e )
            {
                // nothing, keep moving
            }
            catch ( RepositoryNotAvailableException e )
            {
                // nothing, keep moving
            }
            catch ( ItemNotFoundException e )
            {
                // nothing
            }
            catch ( AccessDeniedException e )
            {
                wasAccessDeniedException = true;
            }
            catch ( StorageException e )
            {
                // nothing, but log it
                getLogger().info( "We had a storage IO problem during group processing, continuing...", e );
            }
        }

        if ( resultItems.size() > 0 )
        {
            StorageItem result = retrieveItemPostprocessor( request, resultItems );

            if ( ( result instanceof StorageCollectionItem ) && !isBrowseable() )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug(
                        getId() + " retrieveItem() :: FOUND a collection on "
                            + result.getRepositoryItemUid().toString() + " but group is not Browseable." );
                }

                throw new ItemNotFoundException( result.getRepositoryItemUid() );
            }
            else
            {
                return result;
            }
        }
        else
        {
            if ( wasAccessDeniedException )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Banned from all searched repositories, throwing AccessDeniedException." );
                }

                throw new AccessDeniedException( request, "All group members of group ID='" + getId()
                    + "' denied access!" );
            }
            else
            {
                throw new ItemNotFoundException( request.getRequestPath() );
            }
        }
    }

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( !isBrowseable() )
        {
            throw new RepositoryNotListableException( getId() );
        }

        List<StorageItem> resultItems = new ArrayList<StorageItem>();

        for ( Repository repository : memberRepositories )
        {
            try
            {
                if ( checkConditions( repository, request, Action.read ) )
                {
                    resultItems.addAll( repository.list( request ) );
                }
            }
            catch ( NoSuchResourceStoreException e )
            {
                // nothing, keep moving
            }
            catch ( RepositoryNotListableException e )
            {
                // nothing, keep moving
            }
            catch ( RepositoryNotAvailableException e )
            {
                // nothing, keep moving
            }
            catch ( ItemNotFoundException e )
            {
                // nothing
            }
            catch ( AccessDeniedException e )
            {
                // nothing
            }
            catch ( StorageException e )
            {
                // nothing, but log it
                getLogger().info( "We had a storage IO problem during group processing, continuing...", e );
            }
        }

        return resultItems;
    }

    protected AbstractStorageItem doRetrieveRemoteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            StorageException
    {
        throw new StorageException( "Functionality not implemented!" );
    }

    protected Collection<StorageItem> doListItems( RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        return getLocalStorage().listItems( uid );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        super.onProximityEvent( evt );

        // we are simply "proxying"
        notifyProximityEventListeners( evt );
    }

    // ==

    protected StorageItem retrieveItemPostprocessor( ResourceStoreRequest request, List<StorageItem> listOfStorageItems )
        throws StorageException
    {
        return null;
    }

}
