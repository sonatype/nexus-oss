package org.sonatype.nexus.rest.templates.repositories;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.template.RepositoryTemplate;

@Component( role = RepositoryTemplate.class, hint = VirtualTemplate.ID )
public class VirtualTemplate
    implements RepositoryTemplate
{

    public static final String ID = "default_virtual";

    public RepositoryBaseResource getContent()
    {
        RepositoryShadowResource repo = new RepositoryShadowResource();

        repo.setId( ID );
        repo.setName( "Default Virtual Repository Template" );
        repo.setRepoType( "virtual" );
        repo.setProvider( "m2-m1-shadow" );
        repo.setSyncAtStartup( false );

        return repo;
    }

    public String getId()
    {
        return ID;
    }

}
