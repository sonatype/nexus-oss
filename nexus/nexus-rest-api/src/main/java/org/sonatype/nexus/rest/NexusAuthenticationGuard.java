/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.NexusConfiguration;
import org.sonatype.nexus.security.AuthenticationSource;
import org.sonatype.nexus.security.SimpleUser;
import org.sonatype.nexus.security.User;
import org.sonatype.nexus.session.Session;
import org.sonatype.nexus.session.SessionStore;
import org.sonatype.plexus.rest.PlexusRestletUtils;

/**
 * A guard to protect Nexus Repositories and Routers (ResourceStorages). Current implementation simply injects the
 * Anonymous user to request context.
 * 
 * @author cstamas
 */
public class NexusAuthenticationGuard
    extends Guard
{
    public static final String REST_USER_KEY = "user";

    public static final String NEXUS_AUTH_TOKEN_SCHEME = "HTTP_NexusAuthToken";

    public static final String NEXUS_AUTH_TOKEN_KEY = "NexusAuthToken";

    private SessionStore sessionStore;

    private AuthenticationSource authenticationSource;

    private String usernameFilter;

    public NexusAuthenticationGuard( Context context )
    {
        super( context, ChallengeScheme.HTTP_BASIC, "Nexus REST API" );

        try
        {
            sessionStore = (SessionStore) PlexusRestletUtils.plexusLookup( getContext(), SessionStore.ROLE );

            NexusConfiguration nc = (NexusConfiguration) PlexusRestletUtils.plexusLookup(
                getContext(),
                NexusConfiguration.ROLE );

            authenticationSource = nc.getAuthenticationSource();
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Cannot lookup sessionStore or authenticationSource!", e );
        }
        catch ( ConfigurationException e )
        {
            throw new IllegalStateException( "Configuration exception!", e );
        }
    }

    public NexusAuthenticationGuard( Context context, String usernameFilter )
    {
        this( context );

        this.usernameFilter = usernameFilter;
    }

    public SessionStore getSessionStore()
    {
        return sessionStore;
    }

    public AuthenticationSource getAuthenticationSource()
    {
        return authenticationSource;
    }

    public String getUsernameFilter()
    {
        return usernameFilter;
    }

    public void doHandle( Request request, Response response )
    {
        super.doHandle( request, response );

        if ( Status.CLIENT_ERROR_UNAUTHORIZED.equals( response.getStatus() ) )
        {
            challenge( response );
        }
    }

    public int authenticate( Request request )
    {
        int result = 0;

        String token = null;

        Session session = null;

        ChallengeResponse cr = request.getChallengeResponse();

        if ( cr != null && NEXUS_AUTH_TOKEN_SCHEME.equals( cr.getScheme().getName() ) )
        {
            // we have challenge, let's start with it as wrong
            result = -1;

            // The challenge schemes are compatible
            token = request.getChallengeResponse().getCredentials();

            if ( token != null )
            {
                session = getSessionStore().getSession( token );

                if ( session != null )
                {
                    // check IP
                    if ( session.getRemoteAddress().equals( request.getClientInfo().getAddress() ) )
                    {
                        result = 1;
                    }
                    else
                    {
                        session = null;

                        token = null;
                    }
                }
                else
                {
                    token = null;
                }
            }
        }

        if ( result == 0 )
        {
            result = super.authenticate( request );
        }

        if ( result == 1 )
        {
            if ( session != null )
            {
                request.getAttributes().put( NEXUS_AUTH_TOKEN_KEY, token );

                request.getAttributes().put( REST_USER_KEY, session.getUser() );
            }
            else
            {
                SimpleUser user = new SimpleUser( request.getChallengeResponse().getIdentifier() );

                request.getAttributes().put( REST_USER_KEY, user );
            }
        }

        if ( getUsernameFilter() == null && result != 1 && getAuthenticationSource().isAnynonymousAllowed() )
        {
            request.getAttributes().put( REST_USER_KEY, SimpleUser.ANONYMOUS_USER );

            return 1;
        }
        else
        {
            return result;
        }

    }

    protected boolean checkSecret( String identifier, char[] secret )
    {
        if ( getUsernameFilter() == null || getUsernameFilter().equals( identifier ) )
        {
            if ( secret == null )
            {
                return false;
            }

            User user = getAuthenticationSource().authenticate( identifier, new String( secret ) );

            return user != null && !user.isAnonymous();
        }
        else
        {
            return false;
        }
    }

    protected String getHeader( Request request, String header )
    {
        String result = null;

        Form headers = (Form) request.getAttributes().get( "org.restlet.http.headers" );

        result = headers.getFirstValue( header );

        if ( result == null )
        { // try lowercase; some containers do this
            result = headers.getFirstValue( header.toLowerCase() );
        }

        return result;
    }

    protected void addHeader( Response response, String header, String value )
    {
        Form headers = (Form) response.getAttributes().get( "org.restlet.http.headers" );

        if ( headers == null )
        {
            headers = new Form();

            response.getAttributes().put( "org.restlet.http.headers", headers );
        }

        headers.add( header, value );
    }
}
