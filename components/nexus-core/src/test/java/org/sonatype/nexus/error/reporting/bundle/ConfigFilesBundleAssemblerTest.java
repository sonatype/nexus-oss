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

import javax.annotation.Nullable;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.pr.bundle.Bundle;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.io.Files;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @since 2.1
 */
public class ConfigFilesBundleAssemblerTest
    extends TestSupport
{

  private ConfigFilesBundleAssembler underTest;

  @Mock
  private NexusConfiguration config;

  @Mock
  private IssueSubmissionRequest request;

  private File tempDir;

  private File file1;

  private File file2;

  @Before
  public void setUp()
      throws IOException
  {
    tempDir = util.createTempDir(getClass().getSimpleName() + "-nexus-conf");

    when(config.getWorkingDirectory("conf")).thenReturn(
        tempDir);

    file1 = new File(tempDir, "file1");
    file2 = new File(tempDir, "file2");

    Files.write("test1".getBytes("utf-8"), file1);
    Files.write("test2".getBytes("utf-8"), file2);

    underTest = new ConfigFilesBundleAssembler(config);
  }

  @After
  public void cleanup() {
    file1.delete();
    file2.delete();
    tempDir.delete();
  }

  @Test
  public void testParticipation() {
    assertThat(underTest.isParticipating(request), is(true));
  }

  @Test
  public void testAssembly()
      throws IssueSubmissionException
  {
    final Bundle bundle = underTest.assemble(request);

    assertThat(bundle.getSubBundles(), hasSize(2));
    assertThat(Collections2.transform(bundle.getSubBundles(), new Function<Bundle, String>()
    {
      @Override
      public String apply(@Nullable final Bundle input) {
        return input.getName();
      }
    }), containsInAnyOrder("file1", "file2"));
  }
}
