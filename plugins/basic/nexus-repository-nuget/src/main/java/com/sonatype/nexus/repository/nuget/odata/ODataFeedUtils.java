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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Helper methods for constructing NuGet XML responses.
 *
 * @since 3.0
 */
public class ODataFeedUtils
{
  private static final DateTimeFormatter ISO_PRINTER =
      ISODateTimeFormat.dateTime().withLocale(Locale.ENGLISH).withZoneUTC();

  /**
   * Constructs an OData skip link for the given data position.
   *
   * @param query Components of the query string
   * @return Encoded skip link
   */
  public static String skipLinkQueryString(final Map<String, String> query) {
    Map<String, String> nextPageQuery = queryForNextPage(query);
    return toQueryString(nextPageQuery);

  }

  @Nonnull
  private static String toQueryString(final Map<String, String> nextPageQuery) {
    final StringBuilder link = new StringBuilder();

    for (Entry<String, String> entry : nextPageQuery.entrySet()) {
      if (link.length() > 0) {
        link.append("&");
      }
      link.append(entry.getKey()).append("=").append(encode(entry.getValue()));
    }

    return link.toString();
  }

  @Nonnull
  private static Map<String, String> queryForNextPage(final Map<String, String> query) {
    Map<String, String> nextPageQuery = new HashMap<>(query);

    if (query.containsKey("$top")) {
      // Since the original query asked for the top 'x' entries, subtract a page's worth from that
      final int top = Integer.parseInt(query.get("$top"));
      nextPageQuery.put("$top", Integer.toString(Math.max(1, top - ODataUtils.PAGE_SIZE)));
    }

    int currentSkip;
    if (query.containsKey("$skip")) {
      currentSkip = Integer.parseInt(query.get("$skip"));
    }
    else {
      currentSkip = 0;
    }
    nextPageQuery.put("$skip", Integer.toString(currentSkip + ODataUtils.PAGE_SIZE));
    return nextPageQuery;
  }

  /**
   * Encodes the given raw text using the UTF-8 URL encoding scheme.
   *
   * @param raw text
   * @return encoded text
   */
  private static String encode(final String raw) {
    try {
      return URLEncoder.encode(raw, "UTF-8");
    }
    catch (final UnsupportedEncodingException e) {
      throw Throwables.propagate(e); // This should never happen
    }
  }

  public static String root(final String base) {
    final Map<String, String> data = ImmutableMap.of("BASEURI", base);
    return ODataTemplates.interpolate(ODataTemplates.NUGET_ROOT, data);
  }

  public static String metadata() {
    return ODataTemplates.NUGET_META;
  }

  public static String error(final int code, final String message) {
    final Map<String, String> data = ImmutableMap.of("CODE", Integer.toString(code), "MESSAGE", message);
    return ODataTemplates.interpolate(ODataTemplates.NUGET_ERROR, data);
  }

  public static String datetime(final long millis) {
    return ISO_PRINTER.print(millis);
  }
}
