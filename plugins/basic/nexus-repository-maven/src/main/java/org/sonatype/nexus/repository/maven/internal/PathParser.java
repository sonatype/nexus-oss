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
package org.sonatype.nexus.repository.maven.internal;

import javax.annotation.Nonnull;

/**
 * Component responsible for parsing artifact paths into map of coordinates and other way around.
 *
 * @since 3.0
 */
public interface PathParser
{
  /**
   * Parses Maven path into {@link Coordinates}.
   */
  @Nonnull
  Coordinates parsePath(String path);

  /**
   * Returns {@code true} if version string represents a Maven SNAPSHOT version.
   */
  boolean isSnapshotVersion(String version);
}
