/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal.odata;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.OrderByExpression;

import static org.odata4j.expression.Expression.asFilterString;
import static org.odata4j.expression.Expression.literal;
import static org.odata4j.producer.resources.OptionsQueryParser.parseOrderBy;

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
   * @param data  Current data position
   * @param query Components of the query string
   * @return Encoded skip link
   */
  public static String skipLink(final Map<String, ?> data, final Map<String, String> query) {
    final StringBuilder link = new StringBuilder();
    final String searchterm = query.get("searchterm");
    if (null != searchterm) {
      link.append("searchterm=").append(encode(searchterm)).append("&amp;");
    }
    final String id = query.get("id");
    if (null != id) {
      link.append("id=").append(encode(id)).append("&amp;");
    }
    final String filter = query.get("$filter");
    if (null != filter) {
      link.append("$filter=").append(encode(filter)).append("&amp;");
    }
    final String orderby = query.get("$orderby");
    if (null != orderby) {
      link.append("$orderby=").append(encode(orderby)).append("&amp;");
    }
    final String top = query.get("$top");
    if (null != top) {
      link.append("$top=").append(Integer.parseInt(top) - ODataUtils.PAGE_SIZE).append("&amp;");
    }
    final String skipToken = skipToken(orderby, data);
    link.append("$skiptoken=").append(encode(skipToken));
    return link.toString();
  }

  /**
   * Constructs an OData skip token for the given data position.
   *
   * @param orderBy $orderBy parameter
   * @param data    Current data position
   * @return Raw skip token
   */
  public static String skipToken(final String orderBy, final Map<String, ?> data) {
    final StringBuilder token = new StringBuilder();
    if (null != orderBy) {
      for (final OrderByExpression o : parseOrderBy(orderBy)) {
        final String name = ((EntitySimpleProperty) o.getExpression()).getPropertyName();
        final Object value = data.get(name.toUpperCase(Locale.ENGLISH));
        token.append(asFilterString(literal(value))).append(',');
      }
    }
    token.append(asFilterString(literal(data.get("ID")))).append(',');
    token.append(asFilterString(literal(data.get("VERSION"))));
    return token.toString();
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
      return raw; // this should never happen
    }
  }

  public static String root(final String base) {
    final Map<String, String> data = ImmutableMap.of("BASEURI", base);
    return ODataTemplates.interpolate(ODataTemplates.NUGET_ROOT, data);
  }

  // TODO: Move over to ODataFeedUtils
  public static String metadata() {
    return ODataTemplates.NUGET_META;
  }

  // TODO: Move over to ODataFeedUtils
  public static String error(final int code, final String message) {
    final Map<String, String> data = ImmutableMap.of("CODE", Integer.toString(code), "MESSAGE", message);
    return ODataTemplates.interpolate(ODataTemplates.NUGET_ERROR, data);
  }

  // TODO: Move over to ODataFeedUtils
  public static String datetime(final long millis) {
    return ISO_PRINTER.print(millis);
  }
}
