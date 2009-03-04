/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.global;

import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.PlexusSecurity;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.SmtpSettings;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * The GlobalConfiguration resource. It simply gets and builds the requested config REST model (DTO) and passes
 * serializes it using underlying representation mechanism.
 * 
 * @author cstamas
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "GlobalConfigurationPlexusResource" )
public class GlobalConfigurationPlexusResource
    extends AbstractGlobalConfigurationPlexusResource
{
    /** The config key used in URI and request attributes */
    public static final String CONFIG_NAME_KEY = "configName";

    /** Name denoting current Nexus configuration */
    public static final String CURRENT_CONFIG_NAME = "current";

    /** Name denoting default Nexus configuration */
    public static final String DEFAULT_CONFIG_NAME = "default";

    @Requirement( hint = "web" )
    private PlexusSecurity securityManager;

    public GlobalConfigurationPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new GlobalConfigurationResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/global_settings/{" + CONFIG_NAME_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/global_settings/*", "authcBasic,perms[nexus:settings]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String configurationName = request.getAttributes().get( CONFIG_NAME_KEY ).toString();

        if ( !DEFAULT_CONFIG_NAME.equals( configurationName ) && !CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        else
        {
            GlobalConfigurationResource resource = new GlobalConfigurationResource();

            if ( DEFAULT_CONFIG_NAME.equals( configurationName ) )
            {
                fillDefaultConfiguration( request, resource );
            }
            else
            {
                fillCurrentConfiguration( request, resource );
            }

            GlobalConfigurationResourceResponse result = new GlobalConfigurationResourceResponse();

            result.setData( resource );

            return result;
        }
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        String configurationName = request.getAttributes().get( CONFIG_NAME_KEY ).toString();

        if ( !DEFAULT_CONFIG_NAME.equals( configurationName ) && !CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        else if ( !CURRENT_CONFIG_NAME.equals( configurationName ) )
        {
            throw new ResourceException( Status.CLIENT_ERROR_METHOD_NOT_ALLOWED );
        }
        else
        {
            GlobalConfigurationResourceResponse configRequest = (GlobalConfigurationResourceResponse) payload;

            if ( configRequest != null )
            {
                GlobalConfigurationResource resource = configRequest.getData();

                try
                {
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

                        config.setSystemEmailAddress( settings.getSystemEmailAddress().trim() );

                        getNexus().updateSmtpConfiguration( config );
                    }

                    if ( resource.getGlobalConnectionSettings() != null )
                    {
                        RemoteConnectionSettings s = resource.getGlobalConnectionSettings();

                        CRemoteConnectionSettings connection = new CRemoteConnectionSettings();

                        connection.setConnectionTimeout( s.getConnectionTimeout() * 1000 );

                        connection.setRetrievalRetryCount( s.getRetrievalRetryCount() );

                        connection.setQueryString( s.getQueryString() );

                        connection.setUserAgentCustomizationString( s.getUserAgentString() );

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

                    getNexus().setRealms( (List<String>) resource.getSecurityRealms() );

                    getNexus().setSecurityEnabled( resource.isSecurityEnabled() );

                    getNexus().setAnonymousAccessEnabled( resource.isSecurityAnonymousAccessEnabled() );

                    if ( resource.isSecurityAnonymousAccessEnabled()
                        && !StringUtils.isEmpty( resource.getSecurityAnonymousUsername() )
                        && !StringUtils.isEmpty( resource.getSecurityAnonymousPassword() ) )
                    {
                        if ( getNexus().getAnonymousUsername().equals( resource.getSecurityAnonymousUsername() )
                            && !getNexus().getAnonymousPassword().equals( resource.getSecurityAnonymousPassword() ) )
                        {
                            // no user change, only password

                            /*
                             * TODO getNexusSecurityConfiguration().changePassword( getNexusInstance( request
                             * ).getAnonymousUsername(), getNexusInstance( request ).getAnonymousPassword(),
                             * resource.getSecurityAnonymousPassword() );
                             */
                        }
                        else
                        {
                            // user changed too
                            try
                            {
                                // try to "log in" with supplied credentials
                                // the anon user a) should exists b) the pwd must work
                                securityManager.authenticate( new UsernamePasswordToken( resource
                                    .getSecurityAnonymousUsername(), resource.getSecurityAnonymousPassword() ) );
                            }
                            catch ( AuthenticationException e )
                            {
                                // the supplied anon auth info is wrong
                                getLogger()
                                    .warn(
                                        "Nexus refused to apply configuration, the supplied anonymous information is wrong.",
                                        e );

                                throw new PlexusResourceException(
                                    Status.CLIENT_ERROR_BAD_REQUEST,
                                    e.getMessage(),
                                    getNexusErrorResponse( "securityAnonymousUsername", e.getMessage() ) );
                            }
                        }

                        getNexus().setAnonymousUsername( resource.getSecurityAnonymousUsername() );

                        getNexus().setAnonymousPassword( resource.getSecurityAnonymousPassword() );
                    }
                    else if ( resource.isSecurityAnonymousAccessEnabled() )
                    {
                        // the supplied anon auth info is wrong
                        getLogger()
                            .warn(
                                "Nexus refused to apply configuration, the supplied anonymous username/pwd information is empty." );

                        throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, getNexusErrorResponse(
                            "securityAnonymousUsername",
                            "Cannot be empty when Anonynous access is enabled" ) );
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
                            // setting it using reference object to normalize the hostname (all lowercase)
                            getNexus().setBaseUrl( new Reference( resource.getBaseUrl() ).getTargetRef().toString() );
                        }

                        getNexus().setForceBaseUrl( resource.isForceBaseUrl() );
                    }
                }
                catch ( ConfigurationException e )
                {
                    getLogger().warn( "Nexus refused to apply configuration.", e );

                    throw new PlexusResourceException(
                        Status.CLIENT_ERROR_BAD_REQUEST,
                        e.getMessage(),
                        getNexusErrorResponse( "*", e.getMessage() ) );
                }
                catch ( IOException e )
                {
                    getLogger().warn( "Got IO Exception during update of Nexus configuration.", e );

                    throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
                }

            }
        }
        // TODO: this method needs some serious cleaning up...
        response.setStatus( Status.SUCCESS_NO_CONTENT );
        return null;
    }

    /**
     * Externalized Nexus object to DTO's conversion, using default Nexus configuration.
     * 
     * @param resource
     */
    protected void fillDefaultConfiguration( Request request, GlobalConfigurationResource resource )
    {
        resource.setSecurityEnabled( getNexus().isDefaultSecurityEnabled() );

        resource.setSecurityAnonymousAccessEnabled( getNexus().isDefaultAnonymousAccessEnabled() );

        resource.setSecurityRealms( getNexus().getDefaultRealms() );

        resource.setSecurityAnonymousUsername( getNexus().getDefaultAnonymousUsername() );

        resource.setSecurityAnonymousPassword( getNexus().getDefaultAnonymousPassword() );

        resource.setGlobalConnectionSettings( convert( getNexus().readDefaultGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( getNexus().readDefaultGlobalRemoteHttpProxySettings() ) );

        resource.setBaseUrl( getContextRoot( request ).getTargetRef().toString() );

        resource.setForceBaseUrl( getNexus().isForceBaseUrl() );

        resource.setSmtpSettings( convert( getNexus().readDefaultSmtpConfiguration() ) );
    }

    /**
     * Externalized Nexus object to DTO's conversion, using current Nexus configuration.
     * 
     * @param resource
     */
    protected void fillCurrentConfiguration( Request request, GlobalConfigurationResource resource )
    {
        resource.setSecurityEnabled( getNexus().isSecurityEnabled() );

        resource.setSecurityAnonymousAccessEnabled( getNexus().isAnonymousAccessEnabled() );

        resource.setSecurityRealms( getNexus().getRealms() );

        resource.setSecurityAnonymousUsername( getNexus().getAnonymousUsername() );

        resource.setSecurityAnonymousPassword( getNexus().getAnonymousPassword() );

        resource.setGlobalConnectionSettings( convert( getNexus().readGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( getNexus().readGlobalRemoteHttpProxySettings() ) );

        resource.setBaseUrl( StringUtils.isEmpty( getNexus().getBaseUrl() ) ? getContextRoot( request ).getTargetRef().toString() : getNexus().getBaseUrl() );

        resource.setForceBaseUrl( getNexus().isForceBaseUrl() );

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
