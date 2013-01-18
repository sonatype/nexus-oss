package org.sonatype.nexus.proxy.maven.wl.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.maven.wl.discovery.Strategy;

/**
 * Abstract class for {@link Strategy} implementations.
 * 
 * @author cstamas
 */
public abstract class AbstractStrategy
    extends AbstractLoggingComponent
    implements Strategy
{
    private final String id;

    private final int priority;

    protected AbstractStrategy( final String id, final int priority )
    {
        this.id = checkNotNull( id );
        this.priority = priority;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }
}
