/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.restlet1x.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.noelios.restlet.Engine;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class RestletHeaderFilterTest
    extends TestSupport
{

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpServletRequest request;

  @Mock
  private ServletResponse notInstanceOfHttpServletResponse;


  @Test
  public void noServerHeader() throws Exception {
    when(response.getHeaders("Server")).thenReturn(Collections.EMPTY_LIST);

    RestletHeaderFilter filter = new RestletHeaderFilter();
    filter.preHandle(request,response);

    verify(response).setHeader("Server", Engine.VERSION_HEADER);
  }

  @Test
  public void oneServerHeader() throws Exception {
    List<String> data = Arrays.asList("special/5.0.0");
    final Collection<String> serverHeaders = Collections.unmodifiableCollection(data);
    when(response.getHeaders("Server")).thenReturn(serverHeaders);

    RestletHeaderFilter filter = new RestletHeaderFilter();
    filter.preHandle(request, response);

    verify(response).setHeader("Server", "special/5.0.0 " + Engine.VERSION_HEADER);
  }

  @Test
  public void multipleServerHeaders() throws Exception {
    List<String> data = Arrays.asList("special/5.0.0", "server2/v1");
    final Collection<String> serverHeaders = Collections.unmodifiableCollection(data);
    when(response.getHeaders("Server")).thenReturn(serverHeaders);

    RestletHeaderFilter filter = new RestletHeaderFilter();
    filter.preHandle(request,response);

    verify(response).setHeader("Server", "special/5.0.0 server2/v1 " + Engine.VERSION_HEADER);
  }

  @Test
  public void ifNotInstanceOfHttpServletResponseDoNotProcessResponse() throws Exception {
    RestletHeaderFilter filter = new RestletHeaderFilter();
    filter.preHandle(request, notInstanceOfHttpServletResponse);

    verifyZeroInteractions(response);
  }

}