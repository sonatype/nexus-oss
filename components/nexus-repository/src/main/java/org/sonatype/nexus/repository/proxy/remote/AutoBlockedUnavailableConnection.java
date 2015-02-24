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
package org.sonatype.nexus.repository.proxy.remote;

import java.io.IOException;
import java.net.URI;

import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.sisu.goodies.common.Time;

import org.joda.time.DateTime;

/**
 * @since 3.0
 */
public class AutoBlockedUnavailableConnection
    extends RemoteConnectionSupport
{

  private DateTime blockedUntil;

  private IOException exception;

  public AutoBlockedUnavailableConnection(final ProxyFacetSupport proxyFacet,
                                          final IOException e)
  {
    super(proxyFacet);
    this.exception = e;
    blockedUntil = DateTime.now().plus(increment());
  }

  @Override
  public GetResult get(final URI uri) throws IOException {
    if (blockedUntil.isBeforeNow()) {
      try {
        GetResult result = super.get(uri);
        proxyFacet.setRemoteConnection(new AutoBlockingAvailableConnection(proxyFacet));
        return result;
      }
      catch (IOException e) {
        exception = e;
        if (isRemoteUnavailable(e)) {
          blockedUntil = DateTime.now().plus(increment());
        }
        else {
          proxyFacet.setRemoteConnection(new AutoBlockingAvailableConnection(proxyFacet));
        }
      }
    }
    throw exception;
  }

  @Override
  public RemoteConnectionStatus getStatus() {
    return new RemoteConnectionStatus("Remote Automatically Blocked and Unavailable", exception.getMessage());
  }

  private long increment() {
    return Time.minutes(1).toMillis();
  }


}
