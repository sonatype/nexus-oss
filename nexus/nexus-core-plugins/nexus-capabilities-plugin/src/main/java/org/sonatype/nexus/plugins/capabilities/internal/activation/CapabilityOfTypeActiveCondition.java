package org.sonatype.nexus.plugins.capabilities.internal.activation;

import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;

/**
 * A condition that is satisfied when a capability of a specified type exists and is in an active state.
 *
 * @since 1.10.0
 */
public class CapabilityOfTypeActiveCondition
    extends CapabilityOfTypeExistsCondition
{

    public CapabilityOfTypeActiveCondition( final ActivationContext activationContext,
                                            final CapabilityRegistry capabilityRegistry,
                                            final Class<? extends Capability> type )
    {
        super( activationContext, capabilityRegistry, type );
    }

    @Override
    boolean isSatisfied( final CapabilityReference reference )
    {
        return super.isSatisfied( reference ) && reference.isActive();
    }

    @Override
    public void onActivate( final CapabilityReference reference )
    {
        if ( !isSatisfied() && type.isAssignableFrom( reference.capability().getClass() ) )
        {
            checkAllCapabilities();
        }
    }

    @Override
    public void onPassivate( final CapabilityReference reference )
    {
        if ( isSatisfied() && type.isAssignableFrom( reference.capability().getClass() ) )
        {
            checkAllCapabilities();
        }
    }

    @Override
    public String toString()
    {
        return "Active " + type.getSimpleName();
    }
}
