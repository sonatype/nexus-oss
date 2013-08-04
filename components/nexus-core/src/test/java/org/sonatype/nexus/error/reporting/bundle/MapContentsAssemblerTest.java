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
import java.util.Map;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.error.reporting.ErrorReportRequest;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.pr.bundle.Bundle;

import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * @since 2.1
 */
public class MapContentsAssemblerTest
    extends TestSupport
{

  @Mock
  private NexusConfiguration config;

  @Mock
  private IssueSubmissionRequest request;

  @Mock
  private ErrorReportRequest errorReportRequest;

  private MapContentsAssembler underTest;

  private Map<String, Object> context = Maps.newHashMap();

  @Before
  public void setUp()
      throws IOException
  {
    context.put("test", "value");

    underTest = new MapContentsAssembler();
  }

  @Test
  public void testParticipation() {
    assertThat(underTest.isParticipating(request), is(false));

    when(request.getContext()).thenReturn(new Object());
    assertThat(underTest.isParticipating(request), is(false));

    when(request.getContext()).thenReturn(errorReportRequest);
    assertThat(underTest.isParticipating(request), is(true));
  }

  @Test
  public void testAssembly()
      throws IssueSubmissionException, IOException
  {
    when(request.getContext()).thenReturn(errorReportRequest);
    when(errorReportRequest.getContext()).thenReturn(context);
    final Bundle bundle = underTest.assemble(request);

    assertThat(bundle.getName(), is("contextListing.txt"));
    assertThat(bundle.getContentLength(), greaterThan(0L));

    assertThat(
        CharStreams.toString(new InputStreamReader(bundle.getInputStream())),
        allOf(
            containsString("test"),
            containsString("value")
        ));
  }
}
