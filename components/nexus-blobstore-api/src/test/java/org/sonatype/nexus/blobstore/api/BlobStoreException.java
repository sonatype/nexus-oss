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
package org.sonatype.nexus.blobstore.api;

import javax.annotation.Nullable;

/**
 * @since 3.0
 */
public class BlobStoreException extends RuntimeException
{
  private String blobStoreName;

  private BlobId blobId;

  public BlobStoreException(final String message, final String blobStoreName, final BlobId blobId) {
    super(message);
    this.blobStoreName = blobStoreName;
    this.blobId = blobId;
  }

  public BlobStoreException(final String message, final Throwable cause, final String blobStoreName,
                            final BlobId blobId)
  {
    super(message, cause);
    this.blobStoreName = blobStoreName;
    this.blobId = blobId;
  }

  public BlobStoreException(final Throwable cause, final String blobStoreName, final BlobId blobId) {
    super(cause);
    this.blobStoreName = blobStoreName;
    this.blobId = blobId;
  }

  /**
   * The {@link BlobStore#getName() name} of the BlobStore that generated this exception.
   */
  public String getBlobStoreName() {
    return blobStoreName;
  }

  /**
   * The BlobId of the blob related to this exception, or {@code null} if there is none.
   */
  @Nullable
  public BlobId getBlobId() {
    return blobId;
  }
}
