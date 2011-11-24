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
package org.sonatype.nexus.plugins.capabilities.support.activation;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.activation.ActivationContext;
import org.sonatype.nexus.plugins.capabilities.api.activation.Condition;

/**
 * Factory of {@link Condition}s related to Capabilities.
 *
 * @since 1.10.0
 */
@Named
@Singleton
public class CapabilityConditions
{

    private final CapabilityRegistry capabilityRegistry;

    private final ActivationContext activationContext;

    @Inject
    public CapabilityConditions( final CapabilityRegistry capabilityRegistry,
                                 final ActivationContext activationContext )
    {
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.activationContext = checkNotNull( activationContext );
    }

    /**
     * Creates a new condition that is satisfied when a capability of a specified type exists.
     *
     * @param type class of capability that should exist
     * @return created condition
     */
    public Condition capabilityOfTypeExists( final Class<? extends Capability> type )
    {
        return new CapabilityOfTypeExistsCondition( activationContext, capabilityRegistry, type );
    }

    /**
     * Creates a new condition that is satisfied when a capability of a specified type exists and is in an active state.
     *
     * @param type class of capability that should exist and be active
     * @return created condition
     */
    public Condition capabilityOfTypeActive( final Class<? extends Capability> type )
    {
        return new CapabilityOfTypeActiveCondition( activationContext, capabilityRegistry, type );
    }

    /**
     * Creates a new condition that can be programatically satisfied/unsatisfied.
     *
     * @return created condition
     */
    public OnDemand onDemand()
    {
        return new OnDemand( activationContext );
    }

    /**
     * A condition that is satisfied when a capability of a specified type exists.
     *
     * @since 1.10.0
     */
    private static class CapabilityOfTypeExistsCondition
        extends AbstractCondition
        implements CapabilityRegistry.Listener
    {

        private final CapabilityRegistry capabilityRegistry;

        final Class<?> type;

        CapabilityOfTypeExistsCondition( final ActivationContext activationContext,
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

    /**
     * A condition that is satisfied when a capability of a specified type exists and is in an active state.
     *
     * @since 1.10.0
     */
    private static class CapabilityOfTypeActiveCondition
        extends CapabilityOfTypeExistsCondition
    {

        CapabilityOfTypeActiveCondition( final ActivationContext activationContext,
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

    /**
     * A condition that allows a targeted capability to activated / passivated.
     *
     * @since 1.10.0
     */
    public static class OnDemand
        extends AbstractCondition
    {

        OnDemand( final ActivationContext activationContext )
        {
            super( activationContext, true );
        }

        /**
         * Reactivates the condition, fact that can trigger passivate / activate on capability using it.
         *
         * @return itself, for fluent api usage
         */
        public OnDemand reactivate()
        {
            unsatisfy();
            satisfy();
            return this;
        }

        /**
         * Marks condition as satisfied, fact that can trigger activation of capability using it.
         *
         * @return itself, for fluent api usage
         */
        public OnDemand satisfy()
        {
            setSatisfied( true );
            return this;
        }

        /**
         * Marks condition as unsatisfied, fact that can trigger passivation of capability using it.
         *
         * @return itself, for fluent api usage
         */
        public OnDemand unsatisfy()
        {
            setSatisfied( false );
            return this;
        }

    }

}
