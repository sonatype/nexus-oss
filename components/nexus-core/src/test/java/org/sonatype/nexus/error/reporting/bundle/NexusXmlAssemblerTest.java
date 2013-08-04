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

import java.io.IOException;
import java.io.InputStreamReader;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.ConfigurationHelper;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.StorageManager;
import org.sonatype.sisu.pr.bundle.internal.TmpFileStorageManager;

import com.google.common.io.CharStreams;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link NexusXmlAssembler}.
 * Asserting basic operation and masking of passwords.
 *
 * @since 2.1
 */
public class NexusXmlAssemblerTest
    extends TestSupport
{

  @Mock
  private ConfigurationHelper helper;

  @Mock
  private NexusConfiguration config;

  @Mock
  private IssueSubmissionRequest request;

  @Mock
  private Configuration model;

  private StorageManager storage;

  private NexusXmlAssembler underTest;

  @Before
  public void init() {
    when(config.getConfigurationModel()).thenReturn(model);

    storage = new TmpFileStorageManager(util.createTempDir(getClass().getSimpleName()));

    underTest = new NexusXmlAssembler(helper, config, storage);
  }

  @After
  public void cleanup() {
    storage.release();
  }

  @Test
  public void testParticipation() {
    assertThat(underTest.isParticipating(request), is(true));
    when(config.getConfigurationModel()).thenReturn(null);
    assertThat(underTest.isParticipating(request), is(false));
  }

  @Test
  public void testAssemblyNoModel()
      throws IssueSubmissionException, IOException
  {
    final Bundle bundle = underTest.assemble(request);

    assertThat(bundle.getName(), is("nexus.xml"));

    // hm, how to assert some kind of error message? message is short...
    assertThat(bundle.getContentLength(), lessThan(40L));

    verify(helper).maskPasswords(model);
  }

  @Test
  public void testAssembly()
      throws IssueSubmissionException, IOException
  {
    when(helper.maskPasswords(model)).thenReturn(model);

    final Bundle bundle = underTest.assemble(request);
    assertThat(bundle.getName(), is("nexus.xml"));

    assertThat(bundle.getContentLength(), greaterThan(40L));

    final InputStreamReader reader = new InputStreamReader(bundle.getInputStream());
    try {
      // basically empty configuration, only xml header and 'nexusConfiguration' tag
      assertThat(CharStreams.toString(reader), containsString("<nexusConfiguration />"));
    }
    finally {
      reader.close();
    }

    verify(helper).maskPasswords(model);
  }
}
