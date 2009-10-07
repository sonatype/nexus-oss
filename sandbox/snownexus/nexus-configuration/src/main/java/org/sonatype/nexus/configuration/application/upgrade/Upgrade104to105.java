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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.model.v1_0_4.Configuration;
import org.sonatype.nexus.configuration.model.v1_0_4.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.v1_0_5.CGroupsSetting;
import org.sonatype.nexus.configuration.model.v1_0_5.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.v1_0_5.CHttpProxySettings;
import org.sonatype.nexus.configuration.model.v1_0_5.CLocalStorage;
import org.sonatype.nexus.configuration.model.v1_0_5.CProps;
import org.sonatype.nexus.configuration.model.v1_0_5.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.v1_0_5.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.v1_0_5.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.v1_0_5.CRemoteStorage;
import org.sonatype.nexus.configuration.model.v1_0_5.CRepository;
import org.sonatype.nexus.configuration.model.v1_0_5.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.v1_0_5.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.v1_0_5.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.v1_0_5.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.v1_0_5.CRestApiSettings;
import org.sonatype.nexus.configuration.model.v1_0_5.CRouting;
import org.sonatype.nexus.configuration.model.v1_0_5.CScheduleConfig;
import org.sonatype.nexus.configuration.model.v1_0_5.CScheduledTask;
import org.sonatype.nexus.configuration.model.v1_0_5.CSecurity;
import org.sonatype.nexus.configuration.model.v1_0_5.CSmtpConfiguration;

/**
 * Upgrades configuration model from version 1.0.4 to 1.0.5.
 * 
 * @author cstamas
 */
