package org.sonatype.security;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.jsecurity.subject.Subject;
import org.sonatype.security.authentication.AuthenticationException;

public class DefaultSecuritySystemTest
    extends PlexusTestCase
{

    private SecuritySystem getSecuritySystem()
        throws Exception
    {
        return this.lookup( SecuritySystem.class );
    }

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

}
