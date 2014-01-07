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
package org.sonatype.nexus.webapp;

import javax.servlet.http.HttpServletRequest;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.webapp.WwwAuthenticateViaBrowserOmissionFilter.USER_AGENT;
import static org.sonatype.nexus.webapp.WwwAuthenticateViaBrowserOmissionFilter.WWW_AUTHENTICATE;

/**
 * Tests for {@link WwwAuthenticateViaBrowserOmissionFilter}.
 */
public class WwwAuthenticateViaBrowserOmissionFilterTest
  extends TestSupport
{
  private WwwAuthenticateViaBrowserOmissionFilter underTest;

  @Mock
  private HttpServletRequest request;

  @Before
  public void setUp() throws Exception {
    underTest = new WwwAuthenticateViaBrowserOmissionFilter();
  }

  private void whenUserAgent(final String userAgent) {
    when(request.getHeader(USER_AGENT)).thenReturn(userAgent);
  }

  @Test
  public void omit_chrome() {
    whenUserAgent("User-Agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
    assertThat(underTest.shouldOmit(WWW_AUTHENTICATE, request), is(true));
  }

  @Test
  public void omit_firefox() {
    whenUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:25.0) Gecko/20100101 Firefox/25.0");
    assertThat(underTest.shouldOmit(WWW_AUTHENTICATE, request), is(true));
  }

  @Test
  public void notOmit_notWwwAuthenticate() {
    assertThat(underTest.shouldOmit("not-" + WWW_AUTHENTICATE, request), is(false));
  }

  @Test
  public void notOmit_httpclient() {
    whenUserAgent("Apache-HttpClient/release (java 1.5)");
    assertThat(underTest.shouldOmit(WWW_AUTHENTICATE, request), is(false));
  }

  @Test
  public void notOmit_missingUserAgent() {
    assertThat(underTest.shouldOmit(WWW_AUTHENTICATE, request), is(false));
  }
}
