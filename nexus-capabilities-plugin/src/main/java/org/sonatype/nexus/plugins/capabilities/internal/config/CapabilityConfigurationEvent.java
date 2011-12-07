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
package org.sonatype.nexus.plugins.capabilities.internal.config;

import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.plexus.appevents.AbstractEvent;

/**
 * Capability configuration (persistent storage) related events.
 *
 * @since 1.10.0
 */
public class CapabilityConfigurationEvent
    extends AbstractEvent<CCapability>
{

    public CapabilityConfigurationEvent( final CCapability capability )
    {
        super( capability );
    }

    public CCapability getCapability()
    {
        return getEventSender();
    }

    @Override
    public String toString()
    {
        final CCapability capability = getCapability();
        return String.format(
            "capability configuration {id='%s', type='%s', enabled='%s', description='%s'}",
            capability.getId(), capability.getTypeId(), capability.isEnabled(), capability.getDescription()
        );
    }

    /**
     * Event fired after capability was added to persistence store.
     *
     * @since 1.10.0
     */
    public static class Added
        extends CapabilityConfigurationEvent
    {

        public Added( final CCapability capability )
        {
            super( capability );
        }

        @Override
        public String toString()
        {
            return "Added " + super.toString();
        }

    }

    /**
     * Event fired after capability was loaded from persistence store.
     *
     * @since 1.10.0
     */
    public static class Loaded
        extends CapabilityConfigurationEvent
    {

        public Loaded( final CCapability capability )
        {
            super( capability );
        }

        @Override
        public String toString()
        {
            return "Loaded " + super.toString();
        }

    }

    /**
     * Event fired after capability was removed from persistence store.
     *
     * @since 1.10.0
     */
    public static class Removed
        extends CapabilityConfigurationEvent
    {

        public Removed( final CCapability capability )
        {
            super( capability );
        }

        @Override
        public String toString()
        {
            return "Removed " + super.toString();
        }

    }

    /**
     * Event fired after capability was updated on persistence store.
     *
     * @since 1.10.0
     */
    public static class Updated
        extends CapabilityConfigurationEvent
    {

        private final CCapability previous;

        public Updated( final CCapability capability, final CCapability previous )
        {
            super( capability );
            this.previous = previous;
        }

        public CCapability getPreviousCapability()
        {
            return previous;
        }

        @Override
        public String toString()
        {
            return "Updated " + super.toString();
        }

    }

}