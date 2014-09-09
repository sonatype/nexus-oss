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
package org.sonatype.nexus.component.model;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import org.sonatype.nexus.componentmetadata.Record;

import org.joda.time.DateTime;

/**
 * Represents an identifiable asset of a {@link Component}.
 * 
 * @since 3.0
 */
public interface Asset
{
  /**
   * @return relative path to this asset in the containing component
   */
  String getPath();

  /**
   * @return current metadata record for this asset
   */
  Record getMetadata();

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
   * @return when this asset was first created, {@code null} if unknown
   */
  @Nullable
  DateTime getFirstCreated();

  /**
   * @return when this asset was last modified, {@code null} if unknown
   */
  @Nullable
  DateTime getLastModified();

  /**
   * @return input stream for reading the content of this asset
   */
  InputStream openStream() throws IOException;
}
