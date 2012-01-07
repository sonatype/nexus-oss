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
package org.sonatype.nexus.plugins.capabilities;

import static org.sonatype.appcontext.internal.Preconditions.checkNotNull;

import org.sonatype.plexus.appevents.AbstractEvent;

/**
 * {@link Capability} related events.
 *
 * @since 2.0
 */
public class CapabilityEvent
    extends AbstractEvent<CapabilityRegistry>
{

    private final CapabilityReference reference;

    public CapabilityEvent( final CapabilityRegistry capabilityRegistry,
                            final CapabilityReference reference )
    {
        super( checkNotNull( capabilityRegistry ) );
        this.reference = checkNotNull( reference );
    }

    public CapabilityReference getReference()
    {
        return reference;
    }

    @Override
    public String toString()
    {
        return getReference().toString();
    }

    /**
     * Event fired after a capability was activated.
     *
     * @since 2.0
     */
    public static class AfterActivated
        extends CapabilityEvent
    {

        public AfterActivated( final CapabilityRegistry capabilityRegistry,
                               final CapabilityReference reference )
        {
            super( capabilityRegistry, reference );
        }

        @Override
        public String toString()
        {
            return "Activated " + super.toString();
        }

    }

    /**
     * Event fired before a capability is passivated.
     *
     * @since 2.0
     */
    public static class BeforePassivated
        extends CapabilityEvent
    {

        public BeforePassivated( final CapabilityRegistry capabilityRegistry,
                                 final CapabilityReference reference )
        {
            super( capabilityRegistry, reference );
        }

        @Override
        public String toString()
        {
            return "Passivated " + super.toString();
        }

    }

    /**
     * Event fired before a capability is updated.
     *
     * @since 2.0
     */
    public static class BeforeUpdate
        extends CapabilityEvent
    {

        public BeforeUpdate( final CapabilityRegistry capabilityRegistry,
                             final CapabilityReference reference )
        {
            super( capabilityRegistry, reference );
        }

        @Override
        public String toString()
        {
            return "Before update of " + super.toString();
        }

    }

    /**
     * Event fired after a capability was updated.
     *
     * @since 2.0
     */
    public static class AfterUpdate
        extends CapabilityEvent
    {

        public AfterUpdate( final CapabilityRegistry capabilityRegistry,
                            final CapabilityReference reference )
        {
            super( capabilityRegistry, reference );
        }

        @Override
        public String toString()
        {
            return "After update of " + super.toString();
        }

    }

    /**
     * Event fired when a capability is created (added to registry).
     * <p/>
     * Called before {@link Capability#create(java.util.Map)} / {@link Capability#load(java.util.Map)} are called.
     *
     * @since 2.0
     */
    public static class Created
        extends CapabilityEvent
    {

        public Created( final CapabilityRegistry capabilityRegistry,
                        final CapabilityReference reference )
        {
            super( capabilityRegistry, reference );
        }

        @Override
        public String toString()
        {
            return "Created " + super.toString();
        }

    }

    /**
     * Event fired when a capability is removed from registry.
     * <p/>
     * Called after {@link Capability#remove()} is called.
     *
     * @since 2.0
     */
    public static class AfterRemove
        extends CapabilityEvent
    {

        public AfterRemove( final CapabilityRegistry capabilityRegistry,
                            final CapabilityReference reference )
        {
            super( capabilityRegistry, reference );
        }

        @Override
        public String toString()
        {
            return "After remove of " + super.toString();
        }

    }

}