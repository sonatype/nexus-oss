package org.sonatype.nexus.timeline.feeds.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.rest.FeedContentRenderer;

/**
 * A {@link FeedContentRenderer} that renders feed content as {@link "text/plain"}.
 *
 * @since 3.0
 */
@Singleton
@Named("text/plain")
public class PlainTextFeedContentRenderer
    implements FeedContentRenderer
{
  @Override
  public String getContentType(final FeedEvent evt) {
    return "text/plain";
  }

  @Override
  public String getTitle(final FeedEvent evt) {
    // allow override, like when event is not coming from timeline
    // but is parsed from Nexus Log, see ErrorWarningFeedSource
    if (evt.getData().containsKey("_title")) {
      return evt.getData().get("_title");
    }
    // TODO: implement this
    return evt.getEventClass();
  }

  @Override
  public String getContent(FeedEvent evt) {
    // allow override, like when event is not coming from timeline
    // but is parsed from Nexus Log, see ErrorWarningFeedSource
    if (evt.getData().containsKey("_content")) {
      return evt.getData().get("_content");
    }
    // TODO: implement this
    // get the template by
    evt.getEventClass();
    // render template with
    return evt.getData().toString();
  }
}
