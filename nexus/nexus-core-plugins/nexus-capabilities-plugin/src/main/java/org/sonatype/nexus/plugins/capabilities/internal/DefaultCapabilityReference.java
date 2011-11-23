/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

/**
 * Default {@link CapabilityReference} implementation.
 *
 * @since 1.10.0
 */
class DefaultCapabilityReference
    extends AbstractLoggingComponent
    implements CapabilityReference
{

    private final DefaultCapabilityRegistry registry;

    private final Capability capability;

    private final ActivationContext activationContext;

    private final Condition activateCondition;

    private boolean active;

    private ActivationContextListener activationListener;

    DefaultCapabilityReference( final DefaultCapabilityRegistry registry,
                                final ActivationContext activationContext,
                                final Capability capability )
    {
        this.registry = checkNotNull( registry );
        this.activationContext = checkNotNull( activationContext );
        this.capability = checkNotNull( capability );
        this.activateCondition = capability.activationCondition();
    }

    @Override
    public Capability capability()
    {
        return capability;
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    public void activate()
    {
        if ( !isActive() )
        {
            if ( activateCondition == null || activateCondition.isSatisfied() )
            {
                getLogger().debug( "Activating capability with id '{}' ({})", capability.id(), capability );
                try
                {
                    capability().activate();
                    active = true;
                    registry.notify( this, new DefaultCapabilityRegistry.Notifier( "activated" )
                    {
                        @Override
                        void run( final CapabilityRegistry.Listener listener, final CapabilityReference reference )
                        {
                            listener.onActivate( reference );
                        }
                    } );
                }
                catch ( Exception e )
                {
                    getLogger().error(
                        "Could not activate capability with id '{}' ({})",
                        new Object[]{ capability.id(), capability, e }
                    );
                }
            }
            if ( activateCondition != null )
            {
                activationListener = new ActivationContextListener();
                activationContext.addListener( activationListener, activateCondition );
            }
        }
    }

    @Override
    public void passivate()
    {
        if ( isActive() )
        {
            if ( activationListener != null )
            {
                activationContext.removeListener( activationListener, activateCondition );
            }
            // check again as it could be that in the mean time we deactivate
            if ( isActive() )
            {
                getLogger().debug( "Passivating capability with id '{}' ({})", capability.id(), capability );
                try
                {
                    active = false;
                    registry.notify( this, new DefaultCapabilityRegistry.Notifier( "passivated" )
                    {
                        @Override
                        void run( final CapabilityRegistry.Listener listener, final CapabilityReference reference )
                        {
                            listener.onPassivate( reference );
                        }
                    } );
                    capability().passivate();
                }
                catch ( Exception e )
                {
                    getLogger().error(
                        "Could not passivate capability with id '{}' ({})",
                        new Object[]{ capability.id(), capability, e }
                    );
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{active=" + active + ", capability=" + capability + '}';
    }

    private class ActivationContextListener
        implements ActivationContext.Listener
    {

        @Override
        public void onSatisfied( final Condition condition )
        {
            if ( condition == activateCondition )
            {
                activate();
            }
        }

        @Override
        public void onUnsatisfied( final Condition condition )
        {
            if ( condition == activateCondition )
            {
                passivate();
            }
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "{condition=" + activateCondition + '}';
        }

    }

}
