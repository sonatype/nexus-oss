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
package org.sonatype.nexus.repository.storage;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.BlobRef;

/**
 * Metadata about a file, which may or may not belong to a component.
 *
 * @since 3.0
 */
public interface Asset
    extends MetadataNode
{
  /**
   * Gets the component this asset is part of, or {@code null} if it's standalone.
   */
  @Nullable
  Component component();

  /**
   * Gets the size of the file in bytes or {@code null} if undefined.
   */
  @Nullable
  Long size();

  /**
   * Gets the size of the file in bytes or throws a runtime exception if undefined.
   */
  Long requireSize();

  /**
   * Sets the size to the given value, or {@code null} to un-define it.
   */
  Asset size(@Nullable Long size);

  /**
   * Gets the content type or {@code null} if undefined.
   */
  @Nullable
  String contentType();

  /**
   * Gets the content type or throws a runtime exception if undefined.
   */
  String requireContentType();

  /**
   * Sets the content type to the given value, or {@code null} to un-define it.
   */
  Asset contentType(@Nullable String contentType);

  /**
   * Gets the blobRef or {@code null} if undefined.
   */
  @Nullable
  BlobRef blobRef();

  /**
   * Gets the blobRef or throws a runtime exception if undefined.
   */
  BlobRef requireBlobRef();

  /**
   * Sets the blobRef to the given value, or {@code null} to un-define it.
   */
  Asset blobRef(@Nullable BlobRef blobRef);
}
