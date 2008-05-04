/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

/**
 * The Class PathBasedRequestRepositoryMapper filters repositories to search using supplied list of filter expressions.
 * It is parametrized by java,util.Map, the contents:
 * </p>
 * <tt>
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
 * @plexus.component
 */
public class PathBasedRequestRepositoryMapper
    extends LoggingComponent
    implements RequestRepositoryMapper, Initializable
{

    /**
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    /** The compiled. */
    private boolean compiled = false;

    /** The blockings prepared */
    private List<Pattern> blockings = new ArrayList<Pattern>();

    /** The group inclusions prepared. */
    private Map<Pattern, List<ResourceStore>> inclusionsPrepared = new HashMap<Pattern, List<ResourceStore>>();

    /** The group exclusions prepared. */
    private Map<Pattern, List<ResourceStore>> exclusionsPrepared = new HashMap<Pattern, List<ResourceStore>>();

    public void initialize()
    {
        applicationConfiguration.addConfigurationChangeListener( this );
    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        this.compiled = false;
    }

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    public List<ResourceStore> getMappedRepositories( RepositoryRegistry registry, ResourceStoreRequest request,
        List<ResourceStore> resolvedRepositories )
        throws NoSuchRepositoryException
    {
        if ( !compiled )
        {
            compile( registry );
        }

        boolean mapped = false;

        List<ResourceStore> reposList = new ArrayList<ResourceStore>( resolvedRepositories );

        // if include found, add it to the list.
        boolean firstAdd = true;

        for ( Pattern pattern : blockings )
        {
            if ( pattern.matcher( request.getRequestPath() ).matches() )
            {
                reposList.clear();

                getLogger().info(
                    "The request path [" + request.getRequestPath() + "] is blocked by rule " + pattern.toString() );

                return reposList;
            }
        }

        for ( Pattern pattern : inclusionsPrepared.keySet() )
        {
            if ( pattern.matcher( request.getRequestPath() ).matches() )
            {
                if ( firstAdd )
                {
                    reposList.clear();

                    firstAdd = false;
                }
                mapped = true;

                // add only those that are in initial resolvedRepositories list
                // (preserve groups)
                for ( ResourceStore repo : resolvedRepositories )
                {
                    if ( inclusionsPrepared.get( pattern ).contains( repo ) )
                    {
                        reposList.add( repo );
                    }
                }
            }
        }

        // then, if exlude found, remove it.
        for ( Pattern pattern : exclusionsPrepared.keySet() )
        {
            if ( pattern.matcher( request.getRequestPath() ).matches() )
            {
                mapped = true;

                reposList.removeAll( exclusionsPrepared.get( pattern ) );
            }
        }
        // at the end, if the list is empty, add all repos
        // if reposList is empty, return original list
        if ( !mapped )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "No mapping exists for request path [" + request.getRequestPath() + "]" );
            }
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                if ( reposList.size() == 0 )
                {
                    getLogger().debug(
                        "Mapping for path [" + request.getRequestPath()
                            + "] excluded all storages from servicing the request." );
                }
                else
                {
                    getLogger().debug( "Request path for [" + request.getRequestPath() + "] is MAPPED!" );
                }
            }
        }
        return reposList;
    }

    /**
     * Compile.
     */
    protected void compile( RepositoryRegistry registry )
        throws NoSuchRepositoryException
    {
        compiled = true;

        inclusionsPrepared = new HashMap<Pattern, List<ResourceStore>>();

        exclusionsPrepared = new HashMap<Pattern, List<ResourceStore>>();

        if ( getApplicationConfiguration().getConfiguration() == null
            || getApplicationConfiguration().getConfiguration().getRepositoryGrouping() == null
            || getApplicationConfiguration().getConfiguration().getRepositoryGrouping().getPathMappings() == null )
        {
            getLogger().info( "No Routes defined." );

            return;
        }

        List<CGroupsSettingPathMappingItem> pathMappings = getApplicationConfiguration()
            .getConfiguration().getRepositoryGrouping().getPathMappings();

        for ( CGroupsSettingPathMappingItem pathMapping : pathMappings )
        {
            String regexp = pathMapping.getRoutePattern();

            List<ResourceStore> repositories = new ArrayList<ResourceStore>();

            List<String> repoIds = pathMapping.getRepositories();

            if ( repoIds != null )
            {
                for ( String repoId : repoIds )
                {
                    if ( "*".equals( repoId ) )
                    {
                        repositories.addAll( registry.getRepositories() );
                    }
                    else
                    {
                        if ( !StringUtils.isEmpty( repoId ) )
                        {
                            repositories.add( registry.getRepository( repoId ) );
                        }
                    }
                }
            }

            if ( CGroupsSettingPathMappingItem.BLOCKING_RULE_TYPE.equals( pathMapping.getRouteType() ) )
            {
                blockings.add( Pattern.compile( regexp ) );
            }
            else
            {
                if ( repositories.size() > 0 )
                {
                    if ( CGroupsSettingPathMappingItem.EXCLUSION_RULE_TYPE.equals( pathMapping.getRouteType() ) )
                    {
                        exclusionsPrepared.put( Pattern.compile( regexp ), repositories );
                    }
                    else if ( CGroupsSettingPathMappingItem.INCLUSION_RULE_TYPE.equals( pathMapping.getRouteType() ) )
                    {
                        inclusionsPrepared.put( Pattern.compile( regexp ), repositories );
                    }
                }
                else
                {
                    getLogger().warn(
                        "Route with ID " + pathMapping.getId() + " and with pattern " + pathMapping.getRoutePattern()
                            + " contains no repository. It is ignored." );
                }
            }
        }
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append( "INCLUSIONS:\n" );

        Map<Pattern, List<ResourceStore>> inc = inclusionsPrepared;

        for ( Pattern regexp : inc.keySet() )
        {
            sb.append( regexp.toString() ).append( " = " ).append( inc.get( regexp ) ).append( "\n" );
        }

        sb.append( "EXCLUSIONS:\n" );

        Map<Pattern, List<ResourceStore>> exc = exclusionsPrepared;

        for ( Pattern regexp : exc.keySet() )
        {
            sb.append( regexp.toString() ).append( " = " ).append( exc.get( regexp ) ).append( "\n" );
        }
        return sb.toString();
    }

}
