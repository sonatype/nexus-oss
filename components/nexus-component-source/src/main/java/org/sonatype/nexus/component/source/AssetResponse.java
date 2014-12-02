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
package org.sonatype.nexus.component.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

/**
 * Asset content returned by a component source.
 *
 * @since 3.0
 */
public interface AssetResponse
{
  /**
   * @return length of this asset in bytes, {@code -1} if unknown
   */
  long getContentLength();

  /**
   * @return content type of this asset, {@code null} if unknown
   */
  @Nullable
  String getContentType();

  /**
   * @return when this asset's content was last modified, {@code null} if unknown
   */
  @Nullable
  DateTime getLastModified();

  /**
   * @return whatever metadata is available, depending on the format.
   */
  Map<String, Object> getMetadata();

  /**
   * @return input stream for reading the content of this asset.
   */
  InputStream openStream() throws IOException;
}
