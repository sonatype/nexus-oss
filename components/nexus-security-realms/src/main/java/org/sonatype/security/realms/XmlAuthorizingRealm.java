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
package org.sonatype.security.realms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.Sha1CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Description;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.RoleMappingUserManager;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * An Authorizing Realm backed by an XML file see the security-model-xml module. This model defines users, roles, and
 * privileges. This realm ONLY handles authorization.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( Realm.class )
@Named( XmlAuthorizingRealm.ROLE )
@Description( "Xml Authorizing Realm" )
public class XmlAuthorizingRealm
    extends AuthorizingRealm
    implements Realm
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public static final String ROLE = "XmlAuthorizingRealm";

    private final UserManager userManager;

    private final Map<String, UserManager> userManagerMap;

    private final SecuritySystem securitySystem;

    @Inject
    public XmlAuthorizingRealm( UserManager userManager, SecuritySystem securitySystem,
                                Map<String, UserManager> userManagerMap )
    {
        this.userManager = userManager;
        this.securitySystem = securitySystem;
        this.userManagerMap = userManagerMap;
        setCredentialsMatcher( new Sha1CredentialsMatcher() );
        setName( ROLE );
    }

    @Override
    public boolean supports( AuthenticationToken token )
    {
        return false;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        return null;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        if ( principals == null )
        {
            throw new AuthorizationException( "Cannot authorize with no principals." );
        }

        String username = principals.getPrimaryPrincipal().toString();
        Set<String> roles = new HashSet<String>();

        Set<String> realmNames = new HashSet<String>( principals.getRealmNames() );

        // if the user belongs to this realm, we are most likely using this realm stand alone, or for testing
        if ( !realmNames.contains( this.getName() ) )
        {
            // make sure the realm is enabled
            Collection<Realm> configureadRealms = this.securitySystem.getSecurityManager().getRealms();
            boolean foundRealm = false;
            for ( Realm realm : configureadRealms )
            {
                if ( realmNames.contains( realm.getName() ) )
                {
                    foundRealm = true;
                    break;
                }
            }
            if ( !foundRealm )
            {
                // user is from a realm that is NOT enabled
                throw new AuthorizationException( "User for principals: " + principals.getPrimaryPrincipal()
                    + " belongs to a disabled realm(s): " + principals.getRealmNames() + "." );
            }
        }

        // clean up the realm names for processing (replace the Xml*Realm with default)
        cleanUpRealmList( realmNames );

        if ( RoleMappingUserManager.class.isInstance( userManager ) )
        {
            for ( String realmName : realmNames )
            {
                try
                {
                    for ( RoleIdentifier roleIdentifier : ( (RoleMappingUserManager) userManager ).getUsersRoles( username,
                                                                                                                  realmName ) )
                    {
                        roles.add( roleIdentifier.getRoleId() );
                    }
                }
                catch ( UserNotFoundException e )
                {
                    if ( this.logger.isTraceEnabled() )
                    {
                        this.logger.trace( "Failed to find role mappings for user: " + username + " realm: "
                            + realmName );
                    }
                }
            }
        }
        else if ( realmNames.contains( "default" ) )
        {
            try
            {
                for ( RoleIdentifier roleIdentifier : userManager.getUser( username ).getRoles() )
                {
                    roles.add( roleIdentifier.getRoleId() );
                }
            }
            catch ( UserNotFoundException e )
            {
                throw new AuthorizationException( "User for principals: " + principals.getPrimaryPrincipal()
                    + " could not be found.", e );
            }

        }
        else
        // user not managed by this Realm
        {
            throw new AuthorizationException( "User for principals: " + principals.getPrimaryPrincipal()
                + " not manged by XML realm." );
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo( roles );

        return info;
    }

    private void cleanUpRealmList( Set<String> realmNames )
    {
        for ( UserManager userManager : this.userManagerMap.values() )
        {
            String authRealmName = userManager.getAuthenticationRealmName();
            if ( authRealmName != null && realmNames.contains( authRealmName ) )
            {
                realmNames.remove( authRealmName );
                realmNames.add( userManager.getSource() );
            }
        }

        if ( realmNames.contains( getName() ) )
        {
            realmNames.remove( getName() );
            realmNames.add( "default" );
        }
    }
}
