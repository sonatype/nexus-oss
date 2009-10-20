package org.sonatype.nexus.logging;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;

public abstract class AbstractLoggingComponent
{
    private Logger logger;

    // TODO: double annos here, to be able to use this in Nexus plugins but also in Nexus Core while transitioning!
    @Inject
    @Requirement
    private LoggerProvider loggerProvider;

    /**
     * Default constructor.
     */
    // TODO Drop this when switching to Guice (use constructor injection)
    public AbstractLoggingComponent()
    {

    }

    /**
     * Constructor.
     * 
     * @param loggerProvider logger provider
     */
    @Inject
    public AbstractLoggingComponent( final LoggerProvider loggerProvider )
    {
        this.loggerProvider = loggerProvider;
    }

    protected Logger getLogger()
    {
        if ( logger == null )
        {
            logger = loggerProvider.getLogger( this.getClass().getName() );
        }

        return logger;
    }
}
