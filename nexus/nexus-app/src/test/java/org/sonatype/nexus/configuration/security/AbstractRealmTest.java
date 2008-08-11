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
package org.sonatype.nexus.configuration.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.RejectedExecutionException;

import junit.framework.TestCase;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.jsecurity.authz.Permission;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.security.source.SecurityConfigurationSource;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.util.StringDigester;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.scheduling.schedules.Schedule;

public abstract class AbstractRealmTest
    extends TestCase
{
    protected NexusRealm realm;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        MockNexus nexus = new MockNexus();
        ArrayList<Repository> repositories = new ArrayList<Repository>();
        repositories.add( new MockRepository( "central" ) );
        repositories.add( new MockRepository( "myRepository" ) );
        nexus.repositoryGroups.put( "myGroup", repositories );

        MockMutableNexusSecurityConfiguration securityConfiguration = new MockMutableNexusSecurityConfiguration();

        CUser jason = new CUser();
        jason.setUserId( "jason" );
        jason.setPassword( StringDigester.getSha1Digest( "nosaj" ) );
        jason.addRole( "maven-committer" );
        jason.setStatus( CUser.STATUS_ACTIVE );
        securityConfiguration.createUser( jason );

        CUser dain = new CUser();
        dain.setUserId( "dain" );
        dain.setPassword( StringDigester.getSha1Digest( "niad" ) );
        dain.addRole( "maven-user" );
        dain.setStatus( CUser.STATUS_ACTIVE );
        securityConfiguration.createUser( dain );

        /*CUser locked = new CUser();
        locked.setUserId( "locked" );
        locked.setPassword( StringDigester.getSha1Digest( "locked" ) );
        locked.addRole( "maven-user" );
        locked.setStatus( CUser.STATUS_LOCKED );
        securityConfiguration.createUser( locked );*/

        CUser disabled = new CUser();
        disabled.setUserId( "disabled" );
        disabled.setPassword( StringDigester.getSha1Digest( "disabled" ) );
        disabled.addRole( "maven-user" );
        disabled.setStatus( CUser.STATUS_DISABLED );
        securityConfiguration.createUser( disabled );

        /*CUser expired = new CUser();
        expired.setUserId( "expired" );
        expired.setPassword( StringDigester.getSha1Digest( "expired" ) );
        expired.addRole( "maven-user" );
        expired.setStatus( CUser.STATUS_EXPIRED );
        securityConfiguration.createUser( expired );*/

        CUser illegalStatus = new CUser();
        illegalStatus.setUserId( "illegalStatus" );
        illegalStatus.setPassword( StringDigester.getSha1Digest( "illegalStatus" ) );
        illegalStatus.addRole( "maven-user" );
        illegalStatus.setStatus( "some completely flakey status" );
        securityConfiguration.createUser( illegalStatus );

        CRole mavenCommitter = new CRole();
        mavenCommitter.setId( "maven-committer" );
        mavenCommitter.addRole( "maven-user" );
        mavenCommitter.addPrivilege( "create-maven" );
        mavenCommitter.addPrivilege( "admin-users" );
        mavenCommitter.addPrivilege( "create-repository" );
        securityConfiguration.createRole( mavenCommitter );

        CRole mavenUser = new CRole();
        mavenUser.setId( "maven-user" );
        mavenUser.addPrivilege( "read-maven" );
        mavenUser.addPrivilege( "read-repository" );
        securityConfiguration.createRole( mavenUser );

        CRepoTargetPrivilege createMaven = new CRepoTargetPrivilege();
        createMaven.setId( "create-maven" );
        createMaven.setMethod( "CREATE" );
        createMaven.setRepositoryTargetId( "maven" );
        createMaven.setGroupId( "myGroup" );
        securityConfiguration.createRepoTargetPrivilege( createMaven );

        CRepoTargetPrivilege readMaven = new CRepoTargetPrivilege();
        readMaven.setId( "read-maven" );
        readMaven.setMethod( "READ" );
        readMaven.setRepositoryTargetId( "maven" );
        readMaven.setGroupId( "myGroup" );
        securityConfiguration.createRepoTargetPrivilege( readMaven );

        CApplicationPrivilege createRepository = new CApplicationPrivilege();
        createRepository.setId( "create-repository" );
        createRepository.setMethod( "CREATE" );
        createRepository.setPermission( "nexus:repository" );
        securityConfiguration.createApplicationPrivilege( createRepository );

        CApplicationPrivilege readRepository = new CApplicationPrivilege();
        readRepository.setId( "read-repository" );
        readRepository.setMethod( "READ" );
        readRepository.setPermission( "nexus:repository" );
        securityConfiguration.createApplicationPrivilege( readRepository );

        CApplicationPrivilege adminUsers = new CApplicationPrivilege();
        adminUsers.setId( "admin-users" );
        adminUsers.setPermission( "nexus:user" );
        securityConfiguration.createApplicationPrivilege( adminUsers );

        realm = new NexusRealm();
        realm.setName( "Nexus" );
        realm.setNexus( nexus );
        realm.setSecurityConfiguration( securityConfiguration );
    }

    public static void assertImplied( Permission testPermission, Permission assignedPermission )
    {
        assertTrue( assignedPermission.implies( testPermission ) );
    }

    public static void assertNotImplied( Permission testPermission, Permission assignedPermission )
    {
        assertFalse( assignedPermission.implies( testPermission ) );
    }

    public static void assertImplied( Permission testPermission, Collection<Permission> assignedPermissions )
    {
        for ( Permission assignedPermission : assignedPermissions )
        {
            if ( assignedPermission.implies( testPermission ) )
            {
                return;
            }
        }
        fail( "Expected " + testPermission + " to be implied by " + assignedPermissions );
    }

    public static void assertNotImplied( Permission testPermission, Collection<Permission> assignedPermissions )
    {
        for ( Permission assignedPermission : assignedPermissions )
        {
            if ( assignedPermission.implies( testPermission ) )
            {
                fail( "Expected " + testPermission + " not to be implied by " + assignedPermission );
            }
        }
    }

    public static class MockMutableNexusSecurityConfiguration
        implements NexusSecurityConfiguration
    {
        public final Map<String, CUser> users = new TreeMap<String, CUser>();

        public final Map<String, CRole> roles = new TreeMap<String, CRole>();

        public final Map<String, CRepoTargetPrivilege> repoTargetPrivileges = new TreeMap<String, CRepoTargetPrivilege>();

        public final Map<String, CApplicationPrivilege> applicationPrivileges = new TreeMap<String, CApplicationPrivilege>();

        public CUser readUser( String id )
            throws NoSuchUserException
        {
            CUser user = users.get( id );
            if ( user == null )
            {
                throw new NoSuchUserException( id );
            }
            return user;
        }

        public void createUser( CUser user )
            throws ConfigurationException
        {
            if ( user.getUserId() == null )
            {
                throw new ConfigurationException( "id is null" );
            }
            users.put( user.getUserId(), user );
        }

        public CRole readRole( String id )
            throws NoSuchRoleException
        {
            CRole role = roles.get( id );
            if ( role == null )
            {
                throw new NoSuchRoleException( id );
            }
            return role;
        }

        public void createRole( CRole role )
            throws ConfigurationException
        {
            if ( role.getId() == null )
            {
                throw new ConfigurationException( "id is null" );
            }
            roles.put( role.getId(), role );
        }

        public CApplicationPrivilege readApplicationPrivilege( String id )
            throws NoSuchPrivilegeException
        {
            CApplicationPrivilege applicationPrivilege = applicationPrivileges.get( id );
            if ( applicationPrivilege == null )
            {
                throw new NoSuchPrivilegeException( id );
            }
            return applicationPrivilege;
        }

        public void createApplicationPrivilege( CApplicationPrivilege applicationPrivilege )
            throws ConfigurationException
        {
            if ( applicationPrivilege.getId() == null )
            {
                throw new ConfigurationException( "id is null" );
            }
            applicationPrivileges.put( applicationPrivilege.getId(), applicationPrivilege );
        }

        public CRepoTargetPrivilege readRepoTargetPrivilege( String id )
            throws NoSuchPrivilegeException
        {
            CRepoTargetPrivilege targetPrivilege = repoTargetPrivileges.get( id );
            if ( targetPrivilege == null )
            {
                throw new NoSuchPrivilegeException( id );
            }
            return targetPrivilege;
        }

        public void createRepoTargetPrivilege( CRepoTargetPrivilege targetPrivilege )
            throws ConfigurationException
        {
            if ( targetPrivilege.getId() == null )
            {
                throw new ConfigurationException( "id is null" );
            }
            repoTargetPrivileges.put( targetPrivilege.getId(), targetPrivilege );
        }

        // Unimplemented methods
        public void changePassword( String userId, String oldPassword, String newPassword )
            throws IOException,
                NoSuchUserException,
                InvalidCredentialsException
        {
            throw new UnsupportedOperationException();
        }

        public void resetPassword( String id )
            throws IOException,
                NoSuchUserException
        {
            throw new UnsupportedOperationException();
        }

        public void forgotPassword( String userId, String email )
            throws IOException,
                NoSuchUserException,
                NoSuchEmailException
        {
            throw new UnsupportedOperationException();
        }

        public void forgotUserId( String email )
            throws IOException,
                NoSuchEmailException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CUser> listUsers()
        {
            throw new UnsupportedOperationException();
        }

        public void updateUser( CUser settings )
            throws ConfigurationException,
                NoSuchUserException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteUser( String id )
            throws IOException,
                NoSuchUserException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CRole> listRoles()
        {
            throw new UnsupportedOperationException();
        }

        public void updateRole( CRole settings )
            throws ConfigurationException,
                NoSuchRoleException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteRole( String id )
            throws IOException,
                NoSuchRoleException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CApplicationPrivilege> listApplicationPrivileges()
        {
            throw new UnsupportedOperationException();
        }

        public void updateApplicationPrivilege( CApplicationPrivilege settings )
            throws ConfigurationException,
                NoSuchPrivilegeException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteApplicationPrivilege( String id )
            throws IOException,
                NoSuchPrivilegeException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CRepoTargetPrivilege> listRepoTargetPrivileges()
        {
            throw new UnsupportedOperationException();
        }

        public void updateRepoTargetPrivilege( CRepoTargetPrivilege settings )
            throws ConfigurationException,
                NoSuchPrivilegeException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteRepoTargetPrivilege( String id )
            throws IOException,
                NoSuchPrivilegeException
        {
            throw new UnsupportedOperationException();
        }

        public void applyConfiguration()
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public InputStream getConfigurationAsStream()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public SecurityConfigurationSource getConfigurationSource()
        {
            throw new UnsupportedOperationException();
        }

        public boolean isConfigurationDefaulted()
        {
            throw new UnsupportedOperationException();
        }

        public boolean isConfigurationUpgraded()
        {
            throw new UnsupportedOperationException();
        }

        public boolean isInstanceUpgraded()
        {
            throw new UnsupportedOperationException();
        }

        public void loadConfiguration()
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void loadConfiguration( boolean forceReload )
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public Configuration getConfiguration()
        {
            throw new UnsupportedOperationException();
        }

        public void addConfigurationChangeListener( ConfigurationChangeListener listener )
        {
            throw new UnsupportedOperationException();
        }

        public void notifyConfigurationChangeListeners()
        {
            throw new UnsupportedOperationException();
        }

        public void notifyConfigurationChangeListeners( ConfigurationChangeEvent evt )
        {
            throw new UnsupportedOperationException();
        }

        public void removeConfigurationChangeListener( ConfigurationChangeListener listener )
        {
            throw new UnsupportedOperationException();
        }

        public void saveConfiguration()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void startService()
            throws StartingException
        {
        }

        public void stopService()
            throws StoppingException
        {
        }
    }

    public static class MockNexus
        implements Nexus
    {
        public final Map<String, List<Repository>> repositoryGroups = new TreeMap<String, List<Repository>>();

        public List<Repository> getRepositoryGroup( String repoGroupId )
            throws NoSuchRepositoryGroupException
        {
            return repositoryGroups.get( repoGroupId );
        }

        // Unimplemented methods
        public SystemStatus getSystemStatus()
        {
            throw new UnsupportedOperationException();
        }

        public boolean setState( SystemState state )
        {
            throw new UnsupportedOperationException();
        }

        public NexusConfiguration getNexusConfiguration()
        {
            throw new UnsupportedOperationException();
        }

        public CSmtpConfiguration readDefaultSmtpConfiguration()
        {
            throw new UnsupportedOperationException();
        }

        public Repository getRepository( String repoId )
            throws NoSuchRepositoryException
        {
            throw new UnsupportedOperationException();
        }

        public String getRepositoryGroupType( String repoGroupId )
            throws NoSuchRepositoryGroupException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<Repository> getRepositories()
        {
            throw new UnsupportedOperationException();
        }

        public CSmtpConfiguration readSmtpConfiguration()
        {
            throw new UnsupportedOperationException();
        }

        public void updateSmtpConfiguration( CSmtpConfiguration settings )
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public StorageItem dereferenceLinkItem( StorageItem item )
            throws NoSuchRepositoryException,
                ItemNotFoundException,
                AccessDeniedException,
                RepositoryNotAvailableException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public long getWastebasketItemCount()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public long getWastebasketSize()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void wastebasketPurge()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public InputStream getConfigurationAsStream()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<String> getApplicationLogFiles()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public InputStream getApplicationLogAsStream( String logFile )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void clearAllCaches( String path )
        {
            throw new UnsupportedOperationException();
        }

        public void clearRepositoryCaches( String path, String repositoryId )
            throws NoSuchRepositoryException
        {
            throw new UnsupportedOperationException();
        }

        public void clearRepositoryGroupCaches( String path, String repositoryGroupId )
            throws NoSuchRepositoryGroupException
        {
            throw new UnsupportedOperationException();
        }

        public void reindexAllRepositories( String path )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void reindexRepository( String path, String repositoryId )
            throws NoSuchRepositoryException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void reindexRepositoryGroup( String path, String repositoryGroupId )
            throws NoSuchRepositoryGroupException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void publishAllIndex()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void publishRepositoryIndex( String repositoryId )
            throws IOException,
                NoSuchRepositoryException
        {
            throw new UnsupportedOperationException();
        }

        public void publishRepositoryGroupIndex( String repositoryGroupId )
            throws IOException,
                NoSuchRepositoryGroupException
        {
            throw new UnsupportedOperationException();
        }

        public void rebuildAttributesAllRepositories( String path )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void rebuildAttributesRepository( String path, String repositoryId )
            throws NoSuchRepositoryException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void rebuildAttributesRepositoryGroup( String path, String repositoryGroupId )
            throws NoSuchRepositoryGroupException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<String> evictAllUnusedProxiedItems( long timestamp )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<String> evictRepositoryUnusedProxiedItems( long timestamp, String repositoryId )
            throws NoSuchRepositoryException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<String> evictRepositoryGroupUnusedProxiedItems( long timestamp, String repositoryGroupId )
            throws NoSuchRepositoryGroupException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
            throws NoSuchRepositoryException,
                NoSuchRepositoryGroupException,
                IllegalArgumentException
        {
            throw new UnsupportedOperationException();
        }

        public void addNexusArtifactEvent( NexusArtifactEvent nae )
        {
            throw new UnsupportedOperationException();
        }

        public void addSystemEvent( String action, String message )
        {
            throw new UnsupportedOperationException();
        }

        public SystemProcess systemProcessStarted( String action, String message )
        {
            throw new UnsupportedOperationException();
        }

        public void systemProcessFinished( SystemProcess prc )
        {
            throw new UnsupportedOperationException();
        }

        public void systemProcessBroken( SystemProcess prc, Throwable e )
        {
            throw new UnsupportedOperationException();
        }

        public List<NexusArtifactEvent> getRecentlyStorageChanges()
        {
            throw new UnsupportedOperationException();
        }

        public List<NexusArtifactEvent> getRecentlyDeployedOrCachedArtifacts()
        {
            throw new UnsupportedOperationException();
        }

        public List<NexusArtifactEvent> getRecentlyCachedArtifacts()
        {
            throw new UnsupportedOperationException();
        }

        public List<NexusArtifactEvent> getRecentlyDeployedArtifacts()
        {
            throw new UnsupportedOperationException();
        }

        public List<NexusArtifactEvent> getBrokenArtifacts()
        {
            throw new UnsupportedOperationException();
        }

        public List<SystemEvent> getRepositoryStatusChanges()
        {
            throw new UnsupportedOperationException();
        }

        public List<SystemEvent> getSystemEvents()
        {
            throw new UnsupportedOperationException();
        }

        public <T> void submit( String name, SchedulerTask<T> task )
            throws RejectedExecutionException,
                NullPointerException
        {
            throw new UnsupportedOperationException();
        }

        public <T> ScheduledTask<T> schedule( String name, SchedulerTask<T> nexusTask, Schedule schedule )
            throws RejectedExecutionException,
                NullPointerException
        {
            throw new UnsupportedOperationException();
        }

        public <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
            throws RejectedExecutionException,
                NullPointerException
        {
            throw new UnsupportedOperationException();
        }

        public Map<Class<?>, List<ScheduledTask<?>>> getAllTasks()
        {
            throw new UnsupportedOperationException();
        }

        public Map<Class<?>, List<ScheduledTask<?>>> getActiveTasks()
        {
            throw new UnsupportedOperationException();
        }

        public ScheduledTask<?> getTaskById( String id )
            throws NoSuchTaskException
        {
            throw new UnsupportedOperationException();
        }

        public SchedulerTask<?> createTaskInstance( String taskType )
            throws IllegalArgumentException
        {
            throw new UnsupportedOperationException();
        }

        public SchedulerTask<?> createTaskInstance( Class<?> taskType )
            throws IllegalArgumentException
        {
            throw new UnsupportedOperationException();
        }

        public boolean isDefaultSecurityEnabled()
        {
            throw new UnsupportedOperationException();
        }

        public boolean isDefaultAnonymousAccessEnabled()
        {
            throw new UnsupportedOperationException();
        }

        public InputStream getDefaultConfigurationAsStream()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public String readDefaultWorkingDirectory()
        {
            throw new UnsupportedOperationException();
        }

        public String readDefaultApplicationLogDirectory()
        {
            throw new UnsupportedOperationException();
        }

        public CRemoteConnectionSettings readDefaultGlobalRemoteConnectionSettings()
        {
            throw new UnsupportedOperationException();
        }

        public CRemoteHttpProxySettings readDefaultGlobalRemoteHttpProxySettings()
        {
            throw new UnsupportedOperationException();
        }

        public CRouting readDefaultRouting()
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CRepository> listRepositoryTemplates()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void createRepositoryTemplate( CRepository settings )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public CRepository readRepositoryTemplate( String id )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void updateRepositoryTemplate( CRepository settings )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteRepositoryTemplate( String id )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CRepositoryShadow> listRepositoryShadowTemplates()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void createRepositoryShadowTemplate( CRepositoryShadow settings )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public CRepositoryShadow readRepositoryShadowTemplate( String id )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void updateRepositoryShadowTemplate( CRepositoryShadow settings )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteRepositoryShadowTemplate( String id )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public ArtifactInfo identifyArtifact( String type, String checksum )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public FlatSearchResponse searchArtifactFlat( String term, String repositoryId, String groupId, Integer from,
            Integer count )
        {
            throw new UnsupportedOperationException();
        }

        public FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String cTerm,
            String repositoryId, String groupId, Integer from, Integer count )
        {
            throw new UnsupportedOperationException();
        }

        public boolean isSecurityEnabled()
        {
            throw new UnsupportedOperationException();
        }

        public boolean isAnonymousAccessEnabled()
        {
            throw new UnsupportedOperationException();
        }

        public Collection<ContentClass> listRepositoryContentClasses()
        {
            throw new UnsupportedOperationException();
        }

        public String getBaseUrl()
        {
            throw new UnsupportedOperationException();
        }

        public void setBaseUrl( String baseUrl )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public String readWorkingDirectory()
        {
            throw new UnsupportedOperationException();
        }

        public void updateWorkingDirectory( String settings )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public String readApplicationLogDirectory()
        {
            throw new UnsupportedOperationException();
        }

        public void updateApplicationLogDirectory( String settings )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public CRemoteConnectionSettings readGlobalRemoteConnectionSettings()
        {
            throw new UnsupportedOperationException();
        }

        public void updateGlobalRemoteConnectionSettings( CRemoteConnectionSettings settings )
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void createGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public CRemoteHttpProxySettings readGlobalRemoteHttpProxySettings()
        {
            throw new UnsupportedOperationException();
        }

        public void updateGlobalRemoteHttpProxySettings( CRemoteHttpProxySettings settings )
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteGlobalRemoteHttpProxySettings()
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public CRouting readRouting()
        {
            throw new UnsupportedOperationException();
        }

        public void updateRouting( CRouting settings )
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CRepository> listRepositories()
        {
            throw new UnsupportedOperationException();
        }

        public void createRepository( CRepository settings )
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public CRepository readRepository( String id )
            throws NoSuchRepositoryException
        {
            throw new UnsupportedOperationException();
        }

        public void updateRepository( CRepository settings )
            throws NoSuchRepositoryException,
                ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteRepository( String id )
            throws NoSuchRepositoryException,
                IOException,
                ConfigurationException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CRepositoryShadow> listRepositoryShadows()
        {
            throw new UnsupportedOperationException();
        }

        public void createRepositoryShadow( CRepositoryShadow settings )
            throws ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public CRepositoryShadow readRepositoryShadow( String id )
            throws NoSuchRepositoryException
        {
            throw new UnsupportedOperationException();
        }

        public void updateRepositoryShadow( CRepositoryShadow settings )
            throws NoSuchRepositoryException,
                ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteRepositoryShadow( String id )
            throws NoSuchRepositoryException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CGroupsSettingPathMappingItem> listGroupsSettingPathMapping()
        {
            throw new UnsupportedOperationException();
        }

        public void createGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
            throws NoSuchRepositoryException,
                ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public CGroupsSettingPathMappingItem readGroupsSettingPathMapping( String id )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void updateGroupsSettingPathMapping( CGroupsSettingPathMappingItem settings )
            throws NoSuchRepositoryException,
                ConfigurationException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteGroupsSettingPathMapping( String id )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CRepositoryGroup> listRepositoryGroups()
        {
            throw new UnsupportedOperationException();
        }

        public void createRepositoryGroup( CRepositoryGroup settings )
            throws NoSuchRepositoryException,
                InvalidGroupingException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public CRepositoryGroup readRepositoryGroup( String id )
            throws NoSuchRepositoryGroupException
        {
            throw new UnsupportedOperationException();
        }

        public void updateRepositoryGroup( CRepositoryGroup settings )
            throws NoSuchRepositoryException,
                NoSuchRepositoryGroupException,
                InvalidGroupingException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteRepositoryGroup( String id )
            throws NoSuchRepositoryGroupException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CRepositoryTarget> listRepositoryTargets()
        {
            throw new UnsupportedOperationException();
        }

        public void createRepositoryTarget( CRepositoryTarget settings )
            throws IllegalArgumentException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public CRepositoryTarget readRepositoryTarget( String id )
        {
            throw new UnsupportedOperationException();
        }

        public void updateRepositoryTarget( CRepositoryTarget settings )
            throws IllegalArgumentException,
                IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteRepositoryTarget( String id )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<CRemoteNexusInstance> listRemoteNexusInstances()
        {
            throw new UnsupportedOperationException();
        }

        public CRemoteNexusInstance readRemoteNexusInstance( String alias )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void createRemoteNexusInstance( CRemoteNexusInstance settings )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteRemoteNexusInstance( String alias )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public String getAnonymousPassword()
        {
            throw new UnsupportedOperationException();
        }

        public String getAnonymousUsername()
        {
            throw new UnsupportedOperationException();
        }

        public List<String> getRealms()
        {
            throw new UnsupportedOperationException();
        }

        public void setAnonymousAccessEnabled( boolean enabled )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void setAnonymousPassword( String val )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void setAnonymousUsername( String val )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public void setSecurityEnabled( boolean enabled )
            throws IOException
        {
            throw new UnsupportedOperationException();
        }

        public String getDefaultAnonymousPassword()
        {
            throw new UnsupportedOperationException();
        }

        public String getDefaultAnonymousUsername()
        {
            throw new UnsupportedOperationException();
        }

        public List<String> getDefaultRealms()
        {
            throw new UnsupportedOperationException();
        }

        public RepositoryRouter getRootRouter()
        {
            throw new UnsupportedOperationException();
        }

        public FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, String groupId,
            Integer from, Integer count )
        {
            throw new UnsupportedOperationException();
        }

        public StorageItem dereferenceLinkItem( StorageLinkItem item )
            throws NoSuchResourceStoreException,
                ItemNotFoundException,
                AccessDeniedException,
                RepositoryNotAvailableException,
                StorageException
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public static class MockRepository
        implements Repository
    {
        private String id;

        public MockRepository( String id )
        {
            this.id = id;
        }

        public String getId()
        {
            return id;
        }

        //
        // Unimplemented methods
        //
        public void setId( String id )
        {
            throw new UnsupportedOperationException();
        }

        public ContentClass getRepositoryContentClass()
        {
            throw new UnsupportedOperationException();
        }

        public RepositoryType getRepositoryType()
        {
            throw new UnsupportedOperationException();
        }

        public String getName()
        {
            throw new UnsupportedOperationException();
        }

        public void setName( String name )
        {
            throw new UnsupportedOperationException();
        }

        public int getNotFoundCacheTimeToLive()
        {
            throw new UnsupportedOperationException();
        }

        public void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive )
        {
            throw new UnsupportedOperationException();
        }

        public PathCache getNotFoundCache()
        {
            throw new UnsupportedOperationException();
        }

        public void setNotFoundCache( PathCache notFoundcache )
        {
            throw new UnsupportedOperationException();
        }

        public void addToNotFoundCache( String path )
        {
            throw new UnsupportedOperationException();
        }

        public void removeFromNotFoundCache( String path )
        {
            throw new UnsupportedOperationException();
        }

        public int getItemMaxAge()
        {
            throw new UnsupportedOperationException();
        }

        public void setItemMaxAge( int itemMaxAge )
        {
            throw new UnsupportedOperationException();
        }

        public LocalStatus getLocalStatus()
        {
            throw new UnsupportedOperationException();
        }

        public void setLocalStatus( LocalStatus val )
        {
            throw new UnsupportedOperationException();
        }

        public RemoteStatus getRemoteStatus( boolean forceCheck )
        {
            throw new UnsupportedOperationException();
        }

        public ProxyMode getProxyMode()
        {
            throw new UnsupportedOperationException();
        }

        public void setProxyMode( ProxyMode val )
        {
            throw new UnsupportedOperationException();
        }

        public boolean isBrowseable()
        {
            throw new UnsupportedOperationException();
        }

        public void setBrowseable( boolean val )
        {
            throw new UnsupportedOperationException();
        }

        public boolean isAllowWrite()
        {
            throw new UnsupportedOperationException();
        }

        public void setAllowWrite( boolean val )
        {
            throw new UnsupportedOperationException();
        }

        public boolean isIndexable()
        {
            throw new UnsupportedOperationException();
        }

        public void setIndexable( boolean val )
        {
            throw new UnsupportedOperationException();
        }

        public String getLocalUrl()
        {
            throw new UnsupportedOperationException();
        }

        public void setLocalUrl( String url )
        {
            throw new UnsupportedOperationException();
        }

        public String getRemoteUrl()
        {
            throw new UnsupportedOperationException();
        }

        public void setRemoteUrl( String url )
        {
            throw new UnsupportedOperationException();
        }

        public RemoteStorageContext getRemoteStorageContext()
        {
            throw new UnsupportedOperationException();
        }

        public void setRemoteStorageContext( RemoteStorageContext ctx )
        {
            throw new UnsupportedOperationException();
        }

        public void clearCaches( String path )
        {
            throw new UnsupportedOperationException();
        }

        public Collection<String> evictUnusedItems( long timestamp )
        {
            throw new UnsupportedOperationException();
        }

        public boolean isRemoteStorageReachable()
        {
            throw new UnsupportedOperationException();
        }

        public boolean recreateAttributes( String path, Map<String, String> initialData )
        {
            throw new UnsupportedOperationException();
        }

        public AccessManager getAccessManager()
        {
            throw new UnsupportedOperationException();
        }

        public void setAccessManager( AccessManager accessManager )
        {
            throw new UnsupportedOperationException();
        }

        public LocalRepositoryStorage getLocalStorage()
        {
            throw new UnsupportedOperationException();
        }

        public void setLocalStorage( LocalRepositoryStorage storage )
        {
            throw new UnsupportedOperationException();
        }

        public RemoteRepositoryStorage getRemoteStorage()
        {
            throw new UnsupportedOperationException();
        }

        public void setRemoteStorage( RemoteRepositoryStorage storage )
        {
            throw new UnsupportedOperationException();
        }

        public InputStream retrieveItemContent( RepositoryItemUid uid )
            throws RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public StorageItem retrieveItem( boolean localOnly, RepositoryItemUid uid )
            throws RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public void copyItem( RepositoryItemUid from, RepositoryItemUid to )
            throws UnsupportedStorageOperationException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public void moveItem( RepositoryItemUid from, RepositoryItemUid to )
            throws UnsupportedStorageOperationException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteItem( RepositoryItemUid uid )
            throws UnsupportedStorageOperationException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public void storeItem( AbstractStorageItem item )
            throws UnsupportedStorageOperationException,
                RepositoryNotAvailableException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<StorageItem> list( RepositoryItemUid uid, Map<String, Object> context )
            throws RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<StorageItem> list( StorageCollectionItem item )
            throws RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public void addProximityEventListener( EventListener listener )
        {
            throw new UnsupportedOperationException();
        }

        public void removeProximityEventListener( EventListener listener )
        {
            throw new UnsupportedOperationException();
        }

        public void notifyProximityEventListeners( AbstractEvent evt )
        {
            throw new UnsupportedOperationException();
        }

        public StorageItem retrieveItem( ResourceStoreRequest request )
            throws NoSuchResourceStoreException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException,
                AccessDeniedException
        {
            throw new UnsupportedOperationException();
        }

        public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
            throws UnsupportedStorageOperationException,
                NoSuchResourceStoreException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException,
                AccessDeniedException
        {
            throw new UnsupportedOperationException();
        }

        public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
            throws UnsupportedStorageOperationException,
                NoSuchResourceStoreException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException,
                AccessDeniedException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteItem( ResourceStoreRequest request )
            throws UnsupportedStorageOperationException,
                NoSuchResourceStoreException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException,
                AccessDeniedException
        {
            throw new UnsupportedOperationException();
        }

        public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
            throws UnsupportedStorageOperationException,
                NoSuchResourceStoreException,
                RepositoryNotAvailableException,
                StorageException,
                AccessDeniedException
        {
            throw new UnsupportedOperationException();
        }

        public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
            throws UnsupportedStorageOperationException,
                NoSuchResourceStoreException,
                RepositoryNotAvailableException,
                StorageException,
                AccessDeniedException
        {
            throw new UnsupportedOperationException();
        }

        public Collection<StorageItem> list( ResourceStoreRequest request )
            throws NoSuchResourceStoreException,
                RepositoryNotAvailableException,
                RepositoryNotListableException,
                ItemNotFoundException,
                StorageException,
                AccessDeniedException
        {
            throw new UnsupportedOperationException();
        }

        public void onConfigurationChange( ConfigurationChangeEvent evt )
        {
            throw new UnsupportedOperationException();
        }

        public TargetSet getTargetsForRequest( ResourceStoreRequest request )
        {
            throw new UnsupportedOperationException();
        }

        public TargetSet getTargetsForRequest( RepositoryItemUid uid )
        {
            throw new UnsupportedOperationException();
        }

        public void copyItem( RepositoryItemUid from, RepositoryItemUid to, Map<String, Object> context )
            throws UnsupportedStorageOperationException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public void deleteItem( RepositoryItemUid uid, Map<String, Object> context )
            throws UnsupportedStorageOperationException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public TargetSet getTargetsForRequest( RepositoryItemUid uid, Map<String, Object> context )
        {
            throw new UnsupportedOperationException();
        }

        public void moveItem( RepositoryItemUid from, RepositoryItemUid to, Map<String, Object> context )
            throws UnsupportedStorageOperationException,
                RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public StorageItem retrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
            throws RepositoryNotAvailableException,
                ItemNotFoundException,
                StorageException
        {
            throw new UnsupportedOperationException();
        }

        public RepositoryItemUid createUidForPath( String path )
        {
            throw new UnsupportedOperationException();
        }

        public void release( RepositoryItemUid uid )
        {
            throw new UnsupportedOperationException();
        }
    }
}
