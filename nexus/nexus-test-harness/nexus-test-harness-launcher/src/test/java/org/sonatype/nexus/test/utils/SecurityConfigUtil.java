package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import junit.framework.Assert;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.security.model.io.xpp3.NexusSecurityConfigurationXpp3Reader;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.PrivilegeApplicationStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.UserResource;

public class SecurityConfigUtil
{

    public static void verifyRole( RoleResource role )
        throws IOException
    {
        List<RoleResource> roles = new ArrayList<RoleResource>();
        roles.add( role );
        verifyRoles( roles );
    }

    @SuppressWarnings( "unchecked" )
    public static void verifyRoles( List<RoleResource> roles )
        throws IOException
    {

        for ( Iterator<RoleResource> outterIter = roles.iterator(); outterIter.hasNext(); )
        {
            RoleResource roleResource = outterIter.next();

            CRole secRole = getCRole( roleResource.getId() );
            Assert.assertNotNull( secRole );
            CRole role = RoleConverter.toCRole( roleResource );

            Assert.assertTrue( new RoleComparator().compare( role, secRole ) == 0 );

        }
    }

    public static void verifyUser( UserResource user )
        throws IOException
    {
        List<UserResource> users = new ArrayList<UserResource>();
        users.add( user );
        verifyUsers( users );
    }

    @SuppressWarnings( "unchecked" )
    public static void verifyUsers( List<UserResource> users )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();

        List secUsers = securityConfig.getUsers();

        for ( Iterator<UserResource> outterIter = users.iterator(); outterIter.hasNext(); )
        {
            UserResource userResource = outterIter.next();
            CUser secUser = getCUser( userResource.getUserId() );

            Assert.assertNotNull( secUser );

            CUser user = UserConverter.toCUser( userResource );

            Assert.assertTrue( new UserComparator().compare( user, secUser ) == 0 );

        }
    }

    @SuppressWarnings( "unchecked" )
    public static void verifyRepoTargetPrivileges( List<PrivilegeBaseStatusResource> privs )
        throws IOException
    {

        for ( Iterator<PrivilegeBaseStatusResource> iter = privs.iterator(); iter.hasNext(); )
        {
            PrivilegeBaseStatusResource privResource = iter.next();

            if ( privResource instanceof PrivilegeTargetStatusResource )
            {
                CRepoTargetPrivilege secPriv = getCRepoTargetPrivilege( privResource.getId() );

                Assert.assertNotNull( secPriv );
                PrivilegeTargetStatusResource targetPriv = (PrivilegeTargetStatusResource) privResource;

                Assert.assertEquals( targetPriv.getId(), secPriv.getId() );
                Assert.assertEquals( targetPriv.getName(), secPriv.getName() );
                Assert.assertEquals( targetPriv.getRepositoryGroupId(), secPriv.getGroupId() );
                Assert.assertEquals( targetPriv.getRepositoryId(), secPriv.getRepositoryId() );
                Assert.assertEquals( targetPriv.getRepositoryTargetId(), secPriv.getRepositoryTargetId() );
                Assert.assertEquals( targetPriv.getMethod(), secPriv.getMethod() );
            }
            else if ( privResource instanceof PrivilegeApplicationStatusResource )
            {
                CApplicationPrivilege secPriv = getCApplicationPrivilege( privResource.getId() );

                Assert.assertNotNull( secPriv );
                PrivilegeApplicationStatusResource targetPriv = (PrivilegeApplicationStatusResource) privResource;

                Assert.assertEquals( targetPriv.getId(), secPriv.getId() );
                Assert.assertEquals( targetPriv.getName(), secPriv.getName() );
                Assert.assertEquals( targetPriv.getMethod(), secPriv.getMethod() );
                Assert.assertEquals( targetPriv.getPermission(), secPriv.getPermission() );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    public static CRole getCRole( String roleId )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();
        List<CRole> secRoles = securityConfig.getRoles();

        for ( Iterator<CRole> iter = secRoles.iterator(); iter.hasNext(); )
        {
            CRole cRole = iter.next();

            if ( roleId.equals( cRole.getId() ) )
            {
                return cRole;
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public static CRepoTargetPrivilege getCRepoTargetPrivilege( String privilegeId )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();
        List<CRepoTargetPrivilege> secPrivs = securityConfig.getRepositoryTargetPrivileges();

        for ( Iterator<CRepoTargetPrivilege> iter = secPrivs.iterator(); iter.hasNext(); )
        {
            CRepoTargetPrivilege cPriv = iter.next();

            if ( privilegeId.equals( cPriv.getId() ) )
            {
                return cPriv;
            }
        }
        return null;
    }
    
    @SuppressWarnings( "unchecked" )
    public static CRepoTargetPrivilege getCRepoTargetPrivilegeByName( String privilegeName )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();
        List<CRepoTargetPrivilege> secPrivs = securityConfig.getRepositoryTargetPrivileges();

        for ( Iterator<CRepoTargetPrivilege> iter = secPrivs.iterator(); iter.hasNext(); )
        {
            CRepoTargetPrivilege cPriv = iter.next();

            if ( privilegeName.equals( cPriv.getName() ) )
            {
                return cPriv;
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public static CApplicationPrivilege getCApplicationPrivilege( String privilegeId )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();
        List<CApplicationPrivilege> secPrivs = securityConfig.getApplicationPrivileges();

        for ( Iterator<CApplicationPrivilege> iter = secPrivs.iterator(); iter.hasNext(); )
        {
            CApplicationPrivilege cPriv = iter.next();

            if ( privilegeId.equals( cPriv.getId() ) )
            {
                return cPriv;
            }
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    public static CUser getCUser( String userId )
        throws IOException
    {
        Configuration securityConfig = getSecurityConfig();
        List<CUser> secUsers = securityConfig.getUsers();

        for ( Iterator<CUser> iter = secUsers.iterator(); iter.hasNext(); )
        {
            CUser cUser = iter.next();

            if ( userId.equals( cUser.getUserId() ) )
            {
                return cUser;
            }
        }
        return null;
    }

    public static Configuration getSecurityConfig()
        throws IOException
    {

        ResourceBundle rb = ResourceBundle.getBundle( "baseTest" );

        File secConfigFile = new File( rb.getString( "nexus.base.dir" ) 
                                       + "/"
                                       + AbstractNexusIntegrationTest.RELATIVE_WORK_CONF_DIR
                                       , "security.xml" );

        Reader fr = null;
        Configuration configuration = null;

        try
        {
            NexusSecurityConfigurationXpp3Reader reader = new NexusSecurityConfigurationXpp3Reader();

            fr = new InputStreamReader( new FileInputStream( secConfigFile ) );

            // read again with interpolation
            configuration = reader.read( fr );

        }
        catch ( XmlPullParserException e )
        {
            Assert.fail( "could not parse nexus.xml: " + e.getMessage() );
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }
        return configuration;
    }

}
