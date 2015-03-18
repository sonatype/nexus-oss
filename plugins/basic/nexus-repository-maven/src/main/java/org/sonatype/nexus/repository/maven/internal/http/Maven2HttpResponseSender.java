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
package org.sonatype.nexus.repository.maven.internal.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.httpbridge.HttpResponseSender;
import org.sonatype.nexus.repository.maven.internal.MavenPayload;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2Format;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.hash.HashCode;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;

/**
 * Maven2 {@link HttpResponseSender}.
 *
 * @since 3.0
 */
@Named(Maven2Format.NAME)
@Singleton
public class Maven2HttpResponseSender
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
        httpResponse.setContentLengthLong(payload.getSize());

        if (payload instanceof MavenPayload) {
          final MavenPayload mavenPayload = (MavenPayload) payload;
          httpResponse.setDateHeader(
              HttpHeaders.LAST_MODIFIED,
              mavenPayload.getLastModified().getMillis()
          );
          final HashCode sha1HashCode = mavenPayload.getHashCodes().get(HashAlgorithm.SHA1);
          if (sha1HashCode != null) {
            httpResponse.setHeader(
                HttpHeaders.ETAG,
                "{SHA1{" + sha1HashCode + "}}"
            );
          }
        }

        // TODO: HEAD
        // if (!HttpMethods.HEAD.equals(httpRequest.getMethod())) {
        try (InputStream input = payload.openInputStream(); OutputStream output = httpResponse.getOutputStream()) {
          ByteStreams.copy(input, output);
        }
        // }
      }
    }
    else {
      httpResponse.sendError(status.getCode(), status.getMessage());
    }
  }
}
