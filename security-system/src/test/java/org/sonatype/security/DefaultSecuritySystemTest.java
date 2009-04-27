package org.sonatype.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.jsecurity.subject.Subject;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.usermanagement.User;

public class DefaultSecuritySystemTest
    extends AbstractSecurityTest
{

    public void testLogin()
        throws Exception
    {

        SecuritySystem securitySystem = this.getSecuritySystem();

        // login
        UsernamePasswordToken token = new UsernamePasswordToken( "jcoder", "jcoder" );
        Subject subject = securitySystem.login( token );
        Assert.assertNotNull( subject );

        try
        {
            securitySystem.login( new UsernamePasswordToken( "jcoder", "INVALID" ) );
            Assert.fail( "expected AuthenticationException" );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
    }

    public void testLogout()
        throws Exception
    {

        SecuritySystem securitySystem = this.getSecuritySystem();

        // login
        UsernamePasswordToken token = new UsernamePasswordToken( "jcoder", "jcoder" );
        Subject subject = securitySystem.login( token );
        Assert.assertNotNull( subject );

        // check the logged in user
        Subject loggedinSubject = securitySystem.getSubject();
        Assert.assertEquals( subject.getSession().getId(), loggedinSubject.getSession().getId() );

        // now logout
        securitySystem.logout( new SimplePrincipalCollection( "jcoder", "ANYTHING" ) );

        // the current user should be null
        Assert.assertNull( securitySystem.getSubject().getPrincipal() );
    }

    public void testAuthorization()
        throws Exception
    {
        SecuritySystem securitySystem = this.getSecuritySystem();
        PrincipalCollection principal = new SimplePrincipalCollection( "jcool", "ANYTHING" );
        try
        {
            securitySystem.checkPermission( principal, "INVALID-ROLE:*" );
            Assert.fail( "expected: AuthorizationException" );
        }
        catch ( AuthorizationException e )
        {
            // expected
        }

        securitySystem.checkPermission( principal, "test:read" );

    }

    /*
     * FIXME: BROKEN
     */
    public void BROKENtestPermissionFromRole()
        throws Exception
    {
        SecuritySystem securitySystem = this.getSecuritySystem();
        PrincipalCollection principal = new SimplePrincipalCollection( "jcool", "ANYTHING" );

        securitySystem.checkPermission( principal, "from-role2:read" );

    }

    public void testGetUser()
        throws Exception
    {
        SecuritySystem securitySystem = this.getSecuritySystem();
        User jcoder = securitySystem.getUser( "jcoder", "MockUserManagerA" );

        Assert.assertNotNull( jcoder );

    }

    public void testAuthorizationManager()
        throws Exception
    {
        SecuritySystem securitySystem = this.getSecuritySystem();

        AuthorizationManager authzManager = securitySystem.getAuthorizationManager( "sourceB" );

        Set<Role> roles = authzManager.getRoles();
        Assert.assertEquals( 2, roles.size() );

        Map<String, Role> roleMap = new HashMap<String, Role>();
        for ( Role role : roles )
        {
            roleMap.put( role.getRoleId(), role );
        }

        Assert.assertTrue( roleMap.containsKey( "test-role1" ) );
        Assert.assertTrue( roleMap.containsKey( "test-role2" ) );

        Role role1 = roleMap.get( "test-role1" );
        Assert.assertEquals( "Role 1", role1.getName() );

        Assert.assertTrue( role1.getPermissions().contains( "from-role1:read" ) );
        Assert.assertTrue( role1.getPermissions().contains( "from-role1:delete" ) );

    }

}
