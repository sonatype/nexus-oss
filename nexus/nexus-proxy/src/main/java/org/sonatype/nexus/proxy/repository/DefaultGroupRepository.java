/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
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

public class DefaultGroupRepository
    extends DefaultRepository
    implements GroupRepository, EventListener
{
    private final List<Repository> memberRepositories = new CopyOnWriteArrayList<Repository>();

    private ContentClass contentClass = null;

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public void setRepositoryContentClass( ContentClass contentClass )
    {
        this.contentClass = contentClass;
    }

    public List<Repository> getMemberRepositories()
    {
        // RO + a copy is returned
        return Collections.unmodifiableList( new ArrayList<Repository>( memberRepositories ) );
    }

    public void addMemberRepositories( List<Repository> repositories )
        throws InvalidGroupingException
    {
        if ( repositories == null || repositories.size() == 0 )
        {
            // return silently, do not modify members
            return;
        }

        if ( getMemberRepositories().size() != 0 )
        {
            // we are not empty
            // test for compatibility of all of them at once if we already have members
            for ( Repository repository : repositories )
            {
                if ( !getRepositoryContentClass().isCompatible( repository.getRepositoryContentClass() ) )
                {
                    throw new InvalidGroupingException( getRepositoryContentClass(), repository
                        .getRepositoryContentClass() );
                }
            }
        }
        else
        {
            // we are empty
            // simply get the contentClass of the 1st repository, and check for compatibility
            ContentClass toBeClass = repositories.get( 0 ).getRepositoryContentClass();

            for ( Repository repository : repositories )
            {
                if ( !toBeClass.isCompatible( repository.getRepositoryContentClass() ) )
                {
                    throw new InvalidGroupingException( contentClass, repository.getRepositoryContentClass() );
                }
            }

            setRepositoryContentClass( toBeClass );
        }

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

    public void addMemberRepository( Repository repository )
        throws InvalidGroupingException
    {
        if ( getRepositoryContentClass() == null )
        {
            // group was empty, no contentClass is set
            this.contentClass = repository.getRepositoryContentClass();

            memberRepositories.add( repository );
        }
        else if ( !getRepositoryContentClass().isCompatible( repository.getRepositoryContentClass() ) )
        {
            // invalid grouping, not compatible
            throw new InvalidGroupingException( getRepositoryContentClass(), repository.getRepositoryContentClass() );
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
            contentClass = null;
        }
    }

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException,
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
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( !isBrowseable() )
        {
            throw new RepositoryNotListableException( this );
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
            catch ( IllegalOperationException e )
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

    @Override
    protected AbstractStorageItem doRetrieveRemoteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            StorageException
    {
        throw new StorageException( "Functionality not implemented!" );
    }

    @Override
    protected Collection<StorageItem> doListItems( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            StorageException
    {
        return getLocalStorage().listItems( uid );
    }

    @Override
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
