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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.net.SMTPAppender;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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
import org.sonatype.nexus.configuration.model.CScheduleConfig;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.CSecurity;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.model.v1_0_3.CAdvancedSchedule;
import org.sonatype.nexus.configuration.model.v1_0_3.CDailySchedule;
import org.sonatype.nexus.configuration.model.v1_0_3.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.v1_0_3.CMonthlySchedule;
import org.sonatype.nexus.configuration.model.v1_0_3.COnceSchedule;
import org.sonatype.nexus.configuration.model.v1_0_3.CRunNowSchedule;
import org.sonatype.nexus.configuration.model.v1_0_3.CSchedule;
import org.sonatype.nexus.configuration.model.v1_0_3.CTaskConfiguration;
import org.sonatype.nexus.configuration.model.v1_0_3.CWeeklySchedule;
import org.sonatype.nexus.configuration.model.v1_0_3.Configuration;
import org.sonatype.nexus.configuration.model.v1_0_3.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.upgrade.Upgrader;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.BaseException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Upgrades configuration model from version 1.0.3 to 1.0.4.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.configuration.upgrade.Upgrader" role-hint="1.0.3"
 */
public class Upgrade103to104
    extends AbstractLogEnabled
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

            // alias the versioned class (they are frozen to 1.0.3 only!)
            xstream.alias( "org.sonatype.nexus.configuration.model.CTaskConfiguration", CTaskConfiguration.class );
            xstream.alias(
                "org.sonatype.nexus.configuration.model.CScheduledTask",
                org.sonatype.nexus.configuration.model.v1_0_3.CScheduledTask.class );

            xstream.alias( "org.sonatype.nexus.configuration.model.CAdvancedSchedule", CAdvancedSchedule.class );
            xstream.alias( "org.sonatype.nexus.configuration.model.CMonthlySchedule", CMonthlySchedule.class );
            xstream.alias( "org.sonatype.nexus.configuration.model.CWeeklySchedule", CWeeklySchedule.class );
            xstream.alias( "org.sonatype.nexus.configuration.model.CDailySchedule", CDailySchedule.class );
            xstream.alias( "org.sonatype.nexus.configuration.model.COnceSchedule", COnceSchedule.class );
            xstream.alias( "org.sonatype.nexus.configuration.model.CRunNowSchedule", CRunNowSchedule.class );
            xstream.alias( "org.sonatype.nexus.configuration.model.CSchedule", CSchedule.class );

            tasksConfig = new CTaskConfiguration();

            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream( tasksFile );

                xstream.fromXML( fis, tasksConfig );

                getLogger().info( "tasks.xml file found and loaded..." );
            }
            catch ( BaseException e )
            {
                getLogger().warn( "Could not load tasks.xml, IGNORING IT!", e );

                tasksConfig = null;
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

        // Just add some default config
        CSmtpConfiguration smtp = new CSmtpConfiguration();
        smtp.setHost( "smtp-host" );
        smtp.setPassword( "smtp-password" );
        smtp.setPort( 1234 );
        smtp.setSystemEmailAddress( "system@nexus.org" );
        smtp.setUsername( "smtp-username" );

        newc.setSmtpConfiguration( smtp );

        CSecurity security = new CSecurity();

        // someone already had security enabled
        security.setEnabled( oldc.getSecurity().isEnabled() );
        security.setAnonymousAccessEnabled( oldc.getSecurity().isAnonymousAccessEnabled() );
        security.addRealm( PlexusConstants.PLEXUS_DEFAULT_HINT );

        // Add the new config file
        security.setConfigurationFile( "${runtime}/apps/nexus/conf/security.xml" );

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

                    newItem
                        .setGroupId( org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem.ALL_GROUPS );

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
            getLogger().info( "Found " + tasksConfig.getTasks().size() + " tasks in tasks.xml, migrating them..." );

            List<org.sonatype.nexus.configuration.model.v1_0_3.CScheduledTask> oldTasks = tasksConfig.getTasks();

            for ( org.sonatype.nexus.configuration.model.v1_0_3.CScheduledTask oldTask : oldTasks )
            {
                CScheduledTask task = new CScheduledTask();

                task.setId( oldTask.getId() );

                task.setName( oldTask.getName() );

                task.setEnabled( oldTask.isEnabled() );

                task.setLastRun( oldTask.getLastRun() );

                task.setNextRun( oldTask.getNextRun() );

                task.setStatus( oldTask.getStatus() );

                task.setType( oldTask.getType() );

                List<org.sonatype.nexus.configuration.model.CProps> oldProps = oldTask.getProperties();

                for ( org.sonatype.nexus.configuration.model.CProps oldProp : oldProps )
                {
                    CProps prop = new CProps();

                    prop.setKey( oldProp.getKey() );

                    prop.setValue( oldProp.getValue() );

                    task.addProperty( prop );
                }

                CScheduleConfig scheduleConfig = new CScheduleConfig();

                if ( oldTask.getSchedule() == null )
                {
                    // Manual is null
                    scheduleConfig.setType( CScheduleConfig.TYPE_MANUAL );
                }
                else if ( org.sonatype.nexus.configuration.model.v1_0_3.CAdvancedSchedule.class
                    .isAssignableFrom( oldTask.getSchedule().getClass() ) )
                {
                    CAdvancedSchedule s = new CAdvancedSchedule();

                    s.setCronCommand( ( (org.sonatype.nexus.configuration.model.v1_0_3.CAdvancedSchedule) oldTask
                        .getSchedule() ).getCronCommand() );

                    scheduleConfig.setType( CScheduleConfig.TYPE_ADVANCED );

                    scheduleConfig.setCronCommand( s.getCronCommand() );
                }
                else if ( org.sonatype.nexus.configuration.model.v1_0_3.CRunNowSchedule.class.isAssignableFrom( oldTask
                    .getSchedule().getClass() ) )
                {
                    CRunNowSchedule s = new CRunNowSchedule();

                    scheduleConfig.setType( CScheduleConfig.TYPE_RUN_NOW );
                }
                else if ( org.sonatype.nexus.configuration.model.v1_0_3.CMonthlySchedule.class
                    .isAssignableFrom( oldTask.getSchedule().getClass() ) )
                {
                    CMonthlySchedule s = new CMonthlySchedule();

                    s.setStartDate( ( (org.sonatype.nexus.configuration.model.v1_0_3.CMonthlySchedule) oldTask
                        .getSchedule() ).getStartDate() );

                    s.setEndDate( ( (org.sonatype.nexus.configuration.model.v1_0_3.CMonthlySchedule) oldTask
                        .getSchedule() ).getEndDate() );

                    s.setDaysOfMonth( ( (org.sonatype.nexus.configuration.model.v1_0_3.CMonthlySchedule) oldTask
                        .getSchedule() ).getDaysOfMonth() );

                    scheduleConfig.setType( CScheduleConfig.TYPE_MONTHLY );

                    scheduleConfig.setStartDate( s.getStartDate() );

                    scheduleConfig.setEndDate( s.getEndDate() );

                    if ( s.getDaysOfMonth().size() > 0 )
                    {
                        scheduleConfig.setDaysOfMonth( s.getDaysOfMonth() );
                    }
                }
                else if ( org.sonatype.nexus.configuration.model.v1_0_3.CWeeklySchedule.class.isAssignableFrom( oldTask
                    .getSchedule().getClass() ) )
                {
                    CWeeklySchedule s = new CWeeklySchedule();

                    s.setStartDate( ( (org.sonatype.nexus.configuration.model.v1_0_3.CWeeklySchedule) oldTask
                        .getSchedule() ).getStartDate() );

                    s.setEndDate( ( (org.sonatype.nexus.configuration.model.v1_0_3.CWeeklySchedule) oldTask
                        .getSchedule() ).getEndDate() );

                    s.setDaysOfWeek( ( (org.sonatype.nexus.configuration.model.v1_0_3.CWeeklySchedule) oldTask
                        .getSchedule() ).getDaysOfWeek() );

                    scheduleConfig.setType( CScheduleConfig.TYPE_WEEKLY );

                    scheduleConfig.setStartDate( s.getStartDate() );

                    scheduleConfig.setEndDate( s.getEndDate() );

                    if ( s.getDaysOfWeek().size() > 0 )
                    {
                        scheduleConfig.setDaysOfWeek( s.getDaysOfWeek() );
                    }
                }
                else if ( org.sonatype.nexus.configuration.model.v1_0_3.CDailySchedule.class.isAssignableFrom( oldTask
                    .getSchedule().getClass() ) )
                {
                    CDailySchedule s = new CDailySchedule();

                    s.setStartDate( ( (org.sonatype.nexus.configuration.model.v1_0_3.CDailySchedule) oldTask
                        .getSchedule() ).getStartDate() );

                    s.setEndDate( ( (org.sonatype.nexus.configuration.model.v1_0_3.CDailySchedule) oldTask
                        .getSchedule() ).getEndDate() );

                    scheduleConfig.setType( CScheduleConfig.TYPE_DAILY );

                    scheduleConfig.setStartDate( s.getStartDate() );

                    scheduleConfig.setEndDate( s.getEndDate() );
                }
                else if ( org.sonatype.nexus.configuration.model.v1_0_3.COnceSchedule.class.isAssignableFrom( oldTask
                    .getSchedule().getClass() ) )
                {
                    COnceSchedule s = new COnceSchedule();

                    s.setStartDate( ( (org.sonatype.nexus.configuration.model.v1_0_3.COnceSchedule) oldTask
                        .getSchedule() ).getStartDate() );

                    scheduleConfig.setType( CScheduleConfig.TYPE_ONCE );

                    scheduleConfig.setStartDate( s.getStartDate() );
                }

                task.setSchedule( scheduleConfig );

                newc.addTask( task );
            }
        }

        message.setModelVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        message.setConfiguration( newc );

        // extra step, remove old tasks.xml
        // tasksFile would be null if the upgrade chain started from something older then 1.0.3
        if ( tasksFile != null && tasksFile.exists() )
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
