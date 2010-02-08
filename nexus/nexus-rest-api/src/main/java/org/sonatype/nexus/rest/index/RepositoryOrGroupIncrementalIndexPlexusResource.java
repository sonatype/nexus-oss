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
package org.sonatype.nexus.rest.index;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryOrGroupIncrementalIndexPlexusResource" )
@Path( RepositoryOrGroupIncrementalIndexPlexusResource.RESOURCE_URI )
public class RepositoryOrGroupIncrementalIndexPlexusResource
    extends AbstractIndexPlexusResource
{
    public static final String RESOURCE_URI = "/data_incremental_index/{" + DOMAIN + "}/{" + TARGET_ID + "}";

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/data_incremental_index/*/**", "authcBasic,perms[nexus:index]" );
    }

    @Override
    protected boolean getIsFullReindex()
    {
        return false;
    }

    /**
     * Perform an incremental reindex against the provided repository or group. 
     */
    @Override
    @DELETE
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractIndexPlexusResource.DOMAIN ), @PathParam( AbstractIndexPlexusResource.TARGET_ID ) } )
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        super.delete( context, request, response );
    }
}
