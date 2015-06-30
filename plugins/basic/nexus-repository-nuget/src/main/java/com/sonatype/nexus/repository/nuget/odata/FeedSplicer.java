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
package com.sonatype.nexus.repository.nuget.odata;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts NuGet package metadata from a remote gallery stream.
 */
public final class FeedSplicer
    extends XmlSplicer
{
  // ----------------------------------------------------------------------

  private Map<String, String> data = new HashMap<String, String>();

  private final ODataConsumer consumer;

  private boolean isNull;

  private String next;

  private Integer count;

  private static final Logger log = LoggerFactory.getLogger(FeedSplicer.class);

  // ----------------------------------------------------------------------

  public FeedSplicer(final ODataConsumer consumer) {
    super(new StringBuilder());
    this.consumer = consumer;
  }

  // ----------------------------------------------------------------------

  @Nullable
  public String consumePage(final InputStream is)
      throws XmlPullParserException, IOException
  {
    next = null;
    count = null;
    consume(ReaderFactory.newXmlReader(is));
    return next;
  }

  // ----------------------------------------------------------------------

  public Map<String, String> consumeEntry(final InputStream is)
      throws XmlPullParserException, IOException
  {
    consume(ReaderFactory.newXmlReader(is));
    return data;
  }

  @Nullable
  public Integer getCount() {
    return count;

  }
  // ----------------------------------------------------------------------

  @Override
  void started(final String name, final int len, final boolean isRoot)
      throws XmlPullParserException
  {
    // OData Packages() calls return XML that has an "entry" root element, not a "feed" root element.
    if (isRoot && !"feed".equals(name) && !"entry".equals(name)) {
      throw new XmlPullParserException(
          "Parsed xml has an unexpected start tag: '" + name + "' (expected 'feed' or 'entry')"
      );
    }
    if ("entry".equals(name)) {
      data = new HashMap<>();
    }
    else if ("content".equals(name)) {
      data.put("LOCATION", getAttribute("src"));
    }
    else if ("link".equals(name) && "next".equals(getAttribute("rel"))) {
      next = getAttribute("href");
    }
    else {
      isNull = Boolean.parseBoolean(getAttribute("m:null"));
    }
    xml.setLength(0);
  }

  // ----------------------------------------------------------------------

  @Override
  void ended(final String name, final int len) {
    if ("entry".equals(name)) {
      if (null != consumer) {
        consumer.consume(sanitize(data));
      }
    }
    else if ("m:count".equals(name)) {
      final String strCount = xml.substring(0, xml.length() - len);
      try {
        count = Integer.parseInt(strCount);
      }
      catch (NumberFormatException e) {
        log.warn("Error parsing a nuget feed <m:count> with value {}", strCount);
      }
    }
    else if (!isNull) {
      String key = name.toUpperCase(Locale.ENGLISH);
      if (key.startsWith("D:")) {
        key = key.substring(2);
      }
      else if ("ID".equals(key)) {
        key = "ATOM_ID";
      }
      else if ("TITLE".equals(key)) {
        key = "ID";
      }
      else if ("NAME".equals(key)) {
        key = "AUTHORS";
      }
      else if ("UPDATED".equals(key)) {
        key = "LASTUPDATED";
      }

      data.put(key, xml.substring(0, xml.length() - len));
      isNull = true; // skip over any nested end-tags
    }
  }

  // ----------------------------------------------------------------------

  private static Map<String, String> sanitize(final Map<String, String> data) {
        /*
         * These properties are mandatory, but some servers have been known to omit them
         */
    if (!data.containsKey("CREATED")) {
      data.put("CREATED", data.get("LASTUPDATED"));
    }
    if (!data.containsKey("PUBLISHED")) {
      data.put("PUBLISHED", data.get("CREATED"));
    }
    if (!data.containsKey("DOWNLOADCOUNT")) {
      data.put("DOWNLOADCOUNT", "0");
    }
    if (!data.containsKey("VERSIONDOWNLOADCOUNT")) {
      data.put("VERSIONDOWNLOADCOUNT", data.get("DOWNLOADCOUNT"));
    }
    return data;
  }

  // ----------------------------------------------------------------------
}
