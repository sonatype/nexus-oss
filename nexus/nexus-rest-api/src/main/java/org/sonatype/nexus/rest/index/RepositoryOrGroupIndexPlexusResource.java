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

@Component( role = PlexusResource.class, hint = "RepositoryOrGroupIndexPlexusResource" )
@Path( RepositoryOrGroupIndexPlexusResource.RESOURCE_URI )
public class RepositoryOrGroupIndexPlexusResource
    extends AbstractIndexPlexusResource
{
    public static final String RESOURCE_URI = "/data_index/{" + DOMAIN + "}/{" + TARGET_ID + "}"; 

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/data_index/*/**", "authcBasic,perms[nexus:index]" );
    }

    @Override
    protected boolean getIsFullReindex()
    {
        return true;
    }
    
    /**
     * Perform an full reindex against the provided repository or group. Note that
     * appended to the end of the url should be the path that you want to index.  i.e.
     * /data_index/repositories/myRepo/org/blah will index everything under the org/blah directory.  Leaving blank
     * will simply index whole domain content.
     * 
     * @param domain The domain that will be used, valid options are 'repositories' or 'repo_groups' (Required).
     * @param target The unique id in the domain to use (i.e. repository or group id) (Required).
     */
    @Override
    @DELETE
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractIndexPlexusResource.DOMAIN ), @PathParam( AbstractIndexPlexusResource.TARGET_ID ) } )
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        // TODO Auto-generated method stub
        super.delete( context, request, response );
    }

}