@Component( role = SingleVersionUpgrader.class, hint = "1.0.4" )
public class Upgrade104to105
    extends AbstractLogEnabled
    implements SingleVersionUpgrader
{
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
        org.sonatype.nexus.configuration.model.v1_0_5.Configuration newc = new org.sonatype.nexus.configuration.model.v1_0_5.Configuration();

        newc.setVersion( org.sonatype.nexus.configuration.model.v1_0_5.Configuration.MODEL_VERSION );
        // Working & log directories removed in this revision
        // newc.setWorkingDirectory( oldc.getWorkingDirectory() );
        // newc.setApplicationLogDirectory( oldc.getApplicationLogDirectory() );

        newc.setSmtpConfiguration( copyCSmtpConfiguration1_0_4( oldc.getSmtpConfiguration() ) );

        newc.setSecurity( copyCSecurity1_0_4( oldc.getSecurity() ) );

        newc.setGlobalConnectionSettings( copyCRemoteConnectionSettings1_0_4( oldc.getGlobalConnectionSettings() ) );

        newc.setGlobalHttpProxySettings( copyCRemoteHttpProxySettings1_0_4( oldc.getGlobalHttpProxySettings() ) );

        newc.setRouting( copyCRouting1_0_4( oldc.getRouting() ) );

        newc.setRestApi( copyCRestApi1_0_4( oldc.getRestApi() ) );

        newc.setHttpProxy( copyCHttpProxySettings1_0_4( oldc.getHttpProxy() ) );

        List<CRepositoryTarget> targets = new ArrayList<CRepositoryTarget>( oldc.getRepositoryTargets().size() );

        for ( org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryTarget oldtargets : (List<org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryTarget>) oldc
            .getRepositoryTargets() )
        {
            targets.add( copyCRepositoryTarget1_0_4( oldtargets ) );
        }

        newc.setRepositoryTargets( targets );

        checkRepositoryTargetsForDefaults( newc.getRepositoryTargets() );

        List<CScheduledTask> tasks = new ArrayList<CScheduledTask>( oldc.getTasks().size() );

        for ( org.sonatype.nexus.configuration.model.v1_0_4.CScheduledTask oldtasks : (List<org.sonatype.nexus.configuration.model.v1_0_4.CScheduledTask>) oldc
            .getTasks() )
        {
            tasks.add( copyCScheduledTask1_0_4( oldtasks ) );
        }

        newc.setTasks( tasks );

        List<CRepository> repositories = new ArrayList<CRepository>( oldc.getRepositories().size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_4.CRepository oldrepos : (List<org.sonatype.nexus.configuration.model.v1_0_4.CRepository>) oldc
            .getRepositories() )
        {
            CRepository newrepos = copyCRepository1_0_4( oldrepos );
            newrepos.setRepositoryPolicy( oldrepos.getRepositoryPolicy() );
            repositories.add( newrepos );
        }

        newc.setRepositories( repositories );

        if ( oldc.getRepositoryShadows() != null )
        {
            List<CRepositoryShadow> repositoryShadows = new ArrayList<CRepositoryShadow>( oldc
                .getRepositoryShadows().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryShadow oldshadow : (List<org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryShadow>) oldc
                .getRepositoryShadows() )
            {
                repositoryShadows.add( copyCRepositoryShadow1_0_4( oldshadow ) );
            }
            newc.setRepositoryShadows( repositoryShadows );
        }

        if ( oldc.getRepositoryGrouping() != null )
        {
            CRepositoryGrouping repositoryGrouping = new CRepositoryGrouping();
            if ( oldc.getRepositoryGrouping().getPathMappings() != null )
            {
                for ( org.sonatype.nexus.configuration.model.v1_0_4.CGroupsSettingPathMappingItem oldItem : (List<org.sonatype.nexus.configuration.model.v1_0_4.CGroupsSettingPathMappingItem>) oldc
                    .getRepositoryGrouping().getPathMappings() )
                {
                    repositoryGrouping.addPathMapping( copyCGroupsSettingPathMappingItem1_0_4( oldItem ) );
                }
            }
            List<CRepositoryGroup> repositoryGroups = new ArrayList<CRepositoryGroup>( oldc
                .getRepositoryGrouping().getRepositoryGroups().size() );
            for ( org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryGroup oldgroup : (List<org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryGroup>) oldc
                .getRepositoryGrouping().getRepositoryGroups() )
            {
                repositoryGroups.add( copyCRepositoryGroup1_0_4( oldgroup ) );
            }
            repositoryGrouping.setRepositoryGroups( repositoryGroups );
            newc.setRepositoryGrouping( repositoryGrouping );
        }

        message.setModelVersion( org.sonatype.nexus.configuration.model.v1_0_5.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );
    }

    protected List<CProps> copyCProps1_0_4( List<org.sonatype.nexus.configuration.model.v1_0_4.CProps> oldprops )
    {
        List<CProps> properties = new ArrayList<CProps>( oldprops.size() );
        for ( org.sonatype.nexus.configuration.model.v1_0_4.CProps oldprop : oldprops )
        {
            CProps newprop = new CProps();
            newprop.setKey( oldprop.getKey() );
            newprop.setValue( oldprop.getValue() );
            properties.add( newprop );
        }
        return properties;
    }

    protected CRemoteAuthentication copyCRemoteAuthentication1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CRemoteAuthentication oldauth )
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

    protected CRemoteConnectionSettings copyCRemoteConnectionSettings1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CRemoteConnectionSettings old )
    {
        CRemoteConnectionSettings cs = new CRemoteConnectionSettings();

        if ( old != null )
        {
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
        }
        return cs;
    }

    protected CRemoteHttpProxySettings copyCRemoteHttpProxySettings1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CRemoteHttpProxySettings old )
    {
        if ( old == null )
        {
            return null;
        }

        CRemoteHttpProxySettings cs = new CRemoteHttpProxySettings();
        cs.setProxyHostname( old.getProxyHostname() );
        cs.setProxyPort( old.getProxyPort() );
        cs.setAuthentication( copyCRemoteAuthentication1_0_4( old.getAuthentication() ) );
        return cs;
    }

    protected CRepository copyCRepository1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CRepository oldrepos )
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
        newrepos.setMaintainProxiedRepositoryMetadata( oldrepos.isMaintainProxiedRepositoryMetadata() );
        newrepos.setDownloadRemoteIndexes( oldrepos.isDownloadRemoteIndexes() );
        newrepos.setChecksumPolicy( oldrepos.getChecksumPolicy() );

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
                remoteStorage.setAuthentication( copyCRemoteAuthentication1_0_4( oldrepos
                    .getRemoteStorage().getAuthentication() ) );
            }
            if ( oldrepos.getRemoteStorage().getConnectionSettings() != null )
            {
                remoteStorage.setConnectionSettings( copyCRemoteConnectionSettings1_0_4( oldrepos
                    .getRemoteStorage().getConnectionSettings() ) );
            }
            if ( oldrepos.getRemoteStorage().getHttpProxySettings() != null )
            {
                remoteStorage.setHttpProxySettings( copyCRemoteHttpProxySettings1_0_4( oldrepos
                    .getRemoteStorage().getHttpProxySettings() ) );
            }
            newrepos.setRemoteStorage( remoteStorage );
        }
        return newrepos;
    }

    protected CSmtpConfiguration copyCSmtpConfiguration1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CSmtpConfiguration oldsmtp )
    {
        CSmtpConfiguration smtp = new CSmtpConfiguration();

        if ( oldsmtp != null )
        {
            smtp.setDebugMode( oldsmtp.isDebugMode() );
            smtp.setHost( oldsmtp.getHost() );
            smtp.setPassword( oldsmtp.getPassword() );
            smtp.setPort( oldsmtp.getPort() );
            smtp.setSslEnabled( oldsmtp.isSslEnabled() );
            smtp.setSystemEmailAddress( oldsmtp.getSystemEmailAddress() );
            smtp.setTlsEnabled( oldsmtp.isTlsEnabled() );
            smtp.setUsername( oldsmtp.getUsername() );
        }

        return smtp;
    }

    protected CSecurity copyCSecurity1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CSecurity oldsecurity )
    {
        CSecurity security = new CSecurity();

        if ( oldsecurity != null )
        {
            security.setAnonymousAccessEnabled( oldsecurity.isAnonymousAccessEnabled() );
            security.setAnonymousPassword( oldsecurity.getAnonymousPassword() );
            security.setAnonymousUsername( oldsecurity.getAnonymousUsername() );
            security.setEnabled( oldsecurity.isEnabled() );
            security.setRealms( oldsecurity.getRealms() );
        }

        return security;
    }

    protected CRouting copyCRouting1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CRouting oldrouting )
    {
        CRouting routing = new CRouting();

        if ( oldrouting != null )
        {
            routing.setFollowLinks( oldrouting.isFollowLinks() );
            routing.setNotFoundCacheTTL( oldrouting.getNotFoundCacheTTL() );
            if ( oldrouting.getGroups() != null )
            {
                CGroupsSetting groups = new CGroupsSetting();
                groups.setStopItemSearchOnFirstFoundFile( oldrouting.getGroups().isStopItemSearchOnFirstFoundFile() );
                groups.setMergeMetadata( oldrouting.getGroups().isMergeMetadata() );
                routing.setGroups( groups );
            }
        }

        return routing;
    }

    protected CRestApiSettings copyCRestApi1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CRestApiSettings oldrestapi )
    {
        CRestApiSettings restapi = new CRestApiSettings();

        if ( oldrestapi != null )
        {
            restapi.setAccessAllowedFrom( oldrestapi.getAccessAllowedFrom() );
            restapi.setBaseUrl( oldrestapi.getBaseUrl() );
        }

        return restapi;
    }

    protected CHttpProxySettings copyCHttpProxySettings1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CHttpProxySettings oldproxy )
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

    protected CRepositoryTarget copyCRepositoryTarget1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryTarget oldtarget )
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

    protected void checkRepositoryTargetsForDefaults( List<CRepositoryTarget> targets )
    {
        // check are the defaults here, if not, add them
        Set<String> existingIds = new HashSet<String>();

        for ( CRepositoryTarget target : targets )
        {
            existingIds.add( target.getId() );
        }

        if ( !existingIds.contains( "1" ) )
        {
            // add it
            CRepositoryTarget t = new CRepositoryTarget();
            t.setId( "1" );
            t.setName( "All (Maven2)" );
            t.setContentClass( "maven2" );
            t.addPattern( ".*" );

            targets.add( t );
        }
        if ( !existingIds.contains( "2" ) )
        {
            // add it
            CRepositoryTarget t = new CRepositoryTarget();
            t.setId( "2" );
            t.setName( "All (Maven1)" );
            t.setContentClass( "maven1" );
            t.addPattern( ".*" );

            targets.add( t );
        }
        if ( !existingIds.contains( "3" ) )
        {
            // add it
            CRepositoryTarget t = new CRepositoryTarget();
            t.setId( "3" );
            t.setName( "All but sources (Maven2)" );
            t.setContentClass( "maven2" );
            t.addPattern( "(?!.*-sources.*).*" );

            targets.add( t );
        }
        if ( !existingIds.contains( "4" ) )
        {
            // add it
            CRepositoryTarget t = new CRepositoryTarget();
            t.setId( "4" );
            t.setName( "All Metadata (Maven2)" );
            t.setContentClass( "maven2" );
            t.addPattern( ".*maven-metadata\\.xml.*" );

            targets.add( t );
        }

    }

    protected CScheduledTask copyCScheduledTask1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CScheduledTask oldtask )
    {
        CScheduledTask task = new CScheduledTask();

        if ( oldtask != null )
        {
            task.setEnabled( oldtask.isEnabled() );
            task.setId( oldtask.getId() );
            task.setLastRun( oldtask.getLastRun() );
            task.setNextRun( oldtask.getNextRun() );
            task.setName( oldtask.getName() );
            task.setStatus( oldtask.getStatus() );
            task.setType( oldtask.getType() );
            task.setProperties( copyCProps1_0_4( (List<org.sonatype.nexus.configuration.model.v1_0_4.CProps>) oldtask
                .getProperties() ) );
            task.setSchedule( copyCScheduleConfig1_0_4( oldtask.getSchedule() ) );
        }

        return task;
    }

    protected CScheduleConfig copyCScheduleConfig1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CScheduleConfig oldschedule )
    {
        CScheduleConfig schedule = new CScheduleConfig();

        if ( oldschedule != null )
        {
            schedule.setCronCommand( oldschedule.getCronCommand() );
            schedule.setDaysOfMonth( oldschedule.getDaysOfMonth() );
            schedule.setDaysOfWeek( oldschedule.getDaysOfWeek() );            
            schedule.setEndDate( oldschedule.getEndDate() );
            schedule.setStartDate( oldschedule.getStartDate() );
            schedule.setType( oldschedule.getType() );
            
            // Fix for NEXUS-1252
            if ( schedule.getDaysOfWeek() != null 
                && schedule.getDaysOfWeek().size() > 0 )
            {
                schedule.setType( "weekly" );
            }
        }

        return schedule;
    }

    protected CRepositoryShadow copyCRepositoryShadow1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryShadow oldshadow )
    {
        CRepositoryShadow shadow = new CRepositoryShadow();

        if ( oldshadow != null )
        {
            shadow.setId( oldshadow.getId() );
            shadow.setName( oldshadow.getName() );
            shadow.setLocalStatus( oldshadow.getLocalStatus() );
            shadow.setShadowOf( oldshadow.getShadowOf() );
            shadow.setType( oldshadow.getType() );
            shadow.setSyncAtStartup( oldshadow.isSyncAtStartup() );
        }

        return shadow;
    }

    protected CGroupsSettingPathMappingItem copyCGroupsSettingPathMappingItem1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CGroupsSettingPathMappingItem oldpathmapping )
    {
        CGroupsSettingPathMappingItem pathmapping = new CGroupsSettingPathMappingItem();

        if ( oldpathmapping != null )
        {
            pathmapping.setGroupId( oldpathmapping.getGroupId() );
            pathmapping.setId( oldpathmapping.getId() );
            pathmapping.setRepositories( oldpathmapping.getRepositories() );
            pathmapping.setRoutePattern( oldpathmapping.getRoutePattern() );
            pathmapping.setRouteType( oldpathmapping.getRouteType() );
        }

        return pathmapping;
    }

    protected CRepositoryGroup copyCRepositoryGroup1_0_4(
        org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryGroup oldgroup )
    {
        CRepositoryGroup group = new CRepositoryGroup();

        if ( oldgroup != null )
        {
            group.setGroupId( oldgroup.getGroupId() );
            group.setName( oldgroup.getName() );
            group.setRepositories( oldgroup.getRepositories() );
        }

        return group;
    }
}
