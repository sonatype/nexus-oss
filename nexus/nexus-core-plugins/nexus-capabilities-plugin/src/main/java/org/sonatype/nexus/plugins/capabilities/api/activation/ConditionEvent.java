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
package org.sonatype.nexus.plugins.capabilities.api.activation;

import org.sonatype.plexus.appevents.AbstractEvent;

/**
 * {@link Condition} related events.
 *
 * @since 1.10.0
 */
public class ConditionEvent
    extends AbstractEvent<Condition>
{

    public ConditionEvent( final Condition condition )
    {
        super( condition );
    }

    public Condition getCondition()
    {
        return getEventSender();
    }

        @Override
        public String toString()
        {
            return getCondition().toString();
        }

    /**
     * Event fired when a condition becomes satisfied.
     *
     * @since 1.10.0
     */
    public static class Satisfied
        extends ConditionEvent
    {

        public Satisfied( final Condition condition )
        {
            super( condition );
        }

        @Override
        public String toString()
        {
            return super.toString() + " is satisfied";
        }

    }

    /**
     * Event fired when a condition becomes unsatisfied.
     *
     * @since 1.10.0
     */
    public static class Unsatisfied
        extends ConditionEvent
    {

        public Unsatisfied( final Condition condition )
        {
            super( condition );
        }

        @Override
        public String toString()
        {
            return super.toString() + " is unsatisfied";
        }

    }

}