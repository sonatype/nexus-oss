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
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authc.UsernamePasswordToken;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.micromailer.Address;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.RestApiSettings;
import org.sonatype.nexus.rest.model.SmtpSettings;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * The GlobalConfiguration resource. It simply gets and builds the requested config REST model (DTO) and passes
 * serializes it using underlying representation mechanism.
 * 
 * @author cstamas
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "GlobalConfigurationPlexusResource" )
@Path( GlobalConfigurationPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class GlobalConfigurationPlexusResource
    extends AbstractGlobalConfigurationPlexusResource
{
    /** The config key used in URI and request attributes */
    public static final String CONFIG_NAME_KEY = "configName";

    public static final String RESOURCE_URI = "/global_settings/{" + CONFIG_NAME_KEY + "}";

    /** Name denoting current Nexus configuration */
    public static final String CURRENT_CONFIG_NAME = "current";

    /** Name denoting default Nexus configuration */
    public static final String DEFAULT_CONFIG_NAME = "default";

    @Requirement
    private SecuritySystem securitySystem;

    // DEFAULT CONFIG
    // ==
    @Requirement( hint = "static" )
    private SecurityConfigurationSource defaultSecurityConfigurationSource;

    @Requirement( hint = "static" )
    private ApplicationConfigurationSource configurationSource;

    // ----------------------------------------------------------------------------
    // Default Configuration
    // ----------------------------------------------------------------------------

    public boolean isDefaultSecurityEnabled()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().isEnabled();
    }

    public boolean isDefaultAnonymousAccessEnabled()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().isAnonymousAccessEnabled();
    }

    public String getDefaultAnonymousUsername()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().getAnonymousUsername();
    }

    public String getDefaultAnonymousPassword()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().getAnonymousPassword();
    }

    public List<String> getDefaultRealms()
    {
        return this.defaultSecurityConfigurationSource.getConfiguration().getRealms();
    }

    public CRemoteConnectionSettings readDefaultGlobalRemoteConnectionSettings()
    {
        return configurationSource.getConfiguration().getGlobalConnectionSettings();
    }

    public CRemoteHttpProxySettings readDefaultGlobalRemoteHttpProxySettings()
    {
        return configurationSource.getConfiguration().getGlobalHttpProxySettings();
    }

    public CRestApiSettings readDefaultRestApiSettings()
    {
        return configurationSource.getConfiguration().getRestApi();
    }

    public CSmtpConfiguration readDefaultSmtpConfiguration()
    {
        return configurationSource.getConfiguration().getSmtpConfiguration();
    }

    // ==

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
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/global_settings/*", "authcBasic,perms[nexus:settings]" );
    }

    /**
     * Get the specified global configuration (i.e. current or default)
     * 
     * @param configName The name of the config (as returned by the global configuration list resource) to get.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( GlobalConfigurationPlexusResource.CONFIG_NAME_KEY ) }, output = GlobalConfigurationResourceResponse.class )
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

    /**
     * Update the global configuration.
     * 
     * @param configName The name of the config (as returned by the global configuration list resource) to update.
     */
    @Override
    @PUT
    @ResourceMethodSignature( pathParams = { @PathParam( GlobalConfigurationPlexusResource.CONFIG_NAME_KEY ) }, input = GlobalConfigurationResourceResponse.class )
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

                        getNexusEmailer().setSMTPHostname( settings.getHost() );

                        // lookup old password
                        String oldPassword = getNexusEmailer().getSMTPPassword();

                        getNexusEmailer().setSMTPPassword( this.getActualPassword( settings.getPassword(), oldPassword ) );

                        getNexusEmailer().setSMTPPort( settings.getPort() );

                        getNexusEmailer().setSMTPSslEnabled( settings.isSslEnabled() );

                        getNexusEmailer().setSMTPTlsEnabled( settings.isTlsEnabled() );

                        getNexusEmailer().setSMTPUsername( settings.getUsername() );

                        getNexusEmailer().setSMTPSystemEmailAddress(
                                                                     new Address(
                                                                                  settings.getSystemEmailAddress().trim() ) );
                    }

                    ErrorReportingSettings settings = resource.getErrorReportingSettings();

                    if ( settings != null )
                    {
                        getErrorReportingManager().setEnabled( settings.isReportErrorsAutomatically() );
                        getErrorReportingManager().setJIRAUsername( settings.getJiraUsername() );
                        // look up old password
                        getErrorReportingManager().setJIRAPassword(
                                                                    this.getActualPassword(
                                                                                            settings.getJiraPassword(),
                                                                                            getErrorReportingManager().getJIRAPassword() ) );
                        getErrorReportingManager().setUseGlobalProxy( settings.isUseGlobalProxy() );
                    }
                    else
                    {
                        getErrorReportingManager().setEnabled( false );
                        getErrorReportingManager().setJIRAUsername( null );
                        getErrorReportingManager().setJIRAPassword( null );
                    }

                    if ( resource.getGlobalConnectionSettings() != null )
                    {
                        RemoteConnectionSettings s = resource.getGlobalConnectionSettings();

                        getGlobalRemoteConnectionSettings().setConnectionTimeout( s.getConnectionTimeout() * 1000 );

                        getGlobalRemoteConnectionSettings().setRetrievalRetryCount( s.getRetrievalRetryCount() );

                        getGlobalRemoteConnectionSettings().setQueryString( s.getQueryString() );

                        getGlobalRemoteConnectionSettings().setUserAgentCustomizationString( s.getUserAgentString() );
                    }

                    if ( resource.getGlobalHttpProxySettings() != null
                        && !StringUtils.isEmpty( resource.getGlobalHttpProxySettings().getProxyHostname() ) )
                    {
                        RemoteHttpProxySettings s = resource.getGlobalHttpProxySettings();

                        getGlobalHttpProxySettings().setHostname( s.getProxyHostname() );

                        getGlobalHttpProxySettings().setPort( s.getProxyPort() );

                        List<String> nonProxyHosts = resource.getGlobalHttpProxySettings().getNonProxyHosts();
                        if ( nonProxyHosts != null && !nonProxyHosts.isEmpty() )
                        {
                            // removing nulls and empty strings
                            HashSet<String> cleanNonProxyHosts = new HashSet<String>();
                            for ( String host : nonProxyHosts )
                            {
                                if ( StringUtils.isNotEmpty( host ) )
                                {
                                    cleanNonProxyHosts.add( host );
                                }
                            }
                            getGlobalHttpProxySettings().setNonProxyHosts( cleanNonProxyHosts );
                        }
                        else
                        {
                            // clear it out
                            getGlobalHttpProxySettings().setNonProxyHosts( new HashSet<String>( 0 ) );
                        }

                        if ( s.getAuthentication() != null )
                        {
                            CRemoteAuthentication auth = new CRemoteAuthentication();

                            auth.setUsername( s.getAuthentication().getUsername() );

                            String oldPassword = null;
                            if ( getGlobalHttpProxySettings().getProxyAuthentication() != null )
                            {
                                oldPassword =
                                    ( (UsernamePasswordRemoteAuthenticationSettings) getGlobalHttpProxySettings().getProxyAuthentication() ).getPassword();
                            }

                            auth.setPassword( this.getActualPassword( s.getAuthentication().getPassword(), oldPassword ) );

                            auth.setNtlmDomain( s.getAuthentication().getNtlmDomain() );

                            auth.setNtlmHost( s.getAuthentication().getNtlmHost() );

                            // auth.setPrivateKey( s.getAuthentication().getPrivateKey() );

                            // auth.setPassphrase( s.getAuthentication().getPassphrase() );

                            getGlobalHttpProxySettings().setProxyAuthentication(
                                                                                 getAuthenticationInfoConverter().convertAndValidateFromModel(
                                                                                                                                               auth ) );
                        }
                        else
                        {
                            getGlobalHttpProxySettings().setProxyAuthentication( null );
                        }
                    }
                    else
                    {
                        getGlobalHttpProxySettings().disable();
                    }

                    getNexusConfiguration().setRealms( resource.getSecurityRealms() );

                    getNexusConfiguration().setSecurityEnabled( resource.isSecurityEnabled() );

                    getNexusConfiguration().setAnonymousAccessEnabled( resource.isSecurityAnonymousAccessEnabled() );

                    if ( resource.isSecurityAnonymousAccessEnabled()
                        && !StringUtils.isEmpty( resource.getSecurityAnonymousUsername() )
                        && !StringUtils.isEmpty( resource.getSecurityAnonymousPassword() ) )
                    {

                        // check if the user/pass changed
                        String oldPassword = getNexusConfiguration().getAnonymousPassword();
                        String newPassword =
                            this.getActualPassword( resource.getSecurityAnonymousPassword(), oldPassword );

                        if ( !StringUtils.equals( getNexusConfiguration().getAnonymousUsername(),
                                                  resource.getSecurityAnonymousUsername() )
                            || !StringUtils.equals( newPassword, oldPassword ) )
                        {
                            // test auth
                            try
                            {
                                // try to "log in" with supplied credentials
                                // the anon user a) should exists b) the pwd must work
                                securitySystem.getUser( resource.getSecurityAnonymousUsername() );

                                securitySystem.authenticate( new UsernamePasswordToken(
                                                                                        resource.getSecurityAnonymousUsername(),
                                                                                        newPassword ) );

                            }
                            catch ( UserNotFoundException e )
                            {

                                getLogger().warn(
                                                  "Nexus refused to apply configuration, the supplied anonymous information is wrong.",
                                                  e );

                                String msg = "User '" + resource.getSecurityAnonymousUsername() + "' does not exist.";

                                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, msg,
                                                                   getNexusErrorResponse( "securityAnonymousUsername",
                                                                                          msg ) );
                            }
                            catch ( AuthenticationException e )
                            {
                                // the supplied anon auth info is wrong
                                getLogger().warn(
                                                  "Nexus refused to apply configuration, the supplied anonymous information is wrong.",
                                                  e );

                                String msg =
                                    "The password of user '" + resource.getSecurityAnonymousUsername()
                                        + "' is incorrect.";

                                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, msg,
                                                                   getNexusErrorResponse( "securityAnonymousPassword",
                                                                                          msg ) );
                            }
                        }

                        getNexusConfiguration().setAnonymousUsername( resource.getSecurityAnonymousUsername() );

                        getNexusConfiguration().setAnonymousPassword( newPassword );
                    }
                    else if ( resource.isSecurityAnonymousAccessEnabled() )
                    {
                        // the supplied anon auth info is wrong
                        getLogger().warn(
                                          "Nexus refused to apply configuration, the supplied anonymous username/pwd information is empty." );

                        throw new PlexusResourceException(
                                                           Status.CLIENT_ERROR_BAD_REQUEST,
                                                           getNexusErrorResponse( "securityAnonymousUsername",
                                                                                  "Cannot be empty when Anonynous access is enabled" ) );
                    }

                    if ( resource.getGlobalRestApiSettings() != null )
                    {
                        RestApiSettings restApiSettings = resource.getGlobalRestApiSettings();

                        getGlobalRestApiSettings().setForceBaseUrl( restApiSettings.isForceBaseUrl() );

                        if ( StringUtils.isEmpty( resource.getGlobalRestApiSettings().getBaseUrl() ) )
                        {
                            getGlobalRestApiSettings().setBaseUrl( null );
                        }
                        else
                        {
                            getGlobalRestApiSettings().setBaseUrl(
                                                                   new Reference( restApiSettings.getBaseUrl() ).getTargetRef().toString() );
                        }
                    }
                    else
                    {
                        getGlobalRestApiSettings().disable();
                    }

                    // NEXUS-3064: to "inform" global remote storage context (and hence, all affected proxy
                    // repositories) about the change, but only if config is saved okay
                    // TODO: this is wrong, the config framework should "tell" this changed, but we have some
                    // design flaw here: the globalRemoteStorageContext is NOT a component, while the settings are
                    boolean remoteConnectionSettingsIsDirty = getGlobalRemoteConnectionSettings().isDirty();

                    boolean remoteHttpProxySettingsIsDirty = getGlobalHttpProxySettings().isDirty();

                    getNexusConfiguration().saveConfiguration();

                    // NEXUS-3064: to "inform" global remote storage context (and hence, all affected proxy
                    // repositories) about the change, but only if config is saved okay
                    // TODO: this is wrong, the config framework should "tell" this changed, but we have some
                    // design flaw here: the globalRemoteStorageContext is NOT a component, while the settings are
                    if ( remoteConnectionSettingsIsDirty )
                    {
                        getNexusConfiguration().getGlobalRemoteStorageContext().setRemoteConnectionSettings(
                                                                                                             getGlobalRemoteConnectionSettings() );
                    }

                    if ( remoteHttpProxySettingsIsDirty )
                    {
                        getNexusConfiguration().getGlobalRemoteStorageContext().setRemoteProxySettings(
                                                                                                        getGlobalHttpProxySettings() );
                    }
                }
                catch ( IOException e )
                {
                    getLogger().warn( "Got IO Exception during update of Nexus configuration.", e );

                    throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
                }
                catch ( InvalidConfigurationException e )
                {
                    // TODO: this should be removed from the Global config, as it is NO longer part of the nexus.xml
                    getLogger().debug( "Configuraiton Exception while setting security values", e );
                    this.handleInvalidConfigurationException( e );
                }
                catch ( ConfigurationException e )
                {
                    getLogger().warn( "Nexus refused to apply configuration.", e );

                    throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage(),
                                                       getNexusErrorResponse( "*", e.getMessage() ) );
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
        resource.setSecurityEnabled( isDefaultSecurityEnabled() );

        resource.setSecurityAnonymousAccessEnabled( isDefaultAnonymousAccessEnabled() );

        resource.setSecurityRealms( getDefaultRealms() );

        resource.setSecurityAnonymousUsername( getDefaultAnonymousUsername() );

        resource.setSecurityAnonymousPassword( PASSWORD_PLACE_HOLDER );

        resource.setGlobalConnectionSettings( convert( readDefaultGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( readDefaultGlobalRemoteHttpProxySettings() ) );

        RestApiSettings restApiSettings = convert( readDefaultRestApiSettings() );
        if ( restApiSettings != null )
        {
            restApiSettings.setBaseUrl( getContextRoot( request ).getTargetRef().toString() );
        }
        resource.setGlobalRestApiSettings( restApiSettings );

        resource.setSmtpSettings( convert( readDefaultSmtpConfiguration() ) );
    }

    /**
     * Externalized Nexus object to DTO's conversion, using current Nexus configuration.
     * 
     * @param resource
     */
    protected void fillCurrentConfiguration( Request request, GlobalConfigurationResource resource )
    {
        resource.setSecurityEnabled( getNexusConfiguration().isSecurityEnabled() );

        resource.setSecurityAnonymousAccessEnabled( getNexusConfiguration().isAnonymousAccessEnabled() );

        resource.setSecurityRealms( getNexusConfiguration().getRealms() );

        resource.setSecurityAnonymousUsername( getNexusConfiguration().getAnonymousUsername() );

        resource.setSecurityAnonymousPassword( PASSWORD_PLACE_HOLDER );

        resource.setGlobalConnectionSettings( convert( getGlobalRemoteConnectionSettings() ) );

        resource.setGlobalHttpProxySettings( convert( getGlobalHttpProxySettings() ) );

        RestApiSettings restApiSettings = convert( getGlobalRestApiSettings() );
        if ( restApiSettings != null && StringUtils.isEmpty( restApiSettings.getBaseUrl() ) )
        {
            restApiSettings.setBaseUrl( getContextRoot( request ).getTargetRef().toString() );
        }
        resource.setGlobalRestApiSettings( restApiSettings );

        resource.setSmtpSettings( convert( getNexusEmailer() ) );

        resource.setErrorReportingSettings( convert( getErrorReportingManager() ) );
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
