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
package org.sonatype.nexus.repository.raw.internal;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.httpbridge.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class RawCachingHandler
    implements Handler
{
  private final Repository repository;

  public RawCachingHandler(final Repository repository) {
    this.repository = checkNotNull(repository);
  }

  @Override
  public Response handle(final Context context) throws Exception {

    final Object remoteRequest = context.getAttributes().get("remoteRequest");

    final RawCacheFacet rawCacheFacet = repository.facet(RawCacheFacet.class);

    final PayloadResponse cachedResponse = rawCacheFacet.get(remoteRequest);

    if (isFresh(cachedResponse)) {
      return cachedResponse;
    }

    // talk to the remote - if we get something good, cache it and return it instead
    final Response response = context.proceed();

    if (containsGoodPayload(response)) {
      final PayloadResponse stored = rawCacheFacet.store(cachedResponse);
      return stored;
    }
    else {
      recordNotFound(remoteRequest);

      if (cachedResponse != null) {
        return cachedResponse;
      }

      return HttpResponses.notFound();
    }
  }

  private boolean isFresh(PayloadResponse response) {
    return true;
  }

  private void recordNotFound(Object remoteRequest) {
  }

  private boolean containsGoodPayload(Response response) {
    return response.getStatus().isSuccessful();
  }
}
