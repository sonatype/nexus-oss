/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.test.api;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.security.ldap.realms.api.AbstractLdapRealmPlexusResource;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserListResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserResponseDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequest;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequestDTO;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.LdapConnectionTester;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.realms.LdapManager;
import org.sonatype.security.ldap.realms.persist.ConfigurationValidator;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;
import org.sonatype.security.ldap.realms.persist.UsersGroupAuthTestLdapConfiguration;
import org.sonatype.security.ldap.realms.persist.ValidationResponse;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;

@Component(role=PlexusResource.class, hint="LdapUserAndGroupConfigTestPlexusResource")
public class LdapUserAndGroupConfigTestPlexusResource
    extends AbstractLdapRealmPlexusResource
    implements Contextualizable
{

    @Requirement
    private Logger logger;
    
    @Requirement
    private LdapConnectionTester ldapConnectionTester;

    @Requirement
    private ConfigurationValidator configurationValidator;
    
    private PlexusContainer container;
    
    public LdapUserAndGroupConfigTestPlexusResource()
    {
        this.setModifiable( true );
        this.setReadable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new LdapUserAndGroupConfigTestRequest();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:ldaptestuserconf]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/ldap/test_user_conf";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#get(org.restlet.Context, org.restlet.data.Request,
     *      org.restlet.data.Response, org.restlet.resource.Variant)
     */
    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        
        LdapUserAndGroupConfigTestRequest configResponse = (LdapUserAndGroupConfigTestRequest) payload;
        LdapUserAndGroupConfigTestRequestDTO dto = configResponse.getData();
        
        ValidationResponse validationResponse = this.configurationValidator.validateConnectionInfo( null,this.getConnectionInfo( dto ) );
        // sets the status and throws an exception if the validation was junk.
        // if the validation was ok, then nothing really happens
        this.handleValidationResponse( validationResponse );
        
        // do validation before we try to connect
        validationResponse = this.configurationValidator.validateUserAndGroupAuthConfiguration( null, this.restToLdapModel( dto ) );
        this.handleValidationResponse( validationResponse );
        
        LdapUserListResponse result = new LdapUserListResponse();
        try
        {
            // FIXME: move this to the UI.
            int limit = ( dto.getUserLimitCount() != 0) ? dto.getUserLimitCount() : 20;
            
            result.setLdapUserRoleMappings( this.getPopulatedDTOs( this.convertToAuthConfig(dto), this.getConnectionInfo( dto ), limit ) );
        }
        catch ( Exception e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "LDAP Realm is not configured correctly: " + e.getMessage(),
                e );
        }
        return result;
    }
    
    private CConnectionInfo getConnectionInfo( LdapUserAndGroupConfigTestRequestDTO dto )
    {
        CConnectionInfo connInfo = new CConnectionInfo();
        connInfo.setAuthScheme( dto.getAuthScheme() );
        connInfo.setHost( dto.getHost() );
        connInfo.setPort( dto.getPort() );
        connInfo.setProtocol( dto.getProtocol() );
        connInfo.setSearchBase( dto.getSearchBase() );
        connInfo.setSystemUsername( dto.getSystemUsername() );
        connInfo.setRealm( dto.getRealm() );
        
        // TODO: this is duplicated until the UI can send both objects in
        // check if the request was sent with a password other then the FAKE one
        // if we get the fake one we need to grab the real password from the configuration.
        // if its something different we can update it.
        if( FAKE_PASSWORD.equals( dto.getSystemPassword() ) )
        {
            if( this.getConfiguration().readConnectionInfo() != null )
            {
                connInfo.setSystemPassword( this.getConfiguration().readConnectionInfo().getSystemPassword() );
            }
        }
        else
        {
            connInfo.setSystemPassword( dto.getSystemPassword() );
        }
        
        
        return connInfo;
    }
    
    
    private LdapAuthConfiguration convertToAuthConfig( LdapUserAndGroupConfigurationDTO dto )
    {
        LdapAuthConfiguration authConfig = new LdapAuthConfiguration();

        authConfig.setGroupMemberFormat(dto.getGroupMemberFormat());
        authConfig.setGroupObjectClass(dto.getGroupObjectClass());
        authConfig.setGroupBaseDn(dto.getGroupBaseDn());
        authConfig.setGroupIdAttribute(dto.getGroupIdAttribute());
        authConfig.setGroupMemberAttribute(dto.getGroupMemberAttribute());
        authConfig.setUserObjectClass(dto.getUserObjectClass());
        authConfig.setUserBaseDn(dto.getUserBaseDn());
        authConfig.setUserIdAttribute(dto.getUserIdAttribute());
        authConfig.setPasswordAttribute( dto.getUserPasswordAttribute());
        authConfig.setUserRealNameAttribute(dto.getUserRealNameAttribute());
        authConfig.setEmailAddressAttribute(dto.getEmailAddressAttribute());
        authConfig.setPasswordEncoding(dto.getPreferredPasswordEncoding());
        authConfig.setLdapGroupsAsRoles( dto.isLdapGroupsAsRoles() );
        authConfig.setUserSubtree( dto.isUserSubtree() );
        authConfig.setGroupSubtree( dto.isGroupSubtree() );
        authConfig.setUserMemberOfAttribute( dto.getUserMemberOfAttribute() );
        
        return authConfig;
    }

    protected LdapManager getLdapManager( LdapAuthConfiguration ldapAuthConfiguration, CConnectionInfo connectionInfo )
        throws ResourceException
    {
        // the component we need to replace is nested 2 layers deep, and we need to do this per request
        // which is why I am monkeying around with the container.  Its not exactly 'clean'....
        
        TestLdapManager ldapManager;
        try
        {
            // get the ldapConfig
            UsersGroupAuthTestLdapConfiguration ldapConfiguration = (UsersGroupAuthTestLdapConfiguration) this.container.lookup( LdapConfiguration.class, "UsersGroupAuthTestLdapConfiguration" );
            ldapConfiguration.setLdapAuthConfiguration( ldapAuthConfiguration );
            ldapConfiguration.setConnectionInfo( connectionInfo );
            
            ldapManager = (TestLdapManager) this.container.lookup( LdapManager.class, "TestLdapManager" );
            ldapManager.setLdapConfiguration(ldapConfiguration);        
        }
        catch ( ComponentLookupException e )
        {
            logger.error( e.getMessage(), e );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
        }

        return ldapManager;
    }

    private List<LdapUserResponseDTO> getPopulatedDTOs(LdapAuthConfiguration ldapAuthConfiguration, CConnectionInfo cConnectionInfo, int limit ) throws LdapDAOException, MalformedURLException, NamingException 
    {   
        
        List<LdapUserResponseDTO> result = new ArrayList<LdapUserResponseDTO>();
        Collection<LdapUser> sUsers = ldapConnectionTester.testUserAndGroupMapping( this.buildDefaultLdapContextFactory( cConnectionInfo ), ldapAuthConfiguration, limit );
        
        for ( LdapUser ldapUser : sUsers )
        {
           result.add( getPopulatedUser( ldapUser ));
        }
        return result;
    }
    
    private LdapUserResponseDTO getPopulatedUser( LdapUser ldapUser )
    {   
        LdapUserResponseDTO dto;
        
        dto = new LdapUserResponseDTO();
        
        // now set the rest of the props
        dto.setUserId( ldapUser.getUsername() );
        dto.setEmail( ldapUser.getEmail() );
        dto.setName( ldapUser.getRealName() );
        
        // add the roles
        for ( String role : (Set<String>) ldapUser.getMembership() )
        {
            dto.addRole( role );
        }
        return dto;
    }
    
    public void contextualize( org.codehaus.plexus.context.Context context )
        throws ContextException
    {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
    

}
