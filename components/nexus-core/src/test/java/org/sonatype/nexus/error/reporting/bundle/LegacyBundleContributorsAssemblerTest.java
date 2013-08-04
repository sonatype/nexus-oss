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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.error.report.ErrorReportBundleContentContributor;
import org.sonatype.nexus.error.report.ErrorReportBundleEntry;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.StorageManager;
import org.sonatype.sisu.pr.bundle.internal.TmpFileStorageManager;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 2.1
 */
public class LegacyBundleContributorsAssemblerTest
    extends TestSupport
{

  @Mock
  private IssueSubmissionRequest request;

  @Mock
  private ErrorReportBundleContentContributor contributor;

  @Mock
  private ErrorReportBundleEntry entry;

  final Map<String, ErrorReportBundleContentContributor> contributors = Maps.newHashMap();

  private StorageManager storage;

  private LegacyBundleContributorsAssembler underTest;

  @Before
  public void setUp()
      throws IOException
  {
    storage = new TmpFileStorageManager(this.util.getTmpDir());

    underTest = new LegacyBundleContributorsAssembler(contributors, storage);
  }

  @After
  public void cleanup() {
    storage.release();
  }

  @Test
  public void testParticipation() {
    assertThat(underTest.isParticipating(request), is(false));
    contributors.put("test", contributor);
    assertThat(underTest.isParticipating(request), is(true));
  }

  @Test
  public void testAssembly()
      throws IssueSubmissionException, IOException
  {
    contributors.put("test1", contributor);
    contributors.put("test2", contributor);

    when(contributor.getEntries()).thenReturn(new ErrorReportBundleEntry[]{entry});
    when(entry.getEntryName()).thenReturn("file1", "file2");
    when(entry.getContent()).thenReturn(new ByteArrayInputStream("test".getBytes("utf-8")));

    final Bundle bundle = underTest.assemble(request);

    assertThat(bundle.getName(), is("extra"));
    assertThat(bundle.getSubBundles(), hasSize(2));

    assertThat(Collections2.transform(bundle.getSubBundles(), names()),
        containsInAnyOrder("test1", "test2"));

    assertThat(Collections2.transform(bundle.getSubBundles().get(0).getSubBundles(), names()),
        contains("file1"));

    assertThat(Collections2.transform(bundle.getSubBundles().get(1).getSubBundles(), names()),
        contains("file2"));

    verify(contributor, times(2)).getEntries();
    verify(entry, times(2)).getEntryName();
    verify(entry, times(2)).getContent();
    verify(entry, times(2)).releaseEntry();
  }

  private Function<Bundle, String> names() {
    return new Function<Bundle, String>()
    {
      @Override
      public String apply(@Nullable final Bundle input) {
        return input.getName();
      }
    };
  }
}
