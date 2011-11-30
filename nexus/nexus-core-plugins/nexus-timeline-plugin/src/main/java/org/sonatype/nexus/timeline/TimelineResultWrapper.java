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

import java.util.Iterator;
import javax.annotation.Nullable;

import org.sonatype.timeline.TimelineRecord;
import org.sonatype.timeline.TimelineResult;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

/**
 * Wrapping TimelineResult into Entries.
 *
 * @author: cstamas
 * @since 1.10.0
 */
public class TimelineResultWrapper
    implements Entries
{

    private final TimelineResult result;

    public TimelineResultWrapper( final TimelineResult result )
    {
        this.result = Preconditions.checkNotNull( result );
    }

    @Override
    public void release()
    {
        result.release();
    }

    @Override
    public Iterator<Entry> iterator()
    {
        return Iterators.transform( result.iterator(), new Function<TimelineRecord, Entry>()
        {
            @Override
            public Entry apply( @Nullable final TimelineRecord input )
            {
                return new TimelineRecordWrapper( input );
            }
        } );
    }
}
