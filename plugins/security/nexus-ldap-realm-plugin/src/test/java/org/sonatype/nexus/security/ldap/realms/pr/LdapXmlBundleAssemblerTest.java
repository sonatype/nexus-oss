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

package org.sonatype.nexus.security.ldap.realms.pr;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import org.sonatype.security.ldap.realms.persist.model.Configuration;
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
 * Test for {@link LdapXmlBundleAssembler}.
 * Asserting basic operation and masking of passwords.
 *
 * @since 2.2
 */
public class LdapXmlBundleAssemblerTest
    extends TestSupport
{

  private StorageManager storage;

  private LdapXmlBundleAssembler underTest;

  private File tempDir;

  @Mock
  private IssueSubmissionRequest request;

  @Mock
  private Configuration model;

  @Mock
  private LdapConfiguration source;

  @Mock
  private CConnectionInfo connectionInfo;

  @Mock
  private ApplicationConfiguration cfg;

  @Before
  public void before()
      throws IOException
  {
    tempDir = util.createTempDir(getClass().getSimpleName());
    new File(tempDir, "ldap.xml").createNewFile();

    storage = new TmpFileStorageManager(tempDir);

    when(cfg.getConfigurationDirectory()).thenReturn(tempDir);

    when(source.getConfiguration()).thenReturn(model);
    when(model.getConnectionInfo()).thenReturn(connectionInfo);

    underTest = new LdapXmlBundleAssembler(cfg, source, storage);
  }

  @After
  public void cleanup() {
    storage.release();
  }

  @Test
  public void testParticipation()
      throws IOException
  {
    assertThat(underTest.isParticipating(request), is(true));
    new File(tempDir, "ldap.xml").delete();
    assertThat(underTest.isParticipating(request), is(false));
  }

  @Test
  public void assembleBundleWithNoLdapConfig()
      throws IssueSubmissionException, IOException
  {
    when(source.getConfiguration()).thenReturn(null);
    final Bundle bundle = underTest.assemble(request);

    assertThat(bundle.getName(), is("ldap.xml"));
    assertThat(bundle.getContentLength(), lessThan(30L));
  }

  @Test
  public void assembleBundleWithLdapConfig()
      throws IssueSubmissionException, IOException
  {
    final Bundle bundle = underTest.assemble(request);
    assertThat(bundle.getName(), is("ldap.xml"));

    assertThat(bundle.getContentLength(), greaterThan(30L));

    final InputStreamReader reader = new InputStreamReader(bundle.getInputStream());
    try {
      // basically empty configuration, xml header, 'security' tag and an empty user
      assertThat(CharStreams.toString(reader), containsString("<ldapConfiguration>"));
    }
    finally {
      reader.close();
    }

    verify(connectionInfo).setSystemPassword("***");
  }
}
