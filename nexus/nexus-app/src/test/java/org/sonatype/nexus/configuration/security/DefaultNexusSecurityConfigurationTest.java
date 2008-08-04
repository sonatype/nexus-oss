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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.io.InputStreamFacade;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.configuration.security.model.Configuration;

public class DefaultNexusSecurityConfigurationTest
    extends AbstractNexusTestCase
{
    protected DefaultNexusSecurityConfiguration securityConfiguration;

    protected void setUp()
        throws Exception
    {
        securityConfiguration = (DefaultNexusSecurityConfiguration) this.lookup( NexusSecurityConfiguration.ROLE );
        securityConfiguration.startService();

        super.setUp();
    }

    protected void tearDown()
        throws Exception
    {
        securityConfiguration.stopService();
        super.tearDown();
    }

    protected boolean loadConfigurationAtSetUp()
    {
        return true;
    }

    public void testSaveConfiguration()
        throws Exception
    {
        securityConfiguration.loadConfiguration();

        Configuration config = securityConfiguration.getConfiguration();

        // the m1 and m2 repo perm
        assertEquals("Expected repoTargetPrivs count", 8, config.getRepositoryTargetPrivileges().size() );

        // admin and anon role
        assertEquals( "Expected roles count", 3, config.getRoles().size() );

        // admin and anon user
        assertEquals( "Expected users count", 3, config.getUsers().size() );

        CRepoTargetPrivilege tarPriv = new CRepoTargetPrivilege();
        tarPriv.setId( "id2" );
        tarPriv.setMethod( CApplicationPrivilege.METHOD_CREATE );
        tarPriv.setName( "name" );
        tarPriv.setRepositoryTargetId( "targetId" );
        tarPriv.setRepositoryId( "repoId" );

        config.addRepositoryTargetPrivilege( tarPriv );

        CRole role = new CRole();
        role.setDescription( "description" );
        role.setId( "id1" );
        role.setName( "name" );
        role.setSessionTimeout( 60 );
        role.addPrivilege( "id2" );

        config.addRole( role );

        CRole role2 = new CRole();
        role2.setDescription( "description" );
        role2.setId( "id2" );
        role2.setName( "name" );
        role2.setSessionTimeout( 60 );
        role2.addPrivilege( "id2" );
        role2.addRole( "id1" );

        config.addRole( role2 );

        CUser user = new CUser();
        user.setEmail( "emailaddress" );
        user.setPassword( "password" );
        user.setName( "name" );
        user.setStatus( CUser.STATUS_ACTIVE );
        user.setUserId( "id" );
        user.addRole( "id1" );
        user.addRole( "id2" );

        config.addUser( user );

        securityConfiguration.saveConfiguration();

        securityConfiguration.loadConfiguration();

        config = securityConfiguration.getConfiguration();

        assertTrue( ( (CRepoTargetPrivilege) config.getRepositoryTargetPrivileges().get( 8 ) ).getId().equals( "id2" ) );
        assertTrue( ( (CRepoTargetPrivilege) config.getRepositoryTargetPrivileges().get( 8 ) ).getMethod().equals(
            CApplicationPrivilege.METHOD_CREATE ) );
        assertTrue( ( (CRepoTargetPrivilege) config.getRepositoryTargetPrivileges().get( 8 ) )
            .getName().equals( "name" ) );
        assertTrue( ( (CRepoTargetPrivilege) config.getRepositoryTargetPrivileges().get( 8 ) )
            .getRepositoryTargetId().equals( "targetId" ) );
        assertTrue( ( (CRepoTargetPrivilege) config.getRepositoryTargetPrivileges().get( 8 ) )
            .getRepositoryId().equals( "repoId" ) );

        assertEquals( "Role count after adding 2", 5, config.getRoles().size() );

        assertTrue( ( (CRole) config.getRoles().get( 3 ) ).getDescription().equals( "description" ) );
        assertTrue( ( (CRole) config.getRoles().get( 3 ) ).getId().equals( "id1" ) );
        assertTrue( ( (CRole) config.getRoles().get( 3 ) ).getName().equals( "name" ) );
        assertTrue( ( (CRole) config.getRoles().get( 3 ) ).getSessionTimeout() == 60 );
        assertTrue( ( (CRole) config.getRoles().get( 3 ) ).getPrivileges().size() == 1 );
        assertTrue( ( (CRole) config.getRoles().get( 3 ) ).getRoles().size() == 0 );

        assertTrue( ( (CRole) config.getRoles().get( 4 ) ).getDescription().equals( "description" ) );
        assertTrue( ( (CRole) config.getRoles().get( 4 ) ).getId().equals( "id2" ) );
        assertTrue( ( (CRole) config.getRoles().get( 4 ) ).getName().equals( "name" ) );
        assertTrue( ( (CRole) config.getRoles().get( 4 ) ).getSessionTimeout() == 60 );
        assertTrue( ( (CRole) config.getRoles().get( 4 ) ).getPrivileges().size() == 1 );
        assertTrue( ( (CRole) config.getRoles().get( 4 ) ).getRoles().size() == 1 );

        assertEquals( "User count after adding one", 4, config.getUsers().size()  );

        assertTrue( ( (CUser) config.getUsers().get( 3 ) ).getEmail().equals( "emailaddress" ) );
        assertTrue( ( (CUser) config.getUsers().get( 3 ) ).getName().equals( "name" ) );
        assertTrue( ( (CUser) config.getUsers().get( 3 ) ).getPassword().equals( "password" ) );
        assertTrue( ( (CUser) config.getUsers().get( 3 ) ).getStatus().equals( CUser.STATUS_ACTIVE ) );
        assertTrue( ( (CUser) config.getUsers().get( 3 ) ).getUserId().equals( "id" ) );
        assertTrue( ( (CUser) config.getUsers().get( 3 ) ).getRoles().size() == 2 );
    }

    public void testLoadConfiguration()
        throws Exception
    {
        // this will create default config
        securityConfiguration.loadConfiguration();

        // get it
        Configuration config = securityConfiguration.getConfiguration();

        CApplicationPrivilege priv = new CApplicationPrivilege();
        priv.setId( "testid" );
        priv.setMethod( "read" );
        priv.setName( "testname" );
        priv.setPermission( "a:test:permission" );

        config.addApplicationPrivilege( priv );

        // save it
        securityConfiguration.saveConfiguration();

        // replace it again with default "from behind"
        InputStreamFacade isf = new InputStreamFacade()
        {

            public InputStream getInputStream()
                throws IOException
            {
                return getClass().getResourceAsStream( "/META-INF/nexus/security.xml" );
            }

        };
        FileUtils.copyStreamToFile( isf, new File( getNexusSecurityConfiguration() ) );

        // force reload
        securityConfiguration.loadConfiguration( true );

        // get the config
        config = securityConfiguration.getConfiguration();

        // it again contains default value, coz we overwritten it before
        for ( CApplicationPrivilege appPriv : (List<CApplicationPrivilege>) config.getApplicationPrivileges() )
        {
            if ( appPriv.getId().equals( "testid" ) )
            {
                fail( "Existing config found, should have been overwritten" );
            }
        }
    }

    public void testGetConfiguration()
        throws Exception
    {
        // Since this component is now startable, configuration is auto-loaded
        // so this is now an invalid test
        // assertEquals( null, securityConfiguration.getConfiguration() );

        securityConfiguration.loadConfiguration();

        assertTrue( securityConfiguration.getConfiguration() != null );
    }

    public void testGetConfigurationAsStream()
        throws Exception
    {
        securityConfiguration.loadConfiguration();

        IOUtil.contentEquals( new FileInputStream( new File( getNexusSecurityConfiguration() ) ), securityConfiguration
            .getConfigurationAsStream() );
    }

    public void testGetDefaultConfigurationAsStream()
        throws Exception
    {
        securityConfiguration.loadConfiguration();

        IOUtil.contentEquals( getClass().getResourceAsStream( "/META-INF/nexus/security.xml" ), securityConfiguration
            .getConfigurationSource().getDefaultsSource().getConfigurationAsStream() );
    }
}
