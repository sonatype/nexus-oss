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

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import com.sonatype.nexus.repository.nuget.internal.ComponentQuery;
import com.google.common.collect.Maps;

import org.codehaus.plexus.util.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.expression.OrderByExpression.Direction;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.jpa.JPASkipToken;
import org.odata4j.producer.jpa.JPQLGenerator;

import static com.google.common.base.Strings.nullToEmpty;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.*;
import static org.odata4j.producer.resources.OptionsQueryParser.parseFilter;
import static org.odata4j.producer.resources.OptionsQueryParser.parseOrderBy;
import static org.odata4j.producer.resources.OptionsQueryParser.parseSkip;
import static org.odata4j.producer.resources.OptionsQueryParser.parseSkipToken;
import static org.odata4j.producer.resources.OptionsQueryParser.parseTop;

/**
 * Utility methods for working with OData/SQL expressions.
 */
public final class ODataUtils
{
  public static final Map<String, String> COLUMN_ALIASES = columnAliases();

  // ----------------------------------------------------------------------

  public static final int PAGE_SIZE = 40;

  // ----------------------------------------------------------------------

  private static final DateTimeFormatter ISO_PARSER =
      ISODateTimeFormat.dateTimeParser().withLocale(Locale.ENGLISH).withZoneUTC();

  // ----------------------------------------------------------------------

  public static long datetime(final String millis) {
    return ISO_PARSER.parseMillis(millis);
  }

  public static Date toDate(final String isoFormatDate) {
    return new Date(datetime(isoFormatDate));
  }

  // ----------------------------------------------------------------------

  /**
   * Converts the given OData query and select clause into an SQL expression.
   *
   * @param originalQuery OData parameters
   * @param count         True if the intention is to merely count the items rather than itemizing them
   */
  public static ComponentQuery query(final Map<String, String> originalQuery, final boolean count) {
    final Map<String, String> query = applyQueryDefaults(originalQuery);

    ComponentQuery.Builder q = new ComponentQuery.Builder();

    // TODO: parameters should be case-insensitive

    boolean hasTerms = false;
    for (String term : StringUtils.strip(nullToEmpty(query.get("searchTerm")), "\" '").split("[+\\s]+")) {
      if (StringUtils.isNotBlank(term)) {
        term = '%' + term + '%';
        if (!hasTerms) {
          q.where("(");
          hasTerms = true;
        }
        else {
          q.where(" OR ");
        }
        q.where(nugat(P_KEYWORDS) + " LIKE ").param(term.toLowerCase());
      }
    }
    if (hasTerms) {
      q.where(")");
    }

    final String id = StringUtils.strip(query.get("id"), "\" '");
    if (id != null) {
      if (q.hasWhere()) {
        q.where(" AND ");
      }
      q.where(jsonAttrib(ID) + " = ").param(id);
    }

    if ("false".equalsIgnoreCase(StringUtils.strip(query.get("includePrerelease"), "\" '"))) {
      if (q.hasWhere()) {
        q.where(" AND ");
      }
      q.where(" " + jsonAttrib(IS_PRERELEASE) + "=false ");
    }

    final QueryInfo odata;
    try {
      odata =
          new QueryInfo(null, parseTop(query.get("$top")),
              parseSkip(query.get("$skip")),
              parseFilter(query.get("$filter")),
              parseOrderBy(query.get("$orderby")),
              parseSkipToken(query.get("$skiptoken")),
              // unused arguments
              null, null, null);
    }
    catch (final RuntimeException e) {
      throw new IllegalArgumentException("Bad Request - Error in query syntax.", e);
    }

    final JPQLGenerator generator = new JPQLGenerator(null, null, COLUMN_ALIASES);
    if (odata.filter != null) {
      if (q.hasWhere()) {
        q.where(" AND ");
      }
      q.where("(").where(generator.toJpql(odata.filter)).where(")");
    }
    if (odata.skipToken != null) {
      if (q.hasWhere()) {
        q.where(" AND ");
      }
      q.where(" (").where(generator.toJpql(JPASkipToken.parse(null, odata.orderBy, odata.skipToken,
          "id", "version"))).where(")");
    }

    if (!count) {
      q.suffix(" ORDER BY ");
      if (null != odata.orderBy) {
        for (int i = 0, size = odata.orderBy.size(); i < size; i++) {
          final OrderByExpression o = odata.orderBy.get(i);
          q.suffix(generator.toJpql(o.getExpression()));
          // Orientdb doesn't support an implied sort direction
          if (o.getDirection() == Direction.ASCENDING) {
            q.suffix(" ASC");
          }
          else if (o.getDirection() == Direction.DESCENDING) {
            q.suffix(" DESC");
          }
          q.suffix(", ");
        }
      }
      // Tack on the default ordering
      q.suffix("id asc, version asc");

      int top = PAGE_SIZE;
      if (odata.top != null && odata.top.intValue() < top) {
        top = odata.top.intValue();
      }
      q.suffix(" LIMIT " + top);
      if (odata.skip != null) {
        q.suffix(" OFFSET " + odata.skip);
      }
    }
    return q.build();
  }

