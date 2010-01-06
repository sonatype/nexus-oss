/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import java.net.MalformedURLException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.ldap.realms.DefaultLdapContextFactory;
import org.restlet.data.Status;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.security.ldap.realms.persist.InvalidConfigurationException;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;
import org.sonatype.security.ldap.realms.persist.ValidationMessage;
import org.sonatype.security.ldap.realms.persist.ValidationResponse;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import org.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;
import org.sonatype.security.ldap.realms.tools.LdapURL;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractLdapRealmPlexusResource
    extends AbstractSecurityPlexusResource
    implements LdapRealmPlexusResourceConst
{
    @Requirement( role = LdapConfiguration.class )
    private LdapConfiguration configuration;

    public LdapConfiguration getConfiguration()
    {
        return configuration;
    }

    protected void handleValidationResponse( ValidationResponse validationResponse )
        throws PlexusResourceException
    {
        ErrorResponse nexusErrorResponse = new ErrorResponse();

        if ( validationResponse != null && validationResponse.getValidationErrors().size() > 0 )
        {
            for ( ValidationMessage vm : validationResponse.getValidationErrors() )
            {
                nexusErrorResponse.addError( createNexusError( vm.getKey(), vm.getShortMessage() ) );
            }
            throw new PlexusResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Configuration error.",
                nexusErrorResponse );
        }
    }

    protected void handleConfigurationException( InvalidConfigurationException e )
        throws PlexusResourceException
    {
        getLogger().warn( "Configuration error!", e );

        ErrorResponse nexusErrorResponse = new ErrorResponse();

        ValidationResponse vr = ( (InvalidConfigurationException) e ).getValidationResponse();

        if ( vr != null && vr.getValidationErrors().size() > 0 )
        {
            for ( ValidationMessage vm : vr.getValidationErrors() )
            {
                nexusErrorResponse.addError( createNexusError( vm.getKey(), vm.getShortMessage() ) );
            }

        }
        else
        {
            nexusErrorResponse.addError( createNexusError( "*", e.getMessage() ) );
        }

        throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", nexusErrorResponse );
    }

    protected ErrorMessage createNexusError( String id, String msg )
    {
        ErrorMessage ne = new ErrorMessage();
        ne.setId( id );
        ne.setMsg( msg );
        return ne;
    }

    protected CUserAndGroupAuthConfiguration restToLdapModel( LdapUserAndGroupConfigurationDTO userAndGroupConf )
    {
        CUserAndGroupAuthConfiguration cConf = new CUserAndGroupAuthConfiguration();

        cConf.setGroupMemberFormat( userAndGroupConf.getGroupMemberFormat() );
        cConf.setGroupObjectClass( userAndGroupConf.getGroupObjectClass() );
        cConf.setGroupBaseDn( userAndGroupConf.getGroupBaseDn() );
        cConf.setGroupIdAttribute( userAndGroupConf.getGroupIdAttribute() );
        cConf.setGroupMemberAttribute( userAndGroupConf.getGroupMemberAttribute() );
        cConf.setUserObjectClass( userAndGroupConf.getUserObjectClass() );
        cConf.setUserBaseDn( userAndGroupConf.getUserBaseDn() );
        cConf.setUserIdAttribute( userAndGroupConf.getUserIdAttribute() );
        cConf.setUserPasswordAttribute( userAndGroupConf.getUserPasswordAttribute() );
        cConf.setUserRealNameAttribute( userAndGroupConf.getUserRealNameAttribute() );
        cConf.setEmailAddressAttribute( userAndGroupConf.getEmailAddressAttribute() );
        cConf.setPreferredPasswordEncoding( userAndGroupConf.getPreferredPasswordEncoding() );
        cConf.setLdapGroupsAsRoles( userAndGroupConf.isLdapGroupsAsRoles() );
        cConf.setUserMemberOfAttribute( userAndGroupConf.getUserMemberOfAttribute() );
        cConf.setGroupSubtree( userAndGroupConf.isGroupSubtree() );
        cConf.setUserSubtree( userAndGroupConf.isUserSubtree() );

        return cConf;
    }

    protected LdapUserAndGroupConfigurationDTO ldapToRestModel( CUserAndGroupAuthConfiguration userAndGroupConf )
    {
        LdapUserAndGroupConfigurationDTO cConf = new LdapUserAndGroupConfigurationDTO();

        cConf.setGroupMemberFormat( userAndGroupConf.getGroupMemberFormat() );
        cConf.setGroupObjectClass( userAndGroupConf.getGroupObjectClass() );
        cConf.setGroupBaseDn( userAndGroupConf.getGroupBaseDn() );
        cConf.setGroupIdAttribute( userAndGroupConf.getGroupIdAttribute() );
        cConf.setGroupMemberAttribute( userAndGroupConf.getGroupMemberAttribute() );
        cConf.setUserObjectClass( userAndGroupConf.getUserObjectClass() );
        cConf.setUserBaseDn( userAndGroupConf.getUserBaseDn() );
        cConf.setUserIdAttribute( userAndGroupConf.getUserIdAttribute() );
        cConf.setUserPasswordAttribute( userAndGroupConf.getUserPasswordAttribute() );
        cConf.setUserRealNameAttribute( userAndGroupConf.getUserRealNameAttribute() );
        cConf.setEmailAddressAttribute( userAndGroupConf.getEmailAddressAttribute() );
        cConf.setPreferredPasswordEncoding( userAndGroupConf.getPreferredPasswordEncoding() );
        cConf.setLdapGroupsAsRoles( userAndGroupConf.isLdapGroupsAsRoles() );
        cConf.setUserMemberOfAttribute( userAndGroupConf.getUserMemberOfAttribute() );
        cConf.setGroupSubtree( userAndGroupConf.isGroupSubtree() );
        cConf.setUserSubtree( userAndGroupConf.isUserSubtree() );

        return cConf;
    }

    protected CConnectionInfo restToLdapModel( LdapConnectionInfoDTO restConnInfo )
    {
        CConnectionInfo connInfo = new CConnectionInfo();
        connInfo.setAuthScheme( restConnInfo.getAuthScheme() );
        connInfo.setHost( restConnInfo.getHost() );
        connInfo.setPort( restConnInfo.getPort() );
        connInfo.setProtocol( restConnInfo.getProtocol() );
        connInfo.setSearchBase( restConnInfo.getSearchBase() );
        connInfo.setSystemUsername( restConnInfo.getSystemUsername() );
        connInfo.setRealm( restConnInfo.getRealm() );

        // check if the request was sent with a password other then the FAKE one
        // if we get the fake one we need to grab the real password from the configuration.
        // if its something different we can update it.
        if ( FAKE_PASSWORD.equals( restConnInfo.getSystemPassword() ) )
        {
            if ( this.getConfiguration().readConnectionInfo() != null )
            {
                connInfo.setSystemPassword( this.getConfiguration().readConnectionInfo().getSystemPassword() );
            }
        }
        else
        {
            connInfo.setSystemPassword( restConnInfo.getSystemPassword() );
        }

        return connInfo;
    }

    protected LdapConnectionInfoDTO ldapToRestModel( CConnectionInfo ldapConnInfo )
    {
        LdapConnectionInfoDTO connInfo = new LdapConnectionInfoDTO();
        if ( ldapConnInfo != null )
        {
            connInfo.setAuthScheme( ldapConnInfo.getAuthScheme() );
            connInfo.setHost( ldapConnInfo.getHost() );
            connInfo.setPort( ldapConnInfo.getPort() );
            connInfo.setProtocol( ldapConnInfo.getProtocol() );
            connInfo.setSearchBase( ldapConnInfo.getSearchBase() );
            connInfo.setSystemUsername( ldapConnInfo.getSystemUsername() );
            connInfo.setRealm( ldapConnInfo.getRealm() );

            // if the ldapConInfo password is set hide it by using a fake password
            // if it is not set, we want to return an null.
            if ( StringUtils.isNotEmpty( ldapConnInfo.getSystemPassword() ) )
            {
                connInfo.setSystemPassword( FAKE_PASSWORD );
            }

        }
        return connInfo;
    }

    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );
        new XStreamInitalizer().initXStream( xstream );
    }

    protected DefaultLdapContextFactory buildDefaultLdapContextFactory( CConnectionInfo connectionInfo ) throws MalformedURLException
    {
        DefaultLdapContextFactory ldapContextFactory = new DefaultLdapContextFactory();
        ldapContextFactory.setAuthentication( connectionInfo.getAuthScheme() );
        ldapContextFactory.setSearchBase( connectionInfo.getSearchBase() );
        ldapContextFactory.setSystemPassword( connectionInfo.getSystemPassword() );
        ldapContextFactory.setSystemUsername( connectionInfo.getSystemUsername() );
            ldapContextFactory.setUrl( new LdapURL(
                connectionInfo.getProtocol(),
                connectionInfo.getHost(),
                connectionInfo.getPort(),
                connectionInfo.getSearchBase() ).toString() );

        return ldapContextFactory;
    }
}
