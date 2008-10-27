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
package org.sonatype.nexus.configuration.application.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.v1_0_0.Configuration;
import org.sonatype.nexus.configuration.model.v1_0_0.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.v1_0_1.CAuthSource;
import org.sonatype.nexus.configuration.model.v1_0_1.CAuthzSource;
import org.sonatype.nexus.configuration.model.v1_0_1.CGroupsSetting;
import org.sonatype.nexus.configuration.model.v1_0_1.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.v1_0_1.CLocalStorage;
import org.sonatype.nexus.configuration.model.v1_0_1.CProps;
import org.sonatype.nexus.configuration.model.v1_0_1.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.v1_0_1.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.v1_0_1.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.v1_0_1.CRemoteStorage;
import org.sonatype.nexus.configuration.model.v1_0_1.CRepository;
import org.sonatype.nexus.configuration.model.v1_0_1.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.v1_0_1.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.v1_0_1.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.v1_0_1.CRouting;
import org.sonatype.nexus.configuration.model.v1_0_1.CSecurity;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.upgrade.Upgrader;

/**
 * Upgrades configuration model from version 1.0.0 (SWX: 1.0) to 1.0.1.
 * 
 * @author cstamas
 */
@Component( role = Upgrader.class, hint = "1.0.0" )
public class Upgrade100to101
    implements Upgrader
{
    public Object loadConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException
    {
        FileReader fr = null;

        try
        {
            // reading without interpolation to preserve user settings as variables
            fr = new FileReader( file );

            NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

            return reader.read( fr );
        }
        catch ( XmlPullParserException e )
        {
            throw new ConfigurationIsCorruptedException( file.getAbsolutePath(), e );
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }
    }

    public void upgrade( UpgradeMessage message )
    {
        Configuration oldc = (Configuration) message.getConfiguration();

        org.sonatype.nexus.configuration.model.v1_0_1.Configuration newc = new org.sonatype.nexus.configuration.model.v1_0_1.Configuration();

        newc.setVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        newc.setWorkingDirectory( oldc.getWorkingDirectory() );
        newc.setApplicationLogDirectory( oldc.getApplicationLogDirectory() );

        CSecurity security = new CSecurity();
        security.setEnabled( oldc.getSecurity().isEnabled() );
        security.setAnonymousAccessEnabled( oldc.getSecurity().isAnonymousAccessEnabled() );
        if ( oldc.getSecurity().getAuthenticationSource() != null )
        {
            CAuthSource authenticationSource = new CAuthSource();
            authenticationSource.setType( oldc.getSecurity().getAuthenticationSource().getType() );
            if ( oldc.getSecurity().getAuthenticationSource().getProperties() != null )
            {
                authenticationSource.setProperties( copyCProps1_0_0( oldc
                    .getSecurity().getAuthenticationSource().getProperties() ) );
            }
            security.setAuthenticationSource( authenticationSource );
        }
        if ( oldc.getSecurity().getRealms() != null )
        {
            List<CAuthzSource> realms = new ArrayList<CAuthzSource>( oldc.getSecurity().getRealms().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_0.CAuthzSource oldrealm : (List<org.sonatype.nexus.configuration.model.v1_0_0.CAuthzSource>) oldc
                .getSecurity().getRealms() )
            {
                CAuthzSource newrealm = new CAuthzSource();
                newrealm.setId( oldrealm.getId() );
                newrealm.setType( oldrealm.getType() );
                newrealm.setProperties( copyCProps1_0_0( oldrealm.getProperties() ) );
                realms.add( newrealm );
            }
            security.setRealms( realms );
        }
        newc.setSecurity( security );

        if ( oldc.getGlobalConnectionSettings() != null )
        {
            newc.setGlobalConnectionSettings( copyCRemoteConnectionSettings1_0_0( oldc.getGlobalConnectionSettings() ) );
        }
        else
        {
            newc.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        }

        if ( oldc.getGlobalHttpProxySettings() != null )
        {
            newc.setGlobalHttpProxySettings( copyCRemoteHttpProxySettings1_0_0( oldc.getGlobalHttpProxySettings() ) );
        }

        if ( oldc.getRouting() != null )
        {
            CRouting routing = new CRouting();
            routing.setFollowLinks( oldc.getRouting().getGroups().isFollowLinks() );
            routing.setNotFoundCacheTTL( oldc.getRouting().getGroups().getNotFoundCacheTTL() );
            if ( oldc.getRouting().getGroups() != null )
            {
                CGroupsSetting groups = new CGroupsSetting();
                groups.setStopItemSearchOnFirstFoundFile( oldc
                    .getRouting().getGroups().isStopItemSearchOnFirstFoundFile() );
                groups.setMergeMetadata( oldc.getRouting().getGroups().isMergeMetadata() );
                routing.setGroups( groups );
            }
            newc.setRouting( routing );
        }
        else
        {
            newc.setRouting( new CRouting() );
            newc.getRouting().setGroups( new CGroupsSetting() );
        }

        List<CRepository> repositories = new ArrayList<CRepository>( oldc.getRepositories().size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_0.CRepository oldrepos : (List<org.sonatype.nexus.configuration.model.v1_0_0.CRepository>) oldc
            .getRepositories() )
        {
            CRepository newrepos = copyCRepository1_0_0( oldrepos );
            if ( oldrepos.isShouldServeReleases() && !oldrepos.isShouldServeSnapshots() )
            {
                newrepos.setRepositoryPolicy( CRepository.REPOSITORY_TYPE_RELEASE );
            }
            else if ( !oldrepos.isShouldServeReleases() && oldrepos.isShouldServeSnapshots() )
            {
                newrepos.setRepositoryPolicy( CRepository.REPOSITORY_TYPE_SNAPSHOT );
            }
            else if ( !oldrepos.isShouldServeReleases() && !oldrepos.isShouldServeSnapshots() )
            {
                // this is probably some user error, declaring it release and disabling it
                newrepos.setRepositoryPolicy( CRepository.REPOSITORY_TYPE_RELEASE );
                newrepos.setAvailable( false );
            }
            else
            {
                // the repos is mixed, so we are splitting it automagically
                newrepos.setId( newrepos.getId() + "-release" );
                newrepos.setName( newrepos.getName() + " release" );
                newrepos.setRepositoryPolicy( CRepository.REPOSITORY_TYPE_RELEASE );

                CRepository newrepos1 = copyCRepository1_0_0( oldrepos );
                newrepos1.setId( newrepos.getId() + "-snapshot" );
                newrepos1.setName( newrepos.getName() + " snapshot" );
                newrepos1.setRepositoryPolicy( CRepository.REPOSITORY_TYPE_SNAPSHOT );

                // now a little background: if oldrepos is both release and snapshot, we are
                // forcibly splitting it into two. But, what should we do with it's existing
                // storage? The same storage should not be used by release and snapshot repo
                // simultaneously (think about metadata fix on the fly, it will always screw it)

            }

            repositories.add( newrepos );
        }
        newc.setRepositories( repositories );

        if ( oldc.getRepositoryShadows() != null )
        {
            List<CRepositoryShadow> repositoryShadows = new ArrayList<CRepositoryShadow>( oldc
                .getRepositoryShadows().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_0.CRepositoryShadow oldshadow : (List<org.sonatype.nexus.configuration.model.v1_0_0.CRepositoryShadow>) oldc
                .getRepositoryShadows() )
            {
                CRepositoryShadow newshadow = new CRepositoryShadow();
                newshadow.setId( oldshadow.getId() );
                newshadow.setName( oldshadow.getName() );
                newshadow.setAvailable( true );
                newshadow.setShadowOf( oldshadow.getShadowOf() );
                newshadow.setType( oldshadow.getType() );
                newshadow.setSyncAtStartup( oldshadow.isSyncAtStartup() );
                newshadow.setRealmId( oldshadow.getRealmId() );
                repositoryShadows.add( newshadow );
            }
            newc.setRepositoryShadows( repositoryShadows );
        }

        if ( oldc.getRepositoryGroups() != null )
        {
            CRepositoryGrouping repositoryGrouping = new CRepositoryGrouping();
            if ( oldc.getRouting().getGroups().getPathMapping() != null )
            {
                ArrayList<CGroupsSettingPathMappingItem> mappings = new ArrayList<CGroupsSettingPathMappingItem>();
                mappings.addAll( copyPathMappings1_0_0( true, oldc
                    .getRouting().getGroups().getPathMapping().getInclusions() ) );
                mappings.addAll( copyPathMappings1_0_0( false, oldc
                    .getRouting().getGroups().getPathMapping().getExclusions() ) );
                repositoryGrouping.setPathMappings( mappings );
            }
            List<CRepositoryGroup> repositoryGroups = new ArrayList<CRepositoryGroup>( oldc
                .getRepositoryGroups().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_0.CRepositoryGroup oldgroup : (List<org.sonatype.nexus.configuration.model.v1_0_0.CRepositoryGroup>) oldc
                .getRepositoryGroups() )
            {
                CRepositoryGroup newgroup = new CRepositoryGroup();
                newgroup.setGroupId( oldgroup.getGroupId() );
                // both are List<String>
                newgroup.setRepositories( oldgroup.getRepositories() );
                repositoryGroups.add( newgroup );
            }
            repositoryGrouping.setRepositoryGroups( repositoryGroups );
            newc.setRepositoryGrouping( repositoryGrouping );
        }

        message.setModelVersion( org.sonatype.nexus.configuration.model.v1_0_1.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }

    protected List<CProps> copyCProps1_0_0( List<org.sonatype.nexus.configuration.model.v1_0_0.CProps> oldprops )
    {
        List<CProps> properties = new ArrayList<CProps>( oldprops.size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_0.CProps oldprop : oldprops )
        {
            CProps newprop = new CProps();
            newprop.setKey( oldprop.getKey() );
            newprop.setValue( oldprop.getValue() );
            properties.add( newprop );
        }
        return properties;
    }

    protected List<CGroupsSettingPathMappingItem> copyPathMappings1_0_0( boolean isInclusion,
        List<org.sonatype.nexus.configuration.model.v1_0_0.CProps> oldprops )
    {
        List<CGroupsSettingPathMappingItem> mappings = new ArrayList<CGroupsSettingPathMappingItem>( oldprops.size() );

        int pathNum = 1;

        for ( org.sonatype.nexus.configuration.model.v1_0_0.CProps oldprop : oldprops )
        {
            CGroupsSettingPathMappingItem item = new CGroupsSettingPathMappingItem();

            item.setId( "migrated-" + ( isInclusion ? "I" : "E" ) + ( pathNum++ ) );

            item.setRouteType( isInclusion
                ? CGroupsSettingPathMappingItem.INCLUSION_RULE_TYPE
                : CGroupsSettingPathMappingItem.EXCLUSION_RULE_TYPE );

            item.setRoutePattern( oldprop.getKey() );

            String[] repoIds = oldprop.getValue().split( "," );

            item.setRepositories( Arrays.asList( repoIds ) );

            mappings.add( item );
        }
        return mappings;
    }

    protected CRemoteAuthentication copyCRemoteAuthentication1_0_0(
        org.sonatype.nexus.configuration.model.v1_0_0.CRemoteAuthentication oldauth )
    {
        if ( oldauth != null )
        {
            CRemoteAuthentication newauth = new CRemoteAuthentication();
            newauth.setUsername( oldauth.getUsername() );
            newauth.setPassword( oldauth.getPassword() );
            newauth.setNtlmHost( oldauth.getNtlmHost() );
            newauth.setNtlmDomain( oldauth.getNtlmDomain() );
            newauth.setPrivateKey( oldauth.getPrivateKey() );
            newauth.setPassphrase( oldauth.getPassphrase() );
            return newauth;
        }
        else
        {
            return null;
        }
    }

    protected CRemoteConnectionSettings copyCRemoteConnectionSettings1_0_0(
        org.sonatype.nexus.configuration.model.v1_0_0.CRemoteConnectionSettings old )
    {
        CRemoteConnectionSettings cs = new CRemoteConnectionSettings();
        cs.setConnectionTimeout( old.getConnectionTimeout() );
        cs.setRetrievalRetryCount( old.getRetrievalRetryCount() );
        if ( old.getQueryString() != null )
        {
            cs.setQueryString( old.getQueryString() );
        }
        if ( old.getUserAgentString() != null )
        {
            cs.setUserAgentString( old.getUserAgentString() );
        }
        return cs;
    }

    protected CRemoteHttpProxySettings copyCRemoteHttpProxySettings1_0_0(
        org.sonatype.nexus.configuration.model.v1_0_0.CRemoteHttpProxySettings old )
    {
        CRemoteHttpProxySettings cs = new CRemoteHttpProxySettings();
        cs.setProxyHostname( old.getProxyHostname() );
        cs.setProxyPort( old.getProxyPort() );
        cs.setAuthentication( copyCRemoteAuthentication1_0_0( old.getAuthentication() ) );
        return cs;
    }

    protected CRepository copyCRepository1_0_0( org.sonatype.nexus.configuration.model.v1_0_0.CRepository oldrepos )
    {
        CRepository newrepos = new CRepository();
        newrepos.setId( oldrepos.getId() );
        newrepos.setName( oldrepos.getName() );
        newrepos.setAvailable( oldrepos.isAvailable() );
        newrepos.setReadOnly( oldrepos.isReadOnly() );
        newrepos.setBrowseable( oldrepos.isBrowseable() );
        newrepos.setOffline( oldrepos.isOffline() );
        newrepos.setIndexable( oldrepos.isIndexable() );
        newrepos.setNotFoundCacheTTL( oldrepos.getNotFoundCacheTTL() );
        newrepos.setArtifactMaxAge( oldrepos.getItemMaxAge() );
        newrepos.setMetadataMaxAge( oldrepos.getItemMaxAge() );
        newrepos.setRealmId( oldrepos.getRealmId() );
        newrepos.setMaintainProxiedRepositoryMetadata( false );

        if ( oldrepos.getLocalStorage() != null )
        {
            CLocalStorage localStorage = new CLocalStorage();
            localStorage.setUrl( oldrepos.getLocalStorage().getUrl() );
            newrepos.setLocalStorage( localStorage );
        }

        if ( oldrepos.getRemoteStorage() != null )
        {
            CRemoteStorage remoteStorage = new CRemoteStorage();
            remoteStorage.setUrl( oldrepos.getRemoteStorage().getUrl() );
            if ( oldrepos.getRemoteStorage().getAuthentication() != null )
            {
                remoteStorage.setAuthentication( copyCRemoteAuthentication1_0_0( oldrepos
                    .getRemoteStorage().getAuthentication() ) );
            }
            if ( oldrepos.getRemoteStorage().getConnectionSettings() != null )
            {
                remoteStorage.setConnectionSettings( copyCRemoteConnectionSettings1_0_0( oldrepos
                    .getRemoteStorage().getConnectionSettings() ) );
            }
            if ( oldrepos.getRemoteStorage().getHttpProxySettings() != null )
            {
                remoteStorage.setHttpProxySettings( copyCRemoteHttpProxySettings1_0_0( oldrepos
                    .getRemoteStorage().getHttpProxySettings() ) );
            }
            newrepos.setRemoteStorage( remoteStorage );
        }
        return newrepos;
    }

}
