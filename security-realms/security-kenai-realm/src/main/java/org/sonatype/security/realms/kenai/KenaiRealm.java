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
import java.util.List;

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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
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
            throw new AccountException( "User \"" + upToken.getUsername()
                + "\" cannot be authenticated via Kenai Realm." );
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
                final boolean success =
                    response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 299;
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
        // shortcut for now
        return buildAuthorizationInfo();
    }

    private AuthorizationInfo buildAuthorizationInfo()
    {
        final SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        // add the default role
        authorizationInfo.addRole( kenaiRealmConfiguration.getConfiguration().getDefaultRole() );
        return authorizationInfo;
    }

    // ==

    private HttpClient getHttpClient( final UsernamePasswordToken usernamePasswordToken )
    {
        // risky, but we must blindly assume it is
        final DefaultHttpClient client = (DefaultHttpClient) httpClientProvider.get();
        if ( usernamePasswordToken != null )
        {
            final List<String> authorisationPreference = new ArrayList<String>( 2 );
            authorisationPreference.add( AuthPolicy.DIGEST );
            authorisationPreference.add( AuthPolicy.BASIC );
            final Credentials credentials =
                new UsernamePasswordCredentials( usernamePasswordToken.getUsername(),
                    String.valueOf( usernamePasswordToken.getPassword() ) );
            client.getCredentialsProvider().setCredentials( AuthScope.ANY, credentials );
            client.getParams().setParameter( AuthPNames.TARGET_AUTH_PREF, authorisationPreference );
        }
        return client;
    }
}
