/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.mock;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;

@Singleton
@Typed( Realm.class )
@Named( MockRealm.NAME )
public class MockRealm
    extends AuthorizingRealm
{
    public static final String NAME = "Mock";

    private final UserManager userManager;

    @Inject
    public MockRealm( @Named( "Mock" ) UserManager userManager )
    {
        this.userManager = userManager;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        String userId = principals.getPrimaryPrincipal().toString();

        Set<String> roles = new HashSet<String>();
        try
        {
            for ( RoleIdentifier roleIdentifier : userManager.getUser( userId ).getRoles() )
            {
                roles.add( roleIdentifier.getRoleId() );
            }
        }
        catch ( UserNotFoundException e )
        {
            return null;
        }

        return new SimpleAuthorizationInfo( roles );

    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        String password = new String( upToken.getPassword() );
        String userId = upToken.getUsername();

        // username == password
        try
        {
            if ( userId.endsWith( password ) && userManager.getUser( userId ) != null )
            {
                return new SimpleAuthenticationInfo( new SimplePrincipalCollection( token.getPrincipal(),
                                                                                    this.getName() ), userId );
            }
            else
            {
                throw new IncorrectCredentialsException( "User [" + userId + "] bad credentials." );
            }
        }
        catch ( UserNotFoundException e )
        {
            throw new UnknownAccountException( "User [" + userId + "] not found." );
        }
    }

    @Override
    public String getName()
    {
        return "Mock";
    }

}
