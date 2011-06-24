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

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
    @Inject
    private Logger logger;

    @Inject
    private KenaiRealmConfiguration kenaiRealmConfiguration;

    private static final int PAGE_SIZE = 200;

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
        AuthenticationInfo authInfo = null;
        String username = upToken.getUsername();
        String pass = String.valueOf( upToken.getPassword() );

        // if the user can authenticate we are good to go
        if ( this.authenticateViaUrl( username, pass ) )
        {
            authInfo = buildAuthenticationInfo( username, null );
        }
        else
        {
            throw new AccountException( "User '" + username + "' cannot be authenticated." );
        }

        return authInfo;
    }

    protected Object getAuthenticationCacheKey( PrincipalCollection principals )
    {
        return getAvailablePrincipal( principals );
    }

    protected Object getAuthenticationCacheKey( AuthenticationToken token )
    {
        return token != null ? token.getPrincipal() : null;
    }

    protected AuthenticationInfo buildAuthenticationInfo( Object principal, Object credentials )
    {
        return new SimpleAuthenticationInfo( principal, credentials, getName() );
    }

    @Override
    public boolean supports( AuthenticationToken token )
    {
        return UsernamePasswordToken.class.isAssignableFrom( token.getClass() );
    }

    private boolean authenticateViaUrl( String username, String password )
    {
        Response response = this.makeRemoteRequest( username, password );

        try
        {
            if ( response.getStatus().isSuccess() )
            {
                if ( this.isAuthorizationCachingEnabled() )
                {
                    AuthorizationInfo authorizationInfo =
                        this.buildAuthorizationInfo( username, password, response.getEntity().getText() );

                    Object authorizationCacheKey =
                        this.getAuthorizationCacheKey( new SimplePrincipalCollection( username, this.getName() ) );
                    this.getAvailableAuthorizationCache().put( authorizationCacheKey, authorizationInfo );

                }

                return true;
            }
        }
        catch ( IOException e )
        {
            this.logger.error( "Failed to read response", e );
        }
        catch ( JSONException e )
        {
            this.logger.error( "Failed to read response", e );
        }
        finally
        {
            if ( response != null )
            {
                response.release();
            }
        }

        this.logger.debug( "Failed to authenticate user: {} for url: {} status: {}",
                           new Object[]{ username, response.getRequest().getResourceRef(), response.getStatus() } );
        return false;
    }

    private AuthorizationInfo buildAuthorizationInfo( String username, String password, String responseText )
        throws JSONException, IOException
    {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        // add the default role
        authorizationInfo.addRole( this.kenaiRealmConfiguration.getConfiguration().getDefaultRole() );

        // initial page
        JSONObject jsonObject = buildJsonObject( responseText );

        // collect roles from json object
        Set<String> roles = this.buildRoleSetFromJsonObject( jsonObject );
        authorizationInfo.addRoles( roles );

        // check for pages
        while ( jsonObject.has( "next" ) && jsonObject.getString( "next" ) != "null" )
        {
            String pagedURL = jsonObject.getString( "next" );
            this.logger.debug( "Next page of Kenai project info: {}", pagedURL );
            // make another remote request
            Response response = null;
            try
            {
                response = this.makeRemoteRequest( username, password, pagedURL );
                jsonObject = buildJsonObject( response );
                authorizationInfo.addRoles( this.buildRoleSetFromJsonObject( jsonObject ) );
            }
            finally
            {
                if ( response != null )
                {
                    response.release();
                }
            }
        }

        return authorizationInfo;
    }

    private JSONObject buildJsonObject( Response response )
        throws JSONException, IOException
    {
        if ( response.getStatus().isSuccess() )
        {
            return this.buildJsonObject( response.getEntity().getText() );
        }
        else
        {
            throw new AuthenticationException( "Error retriving response, status code: " + response.getStatus() );
        }
    }

    private JSONObject buildJsonObject( String responseText )
        throws JSONException, IOException
    {
        return new JSONObject( responseText );
    }

    private Set<String> buildRoleSetFromJsonObject( JSONObject jsonObject )
        throws JSONException
    {
        Set<String> roles = new HashSet<String>();
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

        return roles;
    }

    private Response makeRemoteRequest( String username, String password )
    {
        return makeRemoteRequest( username, password, this.kenaiRealmConfiguration.getConfiguration().getBaseUrl()
            + "api/projects/mine.json?size=" + PAGE_SIZE );
    }

    private Response makeRemoteRequest( String username, String password, String url )
    {
        Client restClient = new Client( new Context(), Protocol.HTTP );

        ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
        ChallengeResponse authentication = new ChallengeResponse( scheme, username, password );

        Request request = new Request();

        // FIXME: waiting for response from kenai team on how to get a non-paginated response
        // If that is not possible we will need to add support for paged results.
        request.setResourceRef( url );
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
                this.logger.info(
                    "No cache or cacheManager properties have been set.  Authorization cache cannot be obtained." );

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
