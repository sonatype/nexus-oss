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
package org.sonatype.nexus.timeline.feeds;

import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A feed event encapsulates one entry out of the feed.
 *
 * @since 3.0
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
                   final Date published,
                   @Nullable final String author,
                   @Nullable final String link,
                   final Map<String, String> data)
  {
    this.eventType = checkNotNull(eventType);
    this.eventSubType = checkNotNull(eventSubType);
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
  
  // ==
  
  /**
   * Returns the templateID needed to render this entry. Never returns {@code null}.
   */
  public String getTemplateId() {
    if (getData().containsKey("_template")) {
      return getData().get("_template");
    } else {
      return getEventType() + "-" + getEventSubType();
    }
  }

  /**
   * Allows to override the needed template for this entry.
   */
  public void setTemplateId(final String templateId) {
    checkNotNull(templateId);
    getData().put("_template", templateId);
  }

  /**
   * Returns the title override for this entry. If {@code null} is returned,
   * the default templating mechanism will be used to render title.
   */
  public String getTitle() {
    return getData().get("_title");
  }
  
  /**
   * Allows to override the needed title for this entry.
   */
  public void setTitle(final String title) {
    checkNotNull(title);
    getData().put("_title", title);
  }
  
  /**
   * Returns the content override for this entry. If {@code null} is returned,
   * the default templating mechanism will be used to render content.
   */
  public String getContent() {
    return getData().get("_content");
  }
  
  /**
   * Allows to override the needed content for this entry.
   */
  public void setContent(final String content) {
    checkNotNull(content);
    getData().put("_content", content);
  }
}
