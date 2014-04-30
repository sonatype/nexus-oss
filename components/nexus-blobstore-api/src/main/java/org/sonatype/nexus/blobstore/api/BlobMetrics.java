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


import java.util.Date;

/**
 * @since 3.0
 */
public interface BlobMetrics
{
  Date getCreationTime();

  /**
   * A SHA1 hash of the content bytes (not the headers).
   */
  String getSHA1Hash();

  /**
   * The length, in bytes, of the headers.  This ignores storage considerations like block size.
   */
  long getHeaderSize();

  /**
   * The byte length of the raw content blob, excluding storage considerations like block size.
   */
  long getContentSize();

  /**
   * The length of the header plus content, in bytes.
   */
  long getTotalSize();
}
