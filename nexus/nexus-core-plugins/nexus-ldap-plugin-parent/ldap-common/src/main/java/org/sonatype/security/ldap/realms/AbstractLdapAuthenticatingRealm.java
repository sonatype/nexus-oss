/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
