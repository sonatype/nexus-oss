/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus;

import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.IOUtil;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;

import org.sonatype.security.ldap.realms.AbstractLdapAuthenticatingRealm;

public class MultipleRealmsLdapNotConfiguredTest
    extends AbstractNexusTestCase
{

    public void testAuthentication()
        throws Exception
    {
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();

        // LDAP should fail
        try
        {
            security.authenticate( new UsernamePasswordToken( "cstamas", "cstamas123" ) );
            Assert.fail( "Expected AuthenticationException to be thrown." );
        }
        catch ( AuthenticationException e )
        {
            // expected
        }
        
        
        // xml should not
        Assert.assertNotNull( security.authenticate( new UsernamePasswordToken( "admin", "admin123" ) ) );
        
        Assert.assertNotNull( security.authenticate( new UsernamePasswordToken( "deployment", "deployment123" ) ) );
        
    }

    public void testAuthorization()
        throws Exception
    {

        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();

        // LDAP should fail
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add( "cstamas", AbstractLdapAuthenticatingRealm.class.getName() );

        // if realm is not configured, the user should not be able to be authorized        
        Assert.assertFalse( security.hasRole( principals, "developer" ) );
        Assert.assertFalse( security.hasRole( principals, "JUNK" ) );
        
     // xml user
        principals = new SimplePrincipalCollection();
        principals.add( "deployment", AbstractLdapAuthenticatingRealm.class.getName() );
        
        Assert.assertTrue( security.hasRole( principals, "deployment" ) );
        Assert.assertFalse( security.hasRole( principals, "JUNK" ) );
    }

    public void testAuthorizationPriv()
        throws Exception
    {
        SecuritySystem security = lookup( SecuritySystem.class );
        security.start();

        // LDAP
        SimplePrincipalCollection principals = new SimplePrincipalCollection();
        principals.add( "cstamas", AbstractLdapAuthenticatingRealm.class.getName() );

        // if realm is not configured, the user should not be able to be authorized
        Assert.assertFalse( security.isPermitted( principals, "security:usersforgotpw:create" ) );
        
        // XML
        principals = new SimplePrincipalCollection();
        principals.add( "test-user", AbstractLdapAuthenticatingRealm.class.getName() );
        
        Assert.assertTrue( security.isPermitted( principals, "security:usersforgotpw:create" ) );
        Assert.assertFalse( security.isPermitted( principals, "security:usersforgotpw:delete" ) );
        
        Assert.assertTrue( security.isPermitted( principals, "nexus:target:1:*:delete" ) );
    }

    protected void copyDefaultConfigToPlace()
    throws IOException
{
    IOUtil.copy( getClass().getResourceAsStream( "/test-conf/security-configuration-multipleRealms.xml" ), new FileOutputStream(
        getSecurityConfiguration() ) );
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
