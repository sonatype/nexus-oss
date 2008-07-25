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

import java.io.IOException;
import java.util.logging.Level;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;

/**
 * The GlobalConfiguration resource handler. It simply gets and builds the requested config REST model (DTO) and passes
 * serializes it using underlying representation mechanism.
 * 
 * @author cstamas
 */
public class GlobalConfigurationResourceHandler
    extends AbstractGlobalConfigurationResourceHandler
{

    /** The config key used in URI and request attributes */
    public static final String CONFIG_NAME_KEY = "configName";

    /** Name denoting current Nexus configuration */
    public static final String CURRENT_CONFIG_NAME = "current";

    /** Name denoting default Nexus configuration */
    public static final String DEFAULT_CONFIG_NAME = "default";

    /**
     * The default Resource constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public GlobalConfigurationResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * This handler allows get.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * The representation handler executed on GET calls. Here we build a GlobalConfigurationResource object using Nexus
     * App calls and we ship it back to requestor.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        String configurationName = getRequest().getAttributes().get( CONFIG_NAME_KEY ).toString();

        if ( !DEFAULT_CONFIG_NAME.equals( configurationName ) && !CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND );

            return null;
        }
        else
        {
            GlobalConfigurationResource resource = new GlobalConfigurationResource();

            if ( DEFAULT_CONFIG_NAME.equals( configurationName ) )
            {
                fillDefaultConfiguration( resource );
            }
            else
            {
                fillCurrentConfiguration( resource );
            }

            GlobalConfigurationResourceResponse response = new GlobalConfigurationResourceResponse();

            response.setData( resource );

            return serialize( variant, response );
        }
    }

    /**
     * We allow PUT also (modifiying configuration).
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * On HTTP PUT we should have a DTO in representation entity sent from client. We simply deserialize it, convert it
     * and set the configuration using MutableConfiguration iface. Only the "current" config is PUTtable. On "default"
     * config we return an HTTP "Error not allowed" error.
     */
    public void put( Representation entity )
    {
        String configurationName = getRequest().getAttributes().get( CONFIG_NAME_KEY ).toString();

        if ( !DEFAULT_CONFIG_NAME.equals( configurationName ) && !CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND );

            return;
        }
        else if ( !CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );

            return;
        }
        else
        {
            GlobalConfigurationResourceResponse response = (GlobalConfigurationResourceResponse) deserialize( new GlobalConfigurationResourceResponse() );

            if ( response != null )
            {
                GlobalConfigurationResource resource = response.getData();

                try
                {
                    getNexus().updateWorkingDirectory( resource.getWorkingDirectory() );

                    getNexus().updateApplicationLogDirectory( resource.getLogDirectory() );

                    if ( resource.getGlobalConnectionSettings() != null )
                    {
                        RemoteConnectionSettings s = resource.getGlobalConnectionSettings();

                        CRemoteConnectionSettings connection = new CRemoteConnectionSettings();

                        connection.setConnectionTimeout( s.getConnectionTimeout() * 1000 );

                        connection.setRetrievalRetryCount( s.getRetrievalRetryCount() );

                        connection.setQueryString( s.getQueryString() );

                        connection.setUserAgentString( s.getUserAgentString() );

                        getNexus().updateGlobalRemoteConnectionSettings( connection );
                    }

                    if ( resource.getGlobalHttpProxySettings() != null
                        && !StringUtils.isEmpty( resource.getGlobalHttpProxySettings().getProxyHostname() ) )
                    {
                        RemoteHttpProxySettings s = resource.getGlobalHttpProxySettings();

                        CRemoteHttpProxySettings proxy = new CRemoteHttpProxySettings();

                        proxy.setProxyHostname( s.getProxyHostname() );

                        proxy.setProxyPort( s.getProxyPort() );

                        if ( s.getAuthentication() != null )
                        {
                            CRemoteAuthentication auth = new CRemoteAuthentication();

                            auth.setUsername( s.getAuthentication().getUsername() );

                            auth.setPassword( s.getAuthentication().getPassword() );

                            auth.setNtlmDomain( s.getAuthentication().getNtlmDomain() );

                            auth.setNtlmHost( s.getAuthentication().getNtlmHost() );

                            auth.setPrivateKey( s.getAuthentication().getPrivateKey() );

                            auth.setPassphrase( s.getAuthentication().getPassphrase() );

                            proxy.setAuthentication( auth );
                        }

                        getNexus().updateGlobalRemoteHttpProxySettings( proxy );
                    }
                    else
                    {
                        getNexus().updateGlobalRemoteHttpProxySettings( null );
                    }

                    if ( resource.getSecurityConfiguration() != null )
                    {
                        if ( "off".equalsIgnoreCase( resource.getSecurityConfiguration() ) )
                        {
                            getNexus().setSecurity( false, null );
                        }
                        else if ( "simple".equalsIgnoreCase( resource.getSecurityConfiguration() ) )
                        {
                            getNexus().setSecurity( true, "simple" );
                        }
                        else if ( "custom".equalsIgnoreCase( resource.getSecurityConfiguration() ) )
                        {
                            getNexus().setSecurity( true, "properties" );
                        }
                    }

                    if ( resource.getBaseUrl() != null )
                    {
                        if ( StringUtils.isEmpty( resource.getBaseUrl() ) )
                        {
                            // resetting it
                            getNexus().setBaseUrl( null );
                        }
                        else
                        {
                            // setting it
                            getNexus().setBaseUrl( resource.getBaseUrl() );
                        }
                    }
                }
                catch ( ConfigurationException e )
                {
                    getLogger().log( Level.WARNING, "Nexus refused to apply configuration.", e );

                    getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );

                    getResponse().setEntity( serialize( entity, getNexusErrorResponse( "*", e.getMessage() ) ) );

                    return;
                }
                catch ( IOException e )
                {
                    getLogger().log( Level.SEVERE, "Got IO Exception during update of Nexus configuration.", e );

                    getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                    return;
                }

            }
        }

    }

    /**
     * Externalized Nexus object to DTO's conversion, using default Nexus configuration.
     * 
     * @param resource
     */
    protected void fillDefaultConfiguration( GlobalConfigurationResource resource )
    {
        resource.setSecurityConfiguration( getSecurityConfiguration( getNexus().isDefaultSecurityEnabled(), getNexus()
            .getDefaultAuthenticationSourceType() ) );

        resource.setWorkingDirectory( getNexus().readDefaultWorkingDirectory() );

        resource.setLogDirectory( getNexus().readDefaultApplicationLogDirectory() );

        resource.setGlobalConnectionSettings( convert( getNexus().readDefaultGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( getNexus().readDefaultGlobalRemoteHttpProxySettings() ) );
    }

    /**
     * Externalized Nexus object to DTO's conversion, using current Nexus configuration.
     * 
     * @param resource
     */
    protected void fillCurrentConfiguration( GlobalConfigurationResource resource )
    {
        resource.setSecurityConfiguration( getSecurityConfiguration( getNexus().isSecurityEnabled(), getNexus()
            .getAuthenticationSourceType() ) );

        resource.setWorkingDirectory( getNexus().readWorkingDirectory() );

        resource.setLogDirectory( getNexus().readApplicationLogDirectory() );

        resource.setGlobalConnectionSettings( convert( getNexus().readGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( getNexus().readGlobalRemoteHttpProxySettings() ) );

        resource.setBaseUrl( getNexus().getBaseUrl() );
    }

    protected String getSecurityConfiguration( boolean enabled, String authSourceType )
    {
        if ( !enabled )
        {
            return SECURITY_OFF;
        }
        else
        {
            if ( SECURITY_SIMPLE.equals( authSourceType ) )
            {
                return SECURITY_SIMPLE;
            }
            else
            {
                return SECURITY_CUSTOM;
            }
        }
    }

}
