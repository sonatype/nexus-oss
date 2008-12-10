/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.repotargets;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryTargetPlexusResource" )
public class RepositoryTargetPlexusResource
    extends AbstractRepositoryTargetPlexusResource
{

    public static final String REPO_TARGET_ID_KEY = "repoTargetId";

    public RepositoryTargetPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryTargetResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        // TODO Auto-generated method stub
        return "/repo_targets/{" + REPO_TARGET_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repo_targets/*", "authcBasic,perms[nexus:targets]" );
    }

    private String getRepoTargetId( Request request )
    {
        return request.getAttributes().get( REPO_TARGET_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryTargetResourceResponse result = new RepositoryTargetResourceResponse();

        CRepositoryTarget target = getNexus().readRepositoryTarget( this.getRepoTargetId( request ) );

        if ( target != null )
        {
            RepositoryTargetResource resource = getNexusToRestResource( target, request );

            result.setData( resource );
        }
        else
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No such target!" );
        }
        return result;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryTargetResourceResponse requestResource = (RepositoryTargetResourceResponse) payload;
        RepositoryTargetResourceResponse resultResource = null;
        if ( requestResource != null )
        {
            RepositoryTargetResource resource = requestResource.getData();

            CRepositoryTarget target = getNexus().readRepositoryTarget( this.getRepoTargetId( request ) );

            if ( target != null )
            {
                if ( validate( false, resource ) )
                {
                    try
                    {
                        target = getRestToNexusResource( resource );

                        // update
                        getNexus().updateRepositoryTarget( target );

                        // response
                        resultResource = new RepositoryTargetResourceResponse();

                        resultResource.setData( requestResource.getData() );

                    }
                    catch ( ConfigurationException e )
                    {
                        // builds and throws an exception
                        handleConfigurationException( e );
                    }
                    catch ( IOException e )
                    {
                        getLogger().warn( "Got IOException during creation of repository target!", e );

                        throw new ResourceException(
                            Status.SERVER_ERROR_INTERNAL,
                            "Got IOException during creation of repository target!" );
                    }
                }
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No such target!" );
            }

        }
        return resultResource;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        CRepositoryTarget target = getNexus().readRepositoryTarget( getRepoTargetId( request ) );

        if ( target != null )
        {
            try
            {
                getNexus().deleteRepositoryTarget( getRepoTargetId( request ) );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IOException during removal of repository target!", e );

                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got IOException during removal of repository target!" );
            }
        }
        else
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No such target!" );
        }
    }
}
