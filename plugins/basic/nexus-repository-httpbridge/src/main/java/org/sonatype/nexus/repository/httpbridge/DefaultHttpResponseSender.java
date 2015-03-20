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
import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.io.StreamSupport;
import org.sonatype.nexus.common.property.SystemPropertiesHelper;
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.nexus.repository.view.payloads.DecoratedPayload;
import org.sonatype.nexus.repository.view.payloads.PayloadCaching;
import org.sonatype.nexus.repository.view.payloads.PayloadHashes;
import org.sonatype.nexus.repository.view.payloads.PayloadHeaders;
import org.sonatype.nexus.repository.view.payloads.PayloadTimes;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Iterables;
import com.google.common.hash.HashCode;
import com.google.common.net.HttpHeaders;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import static com.google.common.net.HttpHeaders.CACHE_CONTROL;
import static com.google.common.net.HttpHeaders.EXPIRES;
import static com.google.common.net.HttpHeaders.PRAGMA;

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
  /**
   * Buffer size to be used when pushing content to the {@link HttpServletResponse#getOutputStream()} stream.
   *
   * Default is Jetty default or 8KB.
   */
  private static final int BUFFER_SIZE = SystemPropertiesHelper
      .getInteger(DefaultHttpResponseSender.class.getName() + ".BUFFER_SIZE", -1);

  @Override
  public void send(final @Nullable Request request, final Response response, final HttpServletResponse httpResponse)
      throws ServletException, IOException
  {
    log.trace("Sending response: {}", response);

    handleResponseHeaders(response, httpResponse);

    // add status followed by payload if we have one
    Status status = response.getStatus();
    if (status.isSuccessful() || response instanceof PayloadResponse) {
      httpResponse.setStatus(status.getCode());
      if (response instanceof PayloadResponse) {
        final PayloadResponse payloadResponse = (PayloadResponse) response;
        log.trace("Attaching payload: {}", payloadResponse.getPayload());
        handlePayloadHeaders(payloadResponse.getPayload(), httpResponse);

        if (request != null && !HttpMethods.HEAD.equals(request.getAction())) {
          try (InputStream input = payloadResponse.getPayload().openInputStream()) {
            sendContent(input, httpResponse);
          }
        }
      }
    }
    else {
      httpResponse.sendError(status.getCode(), status.getMessage());
    }
  }

  /**
   * Set HTTP response headers based on {@link Response}.
   */
  protected void handleResponseHeaders(final Response response,
                                       final HttpServletResponse httpResponse)
  {
    // add response headers
    for (Map.Entry<String, String> header : response.getHeaders()) {
      httpResponse.addHeader(header.getKey(), header.getValue());
    }
  }

  /**
   * Set HTTP response headers based on {@link PayloadResponse}.
   */
  protected void handlePayloadHeaders(final Payload payload,
                                      final HttpServletResponse httpResponse)
  {
    if (payload.getContentType() != null) {
      httpResponse.setContentType(payload.getContentType());
    }
    if (payload.getSize() != Payload.UNKNOWN_SIZE) {
      httpResponse.setContentLengthLong(payload.getSize());
    }

    if (payload instanceof DecoratedPayload) {
      final DecoratedPayload decoratedPayload = (DecoratedPayload) payload;
      handleDecoratedPayloadHeaders(decoratedPayload, httpResponse);
    }
  }

  /**
   * Set HTTP response headers based on {@link DecoratedPayload}.
   */
  protected void handleDecoratedPayloadHeaders(final DecoratedPayload decoratedPayload,
                                               final HttpServletResponse httpResponse)
  {
    // extra payload headers
    final PayloadHeaders payloadHeaders = decoratedPayload.decorator(PayloadHeaders.class);
    if (payloadHeaders != null) {
      for (Map.Entry<String, String> header : payloadHeaders.getHeaders()) {
        addHeaderIfNotPresent(
            httpResponse,
            header.getKey(),
            header.getValue()
        );
      }
    }
    // times
    final PayloadTimes payloadTimes = decoratedPayload.decorator(PayloadTimes.class);
    if (payloadTimes != null) {
      addHeaderIfNotPresent(
          httpResponse,
          HttpHeaders.LAST_MODIFIED,
          payloadTimes.getLastModified()
      );
    }
    // ETag using hashes
    final PayloadHashes payloadHashes = decoratedPayload.decorator(PayloadHashes.class);
    if (payloadHashes != null) {
      final HashAlgorithm hashAlgorithm = Iterables.getFirst(payloadHashes.getHashAlgorithms(), null);
      if (hashAlgorithm != null) {
        final HashCode hashCode = payloadHashes.getHashCodes().get(hashAlgorithm);
        if (hashCode != null) {
          addHeaderIfNotPresent(
              httpResponse,
              HttpHeaders.ETAG,
              "\"" + hashCode + "\""
          );
        }
      }
    }
    // Cache control
    final PayloadCaching payloadCaching = decoratedPayload.decorator(PayloadCaching.class);
    if (payloadCaching != null) {
      if (!payloadCaching.isCacheable()) {
        httpResponse.setHeader(PRAGMA, "no-cache"); // HTTP/1.0
        httpResponse.setHeader(CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate"); // HTTP/1.1
        httpResponse.addHeader(CACHE_CONTROL, "post-check=0, pre-check=0"); // MS IE
        httpResponse.setHeader(EXPIRES, "0"); // No caching on Proxies in between client and Nexus
      }
      else {
        if (payloadCaching.getValidUntil() != null) {
          final DateTime now = DateTime.now();
          final Seconds seconds = Seconds.secondsBetween(now, payloadCaching.getValidUntil());
          if (seconds.getSeconds() > 0) {
            addHeaderIfNotPresent(
                httpResponse,
                CACHE_CONTROL,
                "max-age=" + seconds
            ); // HTTP/1.1, safest to do
          }
        }
      }
    }
  }

  /**
   * Adds header to HTTP response if not already present.
   */
  protected void addHeaderIfNotPresent(final HttpServletResponse httpResponse, final String name, final Object value) {
    if (!httpResponse.containsHeader(name)) {
      if (value instanceof DateTime) {
        httpResponse.setDateHeader(name, ((DateTime) value).getMillis());
      }
      else if (value instanceof Date) {
        httpResponse.setDateHeader(name, ((Date) value).getTime());
      }
      else {
        httpResponse.setHeader(name, String.valueOf(value));
      }
    }
  }

  /**
   * Sends content by copying all bytes from the input stream to the response setting the preferred buffer
   * size. At the end, it flushes response buffer. Passed in {@link InputStream} is fully consumed and closed.
   * The passed in {@link HttpServletResponse} after this call returns is committed and flushed.
   */
  protected void sendContent(final InputStream input, final HttpServletResponse response) throws IOException {
    int bufferSize = BUFFER_SIZE;
    if (bufferSize < 1) {
      // if no user override, ask container for bufferSize
      bufferSize = response.getBufferSize();
      if (bufferSize < 1) {
        bufferSize = 8192;
        response.setBufferSize(bufferSize);
      }
    }
    else {
      // user override present, tell container what buffer size we'd like
      response.setBufferSize(bufferSize);
    }
    try (final InputStream from = input; final OutputStream to = response.getOutputStream()) {
      StreamSupport.copy(from, to, bufferSize);
      response.flushBuffer();
    }
  }

}
