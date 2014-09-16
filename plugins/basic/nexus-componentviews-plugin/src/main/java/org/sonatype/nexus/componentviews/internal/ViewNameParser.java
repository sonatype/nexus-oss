/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.componentviews.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * A utility class for parsing the view name out of a request URI.
 *
 * @since 3.0
 */
public class ViewNameParser
{
  private static final Pattern PATTERN = Pattern.compile("/?([^/]*)(.*)");

  private final String viewName;

  private final String remainingPath;

  /**
   * Parses a view name (the first path segment) out of a path.
   */
  public ViewNameParser(final String path) {
    final Matcher matcher = PATTERN.matcher(nullToEmpty(path));

    checkArgument(matcher.matches(), "View name can't be parsed from this path.");

    viewName = matcher.group(1);
    remainingPath = matcher.group(2);
  }

  public String getViewName() {
    return viewName;
  }

  public String getRemainingPath() {
    return remainingPath;
  }
}
