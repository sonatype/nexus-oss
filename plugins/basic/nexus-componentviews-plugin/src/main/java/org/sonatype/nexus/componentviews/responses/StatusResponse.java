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
package org.sonatype.nexus.componentviews.responses;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.componentviews.ViewResponse;

/**
 * A response primarily built around an HTTP return code.
 *
 * @since 3.0
 */
public class StatusResponse
    implements ViewResponse
{
  private final int statusCode;

  private final String errorMessage;

  public StatusResponse(final int statusCode, final String errorMessage) {
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
  }

  public StatusResponse(final int statusCode) {
    this(statusCode, null);
  }

  @Override
  public void send(final HttpServletResponse response) throws IOException {
    if (errorMessage != null) {
      response.sendError(statusCode, errorMessage);
    }
    else {
      response.sendError(statusCode);
    }
  }
}
