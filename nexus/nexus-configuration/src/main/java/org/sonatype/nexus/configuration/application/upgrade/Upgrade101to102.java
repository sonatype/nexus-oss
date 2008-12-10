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
package org.sonatype.nexus.configuration.application.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.v1_0_1.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.v1_0_1.Configuration;
import org.sonatype.nexus.configuration.model.v1_0_1.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.v1_0_2.CAuthSource;
import org.sonatype.nexus.configuration.model.v1_0_2.CAuthzSource;
import org.sonatype.nexus.configuration.model.v1_0_2.CGroupsSetting;
import org.sonatype.nexus.configuration.model.v1_0_2.CHttpProxySettings;
import org.sonatype.nexus.configuration.model.v1_0_2.CLocalStorage;
import org.sonatype.nexus.configuration.model.v1_0_2.CProps;
import org.sonatype.nexus.configuration.model.v1_0_2.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.v1_0_2.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.v1_0_2.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.v1_0_2.CRemoteStorage;
import org.sonatype.nexus.configuration.model.v1_0_2.CRepository;
import org.sonatype.nexus.configuration.model.v1_0_2.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.v1_0_2.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.v1_0_2.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.v1_0_2.CRestApiSettings;
import org.sonatype.nexus.configuration.model.v1_0_2.CRouting;
import org.sonatype.nexus.configuration.model.v1_0_2.CSecurity;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.upgrade.Upgrader;

/**
 * Upgrades configuration model from version 1.0.1 to 1.0.2.
 * 
 * @author cstamas
 */
