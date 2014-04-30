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
package org.sonatype.nexus.blobstore.file;

import java.util.List;

import org.sonatype.nexus.blobstore.api.BlobId;

import static java.util.Arrays.asList;

/**
 * Helps convert BlobIds to file paths safely.
 *
 * @since 3.0
 */
public class BlobIdEscaper
{
  private static final String PERIOD = "\\.";

  private static final String SINGLE_BACKSLASH = "\\\\";

  private static final String SINGLE_FORWARD_SLASH = "/";

  /**
   * Escapes directory navigation characters that might happen to occur in blob IDs.
   */
  public String toFileName(BlobId blobId) {

    String inString = blobId.toString();
    return replaceAll(inString, asList(PERIOD, SINGLE_BACKSLASH, SINGLE_FORWARD_SLASH), "-");
  }

  private String replaceAll(String original, final List<String> unsafeStrings, final String replacement) {
    for (String unsafe : unsafeStrings) {

      original = original.replaceAll(unsafe, replacement);
    }

    return original;
  }
}
