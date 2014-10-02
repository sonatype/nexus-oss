package org.sonatype.nexus.views.rawbinaries.view;

import java.util.Collections;

import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.ViewRequest.HttpMethod;
import org.sonatype.nexus.componentviews.ViewResponse;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinary;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinaryStore;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
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

    handler = new TestableRawProxyHandler(mock(Handler.class), store, source);
  }

  @Test
  public void binariesFoundLocallyAreStreamed() throws Exception {
    ViewRequest request = mockRequest(PATH, HttpMethod.GET);

    final RawBinary localBinary = mock(RawBinary.class);
    when(localBinary.getPath()).thenReturn(PATH);

    when(store.getForPath(PATH)).thenReturn(asList(localBinary));

    handler.handle(request);

    verify(store).getForPath(PATH);

    assertThat(handler.getStreamed(), is(sameInstance(localBinary)));
  }

  @Test
  public void notFoundLocallyCallsSource() throws Exception {
    ViewRequest request = mockRequest(PATH, HttpMethod.GET);

    // There's no matching local raw binary
    when(store.getForPath(PATH)).thenReturn(Collections.<RawBinary>emptyList());

    final ComponentEnvelope<Object> mock = mock(ComponentEnvelope.class);
    Iterable<ComponentEnvelope<Object>> fetchedComponents = asList(mock);

    final ImmutableMap<String, String> fetchParameters = ImmutableMap.of("path", PATH);
    when(source.fetchComponents(fetchParameters)).thenReturn(fetchedComponents);

    final ViewResponse handle = handler.handle(request);

    verify(source).fetchComponents(fetchParameters);
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

    private TestableRawProxyHandler(final Handler inner, final RawBinaryStore binaryStore,
                                    final PullComponentSource source)
    {
      super(inner, binaryStore, source);
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
