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
package org.sonatype.nexus.rest.repositorystatuses;

import java.util.Collection;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.model.RepositoryStatusListResource;
import org.sonatype.nexus.rest.model.RepositoryStatusListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryStatusesListPlexusResource" )
public class RepositoryStatusesListPlexusResource
    extends AbstractRepositoryPlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/repository_statuses";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repostatus]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryStatusListResourceResponse result = new RepositoryStatusListResourceResponse();

        RepositoryStatusListResource repoRes;

        Collection<Repository> repositories = getRepositoryRegistry().getRepositories();

        for ( Repository repository : repositories )
        {
            repoRes = new RepositoryStatusListResource();

            repoRes.setResourceURI( createChildReference( request, this, repository.getId() ).toString() );

            repoRes.setId( repository.getId() );

            repoRes.setName( repository.getName() );

            repoRes.setRepoType( getRestRepoType( repository ) );

            if ( repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
            {
                repoRes.setRepoPolicy( repository.adaptToFacet( MavenRepository.class ).getRepositoryPolicy().toString() );
            }

            repoRes.setFormat( repository.getRepositoryContentClass().getId() );

            repoRes.setStatus( new RepositoryStatusResource() );

            repoRes.getStatus().setLocalStatus( repository.getLocalStatus().toString() );

            if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                repoRes.getStatus().setRemoteStatus(
                                                     getRestRepoRemoteStatus(
                                                                              repository.adaptToFacet( ProxyRepository.class ),
                                                                              request, response ) );

                repoRes.getStatus().setProxyMode(
                                                  repository.adaptToFacet( ProxyRepository.class ).getProxyMode().toString() );
            }

            result.addData( repoRes );
        }

        return result;
    }

}
