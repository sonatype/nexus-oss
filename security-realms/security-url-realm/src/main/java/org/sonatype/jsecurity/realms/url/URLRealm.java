/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.jsecurity.realms.url;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.jsecurity.authc.AccountException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.credential.AllowAllCredentialsMatcher;
import org.jsecurity.authc.credential.CredentialsMatcher;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.cache.Cache;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CRole;
import org.sonatype.jsecurity.realms.privileges.PrivilegeDescriptor;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.NoSuchPrivilegeException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;

@Component( role = Realm.class, hint = "url", description = "URL Realm" )
public class URLRealm
    extends AuthorizingRealm
{
    @Configuration( value = "${authentication-url}" )
    private String authenticationURL;

    @Configuration( value = "${url-authentication-default-role}" )
    private String defaultRole = "default-url-role";

    @Configuration( value = "default-authentication-cache" )
    private String authenticationCacheName;

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    @Requirement( role = PlexusUserManager.class, hint = "additinalRoles" )
    private PlexusUserManager userManager;

    @Requirement( role = PrivilegeDescriptor.class )
    private List<PrivilegeDescriptor> privilegeDescriptors;

    @Requirement
    private Logger logger;

    private Cache authenticatingCache = null;

    private String DEFAULT_AUTHENTICATION_CACHE_POSTFIX = "-authentication";

    private static int INSTANCE_COUNT = 0;

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {

        // check cache
        AuthenticationInfo authInfo = this.getAuthInfoFromCache( token );

        // to normal authentication
        if ( authInfo == null )
        {

            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            String username = upToken.getUsername();
            String pass = String.valueOf( upToken.getPassword() );

            // if the user can authenticate we are good to go
            if ( this.authenticateViaUrl( username, pass ) )
            {
                authInfo = buildAuthenticationInfo( username, null );
                this.putUserInCache( username );
            }
            else
            {
                throw new AccountException( "User '" + username + "' cannot be authenticated." );
            }
        }

        return authInfo;
    }

    private void putUserInCache( String username )
    {
        // get cache
        Cache authCache = this.getAuthenticationCache();

        // check if null
        if ( authCache != null )
        {
            authCache.put( this.getAuthenticationCacheKey( username ), Boolean.TRUE );
            this.logger.debug( "Added user: '" + username + "' to cache." );
        }
        else
        {
            this.logger.debug( "Authentication Cache is disabled." );
        }
    }

    private AuthenticationInfo getAuthInfoFromCache( AuthenticationToken token )
    {
        // get cache
        Cache authCache = this.getAuthenticationCache();

        // check if null
        if ( authCache != null )
        {
            // the supports method already only allows supported tokens
            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            String username = upToken.getUsername();

            String cacheKey = this.getAuthenticationCacheKey( username );
            if ( authCache.get( cacheKey ) != null )
            {
                // return an AuthenticationInfo if we found the username in the cache
                return this.buildAuthenticationInfo( username, null );
            }
        }

        return null;
    }

    private String getAuthenticationCacheKey( String username )
    {
        return username + "-" + this.getClass().getSimpleName();
    }

    private Cache getAuthenticationCache()
    {

        if ( this.authenticatingCache == null )
        {
            this.logger.debug( "No cache implementation set.  Checking cacheManager..." );

            org.jsecurity.cache.CacheManager cacheManager = getCacheManager();

            if ( cacheManager != null )
            {
                String cacheName = this.getAuthenticationCacheName();
                if ( cacheName == null )
                {
                    // Simple default in case they didn't provide one:
                    cacheName = getClass().getName() + "-" + INSTANCE_COUNT++ + DEFAULT_AUTHENTICATION_CACHE_POSTFIX;
                    setAuthorizationCacheName( cacheName );
                }
                this.logger.debug( "CacheManager [" + cacheManager + "] has been configured.  Building "
                    + "authentication cache named [" + cacheName + "]" );
                this.authenticatingCache = cacheManager.getCache( cacheName );
            }
            else
            {

                this.logger.info( "No cache or cacheManager properties have been set.  Authorization caching is "
                    + "disabled." );
            }
        }
        return this.authenticatingCache;
    }

    private String getAuthenticationCacheName()
    {
        return authenticationCacheName;
    }

    protected AuthenticationInfo buildAuthenticationInfo( String username, char[] password )
    {
        return new SimpleAuthenticationInfo( username, password, getName() );
    }

    @Override
    public boolean supports( AuthenticationToken token )
    {
        return UsernamePasswordToken.class.isAssignableFrom( token.getClass() );
    }

    private boolean authenticateViaUrl( String username, String password )
    {
        Client restClient = new Client( new Context(), Protocol.HTTP );

        ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
        ChallengeResponse authentication = new ChallengeResponse( scheme, username, password );

        Request request = new Request();
        request.setResourceRef( this.authenticationURL );
        request.setMethod( Method.GET );
        request.setChallengeResponse( authentication );

        Response response = restClient.handle( request );
        this.logger.debug( "User: " + username + " url validation status: " + response.getStatus() );

        return response.getStatus().isSuccess();
    }

    /*
     * (non-Javadoc)
     * @see org.jsecurity.realm.AuthenticatingRealm#getCredentialsMatcher()
     */
    @Override
    public CredentialsMatcher getCredentialsMatcher()
    {
        // we are managing the authentication ourselfs
        return new AllowAllCredentialsMatcher();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {

        if ( principals == null )
        {
            throw new AuthorizationException( "Cannot authorize with no principals." );
        }

        String username = (String) principals.iterator().next();

        // we need to make sure the user can be managed by this realm
        PlexusUser user = this.userManager.getUser( username, URLUserLocator.SOURCE );

        if ( user == null )
        {
            throw new AuthorizationException( "User '" + username + "' is not managed by this realm." );
        }

        // we don't have a list of users for this realm, so the default role effects ALL users

        LinkedList<String> rolesToProcess = new LinkedList<String>();
        // add the defaultRole and resolve the privleges
        rolesToProcess.add( this.defaultRole );

        // this should be put into a component, or better yet at a higher level
        // REFACTOR below (copied from XmlAuthorizingRealm) vvvv
        Set<String> roleIds = new LinkedHashSet<String>();
        Set<Permission> permissions = new LinkedHashSet<Permission>();
        while ( !rolesToProcess.isEmpty() )
        {
            String roleId = rolesToProcess.removeFirst();
            if ( !roleIds.contains( roleId ) )
            {
                CRole role;
                try
                {
                    role = configuration.readRole( roleId );
                    roleIds.add( roleId );

                    // process the roles this role has
                    rolesToProcess.addAll( role.getRoles() );

                    // add the permissions this role has
                    List<String> privilegeIds = role.getPrivileges();
                    for ( String privilegeId : privilegeIds )
                    {
                        Set<Permission> set = getPermissions( privilegeId );
                        permissions.addAll( set );
                    }
                }
                catch ( NoSuchRoleException e )
                {
                    // skip the permissions, but we still want to add the role
                    roleIds.add( roleId );
                }
            }
        }

        // end refactor ^^^

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo( roleIds );
        info.setObjectPermissions( permissions );

        return info;
    }

    private Set<Permission> getPermissions( String privilegeId )
    {
        try
        {
            CPrivilege privilege = getConfigurationManager().readPrivilege( privilegeId );

            for ( PrivilegeDescriptor descriptor : privilegeDescriptors )
            {
                String permission = descriptor.buildPermission( privilege );

                if ( permission != null )
                {
                    return Collections.singleton( (Permission) new WildcardPermission( permission ) );
                }
            }

            return Collections.emptySet();
        }
        catch ( NoSuchPrivilegeException e )
        {
            return Collections.emptySet();
        }
    }

    protected ConfigurationManager getConfigurationManager()
    {
        return configuration;
    }

    // we only need test to have access to this.
    String getAuthenticationURL()
    {
        return authenticationURL;
    }

    void setAuthenticationURL( String authenticationURL )
    {
        this.authenticationURL = authenticationURL;
    }

}
