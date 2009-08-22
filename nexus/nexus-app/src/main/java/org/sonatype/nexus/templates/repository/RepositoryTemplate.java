package org.sonatype.nexus.templates.repository;

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.ConfigurableTemplate;

public interface RepositoryTemplate
    extends ConfigurableTemplate
{
    ContentClass getContentClass();

    Class<?> getMainFacet();

    ConfigurableRepository getConfigurableRepository();

    Repository create()
        throws ConfigurationException, IOException;
}
