/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;

import org.sonatype.security.ldap.realms.AbstractLdapAuthenticatingRealm;

public class NotConfiguredLdapNexusTest
    extends AbstractNexusTestCase
{

    public void testAuthentication()
        throws Exception
    {
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();

        try
        {
            security.authenticate( new UsernamePasswordToken( "cstamas", "cstamas123" ) );
            Assert.fail( "Expected AuthenticationException to be thrown." );
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

        // if realm is not configured, the user should not be able to be authorized
        
        Assert.assertFalse( security.hasRole( principals, "developer" ) );
        Assert.assertFalse( security.hasRole( principals, "JUNK" ) );
    }

    public void testAuthorizationPriv()
        throws Exception
    {
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();

        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add( "cstamas", AbstractLdapAuthenticatingRealm.class.getName() );

     // if realm is not configured, the user should not be able to be authorized
        Assert.assertFalse( security.isPermitted( principals, "nexus:usersforgotpw:create" ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sonatype.nexus.AbstractNexusTestCase#customizeContext(org.codehaus.plexus.context.Context)
     */
    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );

        ctx.put( LDAP_CONFIGURATION_KEY, CONF_HOME.getAbsolutePath() + "/not-configured/" );
    }

}
