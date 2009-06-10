package org.sonatype.nexus.template;

import org.sonatype.nexus.rest.model.RepositoryBaseResource;

public interface RepositoryTemplate
{

    String getId();

    RepositoryBaseResource getContent();

}
