/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.rest.users;

import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserSearchCriteria;


/**
 * REST resource for listing users.
 * 
 * @author bdemers
 * @see UserListPlexusResource
 */
@Singleton
@Typed( value = PlexusResource.class )
@Named( value = "PlexusUserListPlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( PlexusUserListPlexusResource.RESOURCE_URI )
@Deprecated
public class PlexusUserListPlexusResource
    extends AbstractSecurityPlexusResource
{
    public static final String USER_SOURCE_KEY = "userSource";

    public static final String RESOURCE_URI = "/plexus_users/{" + USER_SOURCE_KEY + "}";

    public PlexusUserListPlexusResource()
    {
        setModifiable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/plexus_users/*", "authcBasic,perms[security:users]" );
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    /**
     * Retrieves the list of users.
     * 
     * @param sourceId The Id of the source.  A source specifies where the users/roles came from, 
     * for example the source Id of 'LDAP' identifies the users/roles as coming from an LDAP source.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = PlexusUserListResourceResponse.class, pathParams = { @PathParam( value = "sourceId") }  )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PlexusUserListResourceResponse result = new PlexusUserListResourceResponse();

        // TODO: this logic should be removed from the this resource
        String source = getUserSource( request );
        Set<User> users = null;
        if ( "all".equalsIgnoreCase( source ) )
        {
            users = this.getSecuritySystem().listUsers();
        }
        else
        {
            users = this.getSecuritySystem().searchUsers( new UserSearchCriteria( null, null, source ) );
        }

        result.setData( this.securityToRestModel( users ) );
        
        return result;
    }

    protected String getUserSource( Request request )
    {
        return getRequestAttribute( request, USER_SOURCE_KEY );
    }
}
