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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Blob reference.
 *
 * Provides a pointer to a blob on a given node, in a given store.
 *
 * @since 3.0
 */
public class BlobRef
{
  private final String node;

  private final String store;

  private final String blob;

  public BlobRef(final String node, final String store, final String blob) {
    this.node = checkNotNull(node);
    this.store = checkNotNull(store);
    this.blob = checkNotNull(blob);
  }

  public String getNode() {
    return node;
  }

  public String getStore() {
    return store;
  }

  public String getBlob() {
    return blob;
  }

  public BlobId getBlobId() {
    return new BlobId(getBlob());
  }

  /**
   * @return the blob ref encoded as a string, using the syntax <store>@<node>:<blob>
   */
  public String toString() {
    return String.format("%s@%s:%s", getStore(), getNode(), getBlob());
  }

  public static BlobRef parse(final String spec) {
    // FIXME: parse it out
    throw new RuntimeException("Not implemented");
  }
}
