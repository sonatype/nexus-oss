/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.ConfigurationPrepareForSaveEvent;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.mapping.RequestRepositoryMapper;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.plexus.appevents.Event;

/**
 * An abstract group repository. The specific behaviour (ie. metadata merge) should be implemented in subclases.
 * 
 * @author cstamas
 */
public abstract class AbstractGroupRepository
    extends AbstractRepository
    implements GroupRepository
{
    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private RequestRepositoryMapper requestRepositoryMapper;

    @Override
    protected AbstractGroupRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (AbstractGroupRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    public void onEvent( Event<?> evt )
    {
        // we must do this before the super.onEvent() call!
        boolean membersChanged =
            getCurrentCoreConfiguration().isDirty()
                && ( getExternalConfiguration( false ).getMemberRepositoryIds().size() != getExternalConfiguration(
                    true ).getMemberRepositoryIds().size() || !getExternalConfiguration( false )
                    .getMemberRepositoryIds().containsAll( getExternalConfiguration( true ).getMemberRepositoryIds() ) );

        super.onEvent( evt );

        // act automatically on repo removal. Remove it from myself if member.
        if ( evt instanceof RepositoryRegistryEventRemove )
        {
            RepositoryRegistryEventRemove revt = (RepositoryRegistryEventRemove) evt;

            if ( this
                .getExternalConfiguration( false ).getMemberRepositoryIds().contains( revt.getRepository().getId() ) )
            {
                removeMemberRepositoryId( revt.getRepository().getId() );
            }
        }
        else if ( evt instanceof ConfigurationPrepareForSaveEvent && membersChanged )
        {
            // fire another event
            getApplicationEventMulticaster().notifyEventListeners( new RepositoryGroupMembersChangedEvent( this ) );
        }

    }

    @Override
    protected Collection<StorageItem> doListItems( ResourceStoreRequest request )
        throws ItemNotFoundException, StorageException
    {
        HashSet<String> names = new HashSet<String>();
        ArrayList<StorageItem> result = new ArrayList<StorageItem>();
        boolean found = false;
        try
        {
            addItems( names, result, getLocalStorage().listItems( this, request ) );

            found = true;
        }
        catch ( ItemNotFoundException ignored )
        {
            // ignored
        }

        if ( !request.isRequestGroupLocalOnly() )
        {
            for ( Repository repo : getMemberRepositories() )
            {
                if ( !request.getProcessedRepositories().contains( repo.getId() ) )
                {
                    try
                    {
                        addItems( names, result, repo.list( false, request ) );

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
                }
                else
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger()
                            .debug(
                                "A repository CYCLE detected (doListItems()), while processing group ID='"
                                    + this.getId()
                                    + "'. The repository with ID='"
                                    + repo.getId()
                                    + "' was already processed during this request! This repository is skipped from processing. Request: "
                                    + request.toString() );
                    }
                }
            }
        }

        if ( !found )
        {
            throw new ItemNotFoundException( request, this );
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
    protected StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        try
        {
            // local always wins
            return super.doRetrieveItem( request );
        }
        catch ( ItemNotFoundException ignored )
        {
            // ignored
        }

        if ( !request.isRequestGroupLocalOnly() )
        {
            for ( Repository repo : getRequestRepositories( request ) )
            {
                if ( !request.getProcessedRepositories().contains( repo.getId() ) )
                {
                    try
                    {
                        StorageItem item = repo.retrieveItem( false, request );

                        if ( item instanceof StorageCollectionItem )
                        {
                            item = new DefaultStorageCollectionItem( this, request, true, false );
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
                else
                {
                    getLogger()
                        .info(
                            "A repository CYCLE detected (doRetrieveItem()), while processing group ID='"
                                + this.getId()
                                + "'. The repository with ID='"
                                + repo.getId()
                                + "' was already processed during this request! This repository is skipped from processing. Request: "
                                + request.toString() );
                }
            }
        }

        throw new ItemNotFoundException( request, this );
    }

    public List<String> getMemberRepositoryIds()
    {
        ArrayList<String> result =
            new ArrayList<String>( getExternalConfiguration( false ).getMemberRepositoryIds().size() );

        for ( String id : getExternalConfiguration( false ).getMemberRepositoryIds() )
        {
            result.add( id );
        }

        return Collections.unmodifiableList( result );
    }

    public void setMemberRepositoryIds( List<String> repositories )
        throws NoSuchRepositoryException, InvalidGroupingException
    {
        getExternalConfiguration( true ).clearMemberRepositoryIds();

        for ( String repoId : repositories )
        {
            addMemberRepositoryId( repoId );
        }
    }

    public void addMemberRepositoryId( String repositoryId )
        throws NoSuchRepositoryException, InvalidGroupingException
    {
        Repository repo = repoRegistry.getRepository( repositoryId );

        if ( repo.getRepositoryContentClass().isCompatible( getRepositoryContentClass() ) )
        {
            getExternalConfiguration( true ).addMemberRepositoryId( repositoryId );
        }
        else
        {
            throw new InvalidGroupingException( getRepositoryContentClass(), repo.getRepositoryContentClass() );
        }
    }

    public void removeMemberRepositoryId( String repositoryId )
    {
        getExternalConfiguration( true ).removeMemberRepositoryId( repositoryId );
    }

    public List<Repository> getMemberRepositories()
    {
        ArrayList<Repository> result = new ArrayList<Repository>();

        try
        {
            for ( String repoId : getMemberRepositoryIds() )
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

    protected List<Repository> getRequestRepositories( ResourceStoreRequest request )
        throws StorageException
    {
        List<Repository> members = getMemberRepositories();

        try
        {
            return requestRepositoryMapper.getMappedRepositories( this, request, members );
        }
        catch ( NoSuchResourceStoreException e )
        {
            throw new StorageException( e );
        }
    }

    public List<StorageItem> doRetrieveItems( ResourceStoreRequest request )
        throws StorageException
    {
        ArrayList<StorageItem> items = new ArrayList<StorageItem>();

        for ( Repository repository : getRequestRepositories( request ) )
        {
            if ( !request.getProcessedRepositories().contains( repository.getId() ) )
            {
                try
                {
                    StorageItem item = repository.retrieveItem( false, request );

                    items.add( item );
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
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger()
                        .debug(
                            "A repository CYCLE detected (doRetrieveItems()), while processing group ID='"
                                + this.getId()
                                + "'. The repository with ID='"
                                + repository.getId()
                                + "' was already processed during this request! This repository is skipped from processing. Request: "
                                + request.toString() );
                }
            }
        }

        return items;
    }

    // ===================================================================================
    // Inner stuff

    @Override
    public void maintainNotFoundCache( String path )
        throws ItemNotFoundException
    {
        // just maintain the cache (ie. expiration), but don't make NFC
        // affect call delegation to members
        try
        {
            super.maintainNotFoundCache( path );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore it
        }
    }

}
