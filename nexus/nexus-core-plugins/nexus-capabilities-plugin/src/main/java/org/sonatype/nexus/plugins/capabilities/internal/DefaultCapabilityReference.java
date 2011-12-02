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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.sonatype.nexus.eventbus.NexusEventBus;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityEvent;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;
import org.sonatype.nexus.plugins.capabilities.api.activation.ConditionEvent;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.support.activation.Conditions;
import org.sonatype.nexus.proxy.IllegalOperationException;
import com.google.common.eventbus.Subscribe;

/**
 * Default {@link CapabilityReference} implementation.
 *
 * @since 1.10.0
 */
class DefaultCapabilityReference
    extends AbstractLoggingComponent
    implements CapabilityReference
{

    private final Capability capability;

    private final NexusEventBus eventBus;

    private final CapabilityConfiguration configuration;

    private final Conditions conditions;

    private final ReentrantReadWriteLock stateLock;

    /**
     * A reference is valid until is removed. Once removed all calls will throw an {@link IllegalOperationException}.
     */
    private boolean valid;

    private boolean active;

    private boolean enabled;

    private Condition activateCondition;

    private ActivationListener activationListener;

    private Condition validityCondition;

    private ValidityListener validityListener;

    private NexusActiveListener nexusActiveListener;

    DefaultCapabilityReference( final NexusEventBus eventBus,
                                final CapabilityConfiguration configuration,
                                final Conditions conditions,
                                final Capability capability )
    {
        this.eventBus = checkNotNull( eventBus );
        this.configuration = checkNotNull( configuration );
        this.conditions = checkNotNull( conditions );
        this.capability = checkNotNull( capability );

        active = false;
        enabled = false;
        valid = true;

        stateLock = new ReentrantReadWriteLock();
    }

    @Override
    public Capability capability()
    {
        return capability;
    }

    @Override
    public boolean isEnabled()
    {
        try
        {
            stateLock.readLock().lock();
            checkValid();

            return enabled;
        }
        finally
        {
            stateLock.readLock().unlock();
        }
    }

    @Override
    public void enable()
    {
        try
        {
            stateLock.writeLock().lock();
            checkValid();

            if ( !isEnabled() )
            {
                getLogger().debug( "Enabling capability {} ({})", capability, capability.id() );
                enabled = true;
                activateCondition = capability().activationCondition();
                if ( activateCondition != null )
                {
                    activateCondition.bind();
                    activationListener = new ActivationListener();
                    eventBus.register( activationListener );
                }
                activate();
            }
        }
        finally
        {
            stateLock.writeLock().unlock();
        }
    }

    @Override
    public void disable()
    {
        try
        {
            stateLock.writeLock().lock();
            checkValid();

            if ( isEnabled() )
            {
                getLogger().debug( "Disabling capability {} ({})", capability, capability.id() );
                if ( activationListener != null )
                {
                    eventBus.unregister( activationListener );
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
        finally
        {
            stateLock.writeLock().unlock();
        }
    }

    @Override
    public boolean isActive()
    {
        try
        {
            stateLock.readLock().lock();
            checkValid();

            return active;
        }
        finally
        {
            stateLock.readLock().unlock();
        }
    }

    @Override
    public void activate()
    {
        try
        {
            stateLock.writeLock().lock();
            checkValid();

            if ( isEnabled() && !isActive() )
            {
                if ( activateCondition == null || activateCondition.isSatisfied() )
                {
                    getLogger().debug( "Activating capability {} ({})", capability, capability.id() );
                    try
                    {
                        capability().activate();
                        getLogger().debug( "Activated capability {} ({})", capability, capability.id() );
                        active = true;
                        eventBus.post( new CapabilityEvent.AfterActivated( this ) );
                    }
                    catch ( Exception e )
                    {
                        getLogger().error(
                            "Could not activate capability {} ({})", new Object[]{ capability, capability.id(), e }
                        );
                    }
                }
                else
                {
                    getLogger().debug( "Capability {} ({}) is not yet activatable", capability, capability.id() );
                }
            }
        }
        finally
        {
            stateLock.writeLock().unlock();
        }
    }

    @Override
    public void passivate()
    {
        try
        {
            stateLock.writeLock().lock();
            checkValid();

            if ( isEnabled() && isActive() )
            {
                getLogger().debug( "Passivating capability {} ({})", capability, capability.id() );
                try
                {
                    active = false;
                    eventBus.post( new CapabilityEvent.BeforePassivated( this ) );
                    capability().passivate();
                    getLogger().debug( "Passivated capability {} ({})", capability, capability.id() );
                }
                catch ( Exception e )
                {
                    getLogger().error(
                        "Could not passivate capability {} ({})", new Object[]{ capability, capability.id(), e }
                    );
                }
            }
        }
        finally
        {
            stateLock.writeLock().unlock();
        }
    }

    @Override
    public void create( final Map<String, String> properties )
    {
        try
        {
            stateLock.writeLock().lock();
            checkValid();

            capability().create( properties );
            if ( nexusActiveListener == null )
            {
                nexusActiveListener = new NexusActiveListener().bind();
            }
        }
        finally
        {
            stateLock.writeLock().unlock();
        }
    }

    @Override
    public void load( final Map<String, String> properties )
    {
        try
        {
            stateLock.writeLock().lock();
            checkValid();

            capability().load( properties );
            if ( nexusActiveListener == null )
            {
                nexusActiveListener = new NexusActiveListener().bind();
            }
        }
        finally
        {
            stateLock.writeLock().unlock();
        }
    }

    @Override
    public void update( final Map<String, String> properties, final Map<String, String> previousProperties )
    {
        if ( !sameProperties( previousProperties, properties ) )
        {
            try
            {
                stateLock.writeLock().lock();
                checkValid();

                eventBus.post( new CapabilityEvent.BeforeUpdate( this ) );
                capability().update( properties );
                eventBus.post( new CapabilityEvent.AfterUpdate( this ) );
            }
            finally
            {
                stateLock.writeLock().unlock();
            }
        }
    }

    @Override
    public void remove()
    {
        try
        {
            stateLock.writeLock().lock();
            checkValid();

            if ( activateCondition != null )
            {
                activateCondition.release();
            }
            disable();
            if ( nexusActiveListener != null )
            {
                nexusActiveListener.release();
            }
            capability().remove();
        }
        finally
        {
            valid = false;
            stateLock.writeLock().unlock();
        }
    }

    @Override
    public String toString()
    {
        return String.format( "capability %s (enabled=%s, active=%s)", capability, enabled, active );
    }

    /**
     * Check if this reference is valid. A reference will be valid until is removed. After that it will throw an
     * {@link IllegalOperationException} on all operations.
     */
    private void checkValid()
    {
        if ( !valid )
        {
            throw new IllegalStateException( "Capability reference is no longer valid (capability has been removed)" );
        }
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

    public class ActivationListener
    {

        @Subscribe
        public void handle( final ConditionEvent.Satisfied event )
        {
            if ( event.getCondition() == activateCondition )
            {
                activate();
            }
        }

        @Subscribe
        public void handle( final ConditionEvent.Unsatisfied event )
        {
            if ( event.getCondition() == activateCondition )
            {
                passivate();
            }
        }

        @Override
        public String toString()
        {
            return String.format(
                "Watching '%s' condition to activate/passivate capability '%s (id=%s)'",
                activateCondition, capability, capability.id()
            );
        }

    }

    public class ValidityListener
    {

        @Subscribe
        public void handle( final ConditionEvent.Unsatisfied event )
        {
            if ( event.getCondition() == validityCondition )
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
                "Watching '%s' condition to validate/invalidate capability '%s (id=%s)'",
                validityCondition, capability, capability.id()
            );
        }

    }

    public class NexusActiveListener
    {

        private Condition nexusActiveCondition;

        @Subscribe
        public void handle( final ConditionEvent.Satisfied event )
        {
            if ( event.getCondition() == nexusActiveCondition )
            {
                validityCondition = capability().validityCondition();
                if ( validityCondition != null )
                {
                    validityCondition.bind();
                    validityListener = new ValidityListener();
                    eventBus.register( validityListener );
                }
            }
        }

        @Subscribe
        public void handle( final ConditionEvent.Unsatisfied event )
        {
            if ( event.getCondition() == nexusActiveCondition )
            {
                if ( validityListener != null )
                {
                    eventBus.unregister( validityListener );
                    validityListener = null;
                }
                if ( validityCondition != null )
                {
                    validityCondition.release();
                    validityCondition = null;
                }
                if ( isActive() )
                {
                    passivate();
                }
            }
        }

        @Override
        public String toString()
        {
            return String.format(
                "Watching '%s' condition to trigger validation of capability '%s (id=%s)'",
                nexusActiveCondition, capability, capability.id()
            );
        }

        public NexusActiveListener bind()
        {
            if ( nexusActiveCondition == null )
            {
                nexusActiveCondition = conditions.nexus().active();
                eventBus.register( this );
                if ( nexusActiveCondition.isSatisfied() )
                {
                    handle( new ConditionEvent.Satisfied( nexusActiveCondition ) );
                }
            }
            return this;
        }

        public NexusActiveListener release()
        {
            if ( nexusActiveCondition != null )
            {
                handle( new ConditionEvent.Unsatisfied( nexusActiveCondition ) );
                eventBus.unregister( this );
            }
            return this;
        }
    }

}
