/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms;

import javax.naming.NamingException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.credential.AllowAllCredentialsMatcher;
import org.jsecurity.authc.credential.CredentialsMatcher;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.realm.ldap.AbstractLdapRealm;
import org.jsecurity.realm.ldap.LdapContextFactory;
import org.jsecurity.subject.PrincipalCollection;

//@Component( role = AbstractLdapAuthenticatingRealm.class, hint = "ConfigurableLdapRealm" )
public abstract class AbstractLdapAuthenticatingRealm
    extends AbstractLdapRealm
{

    @Requirement
    private LdapManager ldapManager;

    @Requirement
    protected Logger logger;

    @Override
    protected AuthenticationInfo queryForAuthenticationInfo( AuthenticationToken token,
        LdapContextFactory ldapContextFactory )
        throws NamingException
    {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        String pass = String.valueOf( upToken.getPassword() );

        try
        {
            this.ldapManager.authenticateUser( username, pass );
            return this.buildAuthenticationInfo( username, null );
        }
        catch ( org.sonatype.security.authentication.AuthenticationException e )
        {
            if ( this.logger.isDebugEnabled() )
            {
                this.logger.debug( "User: " + username + " could not be authenticated ", e );
            }
            throw new org.jsecurity.authc.AuthenticationException( e.getMessage() );
        }
    }

    @Override
    protected AuthorizationInfo queryForAuthorizationInfo( PrincipalCollection principals,
        LdapContextFactory ldapContextFactory )
        throws NamingException
    {
        return null;
    }

    protected AuthenticationInfo buildAuthenticationInfo( String username, char[] password )
    {
        return new SimpleAuthenticationInfo( username, password, getName() );
    }


    @Override
    public String getName()
    {
        return "LDAP";
    }

    /*
     * (non-Javadoc)
     * @see org.jsecurity.realm.AuthenticatingRealm#getCredentialsMatcher()
     */
    @Override
    public CredentialsMatcher getCredentialsMatcher()
    {
        return new AllowAllCredentialsMatcher();
    }
}
