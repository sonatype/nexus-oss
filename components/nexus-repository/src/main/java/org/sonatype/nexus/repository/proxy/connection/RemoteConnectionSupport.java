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
package org.sonatype.nexus.repository.proxy.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.sonatype.nexus.common.sequence.ConstantNumberSequence;
import org.sonatype.nexus.common.sequence.NumberSequence;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;
import org.sonatype.nexus.repository.view.payloads.HttpEntityPayload;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.common.Time;

import com.google.common.io.ByteStreams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public abstract class RemoteConnectionSupport
    extends ComponentSupport
    implements RemoteConnection
{
  protected final ProxyFacetSupport proxyFacet;

  private final NumberSequence expirationSequence;

  private DateTime validUntil;

  public RemoteConnectionSupport(final ProxyFacetSupport proxyFacet) {
    // TODO shall the 5 minutes be configurable?
    this(proxyFacet, new ConstantNumberSequence(Time.minutes(5).toMillis()));
  }

  public RemoteConnectionSupport(final ProxyFacetSupport proxyFacet,
                                 final NumberSequence expirationSequence)
  {
    this.proxyFacet = checkNotNull(proxyFacet);
    this.expirationSequence = checkNotNull(expirationSequence);
    this.validUntil = DateTime.now().plus(expirationSequence.peek());
  }

  public GetResult get(final URI uri) throws IOException {
    try {
      HttpClient client = proxyFacet.getRepository().facet(HttpClientFacet.class).getHttpClient();

      HttpGet request = new HttpGet(uri);
      log.debug("Fetching: {}", request);

      HttpResponse response = client.execute(request);
      log.debug("Response: {}", response);

      StatusLine status = response.getStatusLine();
      log.debug("Status: {}", status);

      Payload payload = null;
      if (status.getStatusCode() == HttpStatus.SC_OK) {
        HttpEntity entity = response.getEntity();
        try {
          log.debug("Entity: {}", entity);
          final HttpEntityPayload httpEntityPayload = new HttpEntityPayload(response, entity);

          payload = readFully(httpEntityPayload);
        }
        finally {
          EntityUtils.consume(entity);
        }
      }
      return new GetResult(status.getStatusCode(), payload);
    }
    finally {
      prolongValidity();
    }
  }

  protected boolean isRemoteUnavailable(final Exception e) {
    if (e instanceof ConnectionPoolTimeoutException) {
      return false;
    }
    return true;
  }

  private void prolongValidity() {
    validUntil = DateTime.now().plus(expirationSequence.next());
  }

  protected DateTime validUntil() {
    return validUntil;
  }

  protected String getReason(final Exception e) {
    if (e instanceof SSLPeerUnverifiedException) {
      return "Untrusted Remote";
    }
    return e.getMessage();
  }

  /**
   * Read an incoming Payload into memory.
   */
  private Payload readFully(final Payload payload) throws IOException {
    try (InputStream stream = payload.openInputStream()) {
      return new BytesPayload(ByteStreams.toByteArray(stream), payload.getContentType());
    }
  }
}
