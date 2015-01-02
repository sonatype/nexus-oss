/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.source.AssetResponse;
import org.sonatype.nexus.component.source.ComponentRequest;
import org.sonatype.nexus.component.source.ComponentResponse;
import org.sonatype.nexus.component.source.ComponentSource;
import org.sonatype.nexus.component.source.ComponentSourceRegistry;
import org.sonatype.nexus.componentviews.HandlerContext;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.ViewRequest.HttpMethod;
import org.sonatype.nexus.componentviews.ViewResponse;
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
import static org.sonatype.nexus.component.services.adapter.AssetAdapter.P_PATH;

public class ProxyingRawBinariesHandlerTest
{
  private TestableRawProxyHandler handler;

  private RawBinaryStore store;

  private ComponentSource source;

  private static final String PATH = "/path/foo";

  private static final String SOURCE_NAME = "test source";

  @Before
  public void initMocks() {
    store = mock(RawBinaryStore.class);
    source = mock(ComponentSource.class);
    final ComponentSourceRegistry registry = mock(ComponentSourceRegistry.class);
    when(registry.getSource(SOURCE_NAME)).thenReturn(source);

    handler = new TestableRawProxyHandler(store, SOURCE_NAME, registry);
  }

  @Test
  public void binariesFoundLocallyAreStreamed() throws Exception {
    ViewRequest request = mockRequest(PATH, HttpMethod.GET);

    HandlerContext context = mock(HandlerContext.class);
    when(context.getRequest()).thenReturn(request);

    final Asset localBinary = new Asset("");
    localBinary.put(P_PATH, PATH);

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
    when(store.getForPath(PATH)).thenReturn(Collections.<Asset>emptyList());

    final ComponentResponse mockComponentResponse = mock(ComponentResponse.class);
    when(mockComponentResponse.getAssets()).thenReturn(asList(mock(AssetResponse.class)));

    final ImmutableMap<String, String> fetchParameters = ImmutableMap.of("path", PATH);
    final ComponentRequest fetchRequest = new ComponentRequest(fetchParameters);
    when(source.fetchComponents(eq(fetchRequest))).thenReturn(mockComponentResponse);

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
    private Asset streamed;

    private TestableRawProxyHandler(final RawBinaryStore binaryStore, final String sourceName,
                                    final ComponentSourceRegistry sourceRegistry)
    {
      super(binaryStore, sourceName, sourceRegistry);
    }

    @Override
    ViewResponse createStreamResponse(final Asset binary) {
      this.streamed = binary;
      return null;
    }

    public Asset getStreamed() {
      return streamed;
    }
  }
}
