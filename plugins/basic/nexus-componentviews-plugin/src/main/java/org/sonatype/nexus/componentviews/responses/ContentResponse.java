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
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.componentviews.ViewResponse;

import com.google.common.io.ByteStreams;

/**
 * A response sending an expected bit of content.
 *
 * @since 3.0
 */
public abstract class ContentResponse
    implements ViewResponse
{
  protected final String contentType;

  private final Date modifiedDate;

  protected ContentResponse(final String contentType, final Date modifiedDate) {
    this.contentType = contentType;
    this.modifiedDate = modifiedDate;
  }

  protected abstract InputStream getInputStream();

  @Override
  public void send(final HttpServletResponse response) throws IOException {
    response.setStatus(200);

    if (modifiedDate != null) {
      response.setDateHeader("Last-Modified", modifiedDate.getTime());
    }
    response.setContentType(contentType);

    ByteStreams.copy(getInputStream(), response.getOutputStream());
  }
}
