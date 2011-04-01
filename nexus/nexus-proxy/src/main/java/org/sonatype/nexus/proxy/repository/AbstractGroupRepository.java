/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.ConfigurationPrepareForSaveEvent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.events.RepositoryEventEvictUnusedItems;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsGroupLocalOnlyAttribute;
import org.sonatype.nexus.proxy.mapping.RequestRepositoryMapper;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.utils.RepositoryUtils;
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
                    true ).getMemberRepositoryIds().size() || !getExternalConfiguration( false ).getMemberRepositoryIds().containsAll(
                    getExternalConfiguration( true ).getMemberRepositoryIds() ) );

        super.onEvent( evt );

        // act automatically on repo removal. Remove it from myself if member.
        if ( evt instanceof RepositoryRegistryEventRemove )
        {
            RepositoryRegistryEventRemove revt = (RepositoryRegistryEventRemove) evt;

            if ( this.getExternalConfiguration( false ).getMemberRepositoryIds().contains( revt.getRepository().getId() ) )
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
    public Collection<String> evictUnusedItems( ResourceStoreRequest request, final long timestamp )
    {
        if ( !getLocalStatus().shouldServiceRequest() )
        {
            return Collections.emptyList();
        }

        getLogger().info(
            "Evicting unused items from group repository \"" + getName() + "\" (id=\"" + getId() + "\") from path "
                + request.getRequestPath() );

        HashSet<String> result = new HashSet<String>();

        // here, we just iterate over members and call evict
        for ( Repository repository : getMemberRepositories() )
        {
            result.addAll( repository.evictUnusedItems( request, timestamp ) );
        }

        getApplicationEventMulticaster().notifyEventListeners( new RepositoryEventEvictUnusedItems( this ) );

        return result;
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

        RepositoryItemUid uid = createUid( request.getRequestPath() );

        final boolean isRequestGroupLocalOnly =
            request.isRequestGroupLocalOnly() || uid.getBooleanAttributeValue( IsGroupLocalOnlyAttribute.class );

        if ( !isRequestGroupLocalOnly )
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
                        getLogger().debug(
                            "Repository ID='"
                                + repo.getId()
                                + "' in group ID='"
                                + this.getId()
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

        boolean hasRequestAuthorizedFlag = request.getRequestContext().containsKey( AccessManager.REQUEST_AUTHORIZED );

        if ( !hasRequestAuthorizedFlag )
        {
            request.getRequestContext().put( AccessManager.REQUEST_AUTHORIZED, Boolean.TRUE );
        }

        try
        {
            RepositoryItemUid uid = createUid( request.getRequestPath() );

            final boolean isRequestGroupLocalOnly =
                request.isRequestGroupLocalOnly() || uid.getBooleanAttributeValue( IsGroupLocalOnlyAttribute.class );

            if ( !isRequestGroupLocalOnly )
            {
                for ( Repository repo : getRequestRepositories( request ) )
                {
                    if ( !request.getProcessedRepositories().contains( repo.getId() ) )
                    {
                        try
                        {
                            StorageItem item = repo.retrieveItem( request );

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
                        catch ( AccessDeniedException e )
                        {
                            // cannot happen, since we add/check for AccessManager.REQUEST_AUTHORIZED flag
                        }
                    }
                    else
                    {
                        getLogger().info(
                            "Repository ID='"
                                + repo.getId()
                                + "' in group ID='"
                                + this.getId()
                                + "' was already processed during this request! This repository is skipped from processing. Request: "
                                + request.toString() );

                    }
                }
            }
        }
        finally
        {
            if ( !hasRequestAuthorizedFlag )
            {
                request.getRequestContext().remove( AccessManager.REQUEST_AUTHORIZED );
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
        // validate THEN modify
        // this will throw NoSuchRepository if needed
        Repository repo = repoRegistry.getRepository( repositoryId );

        // check for cycles
        List<String> memberIds = new ArrayList<String>( getExternalConfiguration( false ).getMemberRepositoryIds() );
        memberIds.add( repo.getId() );
        checkForCyclicReference( getId(), memberIds, getId() );

        // check for compatibility
        if ( !repo.getRepositoryContentClass().isCompatible( getRepositoryContentClass() ) )
        {
            throw new InvalidGroupingException( getRepositoryContentClass(), repo.getRepositoryContentClass() );
        }

        // if we are here, all is well
        getExternalConfiguration( true ).addMemberRepositoryId( repo.getId() );
    }

    private void checkForCyclicReference( final String id, List<String> memberRepositoryIds, String path )
        throws InvalidGroupingException
    {
        if ( memberRepositoryIds.contains( id ) )
        {
            throw new InvalidGroupingException( id, path );
        }

        for ( String memberId : memberRepositoryIds )
        {
            try
            {
                GroupRepository group = repoRegistry.getRepositoryWithFacet( memberId, GroupRepository.class );
                checkForCyclicReference( id, group.getMemberRepositoryIds(), path + '/' + memberId );
            }
            catch ( NoSuchRepositoryException e )
            {
                // not a group repo, just ignore
            }
        }
    }

    public void removeMemberRepositoryId( String repositoryId )
    {
        getExternalConfiguration( true ).removeMemberRepositoryId( repositoryId );
    }

    public List<Repository> getMemberRepositories()
    {
        ArrayList<Repository> result = new ArrayList<Repository>();

        for ( String repoId : getMemberRepositoryIds() )
        {
            try
            {
                Repository repo = repoRegistry.getRepository( repoId );
                result.add( repo );
            }
            catch ( NoSuchRepositoryException e )
            {
                this.getLogger().warn( "Could not find repository: " + repoId, e );
                // XXX throw new StorageException( e );
            }
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
            throw new LocalStorageException( e );
        }
    }

    public List<StorageItem> doRetrieveItems( ResourceStoreRequest request )
        throws StorageException
    {
        ArrayList<StorageItem> items = new ArrayList<StorageItem>();

        RepositoryItemUid uid = createUid( request.getRequestPath() );

        final boolean isRequestGroupLocalOnly =
            request.isRequestGroupLocalOnly() || uid.getBooleanAttributeValue( IsGroupLocalOnlyAttribute.class );

        if ( !isRequestGroupLocalOnly )
        {
            for ( Repository repository : getRequestRepositories( request ) )
            {
                if ( !request.getProcessedRepositories().contains( repository.getId() ) )
                {
                    try
                    {
                        StorageItem item = repository.retrieveItem( false, request );

                        items.add( item );
                    }
                    catch ( ItemNotFoundException e )
                    {
                        // that's okay
                    }
                    catch ( RepositoryNotAvailableException e )
                    {
                        getLogger().debug(
                            "Member repository " + RepositoryUtils.getLoggedNameString( e.getRepository() )
                                + " is not available, request failed." );
                    }
                    catch ( StorageException e )
                    {
                        throw e;
                    }
                    catch ( IllegalOperationException e )
                    {
                        getLogger().warn( "Member repository request failed", e );
                    }
                }
                else
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug(
                            "Repository ID='"
                                + repository.getId()
                                + "' in group ID='"
                                + this.getId()
                                + "' was already processed during this request! This repository is skipped from processing. Request: "
                                + request.toString() );

                    }
                }
            }
        }

        return items;
    }

    // ===================================================================================
    // Inner stuff

    @Override
    public void maintainNotFoundCache( ResourceStoreRequest request )
        throws ItemNotFoundException
    {
        // just maintain the cache (ie. expiration), but don't make NFC
        // affect call delegation to members
        try
        {
            super.maintainNotFoundCache( request );
        }
        catch ( ItemNotFoundException e )
        {
            // ignore it
        }
    }

    @Override
    public void expireCaches( ResourceStoreRequest request )
    {
        List<Repository> members = getMemberRepositories();
        for ( Repository member : members )
        {
            member.expireCaches( request );
        }

        super.expireCaches( request );
    }

    @Override
    public List<Repository> getTransitiveMemberRepositories()
    {
        return getTransitiveMemberRepositories( this );
    }

    protected List<Repository> getTransitiveMemberRepositories( GroupRepository group )
    {
        List<Repository> repos = new ArrayList<Repository>();
        for ( Repository repo : group.getMemberRepositories() )
        {
            if ( repo.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                repos.addAll( getTransitiveMemberRepositories( repo.adaptToFacet( GroupRepository.class ) ) );
            }
            else
            {
                repos.add( repo );
            }
        }
        return repos;
    }

    @Override
    public List<String> getTransitiveMemberRepositoryIds()
    {
        List<String> ids = new ArrayList<String>();
        for ( Repository repo : getTransitiveMemberRepositories() )
        {
            ids.add( repo.getId() );
        }
        return ids;
    }

}
