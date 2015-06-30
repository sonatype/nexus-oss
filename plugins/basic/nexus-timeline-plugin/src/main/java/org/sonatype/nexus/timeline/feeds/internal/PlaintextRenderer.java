/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import java.net.URL;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.timeline.feeds.FeedEvent;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Component renders feed content as {@link "text/plain"}.
 *
 * @since 3.0
 */
@Singleton
@Named
public class PlaintextRenderer
    extends ComponentSupport
{
  private final Provider<TemplateEngine> templateEngineProvider;

  @Inject
  public PlaintextRenderer(@Named("shared-velocity") final Provider<TemplateEngine> templateEngineProvider) {
    this.templateEngineProvider = checkNotNull(templateEngineProvider);
  }

  public String getTitle(final FeedEvent evt) {
    // OOTB provided recorder titles
    if (FeedRecorder.FAMILY_SYSTEM.equals(evt.getEventType())) {
      if (FeedRecorder.SYSTEM_BOOT.equals(evt.getEventSubType())) {
        return "Nexus " + evt.getData().get("bootAction");
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
      final String itemPath = evt.getData().get("itemPath");
      if (FeedRecorder.ITEM_RETRIEVED.equals(evt.getEventSubType())) {
        return "Retrieved: " + itemPath;
      }
      else if (FeedRecorder.ITEM_CACHED.equals(evt.getEventSubType())) {
        return "Cached: " + itemPath;
      }
      else if (FeedRecorder.ITEM_CACHED_UPDATE.equals(evt.getEventSubType())) {
        return "Cached (update): " + itemPath;
      }
      else if (FeedRecorder.ITEM_DEPLOYED.equals(evt.getEventSubType())) {
        return "Deployed: " + itemPath;
      }
      else if (FeedRecorder.ITEM_DEPLOYED_UPDATE.equals(evt.getEventSubType())) {
        return "Deployed (update): " + itemPath;
      }
      else if (FeedRecorder.ITEM_DELETED.equals(evt.getEventSubType())) {
        return "Deleted: " + itemPath;
      }
      else if (FeedRecorder.ITEM_BROKEN.equals(evt.getEventSubType())) {
        return "Validation failure: " + itemPath;
      }
      else if (FeedRecorder.ITEM_BROKEN_INVALID_CONTENT.equals(evt.getEventSubType())) {
        return "Invalid content: " + itemPath;
      }
      else if (FeedRecorder.ITEM_BROKEN_WRONG_REMOTE_CHECKSUM.equals(evt.getEventSubType())) {
        return "Invalid checksum: " + itemPath;
      }
    }

    if (FeedRecorder.FAMILY_TASK.equals(evt.getEventType())) {
      if (FeedRecorder.TASK_STARTED.equals(evt.getEventSubType())) {
        return "Started: " + evt.getData().get("taskName");
      }
      else if (FeedRecorder.TASK_FINISHED.equals(evt.getEventSubType())) {
        return "Finished: " + evt.getData().get("taskName");
      }
      else if (FeedRecorder.TASK_CANCELED.equals(evt.getEventSubType())) {
        return "Canceled: " + evt.getData().get("taskName");
      }
      else if (FeedRecorder.TASK_FAILED.equals(evt.getEventSubType())) {
        return "Failed: " + evt.getData().get("taskName");
      }
    }

    // TODO: Some human-readable fallback?
    return evt.getEventType() + ":" + evt.getEventSubType();
  }

  public String getContent(final FeedEvent evt) {
    final URL templateURL = getTemplateFor(evt);
    if (templateURL != null) {
      final TemplateParameters templateParameters = new TemplateParameters();
      for (Map.Entry<String, String> d : evt.getData().entrySet()) {
        templateParameters.set(d.getKey(), d.getValue());
      }
      return templateEngineProvider.get().render(this, templateURL, templateParameters);
    }
    // TODO: Some human-readable fallback?
    return evt.getData().toString();
  }

  // ==

  private URL getTemplateFor(final FeedEvent evt) {
    return getClass().getResource("plaintext-" + evt.getTemplateId() + ".vm");
  }
}
