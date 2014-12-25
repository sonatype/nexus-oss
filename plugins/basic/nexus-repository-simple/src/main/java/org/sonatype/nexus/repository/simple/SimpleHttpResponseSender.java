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

package org.sonatype.nexus.repository.simple;

import java.io.IOException;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.repository.httpbridge.HttpResponseSender;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * Simple {@link HttpResponseSender}.
 *
 * @since 3.0
 */
@Named(SimpleFormat.NAME)
@Singleton
public class SimpleHttpResponseSender
  extends ComponentSupport
  implements HttpResponseSender
{
  @Override
  public void send(final Response response, final HttpServletResponse httpResponse)
      throws ServletException, IOException
  {
    // TODO: attributes?

    for (Map.Entry<String,String> header : response.getHeaders()) {
      httpResponse.addHeader(header.getKey(), header.getValue());
    }
    // TODO: standard headers

    // TODO: content-type
    // TODO: content-length
    // TODO: write payload

    Status status = response.getStatus();
    if (status.isSuccessful()) {
      httpResponse.setStatus(status.getCode(), status.getMessage());
    }
    else {
      httpResponse.sendError(status.getCode(), status.getMessage());
    }
  }
}
