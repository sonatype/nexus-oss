package org.sonatype.nexus.util.time;

import org.joda.time.DateTime;

/**
 * A provider of the current time, used instead of direct calls to {@link System#currentTimeMillis()} so that
 * the clock can be mocked out.
 *
 * @since 3.0
 */
public class Clock
{
  public long currentTimeMillis(){
    return System.currentTimeMillis();
  }

  public DateTime getTime(){
    return new DateTime();
  }
}
