package org.sonatype.nexus.timeline;

import java.util.Map;
import java.util.Set;

import org.sonatype.timeline.TimelineFilter;
import org.sonatype.timeline.TimelineResult;

/**
 * NexusTimeline just decouples the spice Timeline interface from Nexus guts, to allow more freedom. It has pretty much
 * same methods, but allows us to transition more easily.
 * 
 * @author cstamas
 */
public interface NexusTimeline
{
    /**
     * Adds a record to the timeline.
     * 
     * @param timestamp the timestamp of record. Used for ordering.
     * @param type the type of record. Used for retrieval. Cannot be null.
     * @param subType the subtype of record. Used for retrieval. Cannot be null.
     * @param data the map of "data". Cannot be null.
     */
    void add( long timestamp, String type, String subType, Map<String, String> data );

    /**
     * Retrieves the timeline records that are timestamped in between of fromTs, in descending order (newest 1st, oldest
     * last).
     * 
     * @param fromItem the count of items you want to "skip" (paging). 0 if none, "from beginning".
     * @param count the max count of records you want to fetch.
     * @param types the types you want to fetch or null if "all" (do not filter by types).
     * @param subtypes the subtypes you want to fetch or null if "all" (do not filter by subtypes).
     * @param filter filter, may be null.
     * @return the found records as result.
     */
    TimelineResult retrieve( int fromItem, int count, Set<String> types, Set<String> subtypes, TimelineFilter filter );

    /**
     * Purges all records from timeline that are older than timestamp, has one of the types and subtypes passed in, and
     * is not filtered out by filter.
     * 
     * @param timestamp the timestamp to which compared older records needs to be purged.
     * @param types the types you want to purge only or null if "all" (do not filter by types).
     * @param subTypes the subtypes you want to purge only or null if "all" (do not filter by subtypes).
     * @param filter filter, may be null.
     * @return the count of purged records.
     */
    int purgeOlderThan( long timestamp, Set<String> types, Set<String> subTypes, TimelineFilter filter );
}
