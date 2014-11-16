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

import com.google.common.base.Supplier;
import org.joda.time.DateTime;

/**
 * A file within a {@link Component}.
 * 
 * @since 3.0
 */
public interface Asset
    extends Entity
{
  /**
   * Gets the id of the {@link Component} to which this asset belongs, or {@code null} if it hasn't been stored yet.
   */
  @Nullable
  EntityId getComponentId();

  /**
   * @see #getComponentId()
   */
  void setComponentId(EntityId entityId);

  /**
   * @return length of this asset in bytes, {@code -1} if unknown
   */
  long getContentLength();

  /**
   * @see #getContentLength()
   */
  void setContentLength(long contentLength);

  /**
   * @return content type of this asset, {@code null} if unknown
   */
  @Nullable
  String getContentType();

  /**
   * @see #getContentType()
   */
  void setContentType(String contentType);

  /**
   * Gets the path of this asset within its component. This is an optional property and is used to
   * disambiguate the asset from others within the same component, for view purposes.
   */
  @Nullable
  String getPath();

  /**
   * @see #getPath()
   */
  void setPath(String path);

  /**
   * @return when this asset was first created, {@code null} if unknown
   */
  @Nullable
  DateTime getFirstCreated();

  /**
   * @see #getFirstCreated()
   */
  void setFirstCreated(DateTime firstCreated);

  /**
   * @return when this asset's content was last modified, {@code null} if unknown
   */
  @Nullable
  DateTime getLastModified();

  /**
   * @see #getLastModified()
   */
  void setLastModified(DateTime lastModified);

  /**
   * @return input stream for reading the content of this asset, {@code null} if there is no stream supplier.
   */
  @Nullable
  InputStream openStream() throws IOException;

  /**
   * @see #openStream()
   */
  void setStreamSupplier(Supplier<InputStream> streamSupplier);
}
