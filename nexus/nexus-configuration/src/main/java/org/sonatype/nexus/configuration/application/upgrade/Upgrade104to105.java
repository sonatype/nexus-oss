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
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.CGroupsSetting;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
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
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CScheduleConfig;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.CSecurity;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.model.v1_0_4.Configuration;
import org.sonatype.nexus.configuration.model.v1_0_4.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.nexus.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.upgrade.Upgrader;

/**
 * Upgrades configuration model from version 1.0.4 to 1.0.5.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.configuration.upgrade.Upgrader" role-hint="1.0.4"
 */
public class Upgrade104to105
    extends AbstractLogEnabled
    implements Upgrader
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
        org.sonatype.nexus.configuration.model.Configuration newc = new org.sonatype.nexus.configuration.model.Configuration();

        newc.setVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
        //Working & log directories removed in this revision
        //newc.setWorkingDirectory( oldc.getWorkingDirectory() );
        //newc.setApplicationLogDirectory( oldc.getApplicationLogDirectory() );
        
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

        message.setModelVersion( org.sonatype.nexus.configuration.model.Configuration.MODEL_VERSION );
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
    
    protected CSmtpConfiguration copyCSmtpConfiguration1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CSmtpConfiguration oldsmtp )
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
    
    protected CRestApiSettings copyCRestApi1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CRestApiSettings oldrestapi )
    {
        CRestApiSettings restapi = new CRestApiSettings();
        
        if ( oldrestapi != null )
        {
            restapi.setAccessAllowedFrom( oldrestapi.getAccessAllowedFrom() );
            restapi.setBaseUrl( oldrestapi.getBaseUrl() );
        }
        
        return restapi;
    }   
    
    protected CHttpProxySettings copyCHttpProxySettings1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CHttpProxySettings oldproxy )
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
    
    protected CRepositoryTarget copyCRepositoryTarget1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryTarget oldtarget )
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
    
    protected CScheduledTask copyCScheduledTask1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CScheduledTask oldtask )
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
            task.setProperties( copyCProps1_0_4( (List<org.sonatype.nexus.configuration.model.v1_0_4.CProps>)oldtask.getProperties() ) );
            task.setSchedule( copyCScheduleConfig1_0_4( oldtask.getSchedule() ) );
        }
        
        return task;
    }
    
    protected CScheduleConfig copyCScheduleConfig1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CScheduleConfig oldschedule )
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
        }
        
        return schedule;
    }
    
    protected CRepositoryShadow copyCRepositoryShadow1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryShadow oldshadow )
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
    
    protected CGroupsSettingPathMappingItem copyCGroupsSettingPathMappingItem1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CGroupsSettingPathMappingItem oldpathmapping )
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
    
    protected CRepositoryGroup copyCRepositoryGroup1_0_4( org.sonatype.nexus.configuration.model.v1_0_4.CRepositoryGroup oldgroup )
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
