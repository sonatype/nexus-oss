/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.ldap.realms.persist.InvalidConfigurationException;
import org.sonatype.security.ldap.realms.persist.model.CUserAndGroupAuthConfiguration;

@Component( role = PlexusResource.class, hint = "LdapUserAndGroupsConfigurationPlexusResource" )
@Path( "/ldap/user_group_conf" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class LdapUserAndGroupsConfigurationPlexusResource
    extends AbstractLdapRealmPlexusResource
{

    public LdapUserAndGroupsConfigurationPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new LdapUserAndGroupConfigurationResponse();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:ldapusergroupconf]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/ldap/user_group_conf";
    }

    /**
     * Returns the user and groups mapping that are currently in-effect.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = LdapUserAndGroupConfigurationResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        // this could be null, if so do we want to return defaults? I would guess no...
        CUserAndGroupAuthConfiguration userGroupConf = this.getConfiguration().readUserAndGroupConfiguration();

        LdapUserAndGroupConfigurationResponse result = new LdapUserAndGroupConfigurationResponse();

        // convert and set the info in the result
        if ( userGroupConf != null )
        {
            result.setData( this.ldapToRestModel( userGroupConf ) );
        }
        return result;
    }

    /**
     * Updates the user and group mapping and makes it in-effect.
     */
    @Override
    @PUT
    @ResourceMethodSignature( input = LdapUserAndGroupConfigurationResponse.class, output = LdapUserAndGroupConfigurationResponse.class )
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        LdapUserAndGroupConfigurationResponse confResponse = (LdapUserAndGroupConfigurationResponse) payload;

        if ( confResponse.getData() == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                         "LDAP Connection Info was missing from Request." );
        }

        CUserAndGroupAuthConfiguration userGroupConf = this.restToLdapModel( confResponse.getData() );
        try
        {
            // validation happens in this method
            this.getConfiguration().updateUserAndGroupConfiguration( userGroupConf );
            // if it didn't throw an InvalidConfigurationException, we are good to go.
            this.getConfiguration().save();
        }
        catch ( InvalidConfigurationException e )
        {
            // this will build and thrown an exception.
            this.handleConfigurationException( e );
        }

        // just do a get.
        return this.get( context, request, response, null );
    }
}
