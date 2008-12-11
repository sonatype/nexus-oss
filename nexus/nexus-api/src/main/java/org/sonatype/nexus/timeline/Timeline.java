/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.timeline;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.NexusService;
import org.sonatype.nexus.proxy.events.EventListener;

public interface Timeline
    extends NexusService, EventListener
{
    void add( String type, String subType, Map<String, String> data );

    void addAll( String type, String subType, Collection<Map<String, String>> data );

    void add( long timestamp, String type, String subType, Map<String, String> data );

    void addAll( long timestamp, String type, String subType, Collection<Map<String, String>> data );

    void purgeAll();

    void purgeAll( Set<String> types );

    void purgeAll( Set<String> types, Set<String> subTypes, TimelineFilter filter );

    void purgeOlderThan( long timestamp );

    void purgeOlderThan( long timestamp, Set<String> types );

    void purgeOlderThan( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter );

    List<Map<String, String>> retrieveNewest( int count, Set<String> types );

    List<Map<String, String>> retrieveNewest( int count, Set<String> types, Set<String> subtypes, TimelineFilter filter );

    List<Map<String, String>> retrieve( long fromTs, int count, Set<String> types );

    List<Map<String, String>> retrieve( long fromTs, int count, Set<String> types, Set<String> subtypes,
        TimelineFilter filter );

    List<Map<String, String>> retrieve( int fromItem, int count, Set<String> types );

    List<Map<String, String>> retrieve( int fromItem, int count, Set<String> types, Set<String> subtypes,
        TimelineFilter filter );
}
