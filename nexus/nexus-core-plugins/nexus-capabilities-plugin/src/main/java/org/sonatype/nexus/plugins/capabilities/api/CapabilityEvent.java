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
package org.sonatype.nexus.plugins.capabilities.api;

import org.sonatype.plexus.appevents.AbstractEvent;

/**
 * {@link Capability} related events.
 *
 * @since 1.10.0
 */
public class CapabilityEvent
    extends AbstractEvent<CapabilityReference>
{

    public CapabilityEvent( final CapabilityReference reference )
    {
        super( reference );
    }

    public CapabilityReference getReference()
    {
        return getEventSender();
    }

    @Override
    public String toString()
    {
        return getReference().toString();
    }

    /**
     * Event fired after a capability was activated.
     *
     * @since 1.10.0
     */
    public static class AfterActivated
        extends CapabilityEvent
    {

        public AfterActivated( final CapabilityReference reference )
        {
            super( reference );
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
     * @since 1.10.0
     */
    public static class BeforePassivated
        extends CapabilityEvent
    {

        public BeforePassivated( final CapabilityReference reference )
        {
            super( reference );
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
     * @since 1.10.0
     */
    public static class BeforeUpdate
        extends CapabilityEvent
    {

        public BeforeUpdate( final CapabilityReference reference )
        {
            super( reference );
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
     * @since 1.10.0
     */
    public static class AfterUpdate
        extends CapabilityEvent
    {

        public AfterUpdate( final CapabilityReference reference )
        {
            super( reference );
        }

        @Override
        public String toString()
        {
            return "After update of " + super.toString();
        }

    }
}