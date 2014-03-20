/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.google.common.base.Throwables;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import org.restlet.data.MediaType;
import org.restlet.resource.StreamRepresentation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A restlet.org representation that represents a feed.
 *
 * @since 3.0 (relocated from nexus-timeline-plugin)
 */
public class FeedRepresentation
    extends StreamRepresentation
{
  private final SyndFeed feed;

  public FeedRepresentation(MediaType mediaType, SyndFeed feed) {
    super(mediaType);
    this.feed = checkNotNull(feed);
  }

  @Override
  public InputStream getStream()
      throws IOException
  {
    return null;
  }

  @Override
  public void write(final OutputStream outputStream)
      throws IOException
  {
    try {
      final Writer w = new OutputStreamWriter(outputStream);
      final SyndFeedOutput output = new SyndFeedOutput();
      output.output(feed, w);
    }
    catch (FeedException e) {
      throw Throwables.propagate(e);
    }
  }
}
