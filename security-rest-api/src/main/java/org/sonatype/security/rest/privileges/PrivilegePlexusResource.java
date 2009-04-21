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

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.privileges.application.ApplicationPrivilegeDescriptor;
import org.sonatype.jsecurity.realms.tools.NoSuchPrivilegeException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.rest.model.PrivilegeStatusResourceResponse;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "PrivilegePlexusResource" )
public class PrivilegePlexusResource
    extends AbstractPrivilegePlexusResource
{

    public PrivilegePlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/privileges/{" + PRIVILEGE_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/privileges/*", "authcBasic,perms[security:privileges]" );
    }

    protected String getPrivilegeId( Request request )
    {
        return request.getAttributes().get( PRIVILEGE_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PrivilegeStatusResourceResponse result = new PrivilegeStatusResourceResponse();

        SecurityPrivilege priv = null;

        try
        {
            priv = getPlexusSecurity().readPrivilege( getPrivilegeId( request ) );
        }
        catch ( NoSuchPrivilegeException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }

        result.setData( nexusToRestModel( priv, request ) );

        return result;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        SecurityPrivilege priv;

        try
        {
            priv = getPlexusSecurity().readPrivilege( getPrivilegeId( request ) );

            if ( priv.getType().equals( ApplicationPrivilegeDescriptor.TYPE ) )
            {
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Cannot delete an application type privilege" );
            }
            else
            {
                getPlexusSecurity().deletePrivilege( getPrivilegeId( request ) );
            }
        }
        catch ( NoSuchPrivilegeException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

}
