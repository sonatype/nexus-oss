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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.model.CErrorReporting;
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
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.model.v1_0_8.Configuration;
import org.sonatype.nexus.configuration.model.v1_0_8.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.util.ExternalConfigUtil;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.source.PasswordHelper;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;

/**
 * Upgrades configuration model from version 1.0.8 to 1.4.0.
 * 
 * @author cstamas
 */
@Component( role = SingleVersionUpgrader.class, hint = "1.0.8" )
public class Upgrade108to140
    extends AbstractLogEnabled
    implements SingleVersionUpgrader
{
    @Requirement( hint = "file" )
    private SecurityConfigurationSource securityConfigurationSource;

    @Requirement
    private PasswordHelper passwordHelper;

    private static final String EXTERNAL_CONFIG = "externalConfiguration";

    private static final String GROUP_MEMBERS_NODE = "memberRepositories";

    private static final String GROUP_CHILD_NODE = "memberRepository";

    private final Map<String, String> localStatus = new HashMap<String, String>();

    private final Map<String, String> checksumPolicy = new HashMap<String, String>();

    private final Map<String, String> proxyMode = new HashMap<String, String>();

    private final Map<String, String> taskTypes = new HashMap<String, String>();

    private static final String TASK_EXPIRE_CACHE_OLD = "ClearCacheTask";

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
        this.proxyMode.put( "blockedManual", "BLOCKED_MANUAL" );

        this.taskTypes.put( TASK_EXPIRE_CACHE_OLD, "ExpireCacheTask" );
    }

    public Object loadConfiguration( File file )
        throws IOException, ConfigurationIsCorruptedException
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
        org.sonatype.nexus.configuration.model.Configuration newc =
            new org.sonatype.nexus.configuration.model.Configuration();

        // Security has been moved out of the Nexus.xml
        try
        {
            this.upgradeSecurity( oldc.getSecurity() );
        }
        catch ( IOException e )
        {
            throw new ConfigurationIsCorruptedException( "nexus.xml", e );
        }

        newc.setVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        // SMTP info is the same
        newc.setSmtpConfiguration( copyCSmtpConfiguration1_0_8( oldc.getSmtpConfiguration() ) );
        // Security info is the same
        // newc.setSecurity( copyCSecurity1_0_8( oldc.getSecurity() ) );
        // Global Connection is the same
        newc.setGlobalConnectionSettings( copyCRemoteConnectionSettings1_0_8( oldc.getGlobalConnectionSettings() ) );
        // Global Proxy is the same
        newc.setGlobalHttpProxySettings( copyCRemoteHttpProxySettings1_0_8( oldc.getGlobalHttpProxySettings() ) );
        // the group setttings where moved into the groups
        newc.setRouting( copyCRouting1_0_8( oldc.getRouting() ) );
        // REST Api is the same
        newc.setRestApi( copyCRestApi1_0_8( oldc.getRestApi() ) );
        // http proxy is the same
        newc.setHttpProxy( copyCHttpProxySettings1_0_8( oldc.getHttpProxy() ) );
        // targets are the same
        List<CRepositoryTarget> targets = new ArrayList<CRepositoryTarget>( oldc.getRepositoryTargets().size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget oldtargets : oldc.getRepositoryTargets() )
        {
            targets.add( copyCRepositoryTarget1_0_8( oldtargets ) );
        }
        newc.setRepositoryTargets( targets );

        // tasks are the same, except the clear cache task
        List<CScheduledTask> tasks = new ArrayList<CScheduledTask>( oldc.getTasks().size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask oldTask : oldc.getTasks() )
        {
            upgradeTask( oldTask );

            tasks.add( copyCScheduledTask1_0_8( oldTask ) );
        }
        newc.setTasks( tasks );

        // FIXME: Repositories are NOT the same
        List<CRepository> repositories = new ArrayList<CRepository>();
        for ( org.sonatype.nexus.configuration.model.v1_0_8.CRepository oldRepo : oldc.getRepositories() )
        {
            upgradeRepository( oldRepo );

            repositories.add( copyCRepository1_0_8( oldRepo ) );
        }
        // shadows are repos
        if ( oldc.getRepositoryShadows() != null )
        {
            for ( org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow oldshadow : oldc
                .getRepositoryShadows() )
            {
                upgradeShadowRepository( oldshadow );

                repositories.add( copyCRepositoryShadow1_0_8( oldshadow ) );
            }
        }

        if ( oldc.getRepositoryGrouping() != null )
        {
            CRepositoryGrouping repositoryGrouping = new CRepositoryGrouping();

            if ( oldc.getRepositoryGrouping().getPathMappings() != null )
            {
                for ( org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem oldItem : oldc
                    .getRepositoryGrouping().getPathMappings() )
                {
                    upgradePathMapping( oldItem );

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

            for ( org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup oldgroup : oldc
                .getRepositoryGrouping().getRepositoryGroups() )
            {
                upgradeGroup( oldgroup );

                repositories.add( copyCRepositoryGroup1_0_8( oldgroup, mergeMetadata ) );
            }

            newc.setRepositories( repositories );
        }

        // initialize automatic error reporting
        CErrorReporting errorReporting = new CErrorReporting();
        errorReporting.setEnabled( false );
        errorReporting.setJiraUrl( "https://issues.sonatype.org" );
        errorReporting.setJiraProject( "PR" );

        newc.setErrorReporting( errorReporting );

        message.setModelVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }

    private void upgradeSecurity( org.sonatype.nexus.configuration.model.v1_0_8.CSecurity oldsecurity )
        throws IOException
    {
        // if the oldsecurity is null, we can just use the defaults at runtime
        if ( oldsecurity != null )
        {
            SecurityConfiguration securityConfig = new SecurityConfiguration();
            // set the version
            securityConfig.setVersion( SecurityConfiguration.MODEL_VERSION );
            securityConfig.setAnonymousAccessEnabled( oldsecurity.isAnonymousAccessEnabled() );
            securityConfig.setAnonymousUsername( oldsecurity.getAnonymousUsername() );
            try
            {
                securityConfig.setAnonymousPassword( passwordHelper.decrypt( oldsecurity.getAnonymousPassword() ) );
            }
            catch ( PlexusCipherException e )
            {
                getLogger().error(
                    "Failed to decrype anonymous password in nexus.xml, password might be encrypted in memory.", e );
            }
            securityConfig.setEnabled( oldsecurity.isEnabled() );
            securityConfig.getRealms().addAll( oldsecurity.getRealms() );

            securityConfigurationSource.setConfiguration( securityConfig );
            securityConfigurationSource.storeConfiguration();
        }
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
            try
            {
                newauth.setPassword( passwordHelper.decrypt( oldauth.getPassword() ) );
            }
            catch ( PlexusCipherException e )
            {
                getLogger().error(
                    "Failed to decrype anonymous password in nexus.xml, password might be encrypted in memory.", e );
            }
            newauth.setNtlmHost( oldauth.getNtlmHost() );
            newauth.setNtlmDomain( oldauth.getNtlmDomain() );
            // not used in pre 1.4.x
            // newauth.setPrivateKey( oldauth.getPrivateKey() );
            // newauth.setPassphrase( oldauth.getPassphrase() );
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
        newrepo.setLocalStatus( this.localStatus.get( oldrepos.getLocalStatus() ) );
        newrepo.setBrowseable( oldrepos.isBrowseable() );
        newrepo.setIndexable( oldrepos.isIndexable() );
        newrepo.setSearchable( oldrepos.isIndexable() );
        newrepo.setNotFoundCacheTTL( oldrepos.getNotFoundCacheTTL() );
        newrepo.setExposed( oldrepos.isExposed() );
        newrepo.setMirrors( copyCMirrors1_0_8( oldrepos.getMirrors() ) );
        newrepo.setNotFoundCacheActive( oldrepos.isNotFoundCacheActive() );
        newrepo.setPathPrefix( oldrepos.getPathPrefix() );
        String providerHint = oldrepos.getType() != null ? oldrepos.getType() : "maven2";
        newrepo.setProviderHint( providerHint );
        newrepo.setProviderRole( Repository.class.getName() );
        newrepo.setUserManaged( oldrepos.isUserManaged() );

        // set the write Policy
        if ( oldrepos.isAllowWrite() )
        {
            newrepo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        }
        else
        {
            newrepo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );
        }

        // Manipulate the dom
        Xpp3Dom externalConfig = new Xpp3Dom( EXTERNAL_CONFIG );
        newrepo.setExternalConfiguration( externalConfig );
        ExternalConfigUtil.setNodeValue( externalConfig, "proxyMode", this.proxyMode.get( oldrepos.getProxyMode() ) );
        ExternalConfigUtil.setNodeValue( externalConfig, "artifactMaxAge", Integer.toString( oldrepos
            .getArtifactMaxAge() ) );
        ExternalConfigUtil
            .setNodeValue( externalConfig, "itemMaxAge", Integer.toString( oldrepos.getMetadataMaxAge() ) );
        ExternalConfigUtil.setNodeValue( externalConfig, "cleanseRepositoryMetadata", Boolean.toString( oldrepos
            .isMaintainProxiedRepositoryMetadata() ) );
        ExternalConfigUtil.setNodeValue( externalConfig, "downloadRemoteIndex", Boolean.toString( oldrepos
            .isDownloadRemoteIndexes() ) );
        ExternalConfigUtil.setNodeValue( externalConfig, "checksumPolicy", this.checksumPolicy.get( oldrepos
            .getChecksumPolicy() ) );
        ExternalConfigUtil.setNodeValue( externalConfig, "repositoryPolicy", oldrepos.getRepositoryPolicy() );

        org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage oldLocalStorage = oldrepos.getLocalStorage();
        if ( oldLocalStorage != null )
        {
            CLocalStorage localStorage = new CLocalStorage();
            localStorage.setUrl( oldLocalStorage.getUrl() );
            String provider = oldLocalStorage.getProvider() == null ? "file" : oldLocalStorage.getProvider();
            localStorage.setProvider( provider );
            newrepo.setLocalStorage( localStorage );
        }

        if ( oldrepos.getRemoteStorage() != null )
        {
            CRemoteStorage remoteStorage = new CRemoteStorage();
            remoteStorage.setUrl( oldrepos.getRemoteStorage().getUrl() );
            remoteStorage.setProvider( oldrepos.getRemoteStorage().getProvider() );
            if ( oldrepos.getRemoteStorage().getAuthentication() != null )
            {
                remoteStorage.setAuthentication( copyCRemoteAuthentication1_0_8( oldrepos.getRemoteStorage()
                    .getAuthentication() ) );
            }
            if ( oldrepos.getRemoteStorage().getConnectionSettings() != null )
            {
                remoteStorage.setConnectionSettings( copyCRemoteConnectionSettings1_0_8( oldrepos.getRemoteStorage()
                    .getConnectionSettings() ) );
            }
            if ( oldrepos.getRemoteStorage().getHttpProxySettings() != null )
            {
                remoteStorage.setHttpProxySettings( copyCRemoteHttpProxySettings1_0_8( oldrepos.getRemoteStorage()
                    .getHttpProxySettings() ) );
            }

            remoteStorage.setMirrors( copyCMirrors1_0_8( oldrepos.getRemoteStorage().getMirrors() ) );

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
        if ( oldrestapi == null )
        {
            return null;
        }

        CRestApiSettings restapi = new CRestApiSettings();
        restapi.setBaseUrl( oldrestapi.getBaseUrl() );
        restapi.setForceBaseUrl( oldrestapi.isForceBaseUrl() );

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
            if ( oldtask.getLastRun() != null )
            {
                task.setLastRun( oldtask.getLastRun().getTime() );
            }
            if ( oldtask.getNextRun() != null )
            {
                task.setNextRun( oldtask.getNextRun().getTime() );
            }
            task.setName( oldtask.getName() );
            task.setStatus( oldtask.getStatus() );
            task.setProperties( copyCProps1_0_8( oldtask.getProperties() ) );
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
            if ( oldschedule.getEndDate() != null )
            {
                schedule.setEndDate( oldschedule.getEndDate().getTime() );
            }
            if ( oldschedule.getStartDate() != null )
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
            newShadow.setLocalStatus( this.localStatus.get( oldshadow.getLocalStatus() ) );
            newShadow.setProviderHint( oldshadow.getType() );
            newShadow.setProviderRole( ShadowRepository.class.getName() );
            newShadow.setExposed( oldshadow.isExposed() );
            newShadow.setUserManaged( oldshadow.isUserManaged() );
            newShadow.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );
            newShadow.setBrowseable( true );
            newShadow.setIndexable( false );
            newShadow.setSearchable( false );
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
            ExternalConfigUtil.setNodeValue( externalConfig, "masterRepositoryId", oldshadow.getShadowOf() );
            ExternalConfigUtil.setNodeValue( externalConfig, "synchronizeAtStartup", Boolean.toString( oldshadow
                .isSyncAtStartup() ) );
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
                                                     org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup oldgroup,
                                                     boolean mergeMetadata )
    {
        CRepository groupRepo = new CRepository();

        if ( oldgroup != null )
        {
            groupRepo.setId( oldgroup.getGroupId() );
            groupRepo.setName( oldgroup.getName() );
            String providerHint = oldgroup.getType() != null ? oldgroup.getType() : "maven2";
            groupRepo.setProviderHint( providerHint );
            groupRepo.setProviderRole( GroupRepository.class.getName() );
            groupRepo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );
            groupRepo.setBrowseable( true );
            groupRepo.setExposed( true );
            groupRepo.setIndexable( true );
            groupRepo.setSearchable( false );
            groupRepo.setLocalStatus( LocalStatus.IN_SERVICE.toString() );

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
            ExternalConfigUtil.setNodeValue( externalConfig, "mergeMetadata", Boolean.toString( mergeMetadata ) );
            ExternalConfigUtil.setCollectionValues( externalConfig, GROUP_MEMBERS_NODE, GROUP_CHILD_NODE, oldgroup
                .getRepositories() );
        }

        return groupRepo;
    }

    private void upgradeTask( org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask task )
    {
        if ( TASK_EXPIRE_CACHE_OLD.equals( task.getType() ) )
        {
            task.setType( this.taskTypes.get( TASK_EXPIRE_CACHE_OLD ) );
        }
    }

    private void upgradeRepository( org.sonatype.nexus.configuration.model.v1_0_8.CRepository repo )
    {
        repo.setId( upgradeSlashToHyphen( repo.getId(), true ) );
    }

    private void upgradeShadowRepository( org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow shadowRepo )
    {
        shadowRepo.setId( upgradeSlashToHyphen( shadowRepo.getId(), true ) );

        shadowRepo.setShadowOf( upgradeSlashToHyphen( shadowRepo.getShadowOf(), false ) );
    }

    private void upgradePathMapping(
                                     org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem pathMapping )
    {
        pathMapping.setGroupId( upgradeSlashToHyphen( pathMapping.getGroupId(), true ) );

        List<String> upgradedRepos = new ArrayList<String>( pathMapping.getRepositories().size() );

        for ( String repo : pathMapping.getRepositories() )
        {
            upgradedRepos.add( upgradeSlashToHyphen( repo, false ) );
        }

        pathMapping.setRepositories( upgradedRepos );
    }

    private void upgradeGroup( org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup group )
    {
        group.setGroupId( upgradeSlashToHyphen( group.getGroupId(), true ) );

        List<String> upgradedRepos = new ArrayList<String>( group.getRepositories().size() );

        for ( String repo : group.getRepositories() )
        {
            upgradedRepos.add( upgradeSlashToHyphen( repo, false ) );
        }

        group.setRepositories( upgradedRepos );
    }

    private String upgradeSlashToHyphen( String str, boolean showWarn )
    {
        if ( !str.contains( "/" ) )
        {
            return str;
        }

        String newStr = str.replace( '/', '-' );

        if ( showWarn )
        {
            getLogger().warn(
                "Nexus no longer supports Repository/Group ID with slash in it.\n The ID of Repository/Group '" + str
                    + "' was upgraded to '" + newStr
                    + "'.\n Please move the repository contents manually if necessary." );
        }

        return newStr;
    }
}
