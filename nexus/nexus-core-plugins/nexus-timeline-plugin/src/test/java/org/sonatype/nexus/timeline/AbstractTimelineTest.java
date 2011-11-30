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
