/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms;

import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.AbstractLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.NoLdapUserRolesFoundException;

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
            throw new org.apache.shiro.authc.AuthenticationException( e.getMessage() );
        }
    }

    @Override
    protected AuthorizationInfo queryForAuthorizationInfo( PrincipalCollection principals,
        LdapContextFactory ldapContextFactory )
        throws NamingException
    {
        // only authorize users from this realm
        if( principals.getRealmNames().contains( this.getName() ))
        {
        
            Set<String> roles = new HashSet<String>();
            String username = principals.getPrimaryPrincipal().toString();
            try
            {
                roles = this.ldapManager.getUserRoles(username  );
            }
            catch ( LdapDAOException e )
            {
                this.logger.error( e.getMessage(), e );
                throw new NamingException(e.getMessage());
            }
            catch ( NoLdapUserRolesFoundException e )
            {
                this.logger.debug( "User: " + username + " does not have any ldap roles.", e );
            }
            
            return new SimpleAuthorizationInfo( roles );
        }
        return null;
        
    }

    protected AuthenticationInfo buildAuthenticationInfo( String username, char[] password )
    {
        return new SimpleAuthenticationInfo( username, password, getName() );
    }


    @Override
    public String getName()
    {
        return "LdapAuthenticatingRealm";
    }

    /*
     * (non-Javadoc)
     * @see org.apache.shiro.realm.AuthenticatingRealm#getCredentialsMatcher()
     */
    @Override
    public CredentialsMatcher getCredentialsMatcher()
    {
        return new AllowAllCredentialsMatcher();
    }
}
