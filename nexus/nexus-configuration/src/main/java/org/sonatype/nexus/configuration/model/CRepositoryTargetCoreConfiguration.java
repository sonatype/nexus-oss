package org.sonatype.nexus.configuration.model;

import java.util.List;

import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CRepositoryTargetCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CRepositoryTargetCoreConfiguration( ApplicationConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public List<CRepositoryTarget> getConfiguration( boolean forWrite )
    {
        return (List<CRepositoryTarget>) super.getConfiguration( forWrite );
    }

    @Override
    protected List<CRepositoryTarget> extractConfiguration( Configuration configuration )
    {
        return configuration.getRepositoryTargets();
    }

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        return new ValidationResponse();
    }
}
