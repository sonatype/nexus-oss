package org.sonatype.nexus.plugins.capabilities.internal.activation;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.support.activation.AbstractCondition;

/**
 * Support class for conditions based on Nexus state.
 *
 * @since 1.10.0
 */
@Named
@Singleton
public class NexusIsActiveCondition
    extends AbstractCondition
    implements Condition
{

    @Inject
    NexusIsActiveCondition( final ActivationContext activationContext )
    {
        super( activationContext, false );
        bind();
    }

    NexusIsActiveCondition acknowledgeNexusStarted()
    {
        setSatisfied( true );
        return this;
    }

    NexusIsActiveCondition acknowledgeNexusStopped()
    {
        setSatisfied( false );
        return this;
    }

    @Override
    protected void doBind()
    {
        // do nothing
    }

    @Override
    protected void doRelease()
    {
        // do nothing
    }

}
