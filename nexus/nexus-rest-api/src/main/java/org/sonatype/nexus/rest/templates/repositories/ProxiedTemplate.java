package org.sonatype.nexus.rest.templates.repositories;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.template.RepositoryTemplate;

@Component( role = RepositoryTemplate.class, hint = ProxiedTemplate.ID )
public class ProxiedTemplate
    implements RepositoryTemplate
{

    public static final String ID = "default_proxy_release";

    public RepositoryResource getContent()
    {
        RepositoryProxyResource repo = new RepositoryProxyResource();

        repo.setProvider( "maven2" );
        repo.setRepoType( "maven2" );
        repo.setId( ID );
        repo.setName( "Default Release Hosted Repository Template" );
        repo.setAllowWrite( true );
        repo.setBrowseable( true );
        repo.setIndexable( true );
        repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repo.setNotFoundCacheTTL( 1440 );
        repo.setArtifactMaxAge( -1 );
        repo.setMetadataMaxAge( 1440 );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://some-remote-repository/repo-root" );
        repo.setRemoteStorage( remoteStorage );

        return repo;
    }

    public String getId()
    {
        return ID;
    }

}
