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
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.UsernamePasswordToken;
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
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.security.InvalidCredentialsException;
import org.sonatype.nexus.configuration.security.NoSuchUserException;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.SmtpSettings;

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
                    getNexus().updateApplicationLogDirectory( resource.getLogDirectory() );

                    if ( resource.getSmtpSettings() != null )
                    {
                        SmtpSettings settings = resource.getSmtpSettings();

                        CSmtpConfiguration config = new CSmtpConfiguration();

                        config.setHost( settings.getHost() );

                        config.setPassword( settings.getPassword() );

                        config.setPort( settings.getPort() );

                        config.setSslEnabled( settings.isSslEnabled() );

                        config.setTlsEnabled( settings.isTlsEnabled() );

                        config.setUsername( settings.getUsername() );

                        config.setSystemEmailAddress( settings.getSystemEmailAddress() );

                        getNexus().updateSmtpConfiguration( config );
                    }

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

                    getNexus().setSecurityEnabled( resource.isSecurityEnabled() );

                    getNexus().setAnonymousAccessEnabled( resource.isSecurityAnonymousAccessEnabled() );

                    if ( resource.isSecurityAnonymousAccessEnabled() && resource.getSecurityAnonymousUsername() != null )
                    {
                        if ( getNexus().getAnonymousUsername().equals( resource.getSecurityAnonymousUsername() )
                            && !getNexus().getAnonymousPassword().equals( resource.getSecurityAnonymousPassword() ) )
                        {
                            // no user change, only password
                            try
                            {
                                getNexusSecurityConfiguration().changePassword(
                                    getNexus().getAnonymousUsername(),
                                    getNexus().getAnonymousPassword(),
                                    resource.getSecurityAnonymousPassword() );
                            }
                            catch ( InvalidCredentialsException e )
                            {
                                // the supplied anon auth info is wrong
                                getLogger()
                                    .log(
                                        Level.WARNING,
                                        "Nexus refused to apply configuration, the supplied anonymous information is wrong.",
                                        e );

                                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );

                                getResponse().setEntity(
                                    serialize( entity, getNexusErrorResponse( "securityAnonymousPassword", e
                                        .getMessage() ) ) );

                                return;
                            }
                            catch ( NoSuchUserException e )
                            {
                                // the supplied anon auth info is wrong
                                getLogger()
                                    .log(
                                        Level.WARNING,
                                        "Nexus refused to apply configuration, the supplied anonymous information is wrong.",
                                        e );

                                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );

                                getResponse().setEntity(
                                    serialize( entity, getNexusErrorResponse( "securityAnonymousUsername", e
                                        .getMessage() ) ) );

                                return;
                            }
                        }
                        else
                        {
                            // user changed too
                            try
                            {
                                // try to "log in" with supplied credentials
                                // the anon user a) should exists b) the pwd must work
                                getSecurityManager().authenticate(
                                    new UsernamePasswordToken( resource.getSecurityAnonymousUsername(), resource
                                        .getSecurityAnonymousPassword() ) );
                            }
                            catch ( AuthenticationException e )
                            {
                                // the supplied anon auth info is wrong
                                getLogger()
                                    .log(
                                        Level.WARNING,
                                        "Nexus refused to apply configuration, the supplied anonymous information is wrong.",
                                        e );

                                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );

                                getResponse().setEntity(
                                    serialize( entity, getNexusErrorResponse( "securityAnonymousUsername", e
                                        .getMessage() ) ) );

                                return;
                            }
                        }

                        getNexus().setAnonymousUsername( resource.getSecurityAnonymousUsername() );

                        getNexus().setAnonymousPassword( resource.getSecurityAnonymousPassword() );
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
        resource.setSecurityEnabled( getNexus().isDefaultSecurityEnabled() );

        resource.setSecurityAnonymousAccessEnabled( getNexus().isDefaultAnonymousAccessEnabled() );

        resource.setSecurityAnonymousUsername( getNexus().getDefaultAnonymousUsername() );

        resource.setSecurityAnonymousPassword( getNexus().getDefaultAnonymousPassword() );

        resource.setLogDirectory( getNexus().readDefaultApplicationLogDirectory() );

        resource.setGlobalConnectionSettings( convert( getNexus().readDefaultGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( getNexus().readDefaultGlobalRemoteHttpProxySettings() ) );

        resource.setSmtpSettings( convert( getNexus().readDefaultSmtpConfiguration() ) );
    }

    /**
     * Externalized Nexus object to DTO's conversion, using current Nexus configuration.
     * 
     * @param resource
     */
    protected void fillCurrentConfiguration( GlobalConfigurationResource resource )
    {
        resource.setSecurityEnabled( getNexus().isSecurityEnabled() );

        resource.setSecurityAnonymousAccessEnabled( getNexus().isAnonymousAccessEnabled() );

        resource.setSecurityAnonymousUsername( getNexus().getAnonymousUsername() );

        resource.setSecurityAnonymousPassword( getNexus().getAnonymousPassword() );

        resource.setLogDirectory( getNexus().readApplicationLogDirectory() );

        resource.setGlobalConnectionSettings( convert( getNexus().readGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( getNexus().readGlobalRemoteHttpProxySettings() ) );

        resource.setBaseUrl( getNexus().getBaseUrl() );

        resource.setSmtpSettings( convert( getNexus().readSmtpConfiguration() ) );
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
