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

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.ComponentRequest;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.HandlerContext;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.ViewResponse;
import org.sonatype.nexus.componentviews.responses.Responses;
import org.sonatype.nexus.views.rawbinaries.internal.RawComponent;
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
    implements Handler

{
  private final RawBinaryStore binaryStore;

  private final Provider<PullComponentSource> sourceProvider;

  public ProxyingRawBinariesHandler(final RawBinaryStore binaryStore,
                                    final Provider<PullComponentSource> sourceProvider)
  {
    this.sourceProvider = sourceProvider;
    this.binaryStore = checkNotNull(binaryStore);
  }


  @Override
  public ViewResponse handle(final HandlerContext context) throws Exception {
    final ViewRequest req = context.getRequest();

    final String requestPath = ensureLeadingSlash(
        req.getPath() + (req.getQueryString() == null ? "" : "?" + req.getQueryString()));

    switch (req.getMethod()) {
      case GET:
        // Check locally.

        final List<RawBinary> localBinaries = binaryStore.getForPath(requestPath);

        // Do we have a single result whose path matches the request exactly?

        final RawBinary exactMatch = exactMatch(localBinaries, requestPath);
        if (exactMatch != null) {
          return createStreamResponse(exactMatch);
        }

        // If not, contact the remote source and store whatever it has

        try {
          final ComponentRequest path = new ComponentRequest(ImmutableMap.of("path", requestPath));

          // Here we presume we're getting Component back, since we don't actually use the component metadata
          final Iterable<ComponentEnvelope<RawComponent>> envelopes = sourceProvider.get().fetchComponents(path);

          for (ComponentEnvelope<RawComponent> envelope : envelopes) {
            for (Asset asset : envelope.getAssets()) {
              binaryStore.create(requestPath, asset.getContentType(), asset.openStream());
            }
          }
        }
        catch (IOException e) {
          Throwables.propagate(e);
        }

        return context.proceed();

      default:
        return Responses.methodNotAllowed();
    }
  }

  ViewResponse createStreamResponse(final RawBinary binary) {
    return Responses
        .streamResponse(binary.getInputStream(), binary.getContentType(), binary.getModifiedDate().toDate());
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

  private String ensureLeadingSlash(String path) {
    return path.startsWith("/") ? path : "/" + path;
  }
}
