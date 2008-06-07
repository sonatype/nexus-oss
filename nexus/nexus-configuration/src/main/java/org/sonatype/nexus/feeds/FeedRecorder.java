/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.feeds;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A recorder for events for later retrieval. This is actually event recorder, not feed recorder.
 * 
 * @author cstamas
 */
public interface FeedRecorder
{
    String ROLE = FeedRecorder.class.getName();

    /**
     * System event action: boot
     */
    public static final String SYSTEM_BOOT_ACTION = "BOOT";

    /**
     * System event action: configuration
     */
    public static final String SYSTEM_CONFIG_ACTION = "CONFIG";

    /**
     * System event action: timeline purge
     */
    public static final String SYSTEM_TL_PURGE_ACTION = "TL_PURGE";

    /**
     * System event action: reindex
     */
    public static final String SYSTEM_REINDEX_ACTION = "REINDEX";

    /**
     * System event action: publish indexes
     */
    public static final String SYSTEM_PUBLISHINDEX_ACTION = "PUBLISHINDEX";

    /**
     * System event action: rebuildAttributes
     */
    public static final String SYSTEM_REBUILDATTRIBUTES_ACTION = "REBUILDATTRIBUTES";

    /**
     * System event action: clearCache
     */
    public static final String SYSTEM_CLEARCACHE_ACTION = "CLEARCACHE";

    /**
     * System event action: removeSnapshots
     */
    public static final String SYSTEM_REMOVE_SNAPSHOTS_ACTION = "REMOVESNAPSHOTS";

    // service

    void startService()
        throws IOException;

    void stopService()
        throws IOException;

    // creating

    void addNexusArtifactEvent( NexusArtifactEvent nae );

    void addSystemEvent( String action, String message );

    SystemProcess systemProcessStarted( String action, String message );

    void systemProcessFinished( SystemProcess prc );

    void systemProcessBroken( SystemProcess prc, Throwable e );

    // reading

    List<Map<String, String>> getEvents( Set<String> types, Set<String> subtypes, Integer from, Integer count );

    List<NexusArtifactEvent> getNexusArtifectEvents( Set<String> subtypes, Integer from, Integer count );

    List<SystemEvent> getSystemEvents( Set<String> subtypes, Integer from, Integer count );
}
