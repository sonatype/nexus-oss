package org.sonatype.nexus.configuration.model;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

/**
 * A superclass class that holds an Xpp3Dom and maintains it.
 * 
 * @author cstamas
 */
public class DefaultExternalConfiguration<T extends AbstractXpp3DomExternalConfigurationHolder>
    implements ExternalConfiguration<T>
{
    private final ApplicationConfiguration applicationConfiguration;

    private final CoreConfiguration coreConfiguration;

    private final T configuration;

    private T changedConfiguration;

    public DefaultExternalConfiguration( ApplicationConfiguration applicationConfiguration,
                                         CoreConfiguration coreConfiguration, T configuration )
    {
        this.applicationConfiguration = applicationConfiguration;

        this.coreConfiguration = coreConfiguration;

        this.configuration = configuration;

        this.changedConfiguration = null;
    }

    public boolean isDirty()
    {
        return this.changedConfiguration != null;
    }

    public void validateChanges()
        throws ConfigurationException
    {
        if ( changedConfiguration != null )
        {
            changedConfiguration.validate( getApplicationConfiguration(), coreConfiguration );
        }
    }

    public void commitChanges()
        throws ConfigurationException
    {
        if ( changedConfiguration != null )
        {
            changedConfiguration.validate( getApplicationConfiguration(), coreConfiguration );

            configuration.apply( changedConfiguration );

            changedConfiguration = null;
        }
    }

    public void rollbackChanges()
    {
        changedConfiguration = null;
    }

    @SuppressWarnings( "unchecked" )
    public T getConfiguration( boolean forModification )
    {
        if ( forModification )
        {
            // copy configuration if needed
            if ( changedConfiguration == null )
            {
                changedConfiguration = (T) configuration.clone();
            }

            return changedConfiguration;
        }
        else
        {
            return configuration;
        }
    }

    // ==

    public ValidationResponse doValidateChanges( Xpp3Dom configuration )
    {
        return changedConfiguration.doValidateChanges( getApplicationConfiguration(), coreConfiguration, configuration );

    }

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }
}
