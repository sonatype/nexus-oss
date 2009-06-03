package org.sample.plugin;

import javax.inject.Inject;

import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

public class DefaultCustomComponent
    implements CustomComponent
{
    @Inject
    private RepositoryRegistry repositoryRegistry;

    public String sayHello()
    {
        return "default hello! RepositoryRegistry has " + repositoryRegistry.getRepositories().size()
            + " registered repositories!";
    }
}
