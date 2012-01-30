/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
 * @since 2.0
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
