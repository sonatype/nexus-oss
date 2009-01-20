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
package org.sonatype.nexus.proxy.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class PathBasedRequestRepositoryMapper filters repositories to search using supplied list of filter expressions.
 * It is parametrized by java,util.Map, the contents: </p> <tt>
 * regexp1=repo1,repo2...
 * regexp2=repo3,repo4...
 * ...
 * </tt>
 * <p>
 * An example (with grouped Router and two repositories, one for central and one for inhouse in same group):
 * </p>
 * <tt>
 * /com/company/=inhouse
 * /org/apache/=central
 * </tt>
 * 
 * @author cstamas
 */
@Component( role = RequestRepositoryMapper.class )
public class PathBasedRequestRepositoryMapper
    extends LoggingComponent
    implements RequestRepositoryMapper, Initializable
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    /** The compiled flag. */
    private volatile boolean compiled = false;

    private volatile List<RepositoryPathMapping> blockings = new CopyOnWriteArrayList<RepositoryPathMapping>();

    private volatile List<RepositoryPathMapping> inclusions = new CopyOnWriteArrayList<RepositoryPathMapping>();

    private volatile List<RepositoryPathMapping> exclusions = new CopyOnWriteArrayList<RepositoryPathMapping>();

    public void initialize()
    {
        applicationConfiguration.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            this.compiled = false;
        }
    }

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    public List<Repository> getMappedRepositories( RepositoryRegistry registry, RepositoryItemUid uid,
        List<Repository> resolvedRepositories )
        throws NoSuchRepositoryException
    {
        if ( !compiled )
        {
            compile( registry );
        }

        boolean mapped = false;

        List<Repository> reposList = new ArrayList<Repository>( resolvedRepositories );

        // if include found, add it to the list.
        boolean firstAdd = true;

        for ( RepositoryPathMapping mapping : blockings )
        {
            if ( mapping.matches( uid ) )
            {
                reposList.clear();

                getLogger().info(
                    "The request path [" + uid.getPath() + "] is blocked by rule " + mapping.getPattern().toString()
                        + " defined for group='" + mapping.getGroupId() + "'" );

                return reposList;
            }
        }

        // include, if found a match
        for ( RepositoryPathMapping mapping : inclusions )
        {
            if ( mapping.matches( uid ) )
            {
                if ( firstAdd )
                {
                    reposList.clear();

                    firstAdd = false;
                }

                mapped = true;

                // add only those that are in initial resolvedRepositories list and that are non-user managed
                // (preserve ordering)
                for ( Repository repo : resolvedRepositories )
                {
                    if ( mapping.getResourceStores().contains( repo ) || !repo.isUserManaged() )
                    {
                        reposList.add( repo );
                    }
                }
            }
        }

        // then, if exlude found, remove those
        for ( RepositoryPathMapping mapping : exclusions )
        {
            if ( mapping.matches( uid ) )
            {
                mapped = true;

                for ( Repository store : mapping.getResourceStores() )
                {
                    // but only if is user managed
                    if ( store.isUserManaged() )
                    {
                        reposList.remove( store );
                    }
                }
            }
        }
        // at the end, if the list is empty, add all repos
        // if reposList is empty, return original list
        if ( !mapped )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "No mapping exists for request path [" + uid.getPath() + "]" );
            }
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                if ( reposList.size() == 0 )
                {
                    getLogger().debug(
                        "Mapping for path [" + uid.getPath() + "] excluded all storages from servicing the request." );
                }
                else
                {
                    getLogger().debug( "Request path for [" + uid.getPath() + "] is MAPPED!" );
                }
            }
        }
        return reposList;
    }

    /**
     * Compile.
     */
    protected synchronized void compile( RepositoryRegistry registry )
        throws NoSuchRepositoryException
    {
        if ( compiled )
        {
            return;
        }

        blockings.clear();

        inclusions.clear();

        exclusions.clear();

        if ( getApplicationConfiguration().getConfiguration() == null
            || getApplicationConfiguration().getConfiguration().getRepositoryGrouping() == null
            || getApplicationConfiguration().getConfiguration().getRepositoryGrouping().getPathMappings() == null )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "No Routes defined, have nothing to compile." );
            }

            return;
        }

        List<CGroupsSettingPathMappingItem> pathMappings = getApplicationConfiguration()
            .getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( CGroupsSettingPathMappingItem item : pathMappings )
        {
            List<Repository> reposes = null;

            if ( !CGroupsSettingPathMappingItem.BLOCKING_RULE_TYPE.equals( item.getRouteType() ) )
            {
                List<String> repoIds = item.getRepositories();

                reposes = new ArrayList<Repository>( repoIds.size() );

                for ( String repoId : repoIds )
                {
                    if ( "*".equals( repoId ) )
                    {
                        reposes.addAll( registry.getRepositories() );
                    }
                    else
                    {
                        reposes.add( registry.getRepository( repoId ) );
                    }
                }
            }

            RepositoryPathMapping mapping = new RepositoryPathMapping( CGroupsSettingPathMappingItem.ALL_GROUPS
                .equals( item.getGroupId() ), item.getGroupId(), item.getRoutePattern(), reposes );

            if ( CGroupsSettingPathMappingItem.BLOCKING_RULE_TYPE.equals( item.getRouteType() ) )
            {
                blockings.add( mapping );
            }
            else if ( CGroupsSettingPathMappingItem.INCLUSION_RULE_TYPE.equals( item.getRouteType() ) )
            {
                inclusions.add( mapping );
            }
            else if ( CGroupsSettingPathMappingItem.EXCLUSION_RULE_TYPE.equals( item.getRouteType() ) )
            {
                exclusions.add( mapping );
            }
            else
            {
                getLogger().warn( "Unknown route type: " + item.getRouteType() );

                throw new IllegalArgumentException( "Unknown route type: " + item.getRouteType() );
            }
        }

        compiled = true;
    }
}
