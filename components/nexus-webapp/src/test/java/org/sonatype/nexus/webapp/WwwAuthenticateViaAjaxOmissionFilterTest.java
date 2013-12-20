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
import static org.sonatype.nexus.webapp.WwwAuthenticateViaAjaxOmissionFilter.WWW_AUTHENTICATE;
import static org.sonatype.nexus.webapp.WwwAuthenticateViaAjaxOmissionFilter.XML_HTTP_REQUEST;
import static org.sonatype.nexus.webapp.WwwAuthenticateViaAjaxOmissionFilter.X_REQUESTED_WITH;

/**
 * Tests for {@link WwwAuthenticateViaAjaxOmissionFilter}.
 */
public class WwwAuthenticateViaAjaxOmissionFilterTest
  extends TestSupport
{
  private WwwAuthenticateViaAjaxOmissionFilter underTest;

  @Mock
  private HttpServletRequest request;

  @Before
  public void setUp() throws Exception {
    underTest = new WwwAuthenticateViaAjaxOmissionFilter();
  }

  @Test
  public void omitAjaxRequest() {
    when(request.getHeader(X_REQUESTED_WITH)).thenReturn(XML_HTTP_REQUEST);
    assertThat(underTest.isOmit(WWW_AUTHENTICATE, request), is(true));
  }

  @Test
  public void doNotOmitNonAjaxRequest() {
    when(request.getHeader(X_REQUESTED_WITH)).thenReturn("not-XMLHttpRequest");
    assertThat(underTest.isOmit(WWW_AUTHENTICATE, request), is(false));
  }

  @Test
  public void doNotOmitMissingRequestedWith() {
    assertThat(underTest.isOmit(WWW_AUTHENTICATE, request), is(false));
  }

  @Test
  public void doNotOmitNonWrongHeaderAjaxRequest() {
    when(request.getHeader(X_REQUESTED_WITH)).thenReturn(XML_HTTP_REQUEST);
    assertThat(underTest.isOmit("not-www-authenticate", request), is(false));
  }
}
