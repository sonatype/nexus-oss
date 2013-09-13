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

package org.sonatype.nexus.yum.internal;

import java.io.InputStream;

import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SteadyLinksRequestStrategyTest
    extends TestSupport
{

  private SteadyLinksRequestStrategy underTest;

  @Mock
  private Repository repository;

  @Before
  public void prepareRepository()
      throws Exception
  {
    when(repository.retrieveItem(Mockito.any(ResourceStoreRequest.class))).thenReturn(
        mock(StorageFileItem.class)
    );
  }

  @Test
  public void shouldOnlyChangeReadAction() {
    final ResourceStoreRequest request = mock(ResourceStoreRequest.class);

    new SteadyLinksRequestStrategy().onHandle(
        mock(Repository.class), request, Action.create
    );
    new SteadyLinksRequestStrategy().onHandle(
        mock(Repository.class), request, Action.update
    );
    new SteadyLinksRequestStrategy().onHandle(
        mock(Repository.class), request, Action.delete
    );

    verifyNoMoreInteractions(request);
  }

  @Test
  public void shouldChangePathFromRoot() {
    final ResourceStoreRequest request = mock(ResourceStoreRequest.class);
    final RequestContext context = mock(RequestContext.class);
    when(request.getRequestContext()).thenReturn(context);
    when(request.getRequestPath()).thenReturn("/repodata/primary.xml.gz");

    new SteadyLinksRequestStrategy()
    {
      @Override
      String matchRequestPath(final String requestPath, final InputStream repomd) {
        assertThat(requestPath, is("/repodata/primary.xml.gz"));
        return "/repodata/XYZ-primary.xml.gz";
      }
    }.onHandle(
        repository, request, Action.read
    );

    verify(request).pushRequestPath("/repodata/XYZ-primary.xml.gz");
    verify(context).put(SteadyLinksRequestStrategy.REQUEST_PATH_ORIGINAL, "/repodata/primary.xml.gz");
    verify(context).put(SteadyLinksRequestStrategy.REQUEST_PATH_NEW, "/repodata/XYZ-primary.xml.gz");
  }

  @Test
  public void shouldChangePathFromSubPath() {
    final ResourceStoreRequest request = mock(ResourceStoreRequest.class);
    final RequestContext context = mock(RequestContext.class);
    when(request.getRequestContext()).thenReturn(context);
    when(request.getRequestPath()).thenReturn("/foo/repodata/primary.xml.gz");

    new SteadyLinksRequestStrategy()
    {
      @Override
      String matchRequestPath(final String requestPath, final InputStream repomd) {
        assertThat(requestPath, is("/foo/repodata/primary.xml.gz"));
        return "/foo/repodata/XYZ-primary.xml.gz";
      }
    }.onHandle(
        repository, request, Action.read
    );

    verify(request).pushRequestPath("/foo/repodata/XYZ-primary.xml.gz");
    verify(context).put(SteadyLinksRequestStrategy.REQUEST_PATH_ORIGINAL, "/foo/repodata/primary.xml.gz");
    verify(context).put(SteadyLinksRequestStrategy.REQUEST_PATH_NEW, "/foo/repodata/XYZ-primary.xml.gz");
  }

  @Test
  public void shouldNotChangePath() {
    final ResourceStoreRequest request = mock(ResourceStoreRequest.class);
    final RequestContext context = mock(RequestContext.class);
    when(request.getRequestContext()).thenReturn(context);
    when(request.getRequestPath()).thenReturn("/repodata/primary.xml.gz");

    new SteadyLinksRequestStrategy()
    {
      @Override
      String matchRequestPath(final String requestPath, final InputStream repomd) {
        assertThat(requestPath, is("/repodata/primary.xml.gz"));
        return null;
      }
    }.onHandle(
        repository, request, Action.read
    );

    verify(request).getRequestPath();
    verifyNoMoreInteractions(request);
    verifyNoMoreInteractions(context);
  }

}
