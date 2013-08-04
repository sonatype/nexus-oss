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

import org.sonatype.security.model.CUser;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.source.SecurityModelConfigurationSource;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.StorageManager;
import org.sonatype.sisu.pr.bundle.internal.TmpFileStorageManager;

import com.google.common.collect.Lists;
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
 * Test for {@link SecurityXmlAssembler}.
 * Asserting basic operation and masking of passwords and email.
 *
 * @since 2.1
 */
public class SecurityXmlAssemblerTest
    extends TestSupport
{

  @Mock
  private IssueSubmissionRequest request;

  @Mock
  private Configuration model;

  @Mock
  private SecurityModelConfigurationSource source;

  @Mock
  private CUser user;

  private StorageManager storage;

  private SecurityXmlAssembler underTest;

  @Before
  public void init() {
    when(source.getConfiguration()).thenReturn(model);

    when(model.getUsers()).thenReturn(Lists.newArrayList(user));

    storage = new TmpFileStorageManager(util.createTempDir(getClass().getSimpleName()));

    underTest = new SecurityXmlAssembler(source, storage)
    {
      @Override
      protected Object cloneViaXml(final Object configuration) {
        return configuration;
      }
    };
  }

  @After
  public void cleanup() {
    storage.release();
  }

  @Test
  public void testParticipation() {
    assertThat(underTest.isParticipating(request), is(true));
    when(source.getConfiguration()).thenReturn(null);
    assertThat(underTest.isParticipating(request), is(false));
  }

  @Test
  public void testAssemblyNoModel()
      throws IssueSubmissionException, IOException
  {
    when(source.getConfiguration()).thenReturn(null);
    final Bundle bundle = underTest.assemble(request);

    assertThat(bundle.getName(), is("security.xml"));
    assertThat(bundle.getContentLength(), lessThan(30L));
  }

  @Test
  public void testAssembly()
      throws IssueSubmissionException, IOException
  {
    final Bundle bundle = underTest.assemble(request);
    assertThat(bundle.getName(), is("security.xml"));

    assertThat(bundle.getContentLength(), greaterThan(30L));

    final InputStreamReader reader = new InputStreamReader(bundle.getInputStream());
    try {
      // basically empty configuration, xml header, 'security' tag and an empty user
      assertThat(CharStreams.toString(reader), containsString("<security>"));
    }
    finally {
      reader.close();
    }

    verify(user).setPassword(underTest.PASSWORD_MASK);
    verify(user).setEmail(underTest.PASSWORD_MASK);
  }
}
