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
package org.sonatype.nexus.rest.global;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.AuthenticationSettings;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;

/**
 * The base class for global configuration resources.
 * 
 * @author cstamas
 */
public abstract class AbstractGlobalConfigurationResourceHandler
    extends AbstractNexusResourceHandler
{
    public static final String SECURITY_OFF = "off";

    public static final String SECURITY_SIMPLE = "simple";
    
    public static final String SECURITY_CUSTOM = "custom";
    
    public AbstractGlobalConfigurationResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static RemoteConnectionSettings convert( CRemoteConnectionSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }

        RemoteConnectionSettings result = new RemoteConnectionSettings();

        result.setConnectionTimeout( settings.getConnectionTimeout() / 1000 );

        result.setRetrievalRetryCount( settings.getRetrievalRetryCount() );

        result.setQueryString( settings.getQueryString() );

        result.setUserAgentString( settings.getUserAgentString() );

        return result;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static RemoteHttpProxySettings convert( CRemoteHttpProxySettings settings )
    {
        if ( settings == null )
        {
            return null;
        }

        RemoteHttpProxySettings result = new RemoteHttpProxySettings();

        result.setProxyHostname( settings.getProxyHostname() );

        result.setProxyPort( settings.getProxyPort() );

        result.setAuthentication( convert( settings.getAuthentication() ) );

        return result;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static AuthenticationSettings convert( CRemoteAuthentication settings )
    {
        if ( settings == null )
        {
            return null;
        }

        AuthenticationSettings auth = new AuthenticationSettings();

        auth.setUsername( settings.getUsername() );

        auth.setPassword( settings.getPassword() );

        auth.setNtlmHost( settings.getNtlmHost() );

        auth.setNtlmDomain( settings.getNtlmDomain() );

        auth.setPrivateKey( settings.getPrivateKey() );

        auth.setPassphrase( settings.getPassphrase() );

        return auth;
    }

}
