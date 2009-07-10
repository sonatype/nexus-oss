package org.sonatype.nexus.logging;

import javax.inject.Inject;

import org.slf4j.Logger;

public abstract class AbstractLoggingComponent
{
    private Logger logger;

    @Inject
    private LoggerProvider loggerProvider;

    protected Logger getLogger()
    {
        if ( logger == null )
        {
            logger = loggerProvider.get();
        }

        return logger;
    }
}
