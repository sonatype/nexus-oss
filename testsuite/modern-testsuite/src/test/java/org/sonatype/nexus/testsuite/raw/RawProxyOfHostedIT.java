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
import java.net.URL;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;

import com.google.common.io.Files;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.bytes;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.status;

/**
 * IT for proxy raw repositories
 */
@ExamReactorStrategy(PerClass.class)
public class RawProxyOfHostedIT
    extends RawITSupport
{
  public static final String TEST_PATH = "alphabet.txt";

  public static final String TEST_CONTENT = "alphabet.txt";

  private RawClient hostedClient;

  private RawClient proxyClient;

  private Repository hostedRepo;

  @Before
  public void setUpRepositories() throws Exception {
    hostedRepo = createRepository(hostedConfig("raw-test-hosted"));
    hostedClient = client(hostedRepo);

    URL hostedRepoUrl = repositoryBaseUrl(hostedRepo);
    final Configuration proxyConfig = proxyConfig("raw-test-proxy", hostedRepoUrl.toExternalForm());
    proxyClient = client(createRepository(proxyConfig));
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    deleteRepository(hostedRepo);

    assertThat(status(proxyClient.get(TEST_PATH)), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void responsiveRemoteProduces404() throws Exception {
    assertThat(status(proxyClient.get(TEST_PATH)), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void fetchFromRemote() throws Exception {
    final File testFile = resolveTestFile(TEST_CONTENT);
    hostedClient.put(TEST_PATH, ContentType.TEXT_PLAIN, testFile);

    assertThat(bytes(proxyClient.get(TEST_PATH)), is(Files.toByteArray(testFile)));
  }

  @Test
  public void notFoundCaches404() throws Exception {
    // Ask for a nonexistent file
    proxyClient.get(TEST_PATH);

    // Put the file in the hosted repo
    hostedClient.put(TEST_PATH, ContentType.TEXT_PLAIN, resolveTestFile(TEST_CONTENT));

    // The NFC should ensure we still see the 404
    assertThat(status(proxyClient.get(TEST_PATH)), is(HttpStatus.NOT_FOUND));
  }
}
