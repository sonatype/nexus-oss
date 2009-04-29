/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.security.rest.model.PrivilegeTypePropertyResource;
import org.sonatype.security.rest.model.PrivilegeTypeResource;
import org.sonatype.security.rest.model.PrivilegeTypeResourceResponse;

@Component( role = PlexusResource.class, hint = "PrivilegeTypePlexusResource" )
public class PrivilegeTypePlexusResource
    extends AbstractPrivilegePlexusResource
{
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
        return "/privilege_types";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PrivilegeTypeResourceResponse result = new PrivilegeTypeResourceResponse();

        List<PrivilegeDescriptor> privDescriptors = getConfigurationManager().listPrivilegeDescriptors();

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
