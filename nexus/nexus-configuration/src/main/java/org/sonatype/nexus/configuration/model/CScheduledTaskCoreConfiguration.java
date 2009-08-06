package org.sonatype.nexus.configuration.model;

import java.util.List;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CScheduledTaskCoreConfiguration
    extends AbstractCoreConfiguration
{
    public CScheduledTaskCoreConfiguration( ApplicationConfiguration configuration )
    {
        super( configuration );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public List<CScheduledTask> getConfiguration( boolean forWrite )
    {
        return (List<CScheduledTask>) super.getConfiguration( forWrite );
    }

    @Override
    protected List<CScheduledTask> extractConfiguration( Configuration configuration )
    {
        return configuration.getTasks();
    }

    @Override
    protected void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub
    }
}
