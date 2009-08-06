package org.sonatype.nexus.configuration.model;

import java.util.List;

import org.sonatype.nexus.configuration.ConfigurationException;
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
    protected void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub
        
    }
}
