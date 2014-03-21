/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package org.sonatype.nexus.timeline.feeds.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Named;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.siesta.common.Component;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * A JAX-RS {@link MessageBodyWriter} that is backed by Rome. It supports {@code application/rss+xml} and {@code
 * application/atom+xml} media types only, and instances of {@link SyndFeed}.
 *
 * @since 3.0
 */
@Named
@Provider
@Produces({RomeProvider.APPLICATION_RSS_XML, MediaType.APPLICATION_ATOM_XML, MediaType.TEXT_XML})
public class RomeProvider
    extends ComponentSupport
    implements Component, MessageBodyWriter<Object>
{
  public static final String APPLICATION_RSS_XML = "application/rss+xml";

  public static final MediaType APPLICATION_RSS_XML_TYPE = MediaType.valueOf(APPLICATION_RSS_XML);

  private static final String RSS_2_0 = "rss_2.0";

  private static final String ATOM_1_0 = "atom_1.0";

  @Override
  public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return hasMatchingMediaType(mediaType) && SyndFeed.class.isAssignableFrom(type);
  }

  @Override
  public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
  {
    try {
      final SyndFeed feed = (SyndFeed) t;
      if (matches(MediaType.APPLICATION_ATOM_XML_TYPE, mediaType)) {
        feed.setFeedType(ATOM_1_0);
      }
      else {
        // we write RSS for both media types
        feed.setFeedType(RSS_2_0);
      }
      final Writer w = new OutputStreamWriter(entityStream);
      final SyndFeedOutput output = new SyndFeedOutput();
      output.output(feed, w);
    }
    catch (FeedException e) {
      log.error("Problem during write", e);
      throw new IOException(e);
    }
  }

  private boolean hasMatchingMediaType(final MediaType mediaType) {
    return matches(APPLICATION_RSS_XML_TYPE, mediaType)
        || matches(MediaType.APPLICATION_ATOM_XML_TYPE, mediaType)
        || matches(MediaType.TEXT_XML_TYPE, mediaType);
  }

  private boolean matches(final MediaType m1, final MediaType m2) {
    return m1.getType().equalsIgnoreCase(m2.getType()) && m1.getSubtype().equalsIgnoreCase(m2.getSubtype());
  }
}
