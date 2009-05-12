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
package org.sonatype.nexus.proxy.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryStatusCheckerThread;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

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
    extends AbstractLogEnabled
    implements RepositoryRegistry
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    /** The repo register, [Repository.getId, Repository] */
    private Map<String, Repository> repositories = new HashMap<String, Repository>();

    /** The repo status checkrs */
    private Map<String, RepositoryStatusCheckerThread> repositoryStatusCheckers =
        new HashMap<String, RepositoryStatusCheckerThread>();

    public void addRepository( Repository repository )
    {
        insertRepository( repository );

        getLogger().info(
                          "Added repository ID='" + repository.getId() + "' (contentClass='"
                              + repository.getRepositoryContentClass().getId() + "', mainFacet='"
                              + repository.getRepositoryKind().getMainFacet().getName() + "')" );
    }

    public void removeRepository( String repoId )
        throws NoSuchRepositoryException
    {
        Repository repository = getRepository( repoId );

        deleteRepository( repository, false );

        getLogger().info(
                          "Removed repository ID='" + repository.getId() + "' (contentClass='"
                              + repository.getRepositoryContentClass().getId() + "', mainFacet='"
                              + repository.getRepositoryKind().getMainFacet().getName() + "')" );
    }

    public void removeRepositorySilently( String repoId )
        throws NoSuchRepositoryException
    {
        Repository repository = getRepository( repoId );

        deleteRepository( repository, true );
    }

    public List<Repository> getRepositories()
    {
        return Collections.unmodifiableList( new ArrayList<Repository>( repositories.values() ) );
    }

    public <T> List<T> getRepositoriesWithFacet( Class<T> f )
    {
        ArrayList<T> result = new ArrayList<T>();

        for ( Repository repository : repositories.values() )
        {
            if ( repository.getRepositoryKind().isFacetAvailable( f ) )
            {
                result.add( repository.adaptToFacet( f ) );
            }
        }

        return Collections.unmodifiableList( result );
    }

    public Repository getRepository( String repoId )
        throws NoSuchRepositoryException
    {
        if ( repositories.containsKey( repoId ) )
        {
            return repositories.get( repoId );
        }
        else
        {
            throw new NoSuchRepositoryException( repoId );
        }
    }

    public <T> T getRepositoryWithFacet( String repoId, Class<T> f )
        throws NoSuchRepositoryException
    {
        Repository r = getRepository( repoId );

        if ( r.getRepositoryKind().isFacetAvailable( f ) )
        {
            return r.adaptToFacet( f );
        }
        else
        {
            throw new NoSuchRepositoryException( repoId );
        }
    }

    public boolean repositoryIdExists( String repositoryId )
    {
        return repositories.containsKey( repositoryId );
    }

    public List<String> getGroupsOfRepository( String repositoryId )
    {
        ArrayList<String> result = new ArrayList<String>();

        for ( Repository repository : getRepositories() )
        {
            if ( !repository.getId().equals( repositoryId )
                && repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                GroupRepository group = repository.adaptToFacet( GroupRepository.class );

                for ( Repository member : group.getMemberRepositories() )
                {
                    if ( repositoryId.equals( member.getId() ) )
                    {
                        result.add( group.getId() );

                        break;
                    }
                }
            }
        }

        return result;
    }

    //
    // priv
    //

    private void insertRepository( Repository repository )
    {
        repositories.put( repository.getId(), repository );

        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            if ( repositoryStatusCheckers.containsKey( repository.getId() ) )
            {
                RepositoryStatusCheckerThread thread = repositoryStatusCheckers.remove( repository.getId() );

                thread.interrupt();
            }

            RepositoryStatusCheckerThread thread = new RepositoryStatusCheckerThread( (ProxyRepository) repository );

            repositoryStatusCheckers.put( repository.getId(), thread );

            thread.setDaemon( true );

            thread.start();
        }

        applicationEventMulticaster.notifyEventListeners( new RepositoryRegistryEventAdd( this, repository ) );
    }

    private void deleteRepository( Repository repository, boolean silently )
    {
        if ( !silently )
        {
            applicationEventMulticaster.notifyEventListeners( new RepositoryRegistryEventRemove( this, repository ) );
        }

        repositories.remove( repository.getId() );

        if ( repositoryStatusCheckers.containsKey( repository.getId() ) )
        {
            RepositoryStatusCheckerThread thread = repositoryStatusCheckers.remove( repository.getId() );

            thread.interrupt();
        }
    }

}
