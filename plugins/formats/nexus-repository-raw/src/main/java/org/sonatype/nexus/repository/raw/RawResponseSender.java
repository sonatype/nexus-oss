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
package org.sonatype.nexus.repository.raw;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.repository.httpbridge.HttpResponseSender;
import org.sonatype.nexus.repository.view.Headers;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.io.ByteStreams;

/**
 * Converts {@link Response}s coming from the raw format into {@link HttpServletResponse}s.
 *
 * @since 3.0
 */
public class RawResponseSender
    extends ComponentSupport
    implements HttpResponseSender
{
  @Override
  public void send(final Response response, final HttpServletResponse httpResponse)
      throws ServletException, IOException
  {
    final Status status = response.getStatus();
    if (!status.isSuccessful()) {
      httpResponse.sendError(status.getCode(), status.getMessage());
      return;
    }

    httpResponse.setStatus(status.getCode());

    // TODO: Map headers
    final Headers headers = response.getHeaders();

    if (response instanceof PayloadResponse) {
      PayloadResponse payloadResponse = (PayloadResponse) response;

      final Payload payload = payloadResponse.getPayload();

      httpResponse.setHeader("Content-Length", String.valueOf(payload.getSize()));
      httpResponse.setHeader("Content-Type", payload.getContentType());

      try (InputStream payloadStream = payload.openInputStream()) {
        OutputStream output = httpResponse.getOutputStream();
        ByteStreams.copy(payloadStream, output);
        output.flush();
      }
    }
  }
}
