package org.sonatype.nexus.template;

import org.sonatype.nexus.rest.model.RepositoryBaseResource;

public interface RepositoryTemplateProvider
{

    // ----------------------------------------------------------------------------
    // Repo templates, CRUD
    // ----------------------------------------------------------------------------

    RepositoryBaseResource retrieveTemplate( String id );

    void addTempate( String id, RepositoryBaseResource template );

}
