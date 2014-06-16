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

import javax.net.ssl.SSLContext;

import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.nexus.httpclient.HttpClientFactory;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

import static com.sonatype.nexus.ssl.model.RepositoryTrustStoreKey.repositoryTrustStoreKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link RepositoryClientConnectionOperatorSelector} UTs.
 *
 * @since ssl 1.0
 */
public class RepositoryClientConnectionOperatorSelectorTest
    extends TestSupport
{

  /**
   * Verify that an {@link ClientConnectionOperator} is returned when trust store is enabled for repository
   * (repository present in context under {@link HttpClientFactory#HTTP_CTX_KEY_REPOSITORY} key).
   */
  @Test
  public void operatorReturnedWhenTrustStoreEnabled()
      throws Exception
  {
    final Repository repository = mock(Repository.class);
    when(repository.getId()).thenReturn("foo");

    final TrustStore trustStore = mock(TrustStore.class);
    when(trustStore.getSSLContextFor(repositoryTrustStoreKey("foo"))).thenReturn(SSLContext.getDefault());

    final HttpContext httpContext = mock(HttpContext.class);
    when(httpContext.getAttribute(HttpClientFactory.HTTP_CTX_KEY_REPOSITORY)).thenReturn(repository);

    final RepositoryClientConnectionOperatorSelector underTest = new RepositoryClientConnectionOperatorSelector(
        trustStore
    );
    final SSLContext context = underTest.select(httpContext);

    assertThat(context, is(notNullValue()));
  }

  /**
   * Verify that an no {@link ClientConnectionOperator} is returned when trust store is not enabled for repository
   * (repository present in context under {@link HttpClientFactory#HTTP_CTX_KEY_REPOSITORY} key).
   */
  @Test
  public void noOperatorReturnedWhenTrustStoreIsNotEnabled()
      throws Exception
  {
    final Repository repository = mock(Repository.class);
    when(repository.getId()).thenReturn("foo");

    final TrustStore trustStore = mock(TrustStore.class);
    when(trustStore.getSSLContextFor(repositoryTrustStoreKey("foo"))).thenReturn(null);

    final HttpContext httpContext = mock(HttpContext.class);
    when(httpContext.getAttribute(HttpClientFactory.HTTP_CTX_KEY_REPOSITORY)).thenReturn(repository);

    final RepositoryClientConnectionOperatorSelector underTest = new RepositoryClientConnectionOperatorSelector(
        trustStore
    );
    final SSLContext context = underTest.select(httpContext);

    assertThat(context, is(nullValue()));
  }

  /**
   * Verify that an no {@link ClientConnectionOperator} is returned when trust store is enabled but no repository
   * present in context under {@link HttpClientFactory#HTTP_CTX_KEY_REPOSITORY} key.
   */
  @Test
  public void noOperatorReturnedWhenTrustStoreIsEnabledButNoRepositoryInHttpContext()
      throws Exception
  {
    final Repository repository = mock(Repository.class);
    when(repository.getId()).thenReturn("foo");

    final TrustStore trustStore = mock(TrustStore.class);
    when(trustStore.getSSLContextFor(repositoryTrustStoreKey("foo"))).thenReturn(SSLContext.getDefault());

    final HttpContext httpContext = mock(HttpContext.class);

    final RepositoryClientConnectionOperatorSelector underTest = new RepositoryClientConnectionOperatorSelector(
        trustStore
    );
    final SSLContext context = underTest.select(httpContext);

    assertThat(context, is(nullValue()));
  }

  /**
   * Verify that an no {@link ClientConnectionOperator} is returned when trust store is enabled but under
   * {@link HttpClientFactory#HTTP_CTX_KEY_REPOSITORY} key is not a repository.
   */
  @Test
  public void noOperatorReturnedWhenTrustStoreIsEnabledButHttpContextContainsAnotherTypeUnderKey()
      throws Exception
  {
    final Repository repository = mock(Repository.class);
    when(repository.getId()).thenReturn("foo");

    final TrustStore trustStore = mock(TrustStore.class);
    when(trustStore.getSSLContextFor(repositoryTrustStoreKey("foo"))).thenReturn(SSLContext.getDefault());

    final HttpContext httpContext = mock(HttpContext.class);
    when(httpContext.getAttribute(HttpClientFactory.HTTP_CTX_KEY_REPOSITORY)).thenReturn(new Object());

    final RepositoryClientConnectionOperatorSelector underTest = new RepositoryClientConnectionOperatorSelector(
        trustStore
    );
    final SSLContext context = underTest.select(httpContext);

    assertThat(context, is(nullValue()));
  }

}