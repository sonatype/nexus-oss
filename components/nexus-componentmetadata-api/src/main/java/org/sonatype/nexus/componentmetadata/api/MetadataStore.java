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
package org.sonatype.nexus.componentmetadata.api;

import javax.annotation.Nullable;

public interface MetadataStore<T>
{
  /**
   * Adds new metadata.
   *
   * @return a handle containing the key, version, and metadata.
   */
  MetadataHandle<T> create(T metadata);

  /**
   * Retrieves the metadata with the given key.
   *
   * @return a handle containing the key, version, and metadata, or <code>null</code> if not found.
   */
  @Nullable
  MetadataHandle<T> retrieve(String key);

  /**
   * Updates the metadata with the given key.
   *
   * @param key the key.
   * @param version the previously-known version, or <code>null</code>. If unspecified, the new value will replace
   *        the old one irrespective of the version. If specified, the old value will only be overwritten if
   *        its version matches -- otherwise this method will return <code>null</code>.
   * @param metadata the new value.
   * @return a handle with the new version, or <code>null</code> if not found or the provided version differs from
   *         the one in the store.
   */
  MetadataHandle<T> update(String key, @Nullable String version, T metadata);

  boolean delete(String key, @Nullable String version);

  MetadataKeyIterable find(MetadataBuilder<T> filter, MetadataIndex index);

  @Nullable
  String findFirst(MetadataBuilder<T> filter, MetadataIndex index);
}
