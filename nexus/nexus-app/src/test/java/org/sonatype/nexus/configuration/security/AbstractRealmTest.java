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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.jsecurity.authz.Permission;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.security.source.SecurityConfigurationSource;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.util.StringDigester;

public abstract class AbstractRealmTest
    extends TestCase
{
    protected NexusRealm realm;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        Repository mockCentral = createMock(Repository.class);
        expect(mockCentral.getId()).andReturn( "central" ).anyTimes();
        
        Repository mockMyRepository = createMock(Repository.class);
        expect(mockMyRepository.getId()).andReturn( "myRepository" ).anyTimes();

        ArrayList<Repository> repositories = new ArrayList<Repository>();
        repositories.add( mockCentral );
        repositories.add( mockMyRepository );

        Nexus mockNexus = createMock(Nexus.class);
        expect( mockNexus.getRepositoryGroup( "myGroup" ) ).andReturn( repositories ).anyTimes();

        replay( mockCentral, mockMyRepository, mockNexus );
        
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
        realm.setNexus( mockNexus );
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

}
