package org.sonatype.nexus.plugins.capabilities.internal.activation;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.support.activation.AbstractCondition;

/**
 * A condition that is satisfied when a capability of a specified type exists.
 *
 * @since 1.10.0
 */
public class CapabilityOfTypeExistsCondition
    extends AbstractCondition
    implements CapabilityRegistry.Listener
{

    private final CapabilityRegistry capabilityRegistry;

    final Class<?> type;

    public CapabilityOfTypeExistsCondition( final ActivationContext activationContext,
                                            final CapabilityRegistry capabilityRegistry,
                                            final Class<? extends Capability> type )
    {
        super( activationContext );
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.type = type;
        capabilityRegistry.addListener( this );
    }

    @Override
    public void onAdd( final CapabilityReference reference )
    {
        if ( !isSatisfied() && type.isAssignableFrom( reference.capability().getClass() ) )
        {
            checkAllCapabilities();
        }
    }

    @Override
    public void onRemove( final CapabilityReference reference )
    {
        if ( isSatisfied() && type.isAssignableFrom( reference.capability().getClass() ) )
        {
            checkAllCapabilities();
        }
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

    void checkAllCapabilities()
    {
        for ( final CapabilityReference ref : capabilityRegistry.getAll() )
        {
            if ( isSatisfied( ref ) )
            {
                if ( !isSatisfied() )
                {
                    setSatisfied( true );
                }
                return;
            }
        }
        if ( isSatisfied() )
        {
            setSatisfied( false );
        }
    }

    boolean isSatisfied( final CapabilityReference reference )
    {
        return type.isAssignableFrom( reference.capability().getClass() );
    }

    @Override
    public String toString()
    {
        return type.getSimpleName() + " exists";
    }

}
