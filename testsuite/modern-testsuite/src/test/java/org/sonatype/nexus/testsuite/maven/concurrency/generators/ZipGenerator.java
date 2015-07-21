/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.maven.concurrency.generators;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;

import com.google.common.net.MediaType;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Content generator for ZIP files. Note: this class does NOT generate valid ZIP files, only appends smallest ZIP file
 * to stream beginning with minimal ZIP file and fills the rest with zeroes.
 */
public class ZipGenerator
    extends Generator
{
  private static final byte[] EMPTY_ZIP = new byte[]{
      80, 75, 05, 06, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00
  };

  @Override
  public String getContentType() {
    return MediaType.ZIP.toString();
  }

  @Override
  public long getExactContentLength(final long length) {
    if (length <= EMPTY_ZIP.length) {
      return EMPTY_ZIP.length;
    }
    else {
      return length;
    }
  }

  @Override
  public InputStream generate(final long length) {
    checkArgument(length > 0);
    if (length <= EMPTY_ZIP.length) {
      // ZIP must be "complete", cannot send less than minimal ZIP file
      return new ByteArrayInputStream(EMPTY_ZIP);
    }
    else {
      long timesZeroByte = length - EMPTY_ZIP.length;
      return new SequenceInputStream(new ByteArrayInputStream(EMPTY_ZIP), exactLength(new byte[]{0}, timesZeroByte));
    }
  }
}
