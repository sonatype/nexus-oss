/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.views.rawbinaries.view;

import java.util.Collections;

import javax.inject.Provider;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.ComponentRequest;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.nexus.componentviews.HandlerContext;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.ViewRequest.HttpMethod;
import org.sonatype.nexus.componentviews.ViewResponse;
import org.sonatype.nexus.views.rawbinaries.internal.RawComponent;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinary;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinaryStore;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProxyingRawBinariesHandlerTest
{

  private TestableRawProxyHandler handler;

  private RawBinaryStore store;

  private PullComponentSource source;

  private static final String PATH = "/path/foo";

  @Before
  public void initMocks() {
    store = mock(RawBinaryStore.class);
    source = mock(PullComponentSource.class);

    handler = new TestableRawProxyHandler(store, new Provider<PullComponentSource>()
    {
      @Override
      public PullComponentSource get() {
        return source;
      }
    });
  }

  @Test
  public void binariesFoundLocallyAreStreamed() throws Exception {
    ViewRequest request = mockRequest(PATH, HttpMethod.GET);

    HandlerContext context = mock(HandlerContext.class);
    when(context.getRequest()).thenReturn(request);

    final RawBinary localBinary = mock(RawBinary.class);
    when(localBinary.getPath()).thenReturn(PATH);

    when(store.getForPath(PATH)).thenReturn(asList(localBinary));

    handler.handle(context);

    verify(store).getForPath(PATH);

    assertThat(handler.getStreamed(), is(sameInstance(localBinary)));
  }

  @Test
  public void notFoundLocallyCallsSource() throws Exception {
    ViewRequest request = mockRequest(PATH, HttpMethod.GET);
    HandlerContext context = mock(HandlerContext.class);
    when(context.getRequest()).thenReturn(request);

    // There's no matching local raw binary
    when(store.getForPath(PATH)).thenReturn(Collections.<RawBinary>emptyList());

    final ComponentEnvelope<RawComponent> mockComponent = mock(ComponentEnvelope.class);
    when(mockComponent.getAssets()).thenReturn(asList(mock(Asset.class)));

    Iterable<ComponentEnvelope<RawComponent>> fetchedComponents = asList(mockComponent);

    final ImmutableMap<String, String> fetchParameters = ImmutableMap.of("path", PATH);
    final ComponentRequest fetchRequest = new ComponentRequest(fetchParameters);
    when(source.fetchComponents(eq(fetchRequest))).thenReturn(fetchedComponents);

    final ViewResponse handle = handler.handle(context);

    verify(source).fetchComponents(eq(fetchRequest));
  }

  private ViewRequest mockRequest(final String path, final HttpMethod method) {
    ViewRequest request = mock(ViewRequest.class);
    when(request.getPath()).thenReturn(path);
    when(request.getMethod()).thenReturn(method);
    return request;
  }

  private static class TestableRawProxyHandler
      extends ProxyingRawBinariesHandler
  {
    private RawBinary streamed;

    private TestableRawProxyHandler(final RawBinaryStore binaryStore,
                                    final Provider<PullComponentSource> sourceProvider)
    {
      super(binaryStore, sourceProvider);
    }

    @Override
    ViewResponse createStreamResponse(final RawBinary binary) {
      this.streamed = binary;
      return null;
    }

    public RawBinary getStreamed() {
      return streamed;
    }
  }
}
