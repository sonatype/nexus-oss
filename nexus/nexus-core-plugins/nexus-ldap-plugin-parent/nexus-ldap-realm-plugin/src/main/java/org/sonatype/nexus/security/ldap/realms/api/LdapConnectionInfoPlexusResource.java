/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.ldap.realms.persist.InvalidConfigurationException;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;

/**
 * Resource for managing LDAP connection settings.
 */
@Component( role = PlexusResource.class, hint = "LdapConnectionInfoPlexusResource" )
@Path( "/ldap/conn_info" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class LdapConnectionInfoPlexusResource
    extends AbstractLdapRealmPlexusResource
{

    public LdapConnectionInfoPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new LdapConnectionInfoResponse();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:ldapconninfo]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/ldap/conn_info";
    }

    /**
     * Retrieves the current (in-effect) LDAP connection info.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = LdapConnectionInfoResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        // this could be null, if so do we want to return defaults? I would guess no...
        CConnectionInfo connInfo = this.getConfiguration().readConnectionInfo();

        LdapConnectionInfoResponse result = new LdapConnectionInfoResponse();

        result.setData( this.ldapToRestModel( connInfo ) );

        return result;
    }

    /**
     * Sets the LDAP connection info and makes them in-effect.
     */
    @Override
    @PUT
    @ResourceMethodSignature( input = LdapConnectionInfoResponse.class, output = LdapConnectionInfoResponse.class )
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        LdapConnectionInfoResponse connResponse = (LdapConnectionInfoResponse) payload;

        if ( connResponse.getData() == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                         "LDAP Connection Info was missing from Request." );
        }

        CConnectionInfo connInfo = this.restToLdapModel( connResponse.getData() );
        try
        {
            // validation happens in this method
            this.getConfiguration().updateConnectionInfo( connInfo );
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
