/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.privileges;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.tools.NoSuchPrivilegeException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.nexus.jsecurity.realms.NexusMethodAuthorizingRealm;
import org.sonatype.nexus.rest.model.PrivilegeStatusResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

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
        return new PathProtectionDescriptor( "/privileges/*", "authcBasic,perms[nexus:privileges]" );
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
            priv = getNexusSecurity().readPrivilege( getPrivilegeId( request ) );
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
            priv = getNexusSecurity().readPrivilege( getPrivilegeId( request ) );

            if ( priv.getType().equals( NexusMethodAuthorizingRealm.PRIVILEGE_TYPE_METHOD ) )
            {
                throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Cannot delete an application type privilege" );
            }
            else
            {
                getNexusSecurity().deletePrivilege( getPrivilegeId( request ) );
            }
        }
        catch ( NoSuchPrivilegeException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

}
