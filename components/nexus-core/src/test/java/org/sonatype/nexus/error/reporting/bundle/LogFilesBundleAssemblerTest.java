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

package org.sonatype.nexus.error.reporting.bundle;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.pr.bundle.Bundle;

import com.google.common.io.Files;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @since 2.1
 */
public class LogFilesBundleAssemblerTest
    extends TestSupport
{

  private LogFilesBundleAssembler underTest;

  @Mock
  private NexusConfiguration config;

  @Mock
  private IssueSubmissionRequest request;

  private File tempDir;

  private File logFile;

  private File file2;

  @Before
  public void setUp()
      throws IOException
  {
    tempDir = util.createTempDir(getClass().getSimpleName() + "-nexus-logs");

    when(config.getWorkingDirectory("logs")).thenReturn(
        tempDir);

    logFile = new File(tempDir, "nexus.log");
    Files.write("test".getBytes("utf-8"), logFile);

    underTest = new LogFilesBundleAssembler(config);
  }

  @After
  public void cleanup() {
    logFile.delete();
    tempDir.delete();
  }

  @Test
  public void testParticipation() {
    assertThat(underTest.isParticipating(request), is(true));
    logFile.delete();
    assertThat(underTest.isParticipating(request), is(false));
  }

  @Test
  public void testAssembly()
      throws IssueSubmissionException
  {
    final Bundle bundle = underTest.assemble(request);

    assertThat(bundle.getName(), is("nexus.log"));
    assertThat(bundle.getContentLength(), is(4L));
  }
}
