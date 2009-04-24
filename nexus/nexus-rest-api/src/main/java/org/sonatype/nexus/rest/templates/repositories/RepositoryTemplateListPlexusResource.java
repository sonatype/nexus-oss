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
package org.sonatype.nexus.rest.templates.repositories;

import java.io.IOException;
import java.util.Collection;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryTemplateListPlexusResource" )
public class RepositoryTemplateListPlexusResource
    extends AbstractRepositoryTemplatePlexusResource
{
    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/templates/repositories";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repotemplates]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryListResourceResponse result = new RepositoryListResourceResponse();
        try
        {
            RepositoryListResource repoRes;

            Collection<CRepository> repositories = getNexus().listRepositoryTemplates();

            for ( CRepository repository : repositories )
            {
                repoRes = new RepositoryListResource();

                repoRes.setId( repository.getId() );

                repoRes.setResourceURI( createChildReference( request, this, repository.getId() ).toString() );

                if ( repository.getRemoteStorage() != null && repository.getRemoteStorage().getUrl() != null )
                {
                    repoRes.setRepoType( "proxy" );
                }
                else
                {
                    repoRes.setRepoType( "hosted" );
                }

                repoRes.setName( repository.getName() );

                repoRes.setEffectiveLocalStorageUrl( repository.getLocalStorage() != null
                    && repository.getLocalStorage().getUrl() != null ? repository.getLocalStorage().getUrl()
                                : repository.defaultLocalStorageUrl );

                // XXX cstamas how???
                // repoRes.setRepoPolicy( NexusCompat.getRepositoryPolicy( repository ));
                if ( repository.getId().contains( "_release" ) )
                {
                    repoRes.setRepoPolicy( RepositoryPolicy.RELEASE.toString() );
                }
                else if ( repository.getId().contains( "_snapshot" ) )
                {
                    repoRes.setRepoPolicy( RepositoryPolicy.SNAPSHOT.toString() );
                }

                result.addData( repoRes );
            }
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO exception while listing repository templates!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage() );
        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryResourceResponse repoRequest = (RepositoryResourceResponse) payload;
        RepositoryResourceResponse result = new RepositoryResourceResponse();

        if ( repoRequest != null )
        {
            try
            {
                RepositoryBaseResource resource = repoRequest.getData();

                CRepository normal = getNexus().readRepositoryTemplate( resource.getId() );

                if ( normal == null )
                {
                    normal = getRepositoryAppModel( (RepositoryResource) resource, null );

                    getNexus().createRepositoryTemplate( normal );

                    CRepository resultRepo = getNexus().readRepositoryTemplate( resource.getId() );

                    result.setData( getRepositoryRestModel( resultRepo ) );
                }
                else
                {
                    getLogger().warn( "Repository template with ID=" + resource.getId() + " already exists!" );

                    throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Repository template with ID="
                        + resource.getId() + " already exists!", getNexusErrorResponse( "id", "Repository with id="
                        + resource.getId() + " already exists!" ) );
                }
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
