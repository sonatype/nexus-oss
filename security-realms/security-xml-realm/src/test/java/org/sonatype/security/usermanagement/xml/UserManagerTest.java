package org.sonatype.security.usermanagement.xml;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.jsecurity.authc.credential.Sha1CredentialsMatcher;
import org.sonatype.security.AbstractSecurityTestCase;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.dao.SecurityUser;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.StringDigester;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserStatus;

public class UserManagerTest
    extends AbstractSecurityTestCase
{

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // copy the file to a different location because we are going to change it
        FileUtils.copyFile( new File( "target/test-classes/org/sonatype/security/locators/security.xml" ), new File(
            "target/test-classes/org/sonatype/security/locators/security-test.xml" ) );

        // copy the securityConf into place
        String securityXml = this.getClass().getName().replaceAll( "\\.", "\\/" ) + "-security.xml";
        FileUtils.copyURLToFile( Thread.currentThread().getContextClassLoader().getResource( securityXml ), new File(
            CONFIG_DIR,
            "security.xml" ) );
    }

    public SecuritySystem getSecuritySystem()
        throws Exception
    {
        return this.lookup( SecuritySystem.class );
    }

    public UserManager getUserManager()
        throws Exception
    {
        return this.lookup( UserManager.class );
    }

    public ConfigurationManager getConfigurationManager()
        throws Exception
    {
        return lookup( ConfigurationManager.class, "resourceMerging" );
    }

    public void testGetUser()
        throws Exception
    {
        UserManager userManager = this.getUserManager();

        User user = userManager.getUser( "test-user" );

        Assert.assertEquals( user.getUserId(), "test-user" );
        Assert.assertEquals( user.getEmailAddress(), "changeme1@yourcompany.com" );
        Assert.assertEquals( user.getName(), "Test User" );
        // not exposed anymore
        // Assert.assertEquals( user.getPassword(), "b2a0e378437817cebdf753d7dff3dd75483af9e0" );
        Assert.assertEquals( user.getStatus().name(), "active" );

        List<String> roleIds = this.getRoleIds( user );

        Assert.assertTrue( roleIds.contains( "role1" ) );
        Assert.assertTrue( roleIds.contains( "role2" ) );
        Assert.assertEquals( 2, roleIds.size() );
    }

    public void testAddUser()
        throws Exception
    {
        UserManager userManager = this.getUserManager();

        User user = new DefaultUser();
        user.setUserId( "testCreateUser" );
        user.setName( user.getUserId() + "-name" );
        user.setSource( user.getUserId() + "default" );
        user.setEmailAddress( "email@email" );
        user.setStatus( UserStatus.active );
        user.addRole( new Role( "role1", "Role 1", "default" ) );
        user.addRole( new Role( "role3", "Role 3", "default" ) );

        userManager.addUser( user, "my-password" );

        ConfigurationManager config = this.getConfigurationManager();

        SecurityUser secUser = config.readUser( user.getUserId() );
        Assert.assertEquals( secUser.getId(), user.getUserId() );
        Assert.assertEquals( secUser.getEmail(), user.getEmailAddress() );
        Assert.assertEquals( secUser.getName(), user.getName() );
        Assert.assertEquals( secUser.getPassword(), StringDigester.getSha1Digest( "my-password" ) );

        Assert.assertEquals( secUser.getStatus(), user.getStatus().name() );

        Assert.assertTrue( secUser.getRoles().contains( "role1" ) );
        Assert.assertTrue( secUser.getRoles().contains( "role3" ) );
        Assert.assertEquals( 2, user.getRoles().size() );
    }

    public void testSupportsWrite()
        throws Exception
    {
        Assert.assertTrue( this.getUserManager().supportsWrite() );
    }

    public void testChangePassword()
        throws Exception
    {
        UserManager userManager = this.getUserManager();
        userManager.changePassword( "test-user", "new-user-password" );

        Assert.assertEquals( this.getConfigurationManager().readUser( "test-user" ).getPassword(), StringDigester
            .getSha1Digest( "new-user-password" ) );
    }

    public void testUpdateUser()
        throws Exception
    {
        UserManager userManager = this.getUserManager();

        User user = userManager.getUser( "test-user" );

        user.setName( "new Name" );
        user.setEmailAddress( "newemail@foo" );
        Set<Role> roles = new HashSet<Role>();
        roles.add( new Role( "role3", "Role 3", "default" ) );
        user.setRoles( roles );
        userManager.updateUser( user );

        ConfigurationManager config = this.getConfigurationManager();

        SecurityUser secUser = config.readUser( user.getUserId() );
        Assert.assertEquals( secUser.getId(), user.getUserId() );
        Assert.assertEquals( secUser.getEmail(), user.getEmailAddress() );
        Assert.assertEquals( secUser.getName(), user.getName() );
        Assert.assertEquals( secUser.getPassword(), "b2a0e378437817cebdf753d7dff3dd75483af9e0" );

        Assert.assertEquals( secUser.getStatus(), user.getStatus().name() );

        Assert.assertTrue( secUser.getRoles().contains( "role3" ) );
        Assert.assertEquals( 1, user.getRoles().size() );
    }

    public void testDeleteUser()
        throws Exception
    {
        UserManager userManager = this.getUserManager();
        try
        {
            userManager.deleteUser( "INVALID-USERNAME" );
            Assert.fail( "Expected UserNotFoundException" );
        }
        catch ( UserNotFoundException e )
        {
            // expected
        }

        // this one will work
        userManager.deleteUser( "test-user" );

        // this one should fail
        try
        {
            userManager.deleteUser( "test-user" );
            Assert.fail( "Expected UserNotFoundException" );
        }
        catch ( UserNotFoundException e )
        {
            // expected
        }

        try
        {
            userManager.getUser( "test-user" );
            Assert.fail( "Expected UserNotFoundException" );
        }
        catch ( UserNotFoundException e )
        {
            // expected
        }

        try
        {
            this.getConfigurationManager().readUser( "test-user" );
            Assert.fail( "Expected UserNotFoundException" );
        }
        catch ( UserNotFoundException e )
        {
            // expected
        }

    }

    private List<String> getRoleIds( User user )
    {
        List<String> roleIds = new ArrayList<String>();

        for ( Role role : user.getRoles() )
        {
            roleIds.add( role.getRoleId() );
        }

        return roleIds;
    }

}
