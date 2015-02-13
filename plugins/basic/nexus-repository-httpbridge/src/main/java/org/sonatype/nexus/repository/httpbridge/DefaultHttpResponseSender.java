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

import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.io.ByteStreams;

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

    // add request headers
    for (Map.Entry<String,String> header : response.getHeaders()) {
      httpResponse.addHeader(header.getKey(), header.getValue());
    }

    // write payload details if we have one
    if (response instanceof PayloadResponse) {
      Payload payload = ((PayloadResponse)response).getPayload();
      log.trace("Attaching payload: {}", payload);

      if (payload.getContentType() != null) {
        httpResponse.setContentType(payload.getContentType());
      }
      httpResponse.setContentLength((int) payload.getSize()); // HACK: Upgrade to servlet 3.1 to use proper long values

      try (InputStream input = payload.openInputStream(); OutputStream output = httpResponse.getOutputStream()) {
        ByteStreams.copy(input, output);
      }
    }

    // Set or send status
    Status status = response.getStatus();
    if (status.isSuccessful()) {
      httpResponse.setStatus(status.getCode(), status.getMessage());
    }
    else {
      httpResponse.sendError(status.getCode(), status.getMessage());
    }
  }
}
