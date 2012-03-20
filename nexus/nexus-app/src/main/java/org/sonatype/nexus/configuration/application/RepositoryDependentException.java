package org.sonatype.nexus.configuration.application;

import static java.lang.String.format;
import static org.sonatype.nexus.proxy.utils.RepositoryStringUtils.getHumanizedNameString;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryDependentException
    extends ConfigurationException
{

    private static final long serialVersionUID = -2037859093869479166L;

    private final Repository dependant;

    private final Repository repository;

    public RepositoryDependentException( Repository repository, Repository dependant )
    {
        super( format( "Repository %s cannot be deleted due to dependency: repository %s.",
            getHumanizedNameString( repository ), getHumanizedNameString( dependant ) ) );
        this.repository = repository;
        this.dependant = dependant;
    }

    public Repository getDependant()
    {
        return dependant;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public String getUIMessage()
    {
        return format(
            "Repository '%s' cannot be deleted due to dependencies on repository '%s'.\nDependencies must be removed in order to complete this operation.",
            getHumanizedNameString( repository ), getHumanizedNameString( dependant ) );
    }

}
