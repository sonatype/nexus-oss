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
package org.sonatype.nexus.rest.roles;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.NoSuchRoleException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.nexus.rest.model.RoleResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RolePlexusResource" )
public class RolePlexusResource
    extends AbstractRolePlexusResource
{

    public static final String ROLE_ID_KEY = "roleId";

    public RolePlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RoleResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/roles/{" + ROLE_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/roles/*", "authcBasic,perms[nexus:roles]" );
    }

    protected String getRoleId( Request request )
    {
        return request.getAttributes().get( ROLE_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RoleResourceResponse result = new RoleResourceResponse();

        try
        {
            result.setData( nexusToRestModel( getNexusSecurity().readRole( getRoleId( request ) ), request ) );

        }
        catch ( NoSuchRoleException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );

        }
        return result;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RoleResourceRequest resourceRequest = (RoleResourceRequest) payload;
        RoleResourceResponse resourceResponse = new RoleResourceResponse();

        if ( resourceRequest != null )
        {
            RoleResource resource = resourceRequest.getData();
            
            try
            {
                SecurityRole role = restToNexusModel( getNexusSecurity().readRole( resource.getId() ), resource );

                getNexusSecurity().updateRole( role );

                resourceResponse = new RoleResourceResponse();

                resourceResponse.setData( resourceRequest.getData() );
                
                resourceResponse.getData().setUserManaged( !role.isReadOnly() );

                resourceResponse
                    .getData().setResourceURI( createChildReference( request, resource.getId() ).toString() );

            }
            catch ( NoSuchRoleException e )
            {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
            }
            catch ( InvalidConfigurationException e )
            {
                // build and throw exception
                handleInvalidConfigurationException( e );
            }
        }
        return resourceResponse;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            getNexusSecurity().deleteRole( getRoleId( request ) );
        }
        catch ( NoSuchRoleException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

}
