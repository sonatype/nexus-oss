package org.sonatype.nexus.rest.templates.repositories;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.template.RepositoryTemplate;

@Component( role = RepositoryTemplate.class, hint = HostedTemplate.ID )
public class HostedTemplate
    implements RepositoryTemplate
{

    public static final String ID = "default_hosted_release";

    public RepositoryResource getContent()
    {
        RepositoryResource repo = new RepositoryResource();

        repo.setProvider( "maven2" );
        repo.setRepoType( "maven2" );
        repo.setId( ID );
        repo.setName( "Default Release Hosted Repository Template" );
        repo.setAllowWrite( true );
        repo.setBrowseable( true );
        repo.setIndexable( true );
        repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repo.setNotFoundCacheTTL( 1440 );

        return repo;
    }

    public String getId()
    {
        return ID;
    }

}
