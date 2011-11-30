package org.sonatype.nexus.timeline;

import java.util.Map;

/**
 * Timeline entry.
 *
 * @author: cstamas
 * @since 1.10.0
 */
public interface Entry
{

    long getTimestamp();

    String getType();

    String getSubType();

    Map<String, String> getData();
}
