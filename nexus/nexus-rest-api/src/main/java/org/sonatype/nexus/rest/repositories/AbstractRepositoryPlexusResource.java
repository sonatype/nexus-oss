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

import java.util.Collection;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.template.RepositoryTemplateStore;

public abstract class AbstractRepositoryPlexusResource
    extends AbstractNexusPlexusResource
{

    /** Repo type hosted. */
    public static final String REPO_TYPE_HOSTED = RepositoryTemplateStore.REPO_TYPE_HOSTED;

    /** Repo type proxied. */
    public static final String REPO_TYPE_PROXIED = RepositoryTemplateStore.REPO_TYPE_PROXIED;

    /** Repo type virtual (shadow in nexus). */
    public static final String REPO_TYPE_VIRTUAL = RepositoryTemplateStore.REPO_TYPE_VIRTUAL;

    /** Repo type group. */
    public static final String REPO_TYPE_GROUP = RepositoryTemplateStore.REPO_TYPE_GROUP;

    /** Key to store Repo with which we work against. */
    public static final String REPOSITORY_ID_KEY = "repositoryId";

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private RepositoryTemplateStore repoTemplateStore;

    /**
     * Pull the repository Id out of the Request.
     *
     * @param request
     * @return
     */
    protected String getRepositoryId( Request request )
    {
        return request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    // CLEAN
    public String getRestRepoRemoteStatus( ProxyRepository repository, Request request, Response response )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        boolean forceCheck = form.getFirst( "forceCheck" ) != null;

        RemoteStatus rs = repository.getRemoteStatus( forceCheck );

        if ( RemoteStatus.UNKNOWN.equals( rs ) )
        {
            // set status to ACCEPTED, since we have incomplete info
            response.setStatus( Status.SUCCESS_ACCEPTED );
        }

        return rs == null ? null : rs.toString();
    }

    // CLEAN
    protected String _getRepoFormat( String role, String hint )
    {
        ContentClass cc = repositoryTypeRegistry.getRepositoryContentClass( role, hint );

        if ( cc != null )
        {
            return cc.getId();
        }
        else
        {
            return null;
        }
    }

    // clean
    public String getRestRepoType( Repository repository )
    {
        return this.repoTemplateStore.getRestRepositoryType( repository );
    }

    protected RepositoryListResourceResponse listRepositories( Request request, boolean allReposes )
        throws ResourceException
    {
        return listRepositories( request, allReposes, true );
    }

    // clean
    protected RepositoryListResourceResponse listRepositories( Request request, boolean allReposes,
                                                               boolean includeGroups )
        throws ResourceException
    {
        RepositoryListResourceResponse result = new RepositoryListResourceResponse();

        RepositoryListResource repoRes;

        Collection<Repository> repositories = getRepositoryRegistry().getRepositories();

        for ( Repository repository : repositories )
        {
            // To save UI changes at the moment, not including groups in repo call
            if ( ( allReposes || repository.isUserManaged() )
                && ( includeGroups || !repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) ) )
            {
                repoRes = new RepositoryListResource();

                repoRes.setResourceURI( createRepositoryReference( request, repository.getId() ).toString() );

                repoRes.setRepoType( getRestRepoType( repository ) );

                repoRes.setFormat( repository.getRepositoryContentClass().getId() );

                repoRes.setId( repository.getId() );

                repoRes.setName( repository.getName() );

                repoRes.setUserManaged( repository.isUserManaged() );

                repoRes.setExposed( repository.isExposed() );

                repoRes.setEffectiveLocalStorageUrl( repository.getLocalUrl() );

                if ( repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
                {
                    repoRes.setRepoPolicy( repository.adaptToFacet( MavenRepository.class ).getRepositoryPolicy().toString() );
                }

                if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
                {
                    repoRes.setRemoteUri( repository.adaptToFacet( ProxyRepository.class ).getRemoteUrl() );
                }

                result.addData( repoRes );
            }
        }

        return result;
    }

    // clean
    protected RepositoryResourceResponse getRepositoryResourceResponse( String repoId )
        throws ResourceException
    {
        RepositoryResourceResponse result = new RepositoryResourceResponse();

        try
        {
            RepositoryBaseResource resource = null;

            Repository repository = getRepositoryRegistry().getRepository( repoId );

            resource = getRepositoryRestModel( repository );

            result.setData( resource );
        }
        catch ( NoSuchRepositoryAccessException e )
        {
            getLogger().warn( "Repository access denied, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, "Access Denied to Repository" );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository not found, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
        }
        return result;
    }

    public RepositoryBaseResource getRepositoryRestModel( Repository repository )
    {
        return this.repoTemplateStore.getRepositoryTemplate( repository );
    }

    public RepositoryProxyResource getRepositoryProxyRestModel( ProxyRepository repository )
    {
        return this.repoTemplateStore.getProxyRepositoryTemplate( repository );
    }

    public RepositoryShadowResource getRepositoryShadowRestModel( ShadowRepository repository )
    {
        return this.repoTemplateStore.getShadowRepositoryTemplate( repository );
    }

}
