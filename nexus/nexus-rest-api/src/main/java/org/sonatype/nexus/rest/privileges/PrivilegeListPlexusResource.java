/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.privileges;

import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityProperty;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeListResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * Handles the GET and POST request for the Nexus privileges.
 * 
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "PrivilegeListPlexusResource" )
public class PrivilegeListPlexusResource
    extends AbstractPrivilegePlexusResource
{
    public PrivilegeListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new PrivilegeResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/privileges";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:privileges]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PrivilegeListResourceResponse result = new PrivilegeListResourceResponse();

        Collection<SecurityPrivilege> privs = getNexusSecurity().listPrivileges();

        for ( SecurityPrivilege priv : privs )
        {
            PrivilegeBaseStatusResource res = nexusToRestModel( priv, request );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        PrivilegeResourceRequest resourceRequest = (PrivilegeResourceRequest) payload;
        PrivilegeListResourceResponse result = null;

        if ( resourceRequest != null )
        {
            result = new PrivilegeListResourceResponse();

            PrivilegeBaseResource resource = resourceRequest.getData();

            // currently we are allowing only of repotarget privs, so enforcing checkfor it
            if ( !TYPE_REPO_TARGET.equals( resource.getType() ) )
            {
                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Configuration error.",
                    getNexusErrorResponse( "type", "Not allowed privilege type!" ) );
            }

            List<String> methods = resource.getMethod();

            if ( methods == null || methods.size() == 0 )
            {
                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Configuration error.",
                    getNexusErrorResponse( "method", "No method(s) supplied, must select at least one method." ) );
            }
            else
            {
                try
                {
                    // Add a new privilege for each method
                    for ( String method : methods )
                    {
                        // Currently can only add new target types, application types are hardcoded
                        if ( PrivilegeTargetResource.class.isAssignableFrom( resource.getClass() ) )
                        {
                            PrivilegeTargetResource res = (PrivilegeTargetResource) resource;

                            SecurityPrivilege priv = new SecurityPrivilege();

                            priv.setName( res.getName() != null ? res.getName() + " - (" + method + ")" : null );
                            priv.setDescription( res.getDescription() );
                            priv.setType( "target" );

                            SecurityProperty prop = new SecurityProperty();
                            prop.setKey( "method" );
                            prop.setValue( method );

                            priv.addProperty( prop );

                            prop = new SecurityProperty();
                            prop.setKey( "repositoryTargetId" );
                            prop.setValue( res.getRepositoryTargetId() );

                            priv.addProperty( prop );

                            prop = new SecurityProperty();
                            prop.setKey( "repositoryId" );
                            prop.setValue( res.getRepositoryId() );

                            priv.addProperty( prop );

                            prop = new SecurityProperty();
                            prop.setKey( "repositoryGroupId" );
                            prop.setValue( res.getRepositoryGroupId() );

                            priv.addProperty( prop );

                            getNexusSecurity().createPrivilege( priv );

                            result.addData( nexusToRestModel( priv, request ) );
                        }
                        else
                        {
                            throw new PlexusResourceException(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "Configuration error.",
                                getNexusErrorResponse( "type", "An invalid type was entered." ) );
                        }
                    }

                }
                catch ( InvalidConfigurationException e )
                {
                    // build and throw exctption
                    handleInvalidConfigurationException( e );
                }
            }
        }
        return result;
    }

}
