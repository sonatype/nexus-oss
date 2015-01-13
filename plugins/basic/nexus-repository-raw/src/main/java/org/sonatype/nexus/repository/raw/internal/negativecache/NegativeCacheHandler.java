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
package org.sonatype.nexus.repository.raw.internal.negativecache;

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.httpbridge.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * @since 3.0
 */
public class NegativeCacheHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    assert "GET".equals(context.getRequest().getAction());

    NegativeCacheKey key = cacheKey(context);

    final NegativeCacheFacet nfc = context.getRepository().facet(NegativeCacheFacet.class);

    if (nfc.isNotFound(key)) {
      return HttpResponses.notFound();
    }

    final Response response = context.proceed();

    if (indicatesNotFound(response)) {
      nfc.cacheNotFound(key);
      return HttpResponses.notFound();
    }

    nfc.uncacheNotFound(key);
    return response;
  }

  /**
   * Does this {@link Response} represent a 'not found' situation?
   */
  private boolean indicatesNotFound(final Response response) {
    return false;
  }

  private NegativeCacheKey cacheKey(final Context context) {
    return context.getRepository().facet(NegativeCacheKeyProvider.class).cacheKey(context);
  }
}
