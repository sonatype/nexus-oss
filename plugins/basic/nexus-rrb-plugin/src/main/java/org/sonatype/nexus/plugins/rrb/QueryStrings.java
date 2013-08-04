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

package org.sonatype.nexus.plugins.rrb;

import java.util.Map;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

// Duplicated from goodies-servlet to hack in fix for NXCM-4737

public class QueryStrings
{
  public static final String FIELD_SEPARATOR = "&";

  public static final String VALUE_SEPARATOR = "=";

  // FIXME: Probably should be a Multimap

  /**
   * Parses a query-string into a map.
   *
   * @param input Query-string input ot parse; never null
   * @return Ordered map of parsed query string parameters.
   */
  public static Map<String, String> parse(final String input) {
    checkNotNull(input);
    Map<String, String> result = Maps.newLinkedHashMap();
    String[] fields = input.split(FIELD_SEPARATOR);
    for (String field : fields) {
      String key, value;
      int i = field.indexOf(VALUE_SEPARATOR);
      if (i == -1) {
        key = field;
        value = null;
      }
      else {
        key = field.substring(0, i);
        value = field.substring(i + 1, field.length());
      }
      result.put(key, value);
    }
    return result;
  }
}