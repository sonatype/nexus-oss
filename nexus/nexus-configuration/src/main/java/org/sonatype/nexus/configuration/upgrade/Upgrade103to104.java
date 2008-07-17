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
package org.sonatype.nexus.configuration.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.CAuthSource;
import org.sonatype.nexus.configuration.model.CAuthzSource;
import org.sonatype.nexus.configuration.model.CGroupsSetting;
import org.sonatype.nexus.configuration.model.CHttpProxySettings;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CProps;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSecurity;
import org.sonatype.nexus.configuration.model.CTaskConfiguration;
import org.sonatype.nexus.configuration.model.v1_0_3.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.v1_0_3.Configuration;
import org.sonatype.nexus.configuration.model.v1_0_3.io.xpp3.NexusConfigurationXpp3Reader;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Upgrades configuration model from version 1.0.3 to 1.0.4.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.configuration.upgrade.Upgrader" role-hint="1.0.3"
 */
public class Upgrade103to104
    implements Upgrader
{
    private File tasksFile;

    private CTaskConfiguration tasksConfig;

    public Object loadConfiguration( File file )
        throws IOException,
            ConfigurationIsCorruptedException
    {
        FileReader fr = null;

        Configuration conf = null;

        try
        {
            // reading without interpolation to preserve user settings as variables
            fr = new FileReader( file );

            NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

            conf = reader.read( fr );
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

        // a special sideeeffect of upgrading from 1.0.3 to 1.0.4: we are
        // reading the tasks.xml that cropped out in b4 (model 1.0.3), but dissapears in b5 (model 1.0.4)

        tasksFile = new File( file.getParentFile(), "tasks.xml" );

        if ( tasksFile.exists() )
        {
            // a snippet from old TaskConnfigManager
            XStream xstream = new XStream( new DomDriver() );

            tasksConfig = new CTaskConfiguration();

            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream( tasksFile );

                xstream.fromXML( fis, tasksConfig );
            }
            finally
            {
                if ( fis != null )
                {
                    try
                    {
                        fis.close();
                    }
                    catch ( IOException e )
                    {
                    }
                }
            }
        }
        else
        {
            tasksConfig = null;
        }

        // and return the config read above tasks
        return conf;
    }

    public void upgrade( UpgradeMessage message )
        throws ConfigurationIsCorruptedException
    {
        Configuration oldc = (Configuration) message.getConfiguration();
        org.sonatype.nexus.configuration.model.Configuration newc = new org.sonatype.nexus.configuration.model.Configuration();

        newc.setVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        newc.setWorkingDirectory( oldc.getWorkingDirectory() );
        newc.setApplicationLogDirectory( oldc.getApplicationLogDirectory() );

        CSecurity security = new CSecurity();

        if ( oldc.getSecurity().isEnabled() )
        {
            // someone already had security enabled
            security.setEnabled( oldc.getSecurity().isEnabled() );
            security.setAnonymousAccessEnabled( oldc.getSecurity().isAnonymousAccessEnabled() );
            if ( oldc.getSecurity().getAuthenticationSource() != null )
            {
                CAuthSource authenticationSource = new CAuthSource();
                authenticationSource.setType( oldc.getSecurity().getAuthenticationSource().getType() );
                if ( oldc.getSecurity().getAuthenticationSource().getProperties() != null )
                {
                    authenticationSource.setProperties( copyCProps1_0_3( oldc
                        .getSecurity().getAuthenticationSource().getProperties() ) );
                }
                security.setAuthenticationSource( authenticationSource );
            }
            if ( oldc.getSecurity().getRealms() != null )
            {
                List<CAuthzSource> realms = new ArrayList<CAuthzSource>( oldc.getSecurity().getRealms().size() );
                for ( org.sonatype.nexus.configuration.model.v1_0_3.CAuthzSource oldrealm : (List<org.sonatype.nexus.configuration.model.v1_0_3.CAuthzSource>) oldc
                    .getSecurity().getRealms() )
                {
                    CAuthzSource newrealm = new CAuthzSource();
                    newrealm.setId( oldrealm.getId() );
                    newrealm.setType( oldrealm.getType() );
                    newrealm.setProperties( copyCProps1_0_3( oldrealm.getProperties() ) );
                    realms.add( newrealm );
                }
                security.setRealms( realms );
            }
        }
        else
        {
            // defaulting to "simple" model
            security.setEnabled( true );
            security.setAnonymousAccessEnabled( true );
            security.setAuthenticationSource( new CAuthSource() );
            security.getAuthenticationSource().setType( "simple" );
        }

        newc.setSecurity( security );

        if ( oldc.getGlobalConnectionSettings() != null )
        {
            newc.setGlobalConnectionSettings( copyCRemoteConnectionSettings1_0_3( oldc.getGlobalConnectionSettings() ) );
        }
        else
        {
            newc.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        }

        if ( oldc.getGlobalHttpProxySettings() != null )
        {
            newc.setGlobalHttpProxySettings( copyCRemoteHttpProxySettings1_0_3( oldc.getGlobalHttpProxySettings() ) );
        }

        if ( oldc.getRouting() != null )
        {
            CRouting routing = new CRouting();
            routing.setFollowLinks( oldc.getRouting().isFollowLinks() );
            routing.setNotFoundCacheTTL( oldc.getRouting().getNotFoundCacheTTL() );
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

        newc.setRestApi( new CRestApiSettings() );

        newc.setHttpProxy( new CHttpProxySettings() );

        List<CRepository> repositories = new ArrayList<CRepository>( oldc.getRepositories().size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_3.CRepository oldrepos : (List<org.sonatype.nexus.configuration.model.v1_0_3.CRepository>) oldc
            .getRepositories() )
        {
            CRepository newrepos = copyCRepository1_0_3( oldrepos );
            newrepos.setRepositoryPolicy( oldrepos.getRepositoryPolicy() );
            repositories.add( newrepos );
        }
        newc.setRepositories( repositories );

        if ( oldc.getRepositoryShadows() != null )
        {
            List<CRepositoryShadow> repositoryShadows = new ArrayList<CRepositoryShadow>( oldc
                .getRepositoryShadows().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_3.CRepositoryShadow oldshadow : (List<org.sonatype.nexus.configuration.model.v1_0_3.CRepositoryShadow>) oldc
                .getRepositoryShadows() )
            {
                CRepositoryShadow newshadow = new CRepositoryShadow();
                newshadow.setId( oldshadow.getId() );
                newshadow.setName( oldshadow.getName() );
                newshadow.setLocalStatus( oldshadow.getLocalStatus() );
                newshadow.setShadowOf( oldshadow.getShadowOf() );
                newshadow.setType( oldshadow.getType() );
                newshadow.setSyncAtStartup( oldshadow.isSyncAtStartup() );
                newshadow.setRealmId( oldshadow.getRealmId() );
                repositoryShadows.add( newshadow );
            }
            newc.setRepositoryShadows( repositoryShadows );
        }

        if ( oldc.getRepositoryGrouping() != null )
        {
            CRepositoryGrouping repositoryGrouping = new CRepositoryGrouping();
            if ( oldc.getRepositoryGrouping().getPathMappings() != null )
            {
                for ( CGroupsSettingPathMappingItem oldItem : (List<CGroupsSettingPathMappingItem>) oldc
                    .getRepositoryGrouping().getPathMappings() )
                {
                    org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem newItem = new org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem();

                    newItem.setId( oldItem.getId() );

                    newItem.setRoutePattern( oldItem.getRoutePattern() );

                    newItem.setRouteType( oldItem.getRouteType() );

                    newItem.setRepositories( oldItem.getRepositories() );

                    repositoryGrouping.addPathMapping( newItem );
                }
            }
            List<CRepositoryGroup> repositoryGroups = new ArrayList<CRepositoryGroup>( oldc
                .getRepositoryGrouping().getRepositoryGroups().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_3.CRepositoryGroup oldgroup : (List<org.sonatype.nexus.configuration.model.v1_0_3.CRepositoryGroup>) oldc
                .getRepositoryGrouping().getRepositoryGroups() )
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

        if ( tasksConfig != null && tasksConfig.getTasks().size() > 0 )
        {
            // we were using XStream, and it was tasks.xml was persisted.
            // the model structure wrt tasks did not change between 1.0.3 and 1.0.4,
            // so it is possible to simply "push" all xstream loaded stuff into new configuration.
            newc.getTasks().addAll( tasksConfig.getTasks() );
        }

        message.setModelVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );

        // extra step, remove old tasks.xml
        if ( tasksFile.exists() )
        {
            try
            {
                FileUtils.rename( tasksFile, new File( tasksFile.getParentFile(), "tasks.xml.old" ) );
            }
            catch ( IOException e )
            {
                // silent?
                // after succesful upgrade, this file will not be used anymore
                // it will remain but will be ugly (ie. a file should be deleted in safe manner)
            }
        }
    }

    protected List<CProps> copyCProps1_0_3( List<org.sonatype.nexus.configuration.model.v1_0_3.CProps> oldprops )
    {
        List<CProps> properties = new ArrayList<CProps>( oldprops.size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_3.CProps oldprop : oldprops )
        {
            CProps newprop = new CProps();
            newprop.setKey( oldprop.getKey() );
            newprop.setValue( oldprop.getValue() );
            properties.add( newprop );
        }
        return properties;
    }

    protected CRemoteAuthentication copyCRemoteAuthentication1_0_3(
        org.sonatype.nexus.configuration.model.v1_0_3.CRemoteAuthentication oldauth )
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

    protected CRemoteConnectionSettings copyCRemoteConnectionSettings1_0_3(
        org.sonatype.nexus.configuration.model.v1_0_3.CRemoteConnectionSettings old )
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

    protected CRemoteHttpProxySettings copyCRemoteHttpProxySettings1_0_3(
        org.sonatype.nexus.configuration.model.v1_0_3.CRemoteHttpProxySettings old )
    {
        CRemoteHttpProxySettings cs = new CRemoteHttpProxySettings();
        cs.setProxyHostname( old.getProxyHostname() );
        cs.setProxyPort( old.getProxyPort() );
        cs.setAuthentication( copyCRemoteAuthentication1_0_3( old.getAuthentication() ) );
        return cs;
    }

    protected CRepository copyCRepository1_0_3( org.sonatype.nexus.configuration.model.v1_0_3.CRepository oldrepos )
    {
        CRepository newrepos = new CRepository();
        newrepos.setId( oldrepos.getId() );
        newrepos.setName( oldrepos.getName() );
        newrepos.setType( oldrepos.getType() );
        newrepos.setLocalStatus( oldrepos.getLocalStatus() );
        newrepos.setProxyMode( oldrepos.getProxyMode() );
        newrepos.setAllowWrite( oldrepos.isAllowWrite() );
        newrepos.setBrowseable( oldrepos.isBrowseable() );
        newrepos.setIndexable( oldrepos.isIndexable() );
        newrepos.setNotFoundCacheTTL( oldrepos.getNotFoundCacheTTL() );
        newrepos.setArtifactMaxAge( oldrepos.getArtifactMaxAge() );
        newrepos.setMetadataMaxAge( oldrepos.getMetadataMaxAge() );
        newrepos.setRealmId( oldrepos.getRealmId() );
        newrepos.setMaintainProxiedRepositoryMetadata( oldrepos.isMaintainProxiedRepositoryMetadata() );

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
                remoteStorage.setAuthentication( copyCRemoteAuthentication1_0_3( oldrepos
                    .getRemoteStorage().getAuthentication() ) );
            }
            if ( oldrepos.getRemoteStorage().getConnectionSettings() != null )
            {
                remoteStorage.setConnectionSettings( copyCRemoteConnectionSettings1_0_3( oldrepos
                    .getRemoteStorage().getConnectionSettings() ) );
            }
            if ( oldrepos.getRemoteStorage().getHttpProxySettings() != null )
            {
                remoteStorage.setHttpProxySettings( copyCRemoteHttpProxySettings1_0_3( oldrepos
                    .getRemoteStorage().getHttpProxySettings() ) );
            }
            newrepos.setRemoteStorage( remoteStorage );
        }
        return newrepos;
    }

}
