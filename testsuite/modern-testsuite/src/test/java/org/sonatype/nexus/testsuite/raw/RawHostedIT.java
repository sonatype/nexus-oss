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
package org.sonatype.nexus.testsuite.raw;

import java.io.File;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;

import com.google.common.io.Files;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.bytes;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.status;

/**
 * IT for hosted raw repositories
 */
public class RawHostedIT
    extends RawITSupport
{
  public static final String HOSTED_REPO = "raw-test-hosted";

  public static final String TEST_PATH = "alphabet.txt";

  public static final String TEST_CONTENT = "alphabet.txt";

  private RawClient rawClient;

  @Before
  public void createHostedRepository() throws Exception {
    final Configuration config = hostedConfig(HOSTED_REPO);
    final Repository repository = createRepository(config);
    rawClient = client(repository);
  }

  @Test
  public void uploadAndDownload() throws Exception {
    final File testFile = resolveTestFile(TEST_CONTENT);
    final int response = rawClient.put(TEST_PATH, ContentType.TEXT_PLAIN, testFile);
    assertThat(response, is(HttpStatus.CREATED));

    assertThat(bytes(rawClient.get(TEST_PATH)), is(Files.toByteArray(testFile)));

    assertThat(status(rawClient.delete(TEST_PATH)), is(HttpStatus.NO_CONTENT));

    assertThat("content should be deleted", status(rawClient.get(TEST_PATH)), is(HttpStatus.NOT_FOUND));
  }
}
