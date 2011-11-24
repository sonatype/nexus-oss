package org.sonatype.nexus.plugins.capabilities.internal.activation;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.support.activation.AbstractCondition;

/**
 * A condition that is becoming unsatisfied before an capability is updated and becomes satisfied after capability was
 * updated.
 *
 * @since 1.10.0
 */
public class PassivateCapabilityDuringUpdateCondition
    extends AbstractCondition
    implements CapabilityRegistry.Listener
{

    private final CapabilityRegistry capabilityRegistry;

    private final Capability capability;

    public PassivateCapabilityDuringUpdateCondition( final ActivationContext activationContext,
                                                     final CapabilityRegistry capabilityRegistry,
                                                     final Capability capability )
    {
        super( activationContext, true );
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.capability = checkNotNull( capability );
        capabilityRegistry.addListener( this );
    }

    @Override
    public void onAdd( final CapabilityReference reference )
    {
        // ignore
    }

    @Override
    public void onRemove( final CapabilityReference reference )
    {
        // ignore
    }

    @Override
    public void onActivate( final CapabilityReference reference )
    {
        // ignore
    }

    @Override
    public void onPassivate( final CapabilityReference reference )
    {
        // ignore
    }

    @Override
    public void beforeUpdate( final CapabilityReference reference )
    {
        setSatisfied( false );
    }

    @Override
    public void afterUpdate( final CapabilityReference reference )
    {
        setSatisfied( true );
    }

    @Override
    public PassivateCapabilityDuringUpdateCondition release()
    {
        capabilityRegistry.removeListener( this );
        super.release();

        return this;
    }

    @Override
    public String toString()
    {
        return "Passivate during update of " + capability;
    }

}
