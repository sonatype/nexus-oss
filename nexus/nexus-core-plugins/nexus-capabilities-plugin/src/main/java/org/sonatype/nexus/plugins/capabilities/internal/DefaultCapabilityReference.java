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

import java.util.Map;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.support.activation.Conditions;

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

    private final CapabilityConfiguration configuration;

    private final Conditions conditions;

    private boolean active;

    private boolean enabled;

    private Condition activateCondition;

    private ActivationListener activationListener;

    private Condition validityCondition;

    private ValidityListener validityListener;

    private NexusActiveListener nexusActiveListener;

    DefaultCapabilityReference( final DefaultCapabilityRegistry registry,
                                final ActivationContext activationContext,
                                final CapabilityConfiguration configuration,
                                final Conditions conditions,
                                final Capability capability )
    {
        this.registry = checkNotNull( registry );
        this.activationContext = checkNotNull( activationContext );
        this.configuration = checkNotNull( configuration );
        this.conditions = checkNotNull( conditions );
        this.capability = checkNotNull( capability );

        active = false;
        enabled = false;
    }

    @Override
    public Capability capability()
    {
        return capability;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void enable()
    {
        if ( !isEnabled() )
        {
            getLogger().debug( "Enabling capability with id '{}' ({})", capability.id(), capability );
            enabled = true;
            activate();
            activateCondition = capability().activationCondition();
            if ( activateCondition != null )
            {
                activateCondition.bind();
                activationListener = new ActivationListener();
                activationContext.addListener( activationListener, activateCondition );
            }
        }
    }

    @Override
    public void disable()
    {
        if ( isEnabled() )
        {
            getLogger().debug( "Disabling capability with id '{}' ({})", capability.id(), capability );
            if ( activationListener != null )
            {
                activationContext.removeListener( activationListener, activateCondition );
                activationListener = null;
            }
            if ( activateCondition != null )
            {
                activateCondition.release();
                activateCondition = null;
            }
            passivate();
            enabled = false;
        }
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    public void activate()
    {
        if ( isEnabled() && !isActive() && ( activateCondition == null || activateCondition.isSatisfied() ) )
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
                    "Could not activate capability with id '{}' ({})", new Object[]{ capability.id(), capability, e }
                );
            }

        }
    }

    @Override
    public void passivate()
    {
        if ( isEnabled() && isActive() )
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
                    "Could not passivate capability with id '{}' ({})", new Object[]{ capability.id(), capability, e }
                );
            }
        }
    }

    @Override
    public void create( final Map<String, String> properties )
    {
        capability().create( properties );
        if ( nexusActiveListener == null )
        {
            nexusActiveListener = new NexusActiveListener().bind();
        }
    }

    @Override
    public void load( final Map<String, String> properties )
    {
        capability().load( properties );
        if ( nexusActiveListener == null )
        {
            nexusActiveListener = new NexusActiveListener().bind();
        }
    }

    @Override
    public void update( final Map<String, String> properties, final Map<String, String> previousProperties )
    {
        if ( !sameProperties( previousProperties, properties ) )
        {
            registry.notify( this, new DefaultCapabilityRegistry.Notifier( "updated" )
            {
                @Override
                void run( final CapabilityRegistry.Listener listener, final CapabilityReference reference )
                {
                    listener.beforeUpdate( reference );
                }
            } );
            capability().update( properties );
            registry.notify( this, new DefaultCapabilityRegistry.Notifier( "updated" )
            {
                @Override
                void run( final CapabilityRegistry.Listener listener, final CapabilityReference reference )
                {
                    listener.afterUpdate( reference );
                }
            } );
        }
    }

    @Override
    public void remove()
    {
        if ( activateCondition != null )
        {
            activateCondition.release();
        }
        disable();
        if ( nexusActiveListener!=null )
        {
            nexusActiveListener.release();
        }
        capability().remove();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{active=" + active + ", capability=" + capability + '}';
    }

    // @TestAccessible //
    static boolean sameProperties( final Map<String, String> p1, final Map<String, String> p2 )
    {
        if ( p1 == null )
        {
            return p2 == null;
        }
        else if ( p2 == null )
        {
            return false;
        }
        if ( p1.size() != p2.size() )
        {
            return false;
        }
        return p1.equals( p2 );
    }

    private class ActivationListener
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
            return String.format(
                "Capability '%s (id=%s)' watching for '%s' condition to activate itself",
                capability(), capability().id(), activateCondition
            );
        }

    }

    private class ValidityListener
        implements ActivationContext.Listener
    {

        @Override
        public void onSatisfied( final Condition condition )
        {
            // do nothing
        }

        @Override
        public void onUnsatisfied( final Condition condition )
        {
            if ( condition == validityCondition )
            {
                try
                {
                    configuration.remove( capability().id() );
                }
                catch ( Exception e )
                {
                    getLogger().error( "Failed to remove capability with id '{}'", capability().id(), e );
                }
            }
        }

        @Override
        public String toString()
        {
            return String.format(
                "Capability '%s (id=%s)' watching for '%s' condition to validate itself",
                capability(), capability().id(), validityCondition
            );
        }

    }

    private class NexusActiveListener
        implements ActivationContext.Listener
    {

        private Condition nexusActiveCondition;

        @Override
        public void onSatisfied( final Condition condition )
        {
            if ( condition == nexusActiveCondition )
            {
                validityCondition = capability().validityCondition();
                if ( validityCondition != null )
                {
                    validityCondition.bind();
                    validityListener = new ValidityListener();
                    activationContext.addListener( validityListener, validityCondition );
                }
            }
        }

        @Override
        public void onUnsatisfied( final Condition condition )
        {
            if ( condition == nexusActiveCondition )
            {
                if ( validityListener != null )
                {
                    activationContext.removeListener( validityListener, validityCondition );
                    validityListener = null;
                }
                if ( validityCondition != null )
                {
                    validityCondition.release();
                    validityCondition = null;
                }
            }
        }

        @Override
        public String toString()
        {
            return String.format(
                "Capability '%s (id=%s)' watching for '%s' condition to activate validation check",
                capability(), capability().id(), nexusActiveCondition
            );
        }

        public NexusActiveListener bind()
        {
            if ( nexusActiveCondition == null )
            {
                nexusActiveCondition = conditions.nexus().active();
                activationContext.addListener( this, nexusActiveCondition );
                if ( nexusActiveCondition.isSatisfied() )
                {
                    onSatisfied( nexusActiveCondition );
                }
            }
            return this;
        }

        public NexusActiveListener release()
        {
            if ( nexusActiveCondition != null )
            {
                onUnsatisfied( nexusActiveCondition );
                activationContext.removeListener( this, nexusActiveCondition );
            }
            return this;
        }
    }

}
