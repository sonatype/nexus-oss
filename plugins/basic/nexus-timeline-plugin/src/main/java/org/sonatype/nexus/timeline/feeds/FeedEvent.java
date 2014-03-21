package org.sonatype.nexus.timeline.feeds;

import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A feed event encapsulates one entry out of the feed.
 */
public class FeedEvent
{
  /**
   * 1st level selectivity.
   */
  private final String eventType; // groups events to logical sets

  /**
   * 2nd level selectivity.
   */
  private final String eventSubType; // groups events to logical sets

  /**
   * Event class this feed event originates ("is made from"). It is recommended to use the FQCN of the event
   * or some appropriate identifier. This value will drive the rendering of the event, as based on it will
   * proper template be selected when rendering feed.
   */
  private final String eventClass; // determines exact class of originating Nx event

  /**
   * Date when event happened.
   */
  private final Date published;

  /**
   * "Author" if applicable, is nullable.
   */
  private final String author;

  /**
   * Link if applicable, is nullable.
   */
  private final String link;

  /**
   * Any extra data that was gathered as part of the event.
   */
  private final Map<String, String> data;

  public FeedEvent(final String eventType,
                   final String eventSubType,
                   final String eventClass,
                   final Date published,
                   @Nullable final String author,
                   @Nullable final String link,
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
