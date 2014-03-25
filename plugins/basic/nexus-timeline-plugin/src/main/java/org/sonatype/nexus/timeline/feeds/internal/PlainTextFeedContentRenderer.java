/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.timeline.feeds.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
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
    if (FeedRecorder.FAMILY_SYSTEM.equals(evt.getEventType())) {
      if (FeedRecorder.SYSTEM_BOOT.equals(evt.getEventSubType())) {
        return "Nexus " + evt.getData().get("boot.action");
      }
      else if (FeedRecorder.SYSTEM_CONFIG.equals(evt.getEventSubType())) {
        return "Configuration change";
      }
    }

    if (FeedRecorder.FAMILY_AUTH.equals(evt.getEventType())) {
      if (FeedRecorder.AUTH_AUTHC.equals(evt.getEventSubType())) {
        return "Authentication";
      }
      else if (FeedRecorder.AUTH_AUTHZ.equals(evt.getEventSubType())) {
        return "Authorization";
      }
    }

    if (FeedRecorder.FAMILY_REPO.equals(evt.getEventType())) {
      if (FeedRecorder.REPO_CREATED.equals(evt.getEventSubType())) {
        return "Repository created";
      }
      else if (FeedRecorder.REPO_UPDATED.equals(evt.getEventSubType())) {
        return "Repository updated";
      }
      else if (FeedRecorder.REPO_DROPPED.equals(evt.getEventSubType())) {
        return "Repository dropped";
      }
      else if (FeedRecorder.REPO_LSTATUS.equals(evt.getEventSubType())) {
        return "Repository service state change";
      }
      else if (FeedRecorder.REPO_PSTATUS.equals(evt.getEventSubType())) {
        return "Repository proxy state change";
      }
    }

    if (FeedRecorder.FAMILY_ITEM.equals(evt.getEventType())) {
      if (FeedRecorder.ITEM_RETRIEVED.equals(evt.getEventSubType())) {
        return "Item retrieved";
      }
      else if (FeedRecorder.ITEM_CACHED.equals(evt.getEventSubType())) {
        return "Item cached";
      }
      else if (FeedRecorder.ITEM_CACHED_UPDATE.equals(evt.getEventSubType())) {
        return "Cached Item updated";
      }
      else if (FeedRecorder.ITEM_DEPLOYED.equals(evt.getEventSubType())) {
        return "Item deployed";
      }
      else if (FeedRecorder.ITEM_DEPLOYED_UPDATE.equals(evt.getEventSubType())) {
        return "Item updated";
      }
      else if (FeedRecorder.ITEM_DELETED.equals(evt.getEventSubType())) {
        return "Item deleted";
      }
      else if (FeedRecorder.ITEM_BROKEN.equals(evt.getEventSubType())) {
        return "Validation failure";
      }
      else if (FeedRecorder.ITEM_BROKEN_INVALID_CONTENT.equals(evt.getEventSubType())) {
        return "Validation failure: Invalid content";
      }
      else if (FeedRecorder.ITEM_BROKEN_WRONG_REMOTE_CHECKSUM.equals(evt.getEventSubType())) {
        return "Validation failure: invalid checksum";
      }
    }

    if (FeedRecorder.FAMILY_TASK.equals(evt.getEventType())) {
      if (FeedRecorder.TASK_STARTED.equals(evt.getEventSubType())) {
        return "Started: " + evt.getData().get("task.name");
      }
      else if (FeedRecorder.TASK_FINISHED.equals(evt.getEventSubType())) {
        return "Finished: " + evt.getData().get("task.name");
      }
      else if (FeedRecorder.TASK_CANCELED.equals(evt.getEventSubType())) {
        return "Canceled: " + evt.getData().get("task.name");
      }
      else if (FeedRecorder.TASK_FAILED.equals(evt.getEventSubType())) {
        return "Failed: " + evt.getData().get("task.name");
      }
    }

    // TODO: Some human-readable fallback?
    return evt.getEventType() + ":" + evt.getEventSubType() + ":" + evt.getEventClass();
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
