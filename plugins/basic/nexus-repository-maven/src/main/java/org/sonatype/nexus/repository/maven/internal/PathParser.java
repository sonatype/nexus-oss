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

import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.storage.StorageFacet;

/**
 * Component responsible for parsing artifact paths into map of coordinates and other way around.
 *
 * @since 3.0
 */
public interface PathParser
{
  String GROUP_ID = StorageFacet.P_GROUP;

  String ARTIFACT_ID = StorageFacet.P_NAME;

  String VERSION = StorageFacet.P_VERSION;

  String BASE_VERSION = "baseVersion";

  String CLASSIFIER = "classifier";

  String EXTENSION = "extension";

  /**
   * Parses Maven artifact path into coordinate elements. Returns {@code null} if path cannot be parsed, ie. does not
   * conform to the layout, or, represents a path that cannot be described with coordinates.
   */
  @Nullable
  public Map<String, String> parsePath(String path);

  /**
   * Assembles Maven artifact path from coordinates supplied.
   */
  public String assemblePath(Map<String, String> coordinates);
}
