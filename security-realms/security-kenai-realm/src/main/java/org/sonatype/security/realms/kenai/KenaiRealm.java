/**
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
    public boolean supports( AuthenticationToken token )
    {
        return UsernamePasswordToken.class.isAssignableFrom( token.getClass() );
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

    protected AuthenticationInfo buildAuthenticationInfo( Object principal, Object credentials )
    {
        return new SimpleAuthenticationInfo( principal, credentials, getName() );
    }

    private boolean authenticateViaUrl( String username, String password )
    {
        StringBuffer buffer =
            new StringBuffer( this.kenaiRealmConfiguration.getConfiguration().getBaseUrl() ).append( "api/login/authenticate.json" );
        Response response = this.makeRemoteAuthcRequest( username, password, buffer.toString() );

        try
        {
            boolean success = response.getStatus().isSuccess();
            if ( !success )
            {
                this.logger.debug( "Failed to authenticate user: {} for url: {} status: {}", new Object[] { username,
                    response.getRequest().getResourceRef(), response.getStatus() } );
            }
            return success;
        }
        finally
        {
            if ( response != null )
            {
                response.release();
            }
        }
    }

    private Response makeRemoteAuthcRequest( String username, String password, String url )
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

    // ------------ AUTHORIZATION ------------

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        try
        {
            return this.authorizeViaUrl( principals.getPrimaryPrincipal().toString() );
        }
        catch ( IOException e )
        {
            throw new AuthorizationException( "Failed to Authorize user " + principals.getPrimaryPrincipal(), e );
        }
        catch ( JSONException e )
        {
            throw new AuthorizationException( "Failed to Authorize user " + principals.getPrimaryPrincipal(), e );
        }
    }

    private AuthorizationInfo authorizeViaUrl( String username )
        throws IOException, JSONException
    {
        Response response = null;

        try
        {
            response = this.makeRemoteAuthzRequest( username );
            if ( response.getStatus().isSuccess() )
            {
                AuthorizationInfo authorizationInfo =
                    this.buildAuthorizationInfo( username, response.getEntity().getText() );

                return authorizationInfo;
            }
        }
        finally
        {
            if ( response != null )
            {
                response.release();
            }
        }

        throw new AuthorizationException( "Failed to authorize user: " + username + " for url: "
            + response.getRequest().getResourceRef() + " status:" + response.getStatus() );

    }

    private AuthorizationInfo buildAuthorizationInfo( String username, String responseText )
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
                response = this.makeRemoteAuthzRequest( username, pagedURL );
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

    private Response makeRemoteAuthzRequest( String username )
    {
        StringBuffer buffer = new StringBuffer( this.kenaiRealmConfiguration.getConfiguration().getBaseUrl() );
        buffer.append( "api/projects?size=" ).append( PAGE_SIZE );
        buffer.append( "&username=" ).append( username );
        buffer.append( "&roles=" ).append( "admin%2Cdeveloper" ); // we want just the admin,developer projects

        return makeRemoteAuthzRequest( username, buffer.toString() );
    }

    private Response makeRemoteAuthzRequest( String username, String url )
    {
        Client restClient = new Client( new Context(), Protocol.HTTP );
        Request request = new Request();
        request.setResourceRef( url );
        request.setMethod( Method.GET );
        Response response = restClient.handle( request );
        this.logger.debug( "User: " + username + " url validation status: " + response.getStatus() );

        return response;
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
}
