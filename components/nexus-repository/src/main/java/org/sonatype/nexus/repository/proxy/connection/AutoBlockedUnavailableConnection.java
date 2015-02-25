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
import java.net.URI;

import org.sonatype.nexus.common.sequence.FibonacciNumberSequence;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.sisu.goodies.common.Time;

/**
 * @since 3.0
 */
public class AutoBlockedUnavailableConnection
    extends RemoteConnectionSupport
{

  private IOException cause;

  public AutoBlockedUnavailableConnection(final ProxyFacetSupport proxyFacet,
                                          final IOException cause)
  {
    // TODO shall the 20 seconds be configurable?
    super(proxyFacet, new FibonacciNumberSequence(Time.seconds(20).toMillis()));
    this.cause = cause;
  }

  @Override
  public GetResult get(final URI uri) throws IOException {
    if (validUntil().isBeforeNow()) {
      try {
        GetResult result = super.get(uri);
        proxyFacet.setRemoteConnection(new AutoBlockingAvailableConnection(proxyFacet));
        return result;
      }
      catch (IOException e) {
        cause = e;
        if (!isRemoteUnavailable(e)) {
          proxyFacet.setRemoteConnection(new AutoBlockingAvailableConnection(proxyFacet));
        }
      }
    }
    throw cause;
  }

  @Override
  public RemoteConnectionStatus getStatus() {
    return new RemoteConnectionStatus(
        "Remote Automatically Blocked and Unavailable", getReason(cause), validUntil()
    );
  }

}
