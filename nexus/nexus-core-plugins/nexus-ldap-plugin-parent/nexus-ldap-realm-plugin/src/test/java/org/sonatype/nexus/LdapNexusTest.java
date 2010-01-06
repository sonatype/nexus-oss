/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus;

import junit.framework.Assert;

import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;

import org.sonatype.security.ldap.realms.AbstractLdapAuthenticatingRealm;

public class LdapNexusTest
    extends AbstractNexusTestCase
{

    public void testAuthentication()
        throws Exception
    {
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();

        Assert.assertNotNull( security.authenticate( new UsernamePasswordToken( "cstamas", "cstamas123" ) ) );
    }

    public void testAuthenticationFailure()
        throws Exception
    {
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();

        try
        {
            Assert.assertNull( security.authenticate( new UsernamePasswordToken( "cstamas", "INVALID" ) ) );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
    }

    public void testAuthorization()
        throws Exception
    {
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();

        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add( "cstamas", AbstractLdapAuthenticatingRealm.class.getName() );

        Assert.assertTrue( security.hasRole( principals, "developer" ) );
        Assert.assertFalse( security.hasRole( principals, "JUNK" ) );
    }

    public void testAuthorizationPriv()
        throws Exception
    {
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();

        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add( "cstamas", AbstractLdapAuthenticatingRealm.class.getName() );

        Assert.assertTrue( security.isPermitted( principals, "security:usersforgotpw:create" ) );
        Assert.assertFalse( security.isPermitted( principals, "security:usersforgotpw:delete" ) );
    }
}
