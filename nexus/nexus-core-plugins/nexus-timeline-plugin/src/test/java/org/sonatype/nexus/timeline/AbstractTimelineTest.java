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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.test.NexusTestSupport;

public abstract class AbstractTimelineTest
    extends NexusTestSupport
{
    /**
     * Handy method that does what was done before: keeps all in memory, but this is usable for small amount of data,
     * like these in UT. This should NOT be used in production code, unless you want app that kills itself with OOM.
     * 
     * @param result
     * @return
     */
    protected List<Entry> asList( Entries result )
    {
        ArrayList<Entry> records = new ArrayList<Entry>();

        for ( Entry rec : result )
        {
            records.add( rec );
        }

        return records;
    }
}
