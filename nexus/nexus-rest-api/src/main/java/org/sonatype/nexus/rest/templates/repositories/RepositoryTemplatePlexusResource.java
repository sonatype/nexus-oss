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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.templates.NoSuchTemplateIdException;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.AbstractMavenRepositoryTemplate;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryTemplatePlexusResource" )
@Path( RepositoryTemplatePlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class RepositoryTemplatePlexusResource
    extends AbstractNexusPlexusResource
{
    /* Key to store Repo with which we work against. */
    public static final String REPOSITORY_ID_KEY = "repositoryId";
    
    public static final String RESOURCE_URI = "/templates/repositories/{" + REPOSITORY_ID_KEY + "}"; 

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/templates/repositories/*", "authcBasic,perms[nexus:repotemplates]" );
    }

    protected String getRepositoryId( Request request )
    {
        return request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    /**
     * Retrieve the repository configuration stored in the specified template.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( RepositoryTemplatePlexusResource.REPOSITORY_ID_KEY ) },
                              output = RepositoryResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryResourceResponse result = new RepositoryResourceResponse();

        try
        {
            RepositoryTemplate template = getNexus().getRepositoryTemplateById( getRepositoryId( request ) );

            RepositoryBaseResource repoRes = null;

            if ( ProxyRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes = createProxy( template );
            }
            else if ( HostedRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes = createHosted( template );
            }
            else if ( ShadowRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes = createShadow( template );
            }
            else if ( GroupRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes = new RepositoryGroupResource();

                repoRes.setRepoType( "group" );
            }
            else
            {
                // huh?
                throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unrecognized repository template with ID='"
                    + template.getId() + "' and mainFacet='" + template.getMainFacet().getName() + "'!" );
            }

            repoRes.setId( template.getId() );

            repoRes.setName( template.getDescription() );

            repoRes.setProvider( template.getRepositoryProviderHint() );
            
            repoRes.setProviderRole( template.getRepositoryProviderRole() );

            repoRes.setFormat( template.getContentClass().getId() );

            result.setData( repoRes );
        }
        catch ( NoSuchTemplateIdException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }

        return result;
    }

    private RepositoryBaseResource createShadow( RepositoryTemplate template )
    {
        RepositoryShadowResource repoRes = new RepositoryShadowResource();

        repoRes.setRepoType( "virtual" );

        AbstractShadowRepositoryConfiguration cfg =
            (AbstractShadowRepositoryConfiguration) template.getConfigurableRepository().getCurrentCoreConfiguration().getExternalConfiguration().getConfiguration( false );

        repoRes.setSyncAtStartup( cfg.isSynchronizeAtStartup() );
        repoRes.setShadowOf( cfg.getMasterRepositoryId() );

        return repoRes;
    }

    private RepositoryBaseResource createProxy( RepositoryTemplate template )
    {
        RepositoryProxyResource repoRes = new RepositoryProxyResource();

        repoRes.setRepoType( "proxy" );

        AbstractMavenRepositoryTemplate m2Template = (AbstractMavenRepositoryTemplate) template;
        repoRes.setRepoPolicy( m2Template.getRepositoryPolicy().name() );

        ConfigurableRepository cfg = template.getConfigurableRepository();       
        repoRes.setWritePolicy( cfg.getWritePolicy().name() ); 
        repoRes.setBrowseable( cfg.isBrowseable() );
        repoRes.setIndexable( cfg.isIndexable() );
        repoRes.setExposed( cfg.isExposed() );
        repoRes.setNotFoundCacheTTL( cfg.getNotFoundCacheTimeToLive() );

        AbstractMavenRepositoryConfiguration repoCfg =
            (AbstractMavenRepositoryConfiguration) template.getConfigurableRepository().getCurrentCoreConfiguration().getExternalConfiguration().getConfiguration( false );


        repoRes.setChecksumPolicy( repoCfg.getChecksumPolicy().name() );
        repoRes.setDownloadRemoteIndexes( repoCfg.isDownloadRemoteIndex() );
        repoRes.setArtifactMaxAge( repoCfg.getArtifactMaxAge() );
        repoRes.setMetadataMaxAge( repoCfg.getMetadataMaxAge() );

        return repoRes;
    }

    private RepositoryBaseResource createHosted( RepositoryTemplate template )
    {
        RepositoryResource repoRes = new RepositoryResource();

        repoRes.setRepoType( "hosted" );

        AbstractMavenRepositoryTemplate m2Template = (AbstractMavenRepositoryTemplate) template;
        repoRes.setRepoPolicy( m2Template.getRepositoryPolicy().name() );

        ConfigurableRepository cfg = template.getConfigurableRepository();
        repoRes.setWritePolicy( cfg.getWritePolicy().name() );
        repoRes.setBrowseable( cfg.isBrowseable() );
        repoRes.setIndexable( cfg.isIndexable() );
        repoRes.setExposed( cfg.isExposed() );
        repoRes.setNotFoundCacheTTL( cfg.getNotFoundCacheTimeToLive() );

        return repoRes;
    }
}