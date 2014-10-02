package org.sonatype.nexus.views.rawbinaries.view;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.ViewResponse;
import org.sonatype.nexus.componentviews.WrappingHandler;
import org.sonatype.nexus.componentviews.responses.Responses;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinary;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinaryStore;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A proxying handler for raw binary assets.
 *
 * @since 3.0
 */
public class ProxyingRawBinariesHandler
    extends WrappingHandler
{
  private final RawBinaryStore binaryStore;

  private final PullComponentSource source;

  public ProxyingRawBinariesHandler(final Handler innerHandler,
                                    final RawBinaryStore binaryStore,
                                    final PullComponentSource source)
  {
    super(checkNotNull(innerHandler));
    this.binaryStore = checkNotNull(binaryStore);
    this.source = checkNotNull(source);
  }

  @Nullable
  @Override
  protected ViewResponse before(final ViewRequest req) {
    switch (req.getMethod()) {
      case GET:
        // Check locally.
        final List<RawBinary> localBinaries = binaryStore.getForPath(req.getPath());

        // Do we have a single result whose path matches the request exactly?

        final RawBinary exactMatch = exactMatch(localBinaries, req.getPath());
        if (exactMatch != null) {
          return createStreamResponse(exactMatch);
        }

        // If not, contact the remote source and store whatever it has

        try {
          final Iterable<ComponentEnvelope<Object>> envelopes = source
              .fetchComponents(ImmutableMap.of("path", req.getPath()));

          for (ComponentEnvelope<Object> envelope : envelopes) {
            for (Asset asset : envelope.getAssets()) {
              final RawBinary rawBinary = binaryStore.create(asset.getPath(), asset.getContentType(),
                  asset.openStream());
            }
          }
        }
        catch (IOException e) {
          Throwables.propagate(e);
        }

        // Return null to delegate to the inner handler.
        return null;

      default:
        return Responses.methodNotAllowed();
    }
  }

  ViewResponse createStreamResponse(final RawBinary binary) {
    return Responses
        .streamResponse(binary.getInputStream(), binary.getMimeType(), binary.getModifiedDate().toDate());
  }


  @Nullable
  private RawBinary exactMatch(final List<RawBinary> binaries, final String path) {
    for (RawBinary binary : binaries) {
      if (binary.getPath().equals(path)) {
        return binary;
      }
    }
    return null;
  }
}