  @Nonnull
  private static Map<String, String> applyQueryDefaults(final Map<String, String> originalQuery) {
    final Map<String, String> query = new HashMap<>(originalQuery);

    // NEXUS-6822 Visual Studio doesn't send a sort order by default, leading to unusable results
    if (!query.containsKey("$orderby")) {
      query.put("$orderby", DOWNLOAD_COUNT + " desc");
    }
    else {
      // OrientDB only supports ordering by identifiers, not by functions
      final String orderby = query.get("$orderby");
      query.put("$orderby", orderby.replaceAll("(?i)concat\\(title,id\\)", NAME_ORDER));
    }

    if (query.containsKey("$filter")) {
      final String filter = query.get("$filter");
      query.put("$filter", filter.replaceAll("(?i)(IsLatestVersion|IsAbsoluteLatestVersion)", "($1 eq true)"));
    }
    return query;
  }

  private static Map<String, String> columnAliases() {
    Map<String, String> aliases = Maps.newHashMap();

    aliases.put(CREATED, nugat(P_CREATED));
    aliases.put(DOWNLOAD_COUNT, nugat(P_DOWNLOAD_COUNT));
    aliases.put(ID, nugat(P_ID));
    aliases.put(IS_ABSOLUTE_LATEST_VERSION, nugat(P_IS_ABSOLUTE_LATEST_VERSION));
    aliases.put(IS_LATEST_VERSION, nugat(P_IS_LATEST_VERSION));
    aliases.put(IS_PRERELEASE, nugat(P_IS_PRERELEASE));
    aliases.put(LAST_UPDATED, nugat(P_LAST_UPDATED));
    aliases.put(NAME_ORDER, nugat(P_NAME_ORDER));
    aliases.put(PACKAGE_HASH, nugat(P_PACKAGE_HASH));
    aliases.put(PACKAGE_HASH_ALGORITHM, nugat(P_PACKAGE_HASH_ALGORITHM));
    aliases.put(PACKAGE_SIZE, nugat(P_PACKAGE_SIZE));
    aliases.put(PUBLISHED, nugat(P_PUBLISHED));
    aliases.put(REQUIRE_LICENSE_ACCEPTANCE, nugat(P_REQUIRE_LICENSE_ACCEPTANCE));
    aliases.put(SUMMARY, nugat(P_SUMMARY));
    aliases.put(TITLE, nugat(P_TITLE));
    aliases.put(VERSION, nugat(P_VERSION));
    aliases.put(VERSION_DOWNLOAD_COUNT, nugat(P_VERSION_DOWNLOAD_COUNT));

    return aliases;
  }

  /**
   * Converts an ODATA element name into the name of the json attribute we store it under in orient.
   */
  private static String jsonAttrib(String elementName) {
    return COLUMN_ALIASES.get(elementName);
  }

  /**
   * Fully qualifies a nuget attribute reference.
   */
  private static String nugat(final String column) {
    return "attributes.nuget." + column;
  }
}
