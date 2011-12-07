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

    private final ActivationConditionHandler activationHandler;

    private final ValidityConditionHandler validityHandler;

    private final ReentrantReadWriteLock stateLock;

    private State state;

    DefaultCapabilityReference( final NexusEventBus eventBus,
                                final ActivationConditionHandlerFactory activationListenerFactory,
                                final ValidityConditionHandlerFactory validityConditionHandlerFactory,
                                final Capability capability )
    {
        this.eventBus = checkNotNull( eventBus );
        this.capability = checkNotNull( capability );

        stateLock = new ReentrantReadWriteLock();

        activationHandler = checkNotNull( activationListenerFactory ).create( this );
        validityHandler = checkNotNull( validityConditionHandlerFactory ).create( this );

        state = new NewState();
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
            return state.isEnabled();
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
            state.enable();
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
            state.disable();
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
            return state.isActive();
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
            state.activate();
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
            state.passivate();
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
            state.create( properties );
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
            state.load( properties );
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
                state.update( properties, previousProperties );
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
            state.remove();
        }
        finally
        {
            stateLock.writeLock().unlock();
        }
    }

    @Override
    public String stateDescription()
    {
        try
        {
            stateLock.readLock().lock();
            return state.stateDescription();
        }
        finally
        {
            stateLock.readLock().unlock();
        }
    }

    @Override
    public String toString()
    {
        return String.format( "capability %s (enabled=%s, active=%s)", capability, isEnabled(), isActive() );
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

    private class State
        implements CapabilityReference
    {

        State()
        {
            getLogger().debug(
                "Capability {} ({}) state changed to {}", new Object[]{ capability, capability.id(), this }
            );
        }

        @Override
        public Capability capability()
        {
            return capability;
        }

        @Override
        public boolean isEnabled()
        {
            return false;
        }

        @Override
        public void enable()
        {
            throw new IllegalStateException( "State '" + toString() + "' does not permit 'enable' operation" );
        }

        @Override
        public void disable()
        {
            throw new IllegalStateException( "State '" + toString() + "' does not permit 'disable' operation" );
        }

        @Override
        public boolean isActive()
        {
            return false;
        }

        @Override
        public void activate()
        {
            throw new IllegalStateException( "State '" + toString() + "' does not permit 'activate' operation" );
        }

        @Override
        public void passivate()
        {
            throw new IllegalStateException( "State '" + toString() + "' does not permit 'passivate' operation" );
        }

        @Override
        public void create( final Map<String, String> properties )
        {
            throw new IllegalStateException( "State '" + toString() + "' does not permit 'create' operation" );
        }

        @Override
        public void load( final Map<String, String> properties )
        {
            throw new IllegalStateException( "State '" + toString() + "' does not permit 'load' operation" );
        }

        @Override
        public void update( final Map<String, String> properties, final Map<String, String> previousProperties )
        {
            throw new IllegalStateException( "State '" + toString() + "' does not permit 'update' operation" );
        }

        @Override
        public void remove()
        {
            throw new IllegalStateException( "State '" + toString() + "' does not permit 'remove' operation" );
        }

        @Override
        public String stateDescription()
        {
            return "Undefined";
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName();
        }

        void setDescription( final String description )
        {
            // do nothing
        }

    }

    public class NewState
        extends State
    {

        @Override
        public void create( final Map<String, String> properties )
        {
            try
            {
                capability().create( properties );
                validityHandler.bind();
                state = new ValidState();
            }
            catch ( Exception e )
            {
                state = new InvalidState( "Failed to load: " + e.getMessage() );
                getLogger().error(
                    "Could not create capability {} ({})", new Object[]{ capability, capability.id(), e }
                );
            }
        }

        @Override
        public void load( final Map<String, String> properties )
        {
            try
            {
                capability().load( properties );
                validityHandler.bind();
                state = new ValidState();
            }
            catch ( Exception e )
            {
                state = new InvalidState( "Failed to load: " + e.getMessage() );
                getLogger().error(
                    "Could not load capability {} ({})", new Object[]{ capability, capability.id(), e }
                );
            }
        }

        @Override
        public String stateDescription()
        {
            return "New";
        }

        @Override
        public String toString()
        {
            return "NEW";
        }

    }

    public class ValidState
        extends State
    {

        @Override
        public void enable()
        {
            getLogger().debug( "Enabling capability {} ({})", capability, capability.id() );
            state = new EnabledState( "Not yet activated" );
            activationHandler.bind();
        }

        @Override
        public void disable()
        {
            // do nothing (not yet enabled)
        }

        @Override
        public void passivate()
        {
            // do nothing (not yet activated)
        }

        @Override
        public void update( final Map<String, String> properties, final Map<String, String> previousProperties )
        {
            try
            {
                eventBus.post( new CapabilityEvent.BeforeUpdate( DefaultCapabilityReference.this ) );
                capability().update( properties );
                eventBus.post( new CapabilityEvent.AfterUpdate( DefaultCapabilityReference.this ) );
            }
            catch ( Exception e )
            {
                getLogger().error(
                    "Could not update capability {} ({}).", new Object[]{ capability, capability.id(), e }
                );
                DefaultCapabilityReference.this.passivate();
                state.setDescription( "Update failed: " + e.getMessage() );
            }
        }

        @Override
        public void remove()
        {
            try
            {
                DefaultCapabilityReference.this.disable();
                validityHandler.release();
                capability().remove();
                state = new RemovedState();
            }
            catch ( Exception e )
            {
                state = new InvalidState( "Failed to remove: " + e.getMessage() );
                getLogger().error(
                    "Could not remove capability {} ({})", new Object[]{ capability, capability.id(), e }
                );
            }
        }

        @Override
        public String stateDescription()
        {
            return "Disabled";
        }

        @Override
        public String toString()
        {
            return "VALID";
        }

    }

    public class EnabledState
        extends ValidState
    {

        private String description;

        EnabledState()
        {
            this( "enabled" );
        }

        EnabledState( final String description )
        {
            this.description = description;
        }

        @Override
        public boolean isEnabled()
        {
            return true;
        }

        @Override
        public void enable()
        {
            // do nothing (already enabled)
        }

        @Override
        public void disable()
        {
            getLogger().debug( "Disabling capability {} ({})", capability, capability.id() );
            activationHandler.release();
            DefaultCapabilityReference.this.passivate();
            state = new ValidState();
        }

        @Override
        public void activate()
        {
            if ( activationHandler.isConditionSatisfied() )
            {
                getLogger().debug( "Activating capability {} ({})", capability, capability.id() );
                try
                {
                    capability().activate();
                    getLogger().debug( "Activated capability {} ({})", capability, capability.id() );
                    state = new ActiveState();
                    eventBus.post( new CapabilityEvent.AfterActivated( DefaultCapabilityReference.this ) );
                }
                catch ( Exception e )
                {
                    getLogger().error(
                        "Could not activate capability {} ({})", new Object[]{ capability, capability.id(), e }
                    );
                    state.setDescription( "Activation failed: " + e.getMessage() );
                }
            }
            else
            {
                getLogger().debug( "Capability {} ({}) is not yet activatable", capability, capability.id() );
            }
        }

        @Override
        public void passivate()
        {
            // do nothing (not yet activated)
        }

        @Override
        public String stateDescription()
        {
            return activationHandler.isConditionSatisfied() ? description : activationHandler.explainWhyNotSatisfied();
        }

        @Override
        public String toString()
        {
            return "ENABLED";
        }

        @Override
        void setDescription( final String description )
        {
            this.description = description;
        }
    }

    public class ActiveState
        extends EnabledState
    {

        @Override
        public boolean isActive()
        {
            return true;
        }

        @Override
        public void activate()
        {
            // do nothing (already active)
        }

        @Override
        public void passivate()
        {
            getLogger().debug( "Passivating capability {} ({})", capability, capability.id() );
            try
            {
                state = new EnabledState( "Passivated" );
                eventBus.post( new CapabilityEvent.BeforePassivated( DefaultCapabilityReference.this ) );
                capability().passivate();
                getLogger().debug( "Passivated capability {} ({})", capability, capability.id() );
            }
            catch ( Exception e )
            {
                getLogger().error(
                    "Could not passivate capability {} ({})", new Object[]{ capability, capability.id(), e }
                );
                state.setDescription( "Passivation failed: " + e.getMessage() );
            }
        }

        @Override
        public String stateDescription()
        {
            return "Active";
        }

        @Override
        public String toString()
        {
            return "ACTIVE";
        }

    }

    public class InvalidState
        extends State
    {

        private final String reason;

        InvalidState( final String reason )
        {
            this.reason = reason;
        }

        @Override
        public String stateDescription()
        {
            return reason;
        }

        @Override
        public String toString()
        {
            return "INVALID (" + reason + ")";
        }

    }

    public class RemovedState
        extends State
    {

        @Override
        public String stateDescription()
        {
            return "Removed";
        }

        @Override
        public String toString()
        {
            return "REMOVED";
        }

    }

}
