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
package org.sonatype.nexus.timeline;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

/**
 * A Timeline that is used when no real NexusTimeline implementation is found in system. It does nothing.
 *
 * @author: cstamas
 * @since 1.10.0
 */
public class NoopTimeline
    implements NexusTimeline
{
    static final NexusTimeline INSTANCE = new NoopTimeline();

    private NoopTimeline()
    {
    }

    @Override
    public void add( final long timestamp, final String type, final String subType, final Map<String, String> data )
    {
    }

    @Override
    public Entries retrieve( final int fromItem, final int count, final Set<String> types, final Set<String> subtypes,
                             final Predicate<Entry> filter )
    {
        return new Entries()
        {
            @Override
            public void release()
            {
            }

            @Override
            public Iterator<Entry> iterator()
            {
                return Iterators.emptyIterator();
            }
        };
    }

    @Override
    public int purgeOlderThan( final long timestamp, final Set<String> types, final Set<String> subTypes,
                               final Predicate<Entry> filter )
    {
        return 0;
    }
}
