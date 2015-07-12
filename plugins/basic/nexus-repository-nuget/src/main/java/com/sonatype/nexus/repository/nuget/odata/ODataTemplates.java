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
import java.util.Date;
import java.util.Map;

import com.google.common.io.Resources;

/**
 * Provides templates for various NuGet/OData service responses.
 */
public final class ODataTemplates
{
  // ----------------------------------------------------------------------

  public static final String NUGET_ROOT = load("root.nuget");

  public static final String NUGET_META = load("meta.nuget");

  public static final String NUGET_FEED = load("feed.nuget");

  public static final String NUGET_ENTRY = load("entry.nuget");

  public static final String NUGET_ERROR = load("error.nuget");

  public static final String NUGET_INLINECOUNT = load("inlinecount.nuget");

  // ----------------------------------------------------------------------

  private static final String NULL_PROPERTY = " m:null=\"true\">";

  // ----------------------------------------------------------------------

  /**
   * Interpolates the given template using data in key-value form.
   *
   * @param template The variable template
   * @param data     The data in key-value form
   * @return Interpolated template
   */
  public static String interpolate(final String template, final Map<String, ?> data) {
    final StringBuilder xml = new StringBuilder(template);
    for (int i, j = 0; (i = xml.indexOf("${", j)) > 0 && i < (j = xml.indexOf("}", i)); ) {
      final String key = xml.substring(i + 2, j++);
      final Object value = data.get(key);
      if (null != value) {
        final String text;
        if (value instanceof Date) {
          text = ODataFeedUtils.datetime(((Date) value).getTime());
        }
        else {
          text = value.toString();
        }
        xml.replace(i, j, text);
        j = i + text.length();
      }
      else if (xml.charAt(--i) == '>') {
        xml.replace(i, j, NULL_PROPERTY);
        j = i + NULL_PROPERTY.length();
      }
    }
    return xml.toString();
  }

  // ----------------------------------------------------------------------

  private static String load(final String name) {
    try {
      return new String(Resources.toByteArray(ODataTemplates.class.getResource(name)), "UTF-8");
    }
    catch (final IOException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  // ----------------------------------------------------------------------
}
