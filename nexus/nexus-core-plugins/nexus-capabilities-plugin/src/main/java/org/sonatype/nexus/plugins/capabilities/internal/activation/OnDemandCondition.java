package org.sonatype.nexus.plugins.capabilities.internal.activation;

import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.support.activation.AbstractCondition;
import org.sonatype.nexus.plugins.capabilities.support.activation.CapabilityConditions;

/**
 * A condition that allows a targeted capability to activated / passivated.
 *
 * @since 1.10.0
 */
public class OnDemandCondition
    extends AbstractCondition
    implements CapabilityConditions.OnDemand
{

    public OnDemandCondition( final ActivationContext activationContext )
    {
        super( activationContext, true );
    }

    @Override
    public OnDemandCondition reactivate()
    {
        unsatisfy();
        satisfy();
        return this;
    }

    @Override
    public OnDemandCondition satisfy()
    {
        setSatisfied( true );
        return this;
    }

    @Override
    public OnDemandCondition unsatisfy()
    {
        setSatisfied( false );
        return this;
    }

    @Override
    public String toString()
    {
        return "on-demand";
    }

}
