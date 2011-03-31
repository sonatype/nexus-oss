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
package org.sonatype.security.ldap;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.password.PasswordEncoderManager;

@Component( role = LdapAuthenticator.class )
public class LdapAuthenticator
{
    @Requirement
    private PasswordEncoderManager passwordManager;
    
    public void authenticateUserWithPassword( LdapUser ldapUser, String password ) throws AuthenticationException
    {
        // use the passwordmanager
        if ( !this.passwordManager.isPasswordValid( ldapUser.getPassword(), password, null ) )
        {
            throw new AuthenticationException( "User '" + ldapUser.getUsername() + "' cannot be authenticated." );
        }
    }

    public void authenticateUserWithBind( LdapUser ldapUser, String password,
        LdapContextFactory ldapContextFactory, String authScheme ) throws AuthenticationException
    {
        String userId = ldapUser.getUsername();

        // Binds using the username and password provided by the user.

        String bindUsername = ldapUser.getDn();

        // if we are authorizing against DIGEST-MD5 or CRAM-MD5 then username is not the DN
        if ( "DIGEST-MD5".equals( authScheme ) || "CRAM-MD5".equals( authScheme ) )
        {
            bindUsername = userId;
        }

        // check using bind
        this.checkPasswordUsingBind( ldapContextFactory, bindUsername, password );

    }

    private void checkPasswordUsingBind( LdapContextFactory ldapContextFactory, String user, String pass )
        throws AuthenticationException
    {
        LdapContext ctx = null;
        try
        {
            ctx = ldapContextFactory.getLdapContext( user, pass );
        }
        catch ( javax.naming.AuthenticationException e )
        {
            throw new AuthenticationException( "User '" + user + "' cannot be authenticated.", e );
        }
        catch ( NamingException e )
        {
            throw new AuthenticationException( "User '" + user + "' cannot be authenticated.", e );
        }
        finally
        {
            LdapUtils.closeContext( ctx );
        }
    }

}
