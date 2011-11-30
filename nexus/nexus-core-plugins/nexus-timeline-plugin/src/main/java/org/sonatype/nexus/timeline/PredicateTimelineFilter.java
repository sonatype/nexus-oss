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

import org.sonatype.timeline.TimelineFilter;
import org.sonatype.timeline.TimelineRecord;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

/**
 * Wrapping predicate into TimelineFilter.
 *
 * @author: cstamas
 * @since 1.10.0
 */
public class PredicateTimelineFilter
    implements TimelineFilter
{

    private final Predicate<Entry> predicate;

    public PredicateTimelineFilter( final Predicate<Entry> predicate )
    {
        this.predicate = Preconditions.checkNotNull( predicate );
    }

    @Override
    public boolean accept( final TimelineRecord timelineRecord )
    {
        return predicate.apply( new TimelineRecordWrapper( timelineRecord ) );
    }
}
