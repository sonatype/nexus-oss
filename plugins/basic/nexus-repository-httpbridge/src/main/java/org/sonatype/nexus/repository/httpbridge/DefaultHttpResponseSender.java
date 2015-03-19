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
package org.sonatype.nexus.repository.httpbridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.nexus.repository.view.payloads.DecoratedPayload;
import org.sonatype.nexus.repository.view.payloads.PayloadHashes;
import org.sonatype.nexus.repository.view.payloads.PayloadHeaders;
import org.sonatype.nexus.repository.view.payloads.PayloadTimes;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Iterables;
import com.google.common.hash.HashCode;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

/**
 * Default {@link HttpResponseSender}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class DefaultHttpResponseSender
    extends ComponentSupport
    implements HttpResponseSender
{
  @Override
  public void send(final Response response, final HttpServletResponse httpResponse)
      throws ServletException, IOException
  {
    log.trace("Sending response: {}", response);

    // add response headers
    for (Map.Entry<String, String> header : response.getHeaders()) {
      httpResponse.addHeader(header.getKey(), header.getValue());
    }

    // add status followed by payload if we have one
    Status status = response.getStatus();
    if (status.isSuccessful() || response instanceof PayloadResponse) {
      httpResponse.setStatus(status.getCode());
      if (response instanceof PayloadResponse) {
        Payload payload = ((PayloadResponse) response).getPayload();
        log.trace("Attaching payload: {}", payload);

        if (payload.getContentType() != null) {
          httpResponse.setContentType(payload.getContentType());
        }
        if (payload.getSize() != Payload.UNKNOWN_SIZE) {
          httpResponse.setContentLengthLong(payload.getSize());
        }

        if (payload instanceof DecoratedPayload) {
          final DecoratedPayload decoratedPayload = (DecoratedPayload) payload;
          final PayloadHeaders payloadHeaders = decoratedPayload.decorator(PayloadHeaders.class);
          if (payloadHeaders != null) {
            for (Map.Entry<String, String> header : payloadHeaders.getHeaders()) {
              if (!httpResponse.containsHeader(header.getKey())) {
                httpResponse.addHeader(header.getKey(), header.getValue());
              }
            }
          }
          final PayloadTimes payloadTimes = decoratedPayload.decorator(PayloadTimes.class);
          if (payloadTimes != null && !httpResponse.containsHeader(HttpHeaders.LAST_MODIFIED)) {
            httpResponse.setDateHeader(
                HttpHeaders.LAST_MODIFIED,
                payloadTimes.getLastModified().getMillis()
            );
          }
          final PayloadHashes payloadHashes = decoratedPayload.decorator(PayloadHashes.class);
          if (payloadHashes != null) {
            final HashAlgorithm hashAlgorithm = Iterables.getFirst(payloadHashes.getHashAlgorithms(), null);
            if (hashAlgorithm != null) {
              final HashCode hashCode = payloadHashes.getHashCodes().get(hashAlgorithm);
              if (hashCode != null && !httpResponse.containsHeader(HttpHeaders.ETAG)) {
                httpResponse.setHeader(
                    HttpHeaders.ETAG,
                    hashCode.toString()
                );
              }
            }
          }
        }

        // TODO: Do not do this below is verb is HEAD
        try (InputStream input = payload.openInputStream(); OutputStream output = httpResponse.getOutputStream()) {
          ByteStreams.copy(input, output);
        }
      }
    }
    else {
      httpResponse.sendError(status.getCode(), status.getMessage());
    }
  }
}
