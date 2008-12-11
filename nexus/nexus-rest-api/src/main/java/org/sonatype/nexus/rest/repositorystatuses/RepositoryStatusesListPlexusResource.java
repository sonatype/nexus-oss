/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.rest.repositorystatuses;

import java.util.Collection;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
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

        Collection<CRepository> repositories = getNexus().listRepositories();

        for ( CRepository repository : repositories )
        {
            repoRes = new RepositoryStatusListResource();

            repoRes.setResourceURI( createChildReference( request, repository.getId() ).toString() );

            repoRes.setId( repository.getId() );

            repoRes.setName( repository.getName() );

            repoRes.setRepoType( getRestRepoType( repository ) );

            repoRes.setRepoPolicy( getRestRepoPolicy( repository ) );

            repoRes.setFormat( repository.getType() );

            repoRes.setStatus( new RepositoryStatusResource() );

            repoRes.getStatus().setLocalStatus( getRestRepoLocalStatus( repository ) );

            if ( REPO_TYPE_PROXIED.equals( getRestRepoType( repository ) ) )
            {
                repoRes.getStatus().setRemoteStatus( getRestRepoRemoteStatus( repository, request, response ) );

                repoRes.getStatus().setProxyMode( getRestRepoProxyMode( repository ) );
            }

            result.addData( repoRes );
        }

        Collection<CRepositoryShadow> shadows = getNexus().listRepositoryShadows();

        for ( CRepositoryShadow shadow : shadows )
        {
            repoRes = new RepositoryStatusListResource();

            repoRes.setResourceURI( createChildReference( request, shadow.getId() ).toString() );

            repoRes.setRepoType( getRestRepoType( shadow ) );

            repoRes.setName( shadow.getName() );

            repoRes.setStatus( new RepositoryStatusResource() );

            repoRes.getStatus().setLocalStatus( getRestRepoLocalStatus( shadow ) );

            result.addData( repoRes );
        }

        return result;
    }

}
