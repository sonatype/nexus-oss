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
package org.sonatype.nexus.proxy.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventPostRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryStatusCheckerThread;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.EventListener;

/**
 * Repository registry. It holds handles to registered repositories and sorts them properly. This class is used to get a
 * grip on repositories.
 * <p>
 * Getting reposes from here and changing repo attributes like group, id and rank have no effect on repo registry! For
 * that kind of change, you have to: 1) get repository, 2) remove repository from registry, 3) change repo attributes
 * and 4) add repository.
 * <p>
 * ProximityEvents: this component just "concentrates" the repositiry events of all known repositories by it. It can be
 * used as single point to access all repository events. TODO this is not a good place to keep group repository
 * management code
 * 
 * @author cstamas
 */
@Component( role = RepositoryRegistry.class )
public class DefaultRepositoryRegistry
    implements RepositoryRegistry, Disposable
{
    @Requirement
    private Logger logger;

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    /** The repository register */
    private final Map<String, Repository> repositories = new HashMap<String, Repository>();

    protected Logger getLogger()
    {
        return logger;
    }

    public synchronized void addRepository( final Repository repository )
    {
        final RepositoryTypeDescriptor rtd =
            repositoryTypeRegistry.getRepositoryTypeDescriptor( repository.getProviderRole(),
                repository.getProviderHint() );

        insertRepository( rtd, repository );

        getLogger().info(
            "Added repository ID='" + repository.getId() + "' (contentClass='"
                + repository.getRepositoryContentClass().getId() + "', mainFacet='"
                + repository.getRepositoryKind().getMainFacet().getName() + "')" );
    }

    public synchronized void removeRepository( final String repoId )
        throws NoSuchRepositoryException
    {
        doRemoveRepository( repoId, false );
    }

    public synchronized void removeRepositorySilently( final String repoId )
        throws NoSuchRepositoryException
    {
        doRemoveRepository( repoId, true );
    }

    public List<Repository> getRepositories()
    {
        return Collections.unmodifiableList( new ArrayList<Repository>( getRepositoriesMap().values() ) );
    }

    public <T> List<T> getRepositoriesWithFacet( final Class<T> f )
    {
        final List<Repository> repositories = getRepositories();

        final ArrayList<T> result = new ArrayList<T>();

        for ( Repository repository : repositories )
        {
            if ( repository.getRepositoryKind().isFacetAvailable( f ) )
            {
                result.add( repository.adaptToFacet( f ) );
            }
        }

        return Collections.unmodifiableList( result );
    }

    public Repository getRepository( final String repoId )
        throws NoSuchRepositoryException
    {
        final Map<String, Repository> repositories = getRepositoriesMap();

        if ( repositories.containsKey( repoId ) )
        {
            return repositories.get( repoId );
        }
        else
        {
            throw new NoSuchRepositoryException( repoId );
        }
    }

    public <T> T getRepositoryWithFacet( final String repoId, final Class<T> f )
        throws NoSuchRepositoryException
    {
        final Repository r = getRepository( repoId );

        if ( r.getRepositoryKind().isFacetAvailable( f ) )
        {
            return r.adaptToFacet( f );
        }
        else
        {
            throw new NoSuchRepositoryException( repoId );
        }
    }

    public boolean repositoryIdExists( final String repositoryId )
    {
        return getRepositoriesMap().containsKey( repositoryId );
    }

    public List<String> getGroupsOfRepository( final String repositoryId )
    {
        final ArrayList<String> result = new ArrayList<String>();

        try
        {
            final Repository repository = getRepository( repositoryId );

            for ( GroupRepository group : getGroupsOfRepository( repository ) )
            {
                result.add( group.getId() );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            // ignore, just return empty collection
        }

        return result;
    }

    public List<GroupRepository> getGroupsOfRepository( final Repository repository )
    {
        final ArrayList<GroupRepository> result = new ArrayList<GroupRepository>();

        for ( Repository repo : getRepositories() )
        {
            if ( !repo.getId().equals( repository.getId() )
                && repo.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                final GroupRepository group = repo.adaptToFacet( GroupRepository.class );

                members: for ( Repository member : group.getMemberRepositories() )
                {
                    if ( repository.getId().equals( member.getId() ) )
                    {
                        result.add( group );
                        break members;
                    }
                }
            }
        }

        return result;
    }

    // Disposable plexus iface

    public void dispose()
    {
        // kill the checker daemon threads
        for ( Repository repository : getRepositoriesMap().values() )
        {
            killMonitorThread( repository.adaptToFacet( ProxyRepository.class ) );
        }
    }

    //
    // priv
    //

    /**
     * Returns a copy of map with repositories. Is synchronized method, to allow consistent-read access. Methods
     * modifying this map are all also synchronized (see API Interface and above), while all the "reading" methods from
     * public API will boil down to this single method.
     */
    protected synchronized Map<String, Repository> getRepositoriesMap()
    {
        return Collections.unmodifiableMap( new HashMap<String, Repository>( repositories ) );
    }

    protected void doRemoveRepository( final String repoId, final boolean silently )
        throws NoSuchRepositoryException
    {
        Repository repository = getRepository( repoId );

        RepositoryTypeDescriptor rtd =
            repositoryTypeRegistry.getRepositoryTypeDescriptor( repository.getProviderRole(),
                repository.getProviderHint() );

        deleteRepository( rtd, repository, silently );

        if ( !silently )
        {
            getLogger().info(
                "Removed repository ID='" + repository.getId() + "' (contentClass='"
                    + repository.getRepositoryContentClass().getId() + "', mainFacet='"
                    + repository.getRepositoryKind().getMainFacet().getName() + "')" );
        }
    }

    private void insertRepository( final RepositoryTypeDescriptor rtd, final Repository repository )
    {
        repositories.put( repository.getId(), repository );

        rtd.instanceRegistered( this );

        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            ProxyRepository proxy = repository.adaptToFacet( ProxyRepository.class );

            killMonitorThread( proxy );

            RepositoryStatusCheckerThread thread =
                new RepositoryStatusCheckerThread( LoggerFactory.getLogger( getClass().getName() + "-"
                    + repository.getId() ), (ProxyRepository) repository );

            proxy.setRepositoryStatusCheckerThread( thread );

            thread.setRunning( true );

            thread.setDaemon( true );

            thread.start();
        }

        applicationEventMulticaster.notifyEventListeners( new RepositoryRegistryEventAdd( this, repository ) );
    }

    private void deleteRepository( final RepositoryTypeDescriptor rtd, final Repository repository,
                                   final boolean silently )
    {
        if ( !silently )
        {
            applicationEventMulticaster.notifyEventListeners( new RepositoryRegistryEventRemove( this, repository ) );
        }

        // dump the event listeners, as once deleted doesn't care about config changes any longer
        if ( repository instanceof EventListener )
        {
            applicationEventMulticaster.removeEventListener( (EventListener) repository );
        }

        rtd.instanceUnregistered( this );

        repositories.remove( repository.getId() );

        killMonitorThread( repository.adaptToFacet( ProxyRepository.class ) );

        if ( !silently )
        {
            applicationEventMulticaster.notifyEventListeners( new RepositoryRegistryEventPostRemove( this, repository ) );
        }
    }

    // ==

    protected void killMonitorThread( final ProxyRepository proxy )
    {
        if ( null == proxy )
        {
            return;
        }

        if ( null != proxy.getRepositoryStatusCheckerThread() )
        {
            RepositoryStatusCheckerThread thread =
                (RepositoryStatusCheckerThread) proxy.getRepositoryStatusCheckerThread();

            thread.setRunning( false );

            // and now interrupt it to die
            thread.interrupt();
        }
    }
}
