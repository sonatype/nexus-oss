/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.cache;

public class CacheStatistics
{
    private final long size;

    private final long misses;

    private final long hits;

    public CacheStatistics( long size, long misses, long hits )
    {
        super();

        this.size = size;

        this.misses = misses;

        this.hits = hits;
    }

    public long getSize()
    {
        return size;
    }

    public long getMisses()
    {
        return misses;
    }

    public long getHits()
    {
        return hits;
    }

}
