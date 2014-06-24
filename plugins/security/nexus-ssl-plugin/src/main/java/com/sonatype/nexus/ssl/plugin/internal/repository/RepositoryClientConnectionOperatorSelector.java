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
package com.sonatype.nexus.ssl.plugin.internal.repository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.nexus.ssl.model.TrustStoreKey;
import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.nexus.ssl.plugin.internal.TrustStoreKeyClientConnectionOperatorSelector;

import org.sonatype.nexus.httpclient.HttpClientFactory;
import org.sonatype.nexus.httpclient.SSLContextSelector;
import org.sonatype.nexus.proxy.repository.Repository;

import org.apache.http.protocol.HttpContext;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.nexus.ssl.model.RepositoryTrustStoreKey.repositoryTrustStoreKey;

/**
 * An {@link SSLContextSelector} that will make use of Nexus SSL Trust Store depending on repository
 * configuration.
 *
 * @since ssl 1.0
 */
@Named
@Singleton
public class RepositoryClientConnectionOperatorSelector
    extends TrustStoreKeyClientConnectionOperatorSelector
{

  @Inject
  public RepositoryClientConnectionOperatorSelector(final TrustStore trustStore) {
    super(trustStore);
  }

  @Override
  protected TrustStoreKey getTrustStoreKey(final HttpContext context) {
    final Object ctxKey = checkNotNull(context, "HttpContext/context").getAttribute(
        HttpClientFactory.HTTP_CTX_KEY_REPOSITORY
    );
    if (ctxKey != null && ctxKey instanceof Repository) {
      return repositoryTrustStoreKey(((Repository) ctxKey).getId());
    }
    return null;
  }

}
