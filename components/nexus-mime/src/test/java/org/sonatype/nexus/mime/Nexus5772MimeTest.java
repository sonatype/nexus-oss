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
package org.sonatype.nexus.mime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.sonatype.nexus.mime.internal.DefaultMimeSupport;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

/**
 * Testing content detection aginst some most typical files.
 */
public class Nexus5772MimeTest
    extends TestSupport
{
  protected void assertComplete(final MimeSupport mimeSupport, File file, String expectedMimeType)
      throws IOException
  {
    try (InputStream input = new FileInputStream(file)) {
      final List<String> mimeTypes = mimeSupport.detectMimeTypes(input, file.getName());
      assertThat("Expected MIME type not returned for content:", mimeTypes, hasItem(expectedMimeType));
    }
  }

  @Test
  public void assertComplete() throws IOException {
    final MimeSupport mimeSupport = new DefaultMimeSupport();
    assertComplete(mimeSupport, util.resolveFile("src/test/resources/mime/file.gif"), "image/gif");
    assertComplete(mimeSupport, util.resolveFile("src/test/resources/mime/file.zip"), "application/zip");
    assertComplete(mimeSupport, util.resolveFile("src/test/resources/mime/empty.zip"), "application/zip");
    assertComplete(mimeSupport, util.resolveFile("src/test/resources/mime/file.jar"), "application/zip");
  }

  @Test
  public void assertCompleteWithFiles() throws IOException {
    final MimeSupport mimeSupport = new DefaultMimeSupport();
    {
      final File file = util.resolveFile("src/test/resources/mime/file.gif");
      try (final InputStream is = new FileInputStream(file)) {
        assertThat(mimeSupport.detectMimeType(is, file.getName()), equalTo("image/gif"));
      }
    }
    {
      final File file = util.resolveFile("src/test/resources/mime/file.zip");
      try (final InputStream is = new FileInputStream(file)) {
        assertThat(mimeSupport.detectMimeType(is, file.getName()), equalTo("application/zip"));
      }
    }
    {
      final File file = util.resolveFile("src/test/resources/mime/empty.zip");
      try (final InputStream is = new FileInputStream(file)) {
        assertThat(mimeSupport.detectMimeType(is, file.getName()), equalTo("application/zip"));
      }
    }
    {
      final File file = util.resolveFile("src/test/resources/mime/file.jar");
      try (final InputStream is = new FileInputStream(file)) {
        assertThat(mimeSupport.detectMimeType(is, file.getName()), equalTo("application/java-archive"));
      }
    }
  }
}
