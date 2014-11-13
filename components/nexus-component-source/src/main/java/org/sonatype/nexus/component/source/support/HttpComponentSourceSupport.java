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
package org.sonatype.nexus.component.source.support;

import java.io.IOException;

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.source.ComponentSource;
import org.sonatype.nexus.component.source.ComponentSourceId;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A base class for HTTP-based component sources.
 *
 * @since 3.0
 */
public abstract class HttpComponentSourceSupport<T extends Component>
    extends ComponentSupport
    implements ComponentSource<T>
{
  private final ComponentSourceId sourceId;

  private final CloseableHttpClient httpClient;

  public HttpComponentSourceSupport(final ComponentSourceId sourceId) {
    this.sourceId = checkNotNull(sourceId);
    this.httpClient = HttpClients.createDefault();
  }

  @Override
  public ComponentSourceId getId() {
    return sourceId;
  }

  protected CloseableHttpResponse get(final String uri) throws IOException {
    final HttpGet httpGet = new HttpGet(uri);
    return httpClient.execute(httpGet);
  }

  protected void checkMimeType(final CloseableHttpResponse response, final String allowableContentType)
      throws IOException
  {
    final Header contentTypeHeader = response.getEntity().getContentType();
    if (contentTypeHeader == null) {
      throw new IOException("Response content type not specified.");
    }
    final String mimeType = ContentType.parse(contentTypeHeader.getValue()).getMimeType();

    if (!allowableContentType.equals(mimeType)) {
      throw new IOException("Unexpected response mime type " + mimeType);
    }
  }

  protected void checkStatusCode(final CloseableHttpResponse response, final int allowableStatusCode) throws IOException
  {
    final int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != allowableStatusCode) {
      throw new IOException("Wrong status code.");
    }
  }
}
