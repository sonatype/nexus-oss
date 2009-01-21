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
package org.sonatype.nexus.rest.repositories;

import java.io.IOException;

import org.apache.maven.model.Repository;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.model.RepositoryDependentStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

@Component( role = PlexusResource.class, hint = "RepositoryStatusPlexusResource" )
public class RepositoryStatusPlexusResource
    extends AbstractRepositoryPlexusResource
{

    public RepositoryStatusPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryStatusResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/repositories/{" + REPOSITORY_ID_KEY + "}/status";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*/status", "authcBasic,perms[nexus:repostatus]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryStatusResourceResponse result = null;

        String repoId = getRepositoryId( request );

        try
        {
            RepositoryStatusResource resource = new RepositoryStatusResource();

            try
            {
                CRepository model = getNexus().readRepository( repoId );

                resource.setId( model.getId() );

                resource.setRepoType( getRestRepoType( model ) );

                resource.setFormat( getRepoFormat( Repository.class, model.getType() ) );

                resource.setLocalStatus( getRestRepoLocalStatus( model ) );

                if ( REPO_TYPE_PROXIED.equals( resource.getRepoType() ) )
                {
                    resource.setRemoteStatus( getRestRepoRemoteStatus( model, request, response ) );

                    resource.setProxyMode( getRestRepoProxyMode( model ) );
                }

            }
            catch ( NoSuchRepositoryException e )
            {
                CRepositoryShadow model = getNexus().readRepositoryShadow( repoId );

                resource.setId( model.getId() );

                resource.setRepoType( getRestRepoType( model ) );

                resource.setFormat( getRepoFormat( ShadowRepository.class, model.getType() ) );

                resource.setLocalStatus( getRestRepoLocalStatus( model ) );
            }

            result = new RepositoryStatusResourceResponse();

            result.setData( resource );

        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository not found, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
        }

        return result;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryStatusResourceResponse repoStatusRequest = (RepositoryStatusResourceResponse) payload;

        RepositoryStatusResourceResponse result = null;

        String repoId = getRepositoryId( request );

        if ( repoStatusRequest != null )
        {
            try
            {
                RepositoryStatusResource resource = repoStatusRequest.getData();

                if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
                {
                    CRepositoryShadow shadow = getNexus().readRepositoryShadow( repoId );

                    shadow.setLocalStatus( resource.getLocalStatus() );

                    getNexus().updateRepositoryShadow( shadow );

                    result = (RepositoryStatusResourceResponse) this.get( context, request, response, null );
                }
                else
                {
                    CRepository normal = getNexus().readRepository( repoId );

                    normal.setLocalStatus( resource.getLocalStatus() );

                    if ( REPO_TYPE_PROXIED.equals( getRestRepoType( normal ) ) && resource.getProxyMode() != null )
                    {
                        normal.setProxyMode( resource.getProxyMode() );
                    }

                    // update dependant shadows too
                    for ( CRepositoryShadow shadow : getNexus().listRepositoryShadows() )
                    {
                        if ( normal.getId().equals( shadow.getShadowOf() ) )
                        {
                            shadow.setLocalStatus( resource.getLocalStatus() );

                            getNexus().updateRepositoryShadow( shadow );
                        }
                    }

                    getNexus().updateRepository( normal );

                    result = (RepositoryStatusResourceResponse) this.get( context, request, response, null );

                    for ( CRepositoryShadow shadow : getNexus().listRepositoryShadows() )
                    {
                        if ( normal.getId().equals( shadow.getShadowOf() ) )
                        {
                            RepositoryDependentStatusResource dependent = new RepositoryDependentStatusResource();

                            dependent.setId( shadow.getId() );

                            dependent.setRepoType( getRestRepoType( shadow ) );

                            dependent.setFormat( getRepoFormat( ShadowRepository.class, shadow.getType() ) );

                            dependent.setLocalStatus( getRestRepoLocalStatus( shadow ) );

                            result.getData().addDependentRepo( dependent );
                        }
                    }
                }

            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().warn( "Repository not found, id=" + repoId );

                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
            }
            catch ( ConfigurationException e )
            {
                getLogger().warn( "Configuration unacceptable, repoId=" + repoId, e );

                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Configuration unacceptable, repoId=" + repoId + ": " + e.getMessage(),
                    getNexusErrorResponse( "*", e.getMessage() ) );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception!", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
            }
        }
        return result;
    }

}
