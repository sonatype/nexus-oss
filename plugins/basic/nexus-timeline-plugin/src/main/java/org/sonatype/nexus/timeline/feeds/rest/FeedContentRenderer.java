package org.sonatype.nexus.timeline.feeds.rest;

import org.sonatype.nexus.timeline.feeds.FeedEvent;

public interface FeedContentRenderer
{
  public String getContentType(FeedEvent evt);

  public String getTitle(FeedEvent evt);

  public String getContent(FeedEvent evt);
}
