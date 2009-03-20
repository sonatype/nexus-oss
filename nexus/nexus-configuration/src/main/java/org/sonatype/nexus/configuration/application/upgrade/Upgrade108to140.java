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
package org.sonatype.nexus.configuration.application.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.CHttpProxySettings;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.configuration.model.CPathMappingItem;
import org.sonatype.nexus.configuration.model.CProps;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CScheduleConfig;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.CSecurity;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.model.v1_0_8.Configuration;
import org.sonatype.nexus.configuration.model.v1_0_8.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.upgrade.Upgrader;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

/**
 * Upgrades configuration model from version 1.0.8 to 1.4.0.
 * 
 * @author cstamas
 */
@Component( role = Upgrader.class, hint = "1.0.8" )
public class Upgrade108to140
    extends AbstractLogEnabled
    implements Upgrader
{

    private static final String EXTERNAL_CONFIG = "externalConfiguration";

    private static final String GROUP_MEMBERS_NODE = "memberRepositories";

    private static final String GROUP_CHILD_NODE = "memberRepository";
    
    private final Map<String, String> localStatus = new HashMap<String, String>();
    private final Map<String, String> checksumPolicy = new HashMap<String, String>();
    private final Map<String, String> proxyMode = new HashMap<String, String>();
    
    public Upgrade108to140()
    {
        // migrate to ENUMS
        this.localStatus.put( "inService", "IN_SERVICE" );
        this.localStatus.put( "outOfService", "OUT_OF_SERVICE" );

        this.checksumPolicy.put( "ignore", "IGNORE" );
        this.checksumPolicy.put( "warn", "WARN" );
        this.checksumPolicy.put( "strictIfExists", "STRICT_IF_EXISTS" );
        this.checksumPolicy.put( "strict", "STRICT" );

        this.proxyMode.put( "allow", "ALLOW" );
        this.proxyMode.put( "blockedAuto", "BLOCKED_AUTO" );
        this.proxyMode.put( "blockedManual", "BLOKED_MANUAL" );
    }

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

        return conf;
    }

    public void upgrade( UpgradeMessage message )
        throws ConfigurationIsCorruptedException
    {
        Configuration oldc = (Configuration) message.getConfiguration();
        org.sonatype.nexus.configuration.model.Configuration newc = new org.sonatype.nexus.configuration.model.Configuration();

        newc.setVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        // SMTP info is the same
        newc.setSmtpConfiguration( copyCSmtpConfiguration1_0_8( oldc.getSmtpConfiguration() ) );
        // Security info is the same
        newc.setSecurity( copyCSecurity1_0_8( oldc.getSecurity() ) );
        // Global Connection is the same
        newc.setGlobalConnectionSettings( copyCRemoteConnectionSettings1_0_8( oldc.getGlobalConnectionSettings() ) );
        // Global Proxy is the same
        newc.setGlobalHttpProxySettings( copyCRemoteHttpProxySettings1_0_8( oldc.getGlobalHttpProxySettings() ) );
        // TODO: not sure about routing?
        newc.setRouting( copyCRouting1_0_8( oldc.getRouting() ) );
        // REST Api is the same
        newc.setRestApi( copyCRestApi1_0_8( oldc.getRestApi() ) );
        // http proxy is the same
        newc.setHttpProxy( copyCHttpProxySettings1_0_8( oldc.getHttpProxy() ) );
        // targets are the same
        List<CRepositoryTarget> targets = new ArrayList<CRepositoryTarget>( oldc.getRepositoryTargets().size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget oldtargets : (List<org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget>) oldc
            .getRepositoryTargets() )
        {
            targets.add( copyCRepositoryTarget1_0_8( oldtargets ) );
        }
        newc.setRepositoryTargets( targets );

        // tasks are the same
        List<CScheduledTask> tasks = new ArrayList<CScheduledTask>( oldc.getTasks().size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask oldtasks : (List<org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask>) oldc
            .getTasks() )
        {
            tasks.add( copyCScheduledTask1_0_8( oldtasks ) );
        }
        newc.setTasks( tasks );

        // FIXME: Repositories are NOT the same
        List<CRepository> repositories = new ArrayList<CRepository>();
        for ( org.sonatype.nexus.configuration.model.v1_0_8.CRepository oldrepos : (List<org.sonatype.nexus.configuration.model.v1_0_8.CRepository>) oldc
            .getRepositories() )
        {
            CRepository newrepos = copyCRepository1_0_8( oldrepos );
            repositories.add( newrepos );
        }
        // shadows are repos
        if ( oldc.getRepositoryShadows() != null )
        {
            for ( org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow oldshadow : (List<org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow>) oldc
                .getRepositoryShadows() )
            {
                repositories.add( copyCRepositoryShadow1_0_8( oldshadow ) );
            }
        }

        if ( oldc.getRepositoryGrouping() != null )
        {
            CRepositoryGrouping repositoryGrouping = new CRepositoryGrouping();
            if ( oldc.getRepositoryGrouping().getPathMappings() != null )
            {
                for ( org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem oldItem : (List<org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem>) oldc
                    .getRepositoryGrouping().getPathMappings() )
                {
                    repositoryGrouping.addPathMapping( copyCGroupsSettingPathMappingItem1_0_8( oldItem ) );
                }
                newc.setRepositoryGrouping( repositoryGrouping );
            }
            
            // mergeMetadata
            boolean mergeMetadata = true;

            if ( oldc.getRouting() != null && oldc.getRouting().getGroups() != null )
            {
                mergeMetadata = oldc.getRouting().getGroups().isMergeMetadata();
            }

            for ( org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup oldgroup : (List<org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup>) oldc
                .getRepositoryGrouping().getRepositoryGroups() )
            {
                repositories.add( copyCRepositoryGroup1_0_8( oldgroup, mergeMetadata ) );
            }

            newc.setRepositories( repositories );
        }

        message.setModelVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }

    protected List<CProps> copyCProps1_0_8( List<org.sonatype.nexus.configuration.model.v1_0_8.CProps> oldprops )
    {
        List<CProps> properties = new ArrayList<CProps>( oldprops.size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_8.CProps oldprop : oldprops )
        {
            CProps newprop = new CProps();
            newprop.setKey( oldprop.getKey() );
            newprop.setValue( oldprop.getValue() );
            properties.add( newprop );
        }
        return properties;
    }

    protected CRemoteAuthentication copyCRemoteAuthentication1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication oldauth )
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

    protected CRemoteConnectionSettings copyCRemoteConnectionSettings1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings old )
    {
        CRemoteConnectionSettings cs = new CRemoteConnectionSettings();

        if ( old != null )
        {
            cs.setConnectionTimeout( old.getConnectionTimeout() );
            cs.setRetrievalRetryCount( old.getRetrievalRetryCount() );
            cs.setUserAgentCustomizationString( old.getUserAgentCustomizationString() );
            cs.setQueryString( old.getQueryString() );
        }
        return cs;
    }

    protected CRemoteHttpProxySettings copyCRemoteHttpProxySettings1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings old )
    {
        if ( old == null )
        {
            return null;
        }

        CRemoteHttpProxySettings cs = new CRemoteHttpProxySettings();
        cs.setProxyHostname( old.getProxyHostname() );
        cs.setProxyPort( old.getProxyPort() );
        cs.setAuthentication( copyCRemoteAuthentication1_0_8( old.getAuthentication() ) );
        return cs;
    }

    protected CRepository copyCRepository1_0_8( org.sonatype.nexus.configuration.model.v1_0_8.CRepository oldrepos )
    {
        CRepository newrepo = new CRepository();
        newrepo.setId( oldrepos.getId() );
        newrepo.setName( oldrepos.getName() );
        newrepo.setLocalStatus( this.localStatus.get( oldrepos.getLocalStatus()) );
        newrepo.setAllowWrite( oldrepos.isAllowWrite() );
        newrepo.setBrowseable( oldrepos.isBrowseable() );
        newrepo.setIndexable( oldrepos.isIndexable() );
        newrepo.setNotFoundCacheTTL( oldrepos.getNotFoundCacheTTL() );
        newrepo.setExposed( oldrepos.isExposed() );
        newrepo.setMirrors( copyCMirrors1_0_8( oldrepos.getMirrors() ) );
        newrepo.setNotFoundCacheActive( oldrepos.isNotFoundCacheActive() );
        newrepo.setPathPrefix( oldrepos.getPathPrefix() );
        newrepo.setProviderHint( oldrepos.getType() );
        newrepo.setProviderRole( Repository.class.getName() );
        newrepo.setUserManaged( oldrepos.isUserManaged() );

        // Manipulate the dom
        Xpp3Dom externalConfig = new Xpp3Dom( EXTERNAL_CONFIG );
        newrepo.setExternalConfiguration( externalConfig );
        this.setNodeValue( externalConfig, "proxyMode", this.proxyMode.get( oldrepos.getProxyMode() ));
        this.setNodeValue( externalConfig, "artifactMaxAge", Integer.toString( oldrepos.getArtifactMaxAge() ) );
        this.setNodeValue( externalConfig, "itemMaxAge", Integer.toString( oldrepos.getMetadataMaxAge() ) );
        this.setNodeValue( externalConfig, "cleanseRepositoryMetadata", Boolean.toString( oldrepos
            .isMaintainProxiedRepositoryMetadata() ) ); // TODO
        this
            .setNodeValue( externalConfig, "downloadRemoteIndex", Boolean.toString( oldrepos.isDownloadRemoteIndexes() ) );
        this.setNodeValue( externalConfig, "checksumPolicy", this.checksumPolicy.get( oldrepos.getChecksumPolicy() ));
        this.setNodeValue( externalConfig, "repositoryPolicy", oldrepos.getRepositoryPolicy() );

        if ( oldrepos.getLocalStorage() != null )
        {
            CLocalStorage localStorage = new CLocalStorage();
            localStorage.setUrl( oldrepos.getLocalStorage().getUrl() );
            newrepo.setLocalStorage( localStorage );
        }

        if ( oldrepos.getRemoteStorage() != null )
        {
            CRemoteStorage remoteStorage = new CRemoteStorage();
            remoteStorage.setUrl( oldrepos.getRemoteStorage().getUrl() );
            remoteStorage.setProvider( oldrepos.getRemoteStorage().getProvider() );
            if ( oldrepos.getRemoteStorage().getAuthentication() != null )
            {
                remoteStorage.setAuthentication( copyCRemoteAuthentication1_0_8( oldrepos
                    .getRemoteStorage().getAuthentication() ) );
            }
            if ( oldrepos.getRemoteStorage().getConnectionSettings() != null )
            {
                remoteStorage.setConnectionSettings( copyCRemoteConnectionSettings1_0_8( oldrepos
                    .getRemoteStorage().getConnectionSettings() ) );
            }
            if ( oldrepos.getRemoteStorage().getHttpProxySettings() != null )
            {
                remoteStorage.setHttpProxySettings( copyCRemoteHttpProxySettings1_0_8( oldrepos
                    .getRemoteStorage().getHttpProxySettings() ) );
            }
            newrepo.setRemoteStorage( remoteStorage );
        }
        return newrepo;
    }

    private List<CMirror> copyCMirrors1_0_8( List<org.sonatype.nexus.configuration.model.v1_0_8.CMirror> oldMirrors )
    {
        List<CMirror> newMirrors = new ArrayList<CMirror>();

        for ( org.sonatype.nexus.configuration.model.v1_0_8.CMirror oldMirror : oldMirrors )
        {
            CMirror newMirror = new CMirror();
            newMirror.setId( oldMirror.getId() );
            newMirror.setUrl( oldMirror.getUrl() );
            newMirrors.add( newMirror );
        }

        return newMirrors;
    }

    protected CSmtpConfiguration copyCSmtpConfiguration1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration oldsmtp )
    {
        CSmtpConfiguration smtp = new CSmtpConfiguration();

        if ( oldsmtp != null )
        {
            smtp.setDebugMode( oldsmtp.isDebugMode() );
            smtp.setHostname( oldsmtp.getHost() );
            smtp.setPassword( oldsmtp.getPassword() );
            smtp.setPort( oldsmtp.getPort() );
            smtp.setSslEnabled( oldsmtp.isSslEnabled() );
            smtp.setSystemEmailAddress( oldsmtp.getSystemEmailAddress() );
            smtp.setTlsEnabled( oldsmtp.isTlsEnabled() );
            smtp.setUsername( oldsmtp.getUsername() );
        }

        return smtp;
    }

    protected CSecurity copyCSecurity1_0_8( org.sonatype.nexus.configuration.model.v1_0_8.CSecurity oldsecurity )
    {
        CSecurity security = new CSecurity();

        if ( oldsecurity != null )
        {
            security.setAnonymousAccessEnabled( oldsecurity.isAnonymousAccessEnabled() );
            security.setAnonymousUsername( oldsecurity.getAnonymousUsername() );
            security.setAnonymousPassword( oldsecurity.getAnonymousPassword() );
            security.setEnabled( oldsecurity.isEnabled() );
            security.getRealms().addAll( oldsecurity.getRealms() );
        }

        return security;
    }

    protected CRouting copyCRouting1_0_8( org.sonatype.nexus.configuration.model.v1_0_8.CRouting oldrouting )
    {
        CRouting routing = new CRouting();

        if ( oldrouting != null )
        {
            routing.setResolveLinks( oldrouting.isFollowLinks() );
        }

        return routing;
    }

    protected CRestApiSettings copyCRestApi1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings oldrestapi )
    {
        CRestApiSettings restapi = new CRestApiSettings();

        if ( oldrestapi != null )
        {
            restapi.setBaseUrl( oldrestapi.getBaseUrl() );
            restapi.setForceBaseUrl( oldrestapi.isForceBaseUrl() );
            // restapi.setSessionExpiration( TODO );
            // restapi.setAccessAllowedFrom( oldrestapi.getAccessAllowedFrom() ); //TODO
        }

        return restapi;
    }

    protected CHttpProxySettings copyCHttpProxySettings1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings oldproxy )
    {
        CHttpProxySettings proxy = new CHttpProxySettings();

        if ( oldproxy != null )
        {
            proxy.setEnabled( oldproxy.isEnabled() );
            proxy.setPort( oldproxy.getPort() );
            proxy.setProxyPolicy( oldproxy.getProxyPolicy() );
        }

        return proxy;
    }

    protected CRepositoryTarget copyCRepositoryTarget1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget oldtarget )
    {
        CRepositoryTarget target = new CRepositoryTarget();

        if ( oldtarget != null )
        {
            target.setContentClass( oldtarget.getContentClass() );
            target.setId( oldtarget.getId() );
            target.setName( oldtarget.getName() );
            target.setPatterns( oldtarget.getPatterns() );
        }

        return target;
    }

    protected CScheduledTask copyCScheduledTask1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask oldtask )
    {
        CScheduledTask task = new CScheduledTask();

        if ( oldtask != null )
        {
            task.setType( oldtask.getType() );
            task.setEnabled( oldtask.isEnabled() );
            task.setId( oldtask.getId() );
            if( oldtask.getLastRun() != null )
            {
                task.setLastRun( oldtask.getLastRun().getTime() );
            }
            if( oldtask.getNextRun() != null )
            {
                task.setNextRun( oldtask.getNextRun().getTime() );
            }
            task.setName( oldtask.getName() );
            task.setStatus( oldtask.getStatus() );
            task.setProperties( copyCProps1_0_8( (List<org.sonatype.nexus.configuration.model.v1_0_8.CProps>) oldtask
                .getProperties() ) );
            task.setSchedule( copyCScheduleConfig1_0_8( oldtask.getSchedule() ) );
        }

        return task;
    }

    protected CScheduleConfig copyCScheduleConfig1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfig oldschedule )
    {
        CScheduleConfig schedule = new CScheduleConfig();

        if ( oldschedule != null )
        {
            schedule.setCronCommand( oldschedule.getCronCommand() );
            schedule.setDaysOfMonth( oldschedule.getDaysOfMonth() );
            schedule.setDaysOfWeek( oldschedule.getDaysOfWeek() );
            if( oldschedule.getEndDate() != null )
            {
                schedule.setEndDate( oldschedule.getEndDate().getTime() );
            }
            if( oldschedule.getStartDate() != null )
            {
                schedule.setStartDate( oldschedule.getStartDate().getTime() );
            }
            schedule.setType( oldschedule.getType() );
        }

        return schedule;
    }

    protected CRepository copyCRepositoryShadow1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow oldshadow )
        throws ConfigurationIsCorruptedException
    {
        CRepository newShadow = new CRepository();

        if ( oldshadow != null )
        {
            newShadow.setId( oldshadow.getId() );
            newShadow.setName( oldshadow.getName() );
            newShadow.setLocalStatus( this.localStatus.get( oldshadow.getLocalStatus() ));
            newShadow.setProviderHint( oldshadow.getType() );
            newShadow.setProviderRole( ShadowRepository.class.getName() );
            newShadow.setExposed( oldshadow.isExposed() );
            newShadow.setUserManaged( oldshadow.isUserManaged() );
            newShadow.setAllowWrite( false );
            newShadow.setBrowseable( true );
            newShadow.setIndexable( false );
            newShadow.setLocalStorage( null );
            newShadow.setMirrors( null );
            newShadow.setNotFoundCacheActive( false );
            newShadow.setNotFoundCacheTTL( 15 );
            newShadow.setPathPrefix( null );
            newShadow.setRemoteStorage( null );

            // shadow ShadowRepository.class
            // Manipulate the dom
            Xpp3Dom externalConfig = new Xpp3Dom( EXTERNAL_CONFIG );
            newShadow.setExternalConfiguration( externalConfig );
            this.setNodeValue( externalConfig, "masterRepositoryId", oldshadow.getShadowOf() );
            this.setNodeValue( externalConfig, "syncAtStartup", Boolean.toString( oldshadow.isSyncAtStartup() ) ); // TODO,
            // this
            // is
            // not
            // added
            // yet
        }

        return newShadow;
    }

    protected CPathMappingItem copyCGroupsSettingPathMappingItem1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem oldpathmapping )
    {
        CPathMappingItem pathMappingItem = new CPathMappingItem();

        if ( oldpathmapping != null )
        {
            pathMappingItem.setGroupId( oldpathmapping.getGroupId() );
            pathMappingItem.setId( oldpathmapping.getId() );
            pathMappingItem.setRepositories( oldpathmapping.getRepositories() );

            List<String> patterns = new ArrayList<String>();
            patterns.add( oldpathmapping.getRoutePattern() );
            pathMappingItem.setRoutePatterns( patterns );
            pathMappingItem.setRouteType( oldpathmapping.getRouteType() );
        }

        return pathMappingItem;
    }

    protected CRepository copyCRepositoryGroup1_0_8(
        org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup oldgroup, boolean mergeMetadata )
    {
        CRepository groupRepo = new CRepository();

        if ( oldgroup != null )
        {
            groupRepo.setId( oldgroup.getGroupId() );
            groupRepo.setName( oldgroup.getName() );
            groupRepo.setProviderHint( oldgroup.getType() );
            groupRepo.setProviderRole( GroupRepository.class.getName() );
            groupRepo.setAllowWrite( false );
            groupRepo.setBrowseable( true );
            groupRepo.setExposed( true );
            groupRepo.setIndexable( false );
            groupRepo.setLocalStatus( LocalStatus.IN_SERVICE.toString());
            
            if ( oldgroup.getLocalStorage() != null )
            {
                CLocalStorage localStorage = new CLocalStorage();
                localStorage.setUrl( oldgroup.getLocalStorage().getUrl() );
                groupRepo.setLocalStorage( localStorage );
            }

            groupRepo.setMirrors( null );
            groupRepo.setNotFoundCacheActive( true );
            groupRepo.setNotFoundCacheTTL( 15 );
            groupRepo.setPathPrefix( oldgroup.getPathPrefix() );
            groupRepo.setRemoteStorage( null );
            groupRepo.setUserManaged( true );

            // Manipulate the dom
            Xpp3Dom externalConfig = new Xpp3Dom( EXTERNAL_CONFIG );
            groupRepo.setExternalConfiguration( externalConfig );
            this.setNodeValue( externalConfig, "mergeMetadata", Boolean.toString( mergeMetadata ) );
            this.setCollectionValues( externalConfig, GROUP_MEMBERS_NODE, GROUP_CHILD_NODE, oldgroup.getRepositories() );
        }

        return groupRepo;
    }

    protected void setNodeValue( Xpp3Dom parent, String name, String value )
    {
        // if we do not have a current value, then just return without setting the node;
        if ( value == null )
        {
            return;
        }

        Xpp3Dom node = parent.getChild( name );

        if ( node == null )
        {
            node = new Xpp3Dom( name );

            parent.addChild( node );
        }

        node.setValue( value );
    }

    protected void setCollectionValues( Xpp3Dom parent, String nodeName, String childName, Collection<String> values )
    {
        Xpp3Dom node = parent.getChild( nodeName );

        if ( node != null )
        {
            for ( int i = 0; i < parent.getChildCount(); i++ )
            {
                Xpp3Dom existing = parent.getChild( i );

                if ( StringUtils.equals( nodeName, existing.getName() ) )
                {
                    parent.removeChild( i );

                    break;
                }
            }

            node = null;
        }

        node = new Xpp3Dom( nodeName );

        parent.addChild( node );

        for ( String childVal : values )
        {
            Xpp3Dom childNode = new Xpp3Dom( childName );
            node.addChild( childNode );
            childNode.setValue( childVal );
        }
    }
}
