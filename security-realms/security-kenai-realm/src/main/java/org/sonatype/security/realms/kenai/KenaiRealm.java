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
package org.sonatype.security.realms.kenai;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.codehaus.plexus.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.slf4j.Logger;
import org.sonatype.inject.Description;
import org.sonatype.security.realms.kenai.config.KenaiRealmConfiguration;
import org.sonatype.security.usermanagement.UserManager;

/**
 * A Realm that connects to a java.net kenai API.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( value = Realm.class )
@Named( value = "kenai" )
@Description( value = "Kenai Realm" )
public class KenaiRealm
    extends AuthorizingRealm
{
    @Named( value = "default-authentication-cache" )
    private String authenticationCacheName;

    // @Inject
    // @Named( value = "kenai" )
    // private UserManager userManager;

    @Inject
    private Logger logger;

    @Inject
    private KenaiRealmConfiguration kenaiRealmConfiguration;

    private Cache<Object, Object> authenticatingCache = null;

    private String DEFAULT_AUTHENTICATION_CACHE_POSTFIX = "-authentication";

    private static int INSTANCE_COUNT = 0;

    @Override
    public String getName()
    {
        return "kenai";
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        // check cache
        AuthenticationInfo authInfo = this.getAuthInfoFromCache( upToken );

        // to normal authentication
        if ( authInfo == null )
        {
            String username = upToken.getUsername();
            String pass = String.valueOf( upToken.getPassword() );

            // if the user can authenticate we are good to go
            if ( this.authenticateViaUrl( username, pass ) )
            {
                authInfo = buildAuthenticationInfo( username, null );
                this.putUserInCache( username, pass );
            }
            else
            {
                throw new AccountException( "User '" + username + "' cannot be authenticated." );
            }
        }

        return authInfo;
    }

    private void putUserInCache( String username, String pass )
    {
        // get cache
        Cache authCache = this.getAuthenticationCache();

        // check if null
        if ( authCache != null )
        {
            authCache.put( this.getAuthenticationCacheKey( username, pass ), Boolean.TRUE );
            this.logger.debug( "Added user: '" + username + "' to cache." );
        }
        else
        {
            this.logger.debug( "Authentication Cache is disabled." );
        }
    }

    private AuthenticationInfo getAuthInfoFromCache( UsernamePasswordToken token )
    {
        // get cache
        Cache authCache = this.getAuthenticationCache();

        // check if null
        if ( authCache != null )
        {
            // the supports method already only allows supported tokens
            String username = token.getUsername();
            String pass = String.valueOf( token.getPassword() );

            String cacheKey = this.getAuthenticationCacheKey( username, pass );
            if ( authCache.get( cacheKey ) != null )
            {
                // return an AuthenticationInfo if we found the username in the cache
                return this.buildAuthenticationInfo( username, null );
            }
        }

        return null;
    }

    private String getAuthenticationCacheKey( String username, String pass )
    {
        Sha1Hash h = new Sha1Hash( pass );
        return username + "-" + h.toString() + "-" + this.getClass().getSimpleName();
    }

    private Cache<Object, Object> getAuthenticationCache()
    {

        if ( this.authenticatingCache == null )
        {
            this.logger.debug( "No cache implementation set.  Checking cacheManager..." );

            CacheManager cacheManager = getCacheManager();

            if ( cacheManager != null )
            {
                String cacheName = this.getAuthenticationCacheName();
                if ( cacheName == null )
                {
                    // Simple default in case they didn't provide one:
                    cacheName = getClass().getName() + "-" + INSTANCE_COUNT++ + DEFAULT_AUTHENTICATION_CACHE_POSTFIX;
                    setAuthenticationCacheName( cacheName );
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

    private void setAuthenticationCacheName( String authenticationCacheName )
    {
        this.authenticationCacheName = authenticationCacheName;
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
        Response response = this.makeRemoteRequest( username, password );

        if ( response.getStatus().isSuccess() )
        {
            try
            {
                if ( this.isAuthorizationCachingEnabled() )
                {
                    AuthorizationInfo authorizationInfo =
                        this.buildAuthorizationInfoFromResponseText( response.getEntity().getText() );

                    Object authorizationCacheKey =
                        this.getAuthorizationCacheKey( new SimplePrincipalCollection( username, this.getName() ) );
                    this.getAvailableAuthorizationCache().put( authorizationCacheKey, authorizationInfo );

                }

                return true;
            }
            catch ( IOException e )
            {
                this.logger.warn( "Failed to read response", e );
            }
        }
        this.logger.debug( "Failed to authenticate user: {} for url: {} status: {}", new Object[] { username,
            response.getRequest().getResourceRef(), response.getStatus() } );
        return false;
    }

    private AuthorizationInfo buildAuthorizationInfoFromResponseText( String responseText )
    {
        try
        {
            Set<String> roles = new HashSet<String>();
            roles.add( this.kenaiRealmConfiguration.getConfiguration().getDefaultRole() );

            JSONObject jsonObject = new JSONObject( responseText );
            JSONArray projectArray = jsonObject.getJSONArray( "projects" );

            for ( int ii = 0; ii < projectArray.length(); ii++ )
            {
                JSONObject projectObject = projectArray.getJSONObject( ii );
                if ( projectObject.has( "name" ) )
                {
                    String projectName = projectObject.getString( "name" );
                    if ( StringUtils.isNotEmpty( projectName ) )
                    {
                        this.logger.trace( "Found project {} in request", projectName );
                        roles.add( projectName );
                    }
                    else
                    {
                        this.logger.debug( "Found empty string in json object projects[{}].name", ii );
                    }
                }
            }

            return new SimpleAuthorizationInfo( roles );

        }
        catch ( JSONException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private Response makeRemoteRequest( String username, String password )
    {
        Client restClient = new Client( new Context(), Protocol.HTTP );

        ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
        ChallengeResponse authentication = new ChallengeResponse( scheme, username, password );

        Request request = new Request();

        request.setResourceRef( this.kenaiRealmConfiguration.getConfiguration().getBaseUrl() + "api/projects/mine.json" );
        request.setMethod( Method.GET );
        request.setChallengeResponse( authentication );

        Response response = restClient.handle( request );
        this.logger.debug( "User: " + username + " url validation status: " + response.getStatus() );

        return response;
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

    @Override
    protected Object getAuthorizationCacheKey( PrincipalCollection principals )
    {
        return principals.getPrimaryPrincipal().toString();
    }

    /*
     * Start hacking around private caches, this is dirty, we either need a better way to get a users roles, or a
     * cleaner Shiro api to do this.
     */

    private Cache<Object, AuthorizationInfo> getAvailableAuthorizationCache()
    {
        Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
        if ( cache == null && isAuthorizationCachingEnabled() )
        {
            cache = getAuthorizationCacheLazy();
        }
        return cache;
    }

    private Cache<Object, AuthorizationInfo> getAuthorizationCacheLazy()
    {
        if ( this.getAuthorizationCache() == null )
        {

            this.logger.debug( "No authorizationCache instance set.  Checking for a cacheManager..." );

            CacheManager cacheManager = getCacheManager();

            if ( cacheManager != null )
            {
                String cacheName = getAuthorizationCacheName();
                this.logger.debug( "CacheManager [{}] has been configured.  Building authorization cache named [{}]",
                                   cacheManager, cacheName );

                Cache<Object, AuthorizationInfo> value = cacheManager.getCache( cacheName );
                this.setAuthorizationCache( value );
            }
            else
            {
                this.logger.info( "No cache or cacheManager properties have been set.  Authorization cache cannot be obtained." );

            }
        }

        return this.getAuthorizationCache();
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        // FIXME, always return null, we are setting the cache directly, yes, this is dirty
        return null;
    }
}
