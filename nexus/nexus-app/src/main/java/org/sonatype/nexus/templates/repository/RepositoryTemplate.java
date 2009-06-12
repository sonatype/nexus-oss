package org.sonatype.nexus.templates.repository;

import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.Template;

public interface RepositoryTemplate
    extends Template<Repository>
{
    Class<?> getMainFacet();

    RepositoryPolicy getRepositoryPolicy();

    ContentClass getContentClass();
}
