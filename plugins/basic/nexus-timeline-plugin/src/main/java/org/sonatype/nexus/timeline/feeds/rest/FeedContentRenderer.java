package org.sonatype.nexus.timeline.feeds.rest;

import org.sonatype.nexus.timeline.feeds.FeedEvent;

/**
 * Feed content renderer renders the feed entry content and title. Usually it performs some templating, but
 * does not have to.
 *
 * @since 3.0
 */
public interface FeedContentRenderer
{
  public String getContentType(FeedEvent evt);

  public String getTitle(FeedEvent evt);

  public String getContent(FeedEvent evt);
}
