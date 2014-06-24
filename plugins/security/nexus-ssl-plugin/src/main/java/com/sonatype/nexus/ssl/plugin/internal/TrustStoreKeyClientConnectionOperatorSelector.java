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
package com.sonatype.nexus.ssl.plugin.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;

import com.sonatype.nexus.ssl.model.TrustStoreKey;
import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.nexus.httpclient.SSLContextSelector;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.http.protocol.HttpContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link SSLContextSelector} that will make use of Nexus SSL Trust Store depending on presence of
 * {@link TrustStoreKey#HTTP_CTX_KEY}.
 *
 * @since 2.6
 */
@Named
@Singleton
public class TrustStoreKeyClientConnectionOperatorSelector
    extends ComponentSupport
    implements SSLContextSelector
{

  private final TrustStore trustStore;

  @Inject
  public TrustStoreKeyClientConnectionOperatorSelector(final TrustStore trustStore) {
    this.trustStore = checkNotNull(trustStore);
  }

  @Override
  public SSLContext select(final HttpContext context) {
    final TrustStoreKey ctxKey = getTrustStoreKey(context);
    if (ctxKey != null) {
      final SSLContext sslContext = trustStore.getSSLContextFor(ctxKey);
      if (sslContext != null) {
        log.debug("{} is using own Nexus SSL Trust Store for accessing remote", ctxKey);
        return sslContext;
      }
      log.debug("{} is using a JVM Trust Store for accessing remote", ctxKey);
    }
    return null;
  }

  protected TrustStoreKey getTrustStoreKey(final HttpContext context) {
    final Object ctxKey = checkNotNull(context, "HttpContext/context").getAttribute(
        TrustStoreKey.HTTP_CTX_KEY
    );
    if (ctxKey != null && ctxKey instanceof TrustStoreKey) {
      return (TrustStoreKey) ctxKey;
    }
    return null;
  }

}
