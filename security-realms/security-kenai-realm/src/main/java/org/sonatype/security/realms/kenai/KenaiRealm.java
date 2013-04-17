/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Description;
import org.sonatype.security.realms.kenai.config.KenaiRealmConfiguration;

import com.google.common.collect.Lists;

/**
 * A Realm that connects to a java.net kenai API.
 *
 * @author Brian Demers
 */
@Singleton
@Typed( Realm.class )
@Named( "kenai" )
@Description( "Kenai Realm" )
public class KenaiRealm
    extends AuthorizingRealm
{

    private static final int PAGE_SIZE = 200;

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final KenaiRealmConfiguration kenaiRealmConfiguration;

    private final Provider<HttpClient> httpClientProvider;

    @Inject
    public KenaiRealm( final KenaiRealmConfiguration kenaiRealmConfiguration,
        final Provider<HttpClient> httpClientProvider )
    {
        this.kenaiRealmConfiguration = kenaiRealmConfiguration;
        this.httpClientProvider = httpClientProvider;

        // TODO: write another test before enabling this
        // this.setAuthenticationCachingEnabled( true );
    }

    @Override
    public String getName()
    {
        return "kenai";
    }

    // ------------ AUTHENTICATION ------------

    @Override
    public boolean supports( final AuthenticationToken token )
    {
        return ( token instanceof UsernamePasswordToken );
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( final AuthenticationToken token )
        throws AuthenticationException
    {
        final UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        // if the user can authenticate we are good to go
        if ( authenticateViaUrl( upToken ) )
        {
            return buildAuthenticationInfo( upToken );
        }
        else
        {
            throw new AccountException(
                "User \"" + upToken.getUsername() + "\" cannot be authenticated via Kenai Realm." );
        }
    }

    private AuthenticationInfo buildAuthenticationInfo( final UsernamePasswordToken token )
    {
        return new SimpleAuthenticationInfo( token.getPrincipal(), token.getCredentials(), getName() );
    }

    private boolean authenticateViaUrl( final UsernamePasswordToken usernamePasswordToken )
    {
        final HttpClient client = getHttpClient( null );

        try
        {
            final String url = kenaiRealmConfiguration.getConfiguration().getBaseUrl() + "api/login/authenticate.json";
            final List<NameValuePair> nameValuePairs = Lists.newArrayListWithCapacity( 2 );
            nameValuePairs.add( new BasicNameValuePair( "username", usernamePasswordToken.getUsername() ) );
            nameValuePairs.add( new BasicNameValuePair( "password", new String( usernamePasswordToken.getPassword() ) ) );
            final HttpPost post = new HttpPost( url );
            post.setEntity( new UrlEncodedFormEntity( nameValuePairs, Consts.UTF_8 ) );
            final HttpResponse response = client.execute( post );

            try
            {
                logger.debug( "User \"{}\" validated against URL={} as {}", usernamePasswordToken.getUsername(), url,
                              response.getStatusLine() );
                final boolean success = response.getStatusLine().getStatusCode() >= 200
                    && response.getStatusLine().getStatusCode() <= 299;
                return success;
            }
            finally
            {
                HttpClientUtils.closeQuietly( response );
            }
        }
        catch ( IOException e )
        {
            logger.info( "URLRealm was unable to perform authentication.", e );
            return false;
        }
    }

    // ------------ AUTHORIZATION ------------

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( final PrincipalCollection principals )
    {
        final String username = principals.getPrimaryPrincipal().toString();
        try
        {
            HttpResponse response = null;
            try
            {
                response = makeRemoteAuthzRequest( username, null );
                if ( isSuccess( response ) )
                {
                    return buildAuthorizationInfo( username, EntityUtils.toString( response.getEntity() ) );
                }
            }
            finally
            {
                HttpClientUtils.closeQuietly( response );
            }

            throw new AuthorizationException(
                "Failed to authorize user \"" + username + "\" for Kenai realm, got status: "
                    + response.getStatusLine() );
        }
        catch ( IOException e )
        {
            throw new AuthorizationException( "Failed to Authorize user \"" + username + "\"", e );
        }
        catch ( JSONException e )
        {
            throw new AuthorizationException(
                "Failed to parse JSON Authorization response for user \"" + username + "\"", e );
        }
    }

    private AuthorizationInfo buildAuthorizationInfo( final String username, final String responseText )
        throws JSONException, IOException
    {
        final SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        // add the default role
        authorizationInfo.addRole( kenaiRealmConfiguration.getConfiguration().getDefaultRole() );

        // collect roles from json object
        JSONObject jsonObject = new JSONObject( responseText );
        final Set<String> roles = buildRoleSetFromJsonObject( jsonObject );
        authorizationInfo.addRoles( roles );

        // check for pages
        while ( jsonObject.has( "next" ) && jsonObject.getString( "next" ) != "null" )
        {
            final String pagedURL = jsonObject.getString( "next" );
            logger.debug( "Next page of Kenai project info: {}", pagedURL );
            // make another remote request
            HttpResponse response = null;
            try
            {
                response = makeRemoteAuthzRequest( username, pagedURL );
                jsonObject = new JSONObject( EntityUtils.toString( response.getEntity() ) );
                authorizationInfo.addRoles( buildRoleSetFromJsonObject( jsonObject ) );
            }
            finally
            {
                HttpClientUtils.closeQuietly( response );
            }
        }

        return authorizationInfo;
    }

    private HttpResponse makeRemoteAuthzRequest( String username, String url )
    {
        final String remoteUrl;
        if ( url == null )
        {
            final StringBuilder buffer = new StringBuilder( kenaiRealmConfiguration.getConfiguration().getBaseUrl() );
            buffer.append( "api/projects?size=" ).append( PAGE_SIZE );
            buffer.append( "&username=" ).append( username );
            buffer.append( "&roles=" ).append( "admin%2Cdeveloper" ); // we want just the admin,developer projects
            remoteUrl = buffer.toString();
        }
        else
        {
            remoteUrl = url;
        }

        final HttpResponse response;
        try
        {
            response = getHttpClient( null ).execute( new HttpGet( remoteUrl ) );
            logger.debug( "User \"{}\" on URL {} got validation status {}", username, remoteUrl,
                          response.getStatusLine() );
            return response;
        }
        catch ( IOException e )
        {
            throw new AuthorizationException( "Failed to Authorize user \"" + username + "\"", e );
        }
    }

    private Set<String> buildRoleSetFromJsonObject( final JSONObject jsonObject )
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
                    logger.trace( "Found project {} in request", projectName );
                    roles.add( projectName );
                }
                else
                {
                    logger.debug( "Found empty string in json object projects[{}].name", ii );
                }
            }
        }
        return roles;
    }

    // ==

    private boolean isSuccess( final HttpResponse response )
    {
        return response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 299;
    }

    private HttpClient getHttpClient( final UsernamePasswordToken usernamePasswordToken )
    {
        // risky, but we must blindly assume it is
        final DefaultHttpClient client = (DefaultHttpClient) httpClientProvider.get();
        if ( usernamePasswordToken != null )
        {
            final List<String> authorisationPreference = new ArrayList<String>( 2 );
            authorisationPreference.add( AuthPolicy.DIGEST );
            authorisationPreference.add( AuthPolicy.BASIC );
            final Credentials credentials = new UsernamePasswordCredentials( usernamePasswordToken.getUsername(),
                                                                             String.valueOf(
                                                                                 usernamePasswordToken.getPassword() ) );
            client.getCredentialsProvider().setCredentials( AuthScope.ANY, credentials );
            client.getParams().setParameter( AuthPNames.TARGET_AUTH_PREF, authorisationPreference );
        }
        return client;
    }
}
