/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.test.api;

import java.net.MalformedURLException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.security.ldap.realms.api.AbstractLdapRealmPlexusResource;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapAuthenticationTestRequest;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.ldap.dao.LdapConnectionTester;
import org.sonatype.security.ldap.realms.DefaultLdapContextFactory;
import org.sonatype.security.ldap.realms.persist.ConfigurationValidator;
import org.sonatype.security.ldap.realms.persist.ValidationResponse;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;

@Component( role = PlexusResource.class, hint = "LdapTestAuthenticationPlexusResource" )
public class LdapTestAuthenticationPlexusResource
    extends AbstractLdapRealmPlexusResource
{

    @Requirement
    private LdapConnectionTester ldapConnectionTester;

    @Requirement
    private ConfigurationValidator configurationValidator;

    public LdapTestAuthenticationPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new LdapAuthenticationTestRequest();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:ldaptestauth]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/ldap/test_auth";
    }

    /*
     * (non-Javadoc)
     * @see org.sonatype.plexus.rest.resource.AbstractPlexusResource#put(org.restlet.Context, org.restlet.data.Request,
     * org.restlet.data.Response, java.lang.Object)
     */
    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        LdapAuthenticationTestRequest authRequest = (LdapAuthenticationTestRequest) payload;

        CConnectionInfo connectionInfo = this.restToLdapModel( authRequest.getData() );

        ValidationResponse validationResponse = this.configurationValidator.validateConnectionInfo(
            null,
            connectionInfo );
        // sets the status and throws an exception if the validation was junk.
        // if the validation was ok, then nothing really happens
        this.handleValidationResponse( validationResponse );


        try
        {
            DefaultLdapContextFactory ldapContextFactory = this.buildDefaultLdapContextFactory( connectionInfo );
            ldapConnectionTester.testConnection( ldapContextFactory );
        }
        catch ( MalformedURLException e )
        {
            // should NEVER hit this
            this.getLogger().warn( "Validation of URL was successful, but failed after validation.", e );
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e );
        }
        catch ( Exception e )
        {
            this.getLogger().debug( "Failed to connect to Ldap Server.", e );
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Failed to connect to Ldap Server:\n"
                + e.getMessage(), e );
        }

        response.setStatus( Status.SUCCESS_NO_CONTENT );
        return null;
    }

}
