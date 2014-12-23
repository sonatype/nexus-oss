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
package org.sonatype.nexus.repository.httpbridge.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * A utility class for parsing the repository name and remaining path out of a request URI.
 *
 * @since 3.0
 */
public class RepositoryPath
{
  private static final Pattern PATTERN = Pattern.compile("/?([^/]*)(.*)");

  private final String repositoryName;

  private final String remainingPath;

  private RepositoryPath(final String repositoryName, final String remainingPath) {
    this.repositoryName = repositoryName;
    this.remainingPath = remainingPath;
  }

  /**
   * @return The parsed path or {@code null}
   */
  @Nullable
  public static RepositoryPath parse(String path) {
    final Matcher matcher = PATTERN.matcher(nullToEmpty(path));

    if (!matcher.matches()) {
      return null;
    }

    return new RepositoryPath(matcher.group(1), matcher.group(2));
  }

  public String getRepositoryName() {
    return repositoryName;
  }

  public String getRemainingPath() {
    return remainingPath;
  }
}
