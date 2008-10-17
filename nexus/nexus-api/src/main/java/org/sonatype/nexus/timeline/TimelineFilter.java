package org.sonatype.nexus.timeline;

import java.util.Map;

public interface TimelineFilter
{
    boolean accept( Map<String, String> hit );
}
