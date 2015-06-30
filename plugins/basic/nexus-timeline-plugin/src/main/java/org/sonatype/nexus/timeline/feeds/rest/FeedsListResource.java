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
package org.sonatype.nexus.timeline.feeds.rest;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.timeline.feeds.FeedSource;
import org.sonatype.nexus.timeline.feeds.rest.model.FeedEntriesXO;
import org.sonatype.nexus.timeline.feeds.rest.model.FeedEntryXO;
import org.sonatype.siesta.Resource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A resource that lists existing feeds, implemented by {@link FeedSource} components. This is a REST resource, meaning
 * it produces "usual" JSON or XML response, but is a read only resource.
 *
 * @since 3.0
 */
@Path("/timeline/feeds")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Named
@Singleton
public class FeedsListResource
    extends ComponentSupport
    implements Resource
{
  private final List<FeedSource> feedSources;

  @Inject
  public FeedsListResource(final List<FeedSource> feedSources) {
    this.feedSources = checkNotNull(feedSources);
  }

  /**
   * Lists all the feeds existing.
   */
  @GET
  @RequiresPermissions("nexus:feeds:read")
  public FeedEntriesXO get() {
    final FeedEntriesXO result = new FeedEntriesXO();
    for (FeedSource feedSource : feedSources) {
      final FeedEntryXO entry = new FeedEntryXO();
      entry.setResourceURI(BaseUrlHolder.get() + "/service/siesta/timeline/feeds/" + feedSource.getFeedKey());
      entry.setName(feedSource.getFeedName());
      entry.setDescription(feedSource.getFeedDescription());
      result.getFeedEntries().add(entry);
    }
    return result;
  }
}
