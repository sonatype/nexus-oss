/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.util.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

public class FileSupportTest
    extends TestSupport
{
  private static final byte[] PAYLOAD = "payload".getBytes(Charset.forName("UTF-8"));

  private File root;

  private void createFile(final Path r) throws IOException {
    Files.write(r.resolve("file1.txt"), PAYLOAD);
  }

  @Before
  public void prepare() throws IOException {
    root = util.createTempDir();
    createFile(root.toPath());
  }

  @Test
  public void delete() throws IOException {
    final Path file1 = root.toPath().resolve("file1.txt");
    FileSupport.delete(file1);
    assertThat(file1.toFile(), not(exists()));
  }

  @Test
  public void deleteIfExists() throws IOException {
    final Path file1 = root.toPath().resolve("file1.txt");
    final Path file2 = root.toPath().resolve("file-not-exists.txt");
    assertThat(FileSupport.deleteIfExists(file2), is(false));
    assertThat(FileSupport.deleteIfExists(file1), is(true));
    assertThat(file1.toFile(), not(exists()));
  }
}
