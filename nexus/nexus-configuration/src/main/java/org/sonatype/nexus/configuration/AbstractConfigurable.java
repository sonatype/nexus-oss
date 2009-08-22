package org.sonatype.nexus.configuration;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
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

        try
        {
            initializeConfiguration();
        }
        catch ( ConfigurationException e )
        {
            throw new InitializationException( "Cannot configure the component!", e );
        }
    }

    protected void initializeConfiguration()
        throws ConfigurationException
    {
        // someone needs this, someone not
        // for example, whoever is configged using framework, will not need this,
        // but we still have components on their own, like DefaultTaskConfigManager
        // that are driven by spice Scheduler
    }

    public void dispose()
    {
        applicationEventMulticaster.removeEventListener( this );
    }

    public void onEvent( Event<?> evt )
    {
        // act automatically on config events
        if ( evt instanceof ConfigurationPrepareForLoadEvent )
        {
            ConfigurationPrepareForLoadEvent vevt = (ConfigurationPrepareForLoadEvent) evt;

            try
            {
                // validate
                initializeConfiguration();
            }
            catch ( ConfigurationException e )
            {
                // put a veto
                vevt.putVeto( this, e );
            }
        }
        else if ( evt instanceof ConfigurationPrepareForSaveEvent )
        {
            if ( isDirty() )
            {
                ConfigurationPrepareForSaveEvent psevt = (ConfigurationPrepareForSaveEvent) evt;

                try
                {
                    // prepare
                    prepareForSave();

                    // register ourselves as changed
                    psevt.getChanges().add( this );
                }
                catch ( ConfigurationException e )
                {
                    // put a veto
                    psevt.putVeto( this, e );
                }
            }
        }
        else if ( evt instanceof ConfigurationValidateEvent )
        {
            ConfigurationValidateEvent vevt = (ConfigurationValidateEvent) evt;

            try
            {
                // validate
                getCurrentCoreConfiguration().validateChanges();
            }
            catch ( ConfigurationException e )
            {
                // put a veto
                vevt.putVeto( this, e );
            }
        }
        else if ( evt instanceof ConfigurationCommitEvent )
        {
            try
            {
                commitChanges();
            }
            catch ( ConfigurationException e )
            {
                // FIXME: log or something?
                rollbackChanges();
            }
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

    public final void configure( Object config )
        throws ConfigurationException
    {
        this.coreConfiguration = wrapConfiguration( config );

        doConfigure();
    }

    public boolean isDirty()
    {
        return getCurrentCoreConfiguration().isDirty();
    }

    // FIXME: who will call prepareForSave() if Configurable used directly?
    public void prepareForSave()
        throws ConfigurationException
    {
        if ( isDirty() )
        {
            if ( getConfigurator() != null )
            {
                // prepare for save: transfer what we have in memory (if any) to model
                getConfigurator().prepareForSave( this, getApplicationConfiguration(), getCurrentCoreConfiguration() );
            }

            getCurrentCoreConfiguration().validateChanges();
        }
    }

    public boolean commitChanges()
        throws ConfigurationException
    {
        if ( isDirty() )
        {
            prepareForSave();

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

    protected void doConfigure()
        throws ConfigurationException
    {
        // "pull" the config to make it dirty
        getCurrentConfiguration( true );

        if ( getConfigurator() != null )
        {
            getConfigurator().applyConfiguration( this, getApplicationConfiguration(), getCurrentCoreConfiguration() );
        }

        getCurrentCoreConfiguration().commitChanges();
    }

    // ==

    protected abstract Configurator getConfigurator();

    protected abstract Object getCurrentConfiguration( boolean forWrite );

    protected abstract CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException;
}
