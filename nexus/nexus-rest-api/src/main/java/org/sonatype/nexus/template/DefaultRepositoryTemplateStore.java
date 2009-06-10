package org.sonatype.nexus.template;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.NexusCompat;
import org.sonatype.nexus.rest.global.AbstractGlobalConfigurationPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

@Component( role = RepositoryTemplateStore.class )
public class DefaultRepositoryTemplateStore
    extends AbstractLogEnabled
    implements RepositoryTemplateStore
{

    @Requirement( hint = "protected" )
    private RepositoryRegistry repositoryRegistry;

    @Requirement( role = RepositoryTemplate.class )
    private Map<String, RepositoryTemplate> templates;

    public RepositoryBaseResource retrieveTemplate( String id )
    {
        if ( id.startsWith( RepositoryTemplateStore.TEMPLATE_REPOSITORY_PREFIX ) )
        {
            String repoId = id.substring( TEMPLATE_REPOSITORY_PREFIX.length() );
            Repository repository;
            try
            {
                repository = repositoryRegistry.getRepository( repoId );
            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().error( e.getMessage(), e );
                return null;
            }

            return getRepositoryTemplate( repository );
        }

        if ( templates.containsKey( id ) )
        {
            return templates.get( id ).getContent();
        }

        return null;
    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryBaseResource getRepositoryTemplate( Repository repository )
    {
        RepositoryResource resource = null;

        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            resource = getProxyRepositoryTemplate( repository.adaptToFacet( ProxyRepository.class ) );
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            return getShadowRepositoryTemplate( repository.adaptToFacet( ShadowRepository.class ) );
        }
        else
        {
            resource = new RepositoryResource();
        }

        resource.setProvider( NexusCompat.getRepositoryProviderHint( repository ) );

        resource.setFormat( repository.getRepositoryContentClass().getId() );

        resource.setRepoType( getRestRepositoryType( repository ) );

        resource.setId( repository.getId() );

        resource.setName( repository.getName() );

        resource.setAllowWrite( repository.isAllowWrite() );

        resource.setBrowseable( repository.isBrowseable() );

        resource.setIndexable( repository.isIndexable() );

        resource.setNotFoundCacheTTL( repository.getNotFoundCacheTimeToLive() );

        resource.setDefaultLocalStorageUrl( repository.getLocalUrl() );

        resource.setOverrideLocalStorageUrl( repository.getLocalUrl() );

        if ( repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
        {
            resource.setRepoPolicy( repository.adaptToFacet( MavenRepository.class ).getRepositoryPolicy().toString() );

            if ( repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
            {
                resource.setChecksumPolicy( repository.adaptToFacet( MavenProxyRepository.class ).getChecksumPolicy().toString() );

                resource.setDownloadRemoteIndexes( repository.adaptToFacet( MavenProxyRepository.class ).isDownloadRemoteIndexes() );
            }
        }

        return resource;

    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryProxyResource getProxyRepositoryTemplate( ProxyRepository repository )
    {
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setRemoteStorage( new RepositoryResourceRemoteStorage() );

        resource.getRemoteStorage().setRemoteStorageUrl( repository.getRemoteUrl() );

        resource.getRemoteStorage().setAuthentication(
                                                       AbstractGlobalConfigurationPlexusResource.convert( NexusCompat.getRepositoryRawConfiguration(
                                                                                                                                                     repository ).getRemoteStorage().getAuthentication() ) );

        resource.getRemoteStorage().setConnectionSettings(
                                                           AbstractGlobalConfigurationPlexusResource.convert( NexusCompat.getRepositoryRawConfiguration(
                                                                                                                                                         repository ).getRemoteStorage().getConnectionSettings() ) );

        resource.getRemoteStorage().setHttpProxySettings(
                                                          AbstractGlobalConfigurationPlexusResource.convert( NexusCompat.getRepositoryRawConfiguration(
                                                                                                                                                        repository ).getRemoteStorage().getHttpProxySettings() ) );

        if ( repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
        {
            resource.setArtifactMaxAge( repository.adaptToFacet( MavenProxyRepository.class ).getArtifactMaxAge() );

            resource.setMetadataMaxAge( repository.adaptToFacet( MavenProxyRepository.class ).getMetadataMaxAge() );
        }

        return resource;
    }

    public RepositoryShadowResource getShadowRepositoryTemplate( ShadowRepository shadow )
    {
        RepositoryShadowResource resource = new RepositoryShadowResource();

        resource.setId( shadow.getId() );

        resource.setName( shadow.getName() );

        resource.setProvider( NexusCompat.getRepositoryProviderHint( shadow ) );

        resource.setRepoType( REPO_TYPE_VIRTUAL );

        resource.setFormat( shadow.getRepositoryContentClass().getId() );

        resource.setShadowOf( shadow.getMasterRepositoryId() );

        resource.setSyncAtStartup( shadow.isSynchronizeAtStartup() );

        return resource;
    }

    public String getRestRepositoryType( Repository repository )
    {
        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            return REPO_TYPE_PROXIED;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class ) )
        {
            return REPO_TYPE_HOSTED;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            return REPO_TYPE_VIRTUAL;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            return REPO_TYPE_GROUP;
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + repository.getClass().getName()
                + " is not recognized!" );
        }
    }

}
