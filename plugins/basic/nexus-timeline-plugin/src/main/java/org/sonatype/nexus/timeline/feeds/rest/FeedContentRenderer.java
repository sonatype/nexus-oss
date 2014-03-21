package org.sonatype.nexus.timeline.feeds.rest;

import org.sonatype.nexus.timeline.feeds.FeedEvent;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Feed content renderer renders the feed entry content and title. Usually it performs some templating, but
 * does not have to.
 *
 * @since 3.0
 */
public interface FeedContentRenderer
{
  /**
   * Used to set content type (mime) that this entry will be rendered. See {@link SyndContent#setType(String)}.
   */
  String getContentType(FeedEvent evt);

  /**
   * Returns the title of entry. See {@link SyndEntry#setTitle(String)}.
   */
  String getTitle(FeedEvent evt);

  /**
   * Returns the content of the entry. See {@link SyndContent#setValue(String)}.
   */
  String getContent(FeedEvent evt);
}
