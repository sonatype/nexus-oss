/*
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
package org.sonatype.security.rest.privileges;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.rest.model.PrivilegeTypePropertyResource;
import org.sonatype.security.rest.model.PrivilegeTypeResource;
import org.sonatype.security.rest.model.PrivilegeTypeResourceResponse;

/**
 * REST resource to retrieve the list of Privilege Types. Each type of privilege that can be created is described by a
 * {@link PrivilegeTypeResource}. Each PrivilegeTypeResource lists the set of properties used to define a type of
 * privilege.
 * 
 * @author bdemers
 */
@Singleton
@Typed( PlexusResource.class )
@Named( "PrivilegeTypePlexusResource" )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
@Path( PrivilegeTypePlexusResource.RESOURCE_URI )
public class PrivilegeTypePlexusResource
    extends AbstractPrivilegePlexusResource
{

    public static final String RESOURCE_URI = "/privilege_types";

    @Inject
    @Named( "resourceMerging" )
    private ConfigurationManager configurationManager;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:privilegetypes]" );
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    /**
     * Retrieves the list of privilege types.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = PrivilegeTypeResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PrivilegeTypeResourceResponse result = new PrivilegeTypeResourceResponse();

        List<PrivilegeDescriptor> privDescriptors = this.configurationManager.listPrivilegeDescriptors();

        for ( PrivilegeDescriptor privDescriptor : privDescriptors )
        {
            PrivilegeTypeResource type = new PrivilegeTypeResource();
            type.setId( privDescriptor.getType() );
            type.setName( privDescriptor.getName() );

            for ( PrivilegePropertyDescriptor propDescriptor : privDescriptor.getPropertyDescriptors() )
            {
                PrivilegeTypePropertyResource typeProp = new PrivilegeTypePropertyResource();
                typeProp.setId( propDescriptor.getId() );
                typeProp.setName( propDescriptor.getName() );
                typeProp.setHelpText( propDescriptor.getHelpText() );
                typeProp.setType( propDescriptor.getType() );

                type.addProperty( typeProp );
            }

            result.addData( type );
        }

        return result;
    }
}
