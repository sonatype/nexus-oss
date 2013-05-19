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
package org.sonatype.security.mock.realms;

import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;

@Singleton
@Typed( Realm.class )
@Named( "MockRealmA" )
public class MockRealmA
    extends AuthenticatingRealm
{
    private final UserManager userManager;

    @Inject
    public MockRealmA( @Named( "MockUserManagerA" ) UserManager userManager )
    {
        this.userManager = userManager;
        this.setAuthenticationTokenClass( UsernamePasswordToken.class );
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {

        // only allow jcoder/jcoder

        UsernamePasswordToken userpass = (UsernamePasswordToken) token;
        if ( "jcoder".equals( userpass.getUsername() ) && "jcoder".equals( new String( userpass.getPassword() ) ) )
        {
            return new SimpleAuthenticationInfo( userpass.getUsername(), new String( userpass.getPassword() ),
                                                 this.getName() );
        }

        return null;
    }

    @Override
    public String getName()
    {
        return "MockRealmA";
    }

    public void checkPermission( PrincipalCollection subjectPrincipal, String permission )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkPermission( PrincipalCollection subjectPrincipal, Permission permission )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkPermissions( PrincipalCollection subjectPrincipal, String... permissions )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkPermissions( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public void checkRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }

    public boolean hasAllRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
    {
        // mock this one out using the user manager

        try
        {
            User user = this.userManager.getUser( subjectPrincipal.oneByType( String.class ) );
            for ( RoleIdentifier eachRoleIdentifier : user.getRoles() )
            {
                if ( eachRoleIdentifier.getRoleId().equals( roleIdentifier ) )
                {
                    return true;
                }
            }
        }
        catch ( UserNotFoundException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    public boolean[] hasRoles( PrincipalCollection subjectPrincipal, List<String> roleIdentifiers )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isPermitted( PrincipalCollection principals, String permission )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPermitted( PrincipalCollection subjectPrincipal, Permission permission )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, String... permissions )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, List<Permission> permissions )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, String... permissions )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void checkRoles( PrincipalCollection subjectPrincipal, String... roleIdentifiers )
        throws AuthorizationException
    {
        // TODO Auto-generated method stub

    }
}
