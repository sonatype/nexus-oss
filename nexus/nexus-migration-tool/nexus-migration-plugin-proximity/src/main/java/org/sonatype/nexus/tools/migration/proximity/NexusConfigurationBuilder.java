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
package org.sonatype.nexus.tools.migration.proximity;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.tools.migration.MigrationRequest;
import org.sonatype.nexus.tools.migration.MigrationResult;
import org.sonatype.nexus.util.ApplicationInterpolatorProvider;

/**
 * Builds nexus config from ctx gathered from px1.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.tools.migration.proximity.NexusConfigurationBuilder"
 */
public class NexusConfigurationBuilder
    extends AbstractLogEnabled
{
    public static final String ROLE = NexusConfigurationBuilder.class.getName();

    /**
     * @plexus.requirement
     */
    private ApplicationInterpolatorProvider applicationInterpolatorProvider;

    public void buildConfiguration( ProximitySpringContext ctx, MigrationRequest req, MigrationResult res )
    {

        Configuration configuration = res.getConfiguration();

        if ( req.isMigrateRepositories() )
        {
            for ( String repoId : ctx.getRepositoryBeans().keySet() )
            {
                getLogger().debug( "Migrating repository ID=" + repoId );

                Xpp3Dom repoBean = ctx.getRepositoryBeans().get( repoId );
                CRepository repository = new CRepository();
                repository.setId( getPropertyValue( repoBean, "id" ) );

                if ( !ctx.getRepositoryGroups().containsKey( getPropertyValue( repoBean, "groupId" ) ) )
                {
                    ctx.getRepositoryGroups().put( getPropertyValue( repoBean, "groupId" ), new ArrayList<String>() );
                }
                ctx.getRepositoryGroups().get( getPropertyValue( repoBean, "groupId" ) ).add( repository.getId() );

                repository.setName( "Migrated Proximity repository " + repository.getId() );
                repository.setLocalStatus( Boolean.valueOf( getPropertyValue( repoBean, "available" ) )
                    ? Configuration.LOCAL_STATUS_IN_SERVICE
                    : Configuration.LOCAL_STATUS_OUT_OF_SERVICE );
                repository.setBrowseable( Boolean.valueOf( getPropertyValue( repoBean, "listable" ) ) );
                repository.setProxyMode( Boolean.valueOf( getPropertyValue( repoBean, "offline" ) )
                    ? CRepository.PROXY_MODE_BLOCKED_MANUAL
                    : CRepository.PROXY_MODE_ALLOW );
                repository.setIndexable( Boolean.valueOf( getPropertyValue( repoBean, "indexable" ) ) );

                Xpp3Dom repoLogicBean = ctx.getRepositoryLogicBeans().get(
                    getPropertyValue( repoBean, "repositoryLogic", "ref" ) );

                if ( Boolean.valueOf( getPropertyValue( repoLogicBean, "shouldServeReleases" ) )
                    && !Boolean.valueOf( getPropertyValue( repoLogicBean, "shouldServeSnapshots" ) ) )
                {
                    repository.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_RELEASE );
                    repository.setArtifactMaxAge( getTTLValue( repoLogicBean, "pomExpirationPeriodInSeconds" ) );
                }
                else if ( !Boolean.valueOf( getPropertyValue( repoLogicBean, "shouldServeReleases" ) )
                    && Boolean.valueOf( getPropertyValue( repoLogicBean, "shouldServeSnapshots" ) ) )
                {
                    repository.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_SNAPSHOT );
                    repository.setArtifactMaxAge( getTTLValue( repoLogicBean, "snapshotExpirationPeriodInSeconds" ) );
                }
                else
                {
                    res
                        .getExceptions()
                        .add(
                            new IllegalArgumentException(
                                "Repository with ID="
                                    + repoId
                                    + " is misconfigured. A repository cannot serve snapshot and release artifacts simultaneously!" ) );
                    repository.setRepositoryPolicy( CRepository.REPOSITORY_POLICY_SNAPSHOT );
                    repository.setArtifactMaxAge( getTTLValue( repoLogicBean, "snapshotExpirationPeriodInSeconds" ) );
                }
                repository.setMetadataMaxAge( getTTLValue( repoLogicBean, "metadataExpirationPeriodInSeconds" ) );

                // Proximity have no standard security solution
                repository.setRealmId( null );
                // Proximity did not have this feature
                repository.setMaintainProxiedRepositoryMetadata( false );

                repository.setLocalStorage( new CLocalStorage() );
                Xpp3Dom ls = ctx.getLocalStorageBeans().get( getPropertyValue( repoBean, "localStorage", "ref" ) );
                String lsPath = getPropertyValue( ls, "storageDirFile" );
                lsPath = applicationInterpolatorProvider.interpolate( lsPath, "" );
                try
                {
                    repository.getLocalStorage().setUrl( new File( lsPath ).toURI().toURL().toString() );
                }
                catch ( MalformedURLException e )
                {
                    res.getExceptions().add( e );
                }

                if ( getPropertyValue( repoBean, "remoteStorage", "ref" ) != null )
                {
                    repository.setRemoteStorage( new CRemoteStorage() );
                    Xpp3Dom rs = ctx.getRemoteStorageBeans().get( getPropertyValue( repoBean, "remoteStorage", "ref" ) );
                    repository.getRemoteStorage().setUrl( getPropertyValue( rs, "remoteUrl" ) );
                    // TODO: HTTP proxy settings. Also, see if "global" or "per repo" needs to be set
                    // Proximity did not have "global" HTTP proxy settings
                }

                configuration.addRepository( repository );
            }
        }

        if ( req.isMigrateRepositoryGroupMappings() || req.isMigrateRepositoryGroups() )
        {
            CRepositoryGrouping repositoryGrouping = new CRepositoryGrouping();
            if ( ctx.getGroupRequestMapperBean() != null )
            {
                // TODO: path mapping
                // Px1 had per group mapping, Nexus works differently
                // so, we have to get all group inclusions, merge them. Same for exclusions.
                int pathNum = 1;
                Xpp3Dom[] incAndExc = ctx.getGroupRequestMapperBean().getChildren( "property" );
                for ( int i = 0; i < incAndExc.length; i++ )
                {
                    String name = incAndExc[i].getAttribute( "name" );
                    if ( name != null && ( "inclusions".equals( name ) || "exclusions".equals( name ) ) )
                    {
                        HashMap<String, List<String>> extracted = new HashMap<String, List<String>>();

                        // if we have a map with entries in XML
                        if ( incAndExc[i].getChild( "map" ) != null
                            && incAndExc[i].getChild( "map" ).getChildCount() > 0 )
                        {
                            // the entry key (groupId) is neglected, taking only values
                            Xpp3Dom[] entries = incAndExc[i].getChild( "map" ).getChildren( "entry" );
                            for ( int j = 0; j < entries.length; j++ )
                            {
                                Xpp3Dom[] values = entries[j].getChild( "list" ).getChildren( "value" );
                                for ( int k = 0; k < values.length; k++ )
                                {
                                    String[] mapping = values[k].getValue().split( "=" );
                                    String path = mapping[0];
                                    if ( extracted.containsKey( path ) )
                                    {
                                        extracted.get( path ).addAll( Arrays.asList( mapping[1].split( "," ) ) );
                                    }
                                    else
                                    {
                                        extracted.put( path, Arrays.asList( mapping[1].split( "," ) ) );
                                    }
                                }
                            }

                        }

                        for ( String key : extracted.keySet() )
                        {
                            CGroupsSettingPathMappingItem item = new CGroupsSettingPathMappingItem();

                            item.setId( "migrated-" + ( pathNum++ ) );
                            item.setRoutePattern( ".*/" + key );
                            item.setRouteType( "inclusions".equals( name )
                                ? CGroupsSettingPathMappingItem.INCLUSION_RULE_TYPE
                                : CGroupsSettingPathMappingItem.EXCLUSION_RULE_TYPE );
                            item.setRepositories( extracted.get( key ) );

                            repositoryGrouping.addPathMapping( item );
                        }

                    }
                }

            }
            for ( String groupId : ctx.getRepositoryGroups().keySet() )
            {
                CRepositoryGroup group = new CRepositoryGroup();
                group.setGroupId( groupId );
                group.setRepositories( ctx.getRepositoryGroups().get( groupId ) );
                repositoryGrouping.addRepositoryGroup( group );
            }

            configuration.setRepositoryGrouping( repositoryGrouping );
        }
    }

    protected String getPropertyValue( Xpp3Dom springBean, String propertyName )
    {
        return getPropertyValue( springBean, propertyName, "value" );
    }

    protected String getPropertyValue( Xpp3Dom springBean, String propertyName, String propertyValueAttribute )
    {
        Xpp3Dom[] properties = springBean.getChildren( "property" );
        for ( int i = 0; i < properties.length; i++ )
        {
            if ( propertyName.equals( properties[i].getAttribute( "name" ) ) )
            {
                if ( properties[i].getAttribute( propertyValueAttribute ) != null )
                {
                    return properties[i].getAttribute( propertyValueAttribute );
                }
                else
                {
                    return properties[i].getValue();
                }
            }
        }
        return null;
    }

    protected int getTTLValue( Xpp3Dom springBean, String propertyName )
    {
        int result = Integer.valueOf( getPropertyValue( springBean, propertyName ) );
        if ( result == -1 )
        {
            return -1;
        }
        else
        {
            return result / 60;
        }
    }

}
