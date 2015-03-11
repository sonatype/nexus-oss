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
package org.sonatype.nexus.blobstore.api;

import javax.annotation.Nonnull;

import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

/**
 * A generic storage bin for binary objects of all sizes.
 *
 * In general, most methods can throw {@link BlobStoreException} for conditions such as network connectivity problems,
 * or file IO issues, blob store misconfiguration, or internal corruption.
 *
 * @since 3.0
 */
public interface BlobStore
    extends Lifecycle
{
  /**
   * An identifying name for disaster recovery purposes (which isn't required to be strictly unique)
   */
  String BLOB_NAME_HEADER = "BlobStore.blob-name";

  /**
   * Audit information (e.g. the name of a principal that created the blob)
   */
  String CREATED_BY_HEADER = "BlobStore.created-by";

  Type getType();

  /**
   * Returns the configuration entity for the BlobStore.
   */
  BlobStoreConfiguration getBlobStoreConfiguration();
  
  String getName();

  
  void attach(Facet facet) throws Exception;
  
  // TODO - KR custom exception here?
  @Nonnull
  <T extends Facet> T facet(Class<T> type) throws IllegalStateException;

  void delete() throws Exception;
  
  void init(BlobStoreConfiguration configuration);
}
