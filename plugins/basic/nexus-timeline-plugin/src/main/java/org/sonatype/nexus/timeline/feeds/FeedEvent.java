package org.sonatype.nexus.timeline.feeds;

import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class FeedEvent
{
  private final String eventType; // groups events to logical sets

  private final String eventSubType; // groups events to logical sets

  private final String eventClass; // determines exact class of originating Nx event

  private final Date published;

  private final String author;

  private final String link;

  private final Map<String, String> data;

  public FeedEvent(final String eventType,
                   final String eventSubType,
                   final String eventClass,
                   final Date published,
                   final String author,
                   final String link,
                   final Map<String, String> data)
  {
    this.eventType = checkNotNull(eventType);
    this.eventSubType = checkNotNull(eventSubType);
    this.eventClass = checkNotNull(eventClass);
    this.published = checkNotNull(published);
    this.author = author;
    this.link = link;
    this.data = checkNotNull(data);
  }

  public String getEventType() {
    return eventType;
  }

  public String getEventSubType() {
    return eventSubType;
  }

  public String getEventClass() {
    return eventClass;
  }

  public Date getPublished() {
    return published;
  }

  public String getAuthor() {
    return author;
  }

  public String getLink() {
    return link;
  }

  public Map<String, String> getData() {
    return data;
  }
}