@Component( role = Upgrader.class, hint = "1.0.1" )
public class Upgrade101to102
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
        throws ConfigurationIsCorruptedException
    {
        Configuration oldc = (Configuration) message.getConfiguration();
        org.sonatype.nexus.configuration.model.v1_0_2.Configuration newc = new org.sonatype.nexus.configuration.model.v1_0_2.Configuration();

        newc.setVersion( org.sonatype.nexus.configuration.model.v1_0_2.Configuration.MODEL_VERSION );
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
                authenticationSource.setProperties( copyCProps1_0_1( oldc
                    .getSecurity().getAuthenticationSource().getProperties() ) );
            }
            security.setAuthenticationSource( authenticationSource );
        }
        if ( oldc.getSecurity().getRealms() != null )
        {
            List<CAuthzSource> realms = new ArrayList<CAuthzSource>( oldc.getSecurity().getRealms().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_1.CAuthzSource oldrealm : (List<org.sonatype.nexus.configuration.model.v1_0_1.CAuthzSource>) oldc
                .getSecurity().getRealms() )
            {
                CAuthzSource newrealm = new CAuthzSource();
                newrealm.setId( oldrealm.getId() );
                newrealm.setType( oldrealm.getType() );
                newrealm.setProperties( copyCProps1_0_1( oldrealm.getProperties() ) );
                realms.add( newrealm );
            }
            security.setRealms( realms );
        }
        newc.setSecurity( security );

        if ( oldc.getGlobalConnectionSettings() != null )
        {
            newc.setGlobalConnectionSettings( copyCRemoteConnectionSettings1_0_1( oldc.getGlobalConnectionSettings() ) );
        }
        else
        {
            newc.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        }

        if ( oldc.getGlobalHttpProxySettings() != null )
        {
            newc.setGlobalHttpProxySettings( copyCRemoteHttpProxySettings1_0_1( oldc.getGlobalHttpProxySettings() ) );
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
        for ( org.sonatype.nexus.configuration.model.v1_0_1.CRepository oldrepos : (List<org.sonatype.nexus.configuration.model.v1_0_1.CRepository>) oldc
            .getRepositories() )
        {
            CRepository newrepos = copyCRepository1_0_1( oldrepos );
            newrepos.setRepositoryPolicy( oldrepos.getRepositoryPolicy() );
            repositories.add( newrepos );
        }
        newc.setRepositories( repositories );

        if ( oldc.getRepositoryShadows() != null )
        {
            List<CRepositoryShadow> repositoryShadows = new ArrayList<CRepositoryShadow>( oldc
                .getRepositoryShadows().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_1.CRepositoryShadow oldshadow : (List<org.sonatype.nexus.configuration.model.v1_0_1.CRepositoryShadow>) oldc
                .getRepositoryShadows() )
            {
                CRepositoryShadow newshadow = new CRepositoryShadow();
                newshadow.setId( oldshadow.getId() );
                newshadow.setName( oldshadow.getName() );
                newshadow.setLocalStatus( oldshadow.isAvailable()
                    ? org.sonatype.nexus.configuration.model.Configuration.LOCAL_STATUS_IN_SERVICE
                    : org.sonatype.nexus.configuration.model.Configuration.LOCAL_STATUS_OUT_OF_SERVICE );
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
                    org.sonatype.nexus.configuration.model.v1_0_2.CGroupsSettingPathMappingItem newItem = new org.sonatype.nexus.configuration.model.v1_0_2.CGroupsSettingPathMappingItem();

                    newItem.setId( oldItem.getId() );

                    newItem.setRoutePattern( oldItem.getRoutePattern() );

                    newItem.setRouteType( oldItem.getRouteType() );

                    newItem.setRepositories( oldItem.getRepositories() );

                    repositoryGrouping.addPathMapping( newItem );
                }
            }
            List<CRepositoryGroup> repositoryGroups = new ArrayList<CRepositoryGroup>( oldc
                .getRepositoryGrouping().getRepositoryGroups().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_1.CRepositoryGroup oldgroup : (List<org.sonatype.nexus.configuration.model.v1_0_1.CRepositoryGroup>) oldc
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

        message.setModelVersion( org.sonatype.nexus.configuration.model.v1_0_2.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }

    protected List<CProps> copyCProps1_0_1( List<org.sonatype.nexus.configuration.model.v1_0_1.CProps> oldprops )
    {
        List<CProps> properties = new ArrayList<CProps>( oldprops.size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_1.CProps oldprop : oldprops )
        {
            CProps newprop = new CProps();
            newprop.setKey( oldprop.getKey() );
            newprop.setValue( oldprop.getValue() );
            properties.add( newprop );
        }
        return properties;
    }

    protected CRemoteAuthentication copyCRemoteAuthentication1_0_1(
        org.sonatype.nexus.configuration.model.v1_0_1.CRemoteAuthentication oldauth )
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

    protected CRemoteConnectionSettings copyCRemoteConnectionSettings1_0_1(
        org.sonatype.nexus.configuration.model.v1_0_1.CRemoteConnectionSettings old )
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

    protected CRemoteHttpProxySettings copyCRemoteHttpProxySettings1_0_1(
        org.sonatype.nexus.configuration.model.v1_0_1.CRemoteHttpProxySettings old )
    {
        CRemoteHttpProxySettings cs = new CRemoteHttpProxySettings();
        cs.setProxyHostname( old.getProxyHostname() );
        cs.setProxyPort( old.getProxyPort() );
        cs.setAuthentication( copyCRemoteAuthentication1_0_1( old.getAuthentication() ) );
        return cs;
    }

    protected CRepository copyCRepository1_0_1( org.sonatype.nexus.configuration.model.v1_0_1.CRepository oldrepos )
    {
        CRepository newrepos = new CRepository();
        newrepos.setId( oldrepos.getId() );
        newrepos.setName( oldrepos.getName() );
        newrepos.setType( "maven2" );
        newrepos.setLocalStatus( oldrepos.isAvailable()
            ? org.sonatype.nexus.configuration.model.v1_0_2.Configuration.LOCAL_STATUS_IN_SERVICE
            : org.sonatype.nexus.configuration.model.v1_0_2.Configuration.LOCAL_STATUS_OUT_OF_SERVICE );
        newrepos.setProxyMode( oldrepos.isOffline()
            ? CRepository.PROXY_MODE_BLOCKED_MANUAL
            : CRepository.PROXY_MODE_ALLOW );
        newrepos.setAllowWrite( !oldrepos.isReadOnly() );
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
                remoteStorage.setAuthentication( copyCRemoteAuthentication1_0_1( oldrepos
                    .getRemoteStorage().getAuthentication() ) );
            }
            if ( oldrepos.getRemoteStorage().getConnectionSettings() != null )
            {
                remoteStorage.setConnectionSettings( copyCRemoteConnectionSettings1_0_1( oldrepos
                    .getRemoteStorage().getConnectionSettings() ) );
            }
            if ( oldrepos.getRemoteStorage().getHttpProxySettings() != null )
            {
                remoteStorage.setHttpProxySettings( copyCRemoteHttpProxySettings1_0_1( oldrepos
                    .getRemoteStorage().getHttpProxySettings() ) );
            }
            newrepos.setRemoteStorage( remoteStorage );
        }
        return newrepos;
    }

}
