/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.usermanagement;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;

public class LdapUserManagerTest
    extends AbstractLdapTestEnvironment
{

    public static final String SECURITY_CONFIG_KEY = "security-xml-file";
    public static final String LDAP_CONFIGURATION_KEY = "application-conf";

    protected static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    protected static final File CONF_HOME = new File( PLEXUS_HOME, "conf" );

    @Override
    protected void customizeContext( Context ctx )
    {
        ctx.put( SECURITY_CONFIG_KEY, new File( CONF_HOME, "security.xml" ).getAbsolutePath() );
        ctx.put( LDAP_CONFIGURATION_KEY, CONF_HOME.getAbsolutePath() );
    }

    @Override
    public void setUp()
        throws Exception
    {
        CONF_HOME.mkdirs();
        IOUtil.copy(
            getClass().getResourceAsStream( "/test-conf/conf/security-users-in-both-realms.xml" ),
            new FileOutputStream( new File( CONF_HOME, "security.xml" ) ) );

        IOUtil.copy(
            getClass().getResourceAsStream( "/test-conf/conf/security-configuration.xml" ),
            new FileOutputStream( new File( CONF_HOME, "security-configuration.xml" ) ) );
        
        IOUtil.copy(
            getClass().getResourceAsStream( "/test-conf/conf/ldap.xml" ),
            new FileOutputStream( new File( CONF_HOME, "ldap.xml" ) ) );
        
        super.setUp();
    }

    private SecuritySystem getSecuritySystem()
        throws Exception
    {
        return this.lookup( SecuritySystem.class );
    }

    private UserManager getUserManager()
        throws Exception
    {
        return this.lookup( UserManager.class, "LDAP" );
    }

    public void testGetUserFromUserManager()
        throws Exception
    {   
        
        SecuritySystem securitySystem = this.getSecuritySystem();
        securitySystem.start();
        User user = securitySystem.getUser( "cstamas" );
        Assert.assertNotNull( user );
        Assert.assertEquals( "cstamas", user.getUserId() );
        Assert.assertEquals( "cstamas@sonatype.com", user.getEmailAddress() );
        Assert.assertEquals( "Tamas Cservenak", user.getName() );

        Set<String> roleIds = this.getUserRoleIds( user );
        Assert.assertTrue( roleIds.contains( "repoconsumer" ) ); // from LDAP
        Assert.assertTrue( roleIds.contains( "developer" ) ); // FROM LDAP and XML
        Assert.assertTrue( roleIds.contains( "anonymous" ) ); // FROM XML
        Assert.assertEquals( 3, roleIds.size() );
    }

    public void testGetUserFromLocator()
        throws Exception
    {
        Assert.assertNotNull( this.lookup( LdapConfiguration.class ) );
        
        UserManager userLocator = this.getUserManager();
        User user = userLocator.getUser( "cstamas" );
        Assert.assertNotNull( user );
        Assert.assertEquals( "cstamas", user.getUserId() );
        Assert.assertEquals( "cstamas@sonatype.com", user.getEmailAddress() );
        Assert.assertEquals( "Tamas Cservenak", user.getName() );
    }

    public void testGetUserIds()
        throws Exception
    {
        UserManager userLocator = this.getUserManager();
        Set<String> userIds = userLocator.listUserIds();
        Assert.assertTrue( userIds.contains( "cstamas" ) );
        Assert.assertTrue( userIds.contains( "brianf" ) );
        Assert.assertTrue( userIds.contains( "jvanzyl" ) );
        Assert.assertTrue( userIds.contains( "jdcasey" ) );
        Assert.assertEquals( "Ids: " + userIds, 4, userIds.size() );
    }

    public void testSearch()
        throws Exception
    {
        UserManager userLocator = this.getUserManager();
        Set<User> users = userLocator.searchUsers( new UserSearchCriteria( "j" ) );

        Assert.assertNotNull( this.getById( users, "jvanzyl" ) );
        Assert.assertNotNull( this.getById( users, "jdcasey" ) );
        Assert.assertEquals( "Users: " + users, 2, users.size() );
    }

    public void testEffectiveSearch()
        throws Exception
    {
        UserManager userLocator = this.getUserManager();

        Set<String> allRoleIds = new HashSet<String>();
        for ( Role role : this.getSecuritySystem().listRoles() )
        {
            allRoleIds.add( role.getRoleId() );
        }

        UserSearchCriteria criteria = new UserSearchCriteria( "j", allRoleIds, null );

        Set<User> users = userLocator.searchUsers( criteria );

        Assert.assertNotNull( this.getById( users, "jvanzyl" ) );
        Assert.assertEquals( "Users: " + users, 1, users.size() );
    }

    public void testGetUsers()
        throws Exception
    {
        UserManager userLocator = this.getUserManager();
        Set<User> users = userLocator.listUsers();

        User cstamas = this.getById( users, "cstamas" );
        Assert.assertEquals( "cstamas", cstamas.getUserId() );
        Assert.assertEquals( "cstamas@sonatype.com", cstamas.getEmailAddress() );
        Assert.assertEquals( "Tamas Cservenak", cstamas.getName() );

        User brianf = this.getById( users, "brianf" );
        Assert.assertEquals( "brianf", brianf.getUserId() );
        Assert.assertEquals( "brianf@sonatype.com", brianf.getEmailAddress() );
        Assert.assertEquals( "Brian Fox", brianf.getName() );

        User jvanzyl = this.getById( users, "jvanzyl" );
        Assert.assertEquals( "jvanzyl", jvanzyl.getUserId() );
        Assert.assertEquals( "jvanzyl@sonatype.com", jvanzyl.getEmailAddress() );
        Assert.assertEquals( "Jason Van Zyl", jvanzyl.getName() );

        User jdcasey = this.getById( users, "jdcasey" );
        Assert.assertEquals( "jdcasey", jdcasey.getUserId() );
        Assert.assertEquals( "jdcasey@sonatype.com", jdcasey.getEmailAddress() );
        Assert.assertEquals( "John Casey", jdcasey.getName() );

        Assert.assertEquals( "Ids: " + users, 4, users.size() );
    }

    private User getById( Set<User> users, String userId )
    {
        for ( User User : users )
        {
            if ( User.getUserId().equals( userId ) )
            {
                return User;
            }
        }
        Assert.fail( "Failed to find user: " + userId + " in list." );
        return null;
    }

    private Set<String> getUserRoleIds( User user )
    {
        Set<String> roleIds = new HashSet<String>();
        for ( RoleIdentifier role : user.getRoles() )
        {
            roleIds.add( role.getRoleId() );
        }
        return roleIds;
    }

    public void testOrderOfUserSearch()
        throws Exception
    {
        IOUtil.copy(
            getClass().getResourceAsStream( "/test-conf/conf/security-users-in-both-realms.xml" ),
            new FileOutputStream( new File( CONF_HOME, "security.xml" ) ) );

        SecuritySystem securitySystem = this.getSecuritySystem();
        securitySystem.start();

        List<String> realms = new ArrayList<String>();
        realms.add( "XmlAuthenticatingRealm" );
        realms.add( "LdapAuthenticatingRealm" );

        securitySystem.setRealms( realms );

        // the user developer is in both realms, we need to make sure the order is honored
        User user = securitySystem.getUser( "brianf" );
        Assert.assertEquals( "default", user.getSource() );

        realms.clear();
        realms.add( "LdapAuthenticatingRealm" );
        realms.add( "XmlAuthenticatingRealm" );
        securitySystem.setRealms( realms );

        // now the user should belong to the LDAP realm

        user = securitySystem.getUser( "brianf" );
        Assert.assertEquals( "LDAP", user.getSource() );

    }
}
