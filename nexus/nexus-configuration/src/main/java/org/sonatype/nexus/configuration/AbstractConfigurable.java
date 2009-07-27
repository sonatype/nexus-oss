package org.sonatype.nexus.configuration;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

/**
 * Abstract class to implement configurable components to "click" them in into generic configuration environment.
 * 
 * @author cstamas
 */
public abstract class AbstractConfigurable
    implements Configurable, EventListener, Initializable, Disposable
{
    /** The configuration */
    private CoreConfiguration coreConfiguration;

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    public void initialize()
        throws InitializationException
    {
        applicationEventMulticaster.addEventListener( this );
    }

    public void dispose()
    {
        applicationEventMulticaster.removeEventListener( this );
    }

    public void onEvent( Event<?> evt )
    {
        // act automatically on config events
        if ( evt instanceof ConfigurationPrepareForSaveEvent )
        {
            if ( isDirty() && getConfigurator() != null )
            {
                // prepare for save: transfer what we have in memory (if any) to model
                getConfigurator().prepareForSave( this, getApplicationConfiguration(), getCurrentCoreConfiguration() );

                // register ourselves as changed
                ConfigurationPrepareForSaveEvent psevt = (ConfigurationPrepareForSaveEvent) evt;

                psevt.getChanges().add( this );
            }
        }
        else if ( evt instanceof ConfigurationCommitEvent )
        {
            commitChanges();
        }
        else if ( evt instanceof ConfigurationRollbackEvent )
        {
            rollbackChanges();
        }
    }

    protected ApplicationEventMulticaster getApplicationEventMulticaster()
    {
        return applicationEventMulticaster;
    }

    protected abstract ApplicationConfiguration getApplicationConfiguration();

    // Configurable iface

    public final CoreConfiguration getCurrentCoreConfiguration()
    {
        return coreConfiguration;
    }

    protected ExternalConfiguration getExternalConfiguration()
    {
        return getCurrentCoreConfiguration().getExternalConfiguration();
    }

    public final void configure( Object config )
        throws ConfigurationException
    {
        validateConfiguration( config );

        if ( config instanceof CoreConfiguration )
        {
            this.coreConfiguration = (CoreConfiguration) config;
        }
        else
        {
            this.coreConfiguration = wrapConfiguration( config );
        }

        doConfigure( false );
    }

    public final void configure()
        throws ConfigurationException
    {
        doConfigure( true );
    }

    public boolean isDirty()
    {
        return getCurrentCoreConfiguration().isDirty();
    }

    public boolean commitChanges()
    {
        if ( isDirty() )
        {
            getCurrentCoreConfiguration().commitChanges();

            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean rollbackChanges()
    {
        if ( isDirty() )
        {
            getCurrentCoreConfiguration().rollbackChanges();

            return true;
        }
        else
        {
            return false;
        }
    }

    // ==

    protected final void validateConfiguration( Object config )
        throws ConfigurationException
    {
        if ( config == null )
        {
            throw new InvalidConfigurationException( "This configuration is null!" );
        }

        doValidateConfiguration( config );
    }

    protected void doValidateConfiguration( Object config )
        throws ConfigurationException
    {
        if ( getValidator() != null )
        {
            getValidator().validate( getApplicationConfiguration(), config );
        }
    }

    protected void doConfigure( boolean validate )
        throws ConfigurationException
    {
        if ( validate )
        {
            doValidateConfiguration( getCurrentConfiguration( false ) );
        }

        if ( getConfigurator() != null )
        {
            getConfigurator().applyConfiguration( this, getApplicationConfiguration(), getCurrentCoreConfiguration() );
        }

        getCurrentCoreConfiguration().commitChanges();
    }

    // ==

    protected Validator getValidator()
    {
        // by default we do not have Validator
        return null;
    }

    protected abstract Configurator getConfigurator();

    protected abstract Object getCurrentConfiguration( boolean forWrite );

    protected abstract CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException;
}
