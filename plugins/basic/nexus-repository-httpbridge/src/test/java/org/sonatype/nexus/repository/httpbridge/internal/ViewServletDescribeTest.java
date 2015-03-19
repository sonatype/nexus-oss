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
package org.sonatype.nexus.repository.httpbridge.internal;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.repository.httpbridge.DefaultHttpResponseSender;
import org.sonatype.nexus.repository.httpbridge.internal.describe.DescriptionRenderer;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewServletDescribeTest
    extends TestSupport
{
  @Mock
  private Request request;

  @Mock
  private Parameters parameters;

  @Mock
  private ViewFacet facet;

  @Mock
  private HttpServletRequest servletRequest;

  @Mock
  private HttpServletResponse servletResponse;

  @Mock
  private DefaultHttpResponseSender defaultResponseSender;

  @Mock(name = "facet response")
  private Response facetResponse;

  @Mock(name = "description response")
  private Response descriptionResponse;

  @Mock(name = "facet exception")
  private RuntimeException facetException;

  private ViewServlet viewServlet;

  @Test
  public void normalRequestReturnsFacetResponse() throws Exception {
    descriptionRequested(false);
    facetThrowsException(false);

    viewServlet.dispatchAndSend(request, facet, defaultResponseSender, servletRequest, servletResponse);

    verify(viewServlet, never()).describe(any(Request.class), any(Response.class), any(Exception.class));
    verify(defaultResponseSender).send(request, facetResponse, servletRequest, servletResponse);
  }

  @Test
  public void describeRequestReturnsDescriptionResponse() throws Exception {
    descriptionRequested(true);
    facetThrowsException(false);

    viewServlet.dispatchAndSend(request, facet, defaultResponseSender, servletRequest, servletResponse);

    verify(viewServlet).describe(request, facetResponse, null);
    verify(viewServlet).send(request, descriptionResponse, servletRequest, servletResponse);
  }

  @Test(expected = RuntimeException.class)
  public void facetExceptionsReturnedNormally() throws Exception {
    descriptionRequested(false);
    facetThrowsException(true);

    viewServlet.dispatchAndSend(request, facet, defaultResponseSender, servletRequest, servletResponse);
  }

  @Test
  public void facetExceptionsAreDescribed() throws Exception {
    descriptionRequested(true);
    facetThrowsException(true);

    viewServlet.dispatchAndSend(request, facet, defaultResponseSender, servletRequest, servletResponse);

    // The exception got described
    verify(viewServlet).describe(request, null, facetException);
    verify(viewServlet).send(request, descriptionResponse, servletRequest, servletResponse);
  }

  private void facetThrowsException(final boolean facetThrowsException) throws Exception {
    if (facetThrowsException) {
      when(facet.dispatch(request)).thenThrow(facetException);
    }
    else {
      when(facet.dispatch(request)).thenReturn(facetResponse);
    }
  }

  @Before
  public void prepareMocks() {
    viewServlet = spy(
        new ViewServlet(mock(RepositoryManager.class),
            mock(Map.class),
            defaultResponseSender,
            mock(DescriptionRenderer.class))
    );
    doReturn(descriptionResponse)
        .when(viewServlet).describe(any(Request.class), any(Response.class), any(Exception.class));

    when(request.getParameters()).thenReturn(parameters);

    BaseUrlHolder.set("http://placebo");
  }

  private void descriptionRequested(final boolean describe) {
    when(parameters.contains("describe")).thenReturn(describe);
  }
}