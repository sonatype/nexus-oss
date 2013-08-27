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

package org.sonatype.nexus.test.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import org.sonatype.nexus.integrationtests.RequestFacade;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.junit.Assert;
import org.restlet.data.Method;
import org.restlet.data.Response;

public class FeedUtil
{
  private static final String FEED_URL_PART = "service/local/feeds/";

  public static SyndFeed getFeed(String feedId)
      throws IllegalArgumentException, MalformedURLException, FeedException, IOException
  {
    return getFeed(feedId, null, null, null);
  }

  public static SyndFeed getFeed(String feedId, int from, int count)
      throws IllegalArgumentException, MalformedURLException, FeedException, IOException
  {
    return getFeed(feedId, from, count, null);
  }

  public static SyndFeed getFeed(final String feedId, final Integer from, final Integer count,
                                 final Map<String, String> params)
      throws IllegalArgumentException, MalformedURLException, FeedException, IOException
  {
    final StringBuilder sb = new StringBuilder();
    sb.append("?_dc=" + System.currentTimeMillis());
    if (from != null) {
      sb.append("&from=" + from);
    }
    if (count != null) {
      sb.append("&count=" + count);
    }
    if (params != null && !params.isEmpty()) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        sb.append("&" + entry.getKey());
        if (entry.getValue() != null) {
          sb.append("=" + entry.getValue());
        }
      }
    }
    final Response response =
        RequestFacade.sendMessage(FEED_URL_PART + feedId + sb.toString(), Method.GET);
    final String text = response.getEntity().getText();
    Assert.assertTrue("Unexpected response: " + text, response.getStatus().isSuccess());

    return new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(text.getBytes())));
  }

  @SuppressWarnings("unchecked")
  public static void sortSyndEntryOrderByPublishedDate(final SyndFeed feed) {
    Collections.sort(feed.getEntries(), new Comparator<SyndEntry>()
    {
      public int compare(SyndEntry o1, SyndEntry o2) {
        Date d1 = (o1).getPublishedDate();
        Date d2 = (o2).getPublishedDate();
        // sort desc by date
        if (d2 != null && d1 != null) {
          return d2.compareTo(d1);
        }
        return -1;
      }
    });
  }
}
