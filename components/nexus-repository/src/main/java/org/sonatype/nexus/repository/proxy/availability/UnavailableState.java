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
package org.sonatype.nexus.repository.proxy.availability;

import java.io.IOException;
import java.net.URI;

import org.sonatype.nexus.repository.proxy.FetchResult;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;

/**
 * @since 3.0
 */
public class UnavailableState
    extends AvailabilityStateSupport
{

  private IOException exception;

  public UnavailableState(final ProxyFacetSupport proxyFacet,
                          final IOException e)
  {
    super(proxyFacet);
    this.exception = e;
  }

  @Override
  public FetchResult fetch(final URI uri) throws IOException {
    try {
      FetchResult result = super.fetch(uri);
      proxyFacet.setAvailabilityState(new AvailableState(proxyFacet));
      return result;
    }
    catch (IOException e) {
      exception = e;
      throw e;
    }
  }

  @Override
  public String getStatus() {
    return "Remote Unavailable";
  }

  @Override
  public String getStatusReason() {
    return exception.getMessage();
  }

}
