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
package org.sonatype.nexus.httpclient;

import com.google.common.annotations.VisibleForTesting;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Apache {@link HttpClient} factory.
 *
 * @since 2.2
 */
public interface HttpClientFactory
{
  // FIXME: Move repository specific semantics out of this class

  /**
   * HTTP context key of (usually proxy) repository on who's behalf request is made. To be used with
   * {@link HttpClient#execute(HttpUriRequest, HttpContext)} method of {@link HttpClient} instance got from this
   * provider. Example code snippet:
   *
   * <pre>
   * final HttpGet httpRequest = new HttpGet( proxyRepository.getRemoteUrl() );
   * final BasicHttpContext httpContext = new BasicHttpContext();
   * httpContext.setAttribute( HTTP_CTX_KEY_REPOSITORY, proxyRepository );
   * final HttpResponse httpResponse = httpClient.execute( httpRequest, httpContext );
   * </pre>
   *
   * @since 2.4
   */
  String HTTP_CTX_KEY_REPOSITORY = HttpClientFactory.class.getName() + ".repository";

  HttpClient create();

  /**
   * Allows customization of {@link HttpClient} via {@link Builder}.
   */
  public static interface Customizer
  {
    void customize(Builder builder);
  }

  /**
   * Create a new customized client.
   */
  HttpClient create(Customizer customizer);

  /**
   * Apply customization and return client builder.
   */
  Builder prepare(Customizer customizer);

  /**
   * Helper to build new {@link HttpClient} instances.
   */
  public static class Builder
  {
    private final HttpClientBuilder httpClientBuilder;

    private final ConnectionConfig.Builder connectionConfigBuilder;

    private final SocketConfig.Builder socketConfigBuilder;

    private final RequestConfig.Builder requestConfigBuilder;

    private final CredentialsProvider credentialsProvider;

    private String userAgent;

    private boolean credentialsProviderAltered;

    public Builder() {
      this(HttpClientBuilder.create(),
          ConnectionConfig.copy(ConnectionConfig.DEFAULT),
          SocketConfig.copy(SocketConfig.DEFAULT),
          RequestConfig.copy(RequestConfig.DEFAULT)
      );
    }

    public Builder(final HttpClientBuilder httpClientBuilder,
            final ConnectionConfig.Builder connectionConfigBuilder,
            final SocketConfig.Builder socketConfigBuilder,
            final RequestConfig.Builder requestConfigBuilder)
    {
      this.httpClientBuilder = checkNotNull(httpClientBuilder);
      this.connectionConfigBuilder = checkNotNull(connectionConfigBuilder);
      this.socketConfigBuilder = checkNotNull(socketConfigBuilder);
      this.requestConfigBuilder = checkNotNull(requestConfigBuilder);
      this.credentialsProvider = new BasicCredentialsProvider();
      this.credentialsProviderAltered = false;
    }

    /**
     * Returns the {@link HttpClientBuilder}.
     *
     * <p/>Word of warning about method {@link HttpClientBuilder#setDefaultCredentialsProvider(CredentialsProvider)}:
     * by design, this method replaces any previously set credentials provider, and, there is no getter for it to
     * inspect any existing previously set value. Hence, be careful when using this method! Recommended way to
     * set multiple credentials from multiple places is to use {@link #setCredentials(AuthScope, Credentials)}.
     */
    public HttpClientBuilder getHttpClientBuilder() {
      return httpClientBuilder;
    }

    /**
     * Sets the {@link Credentials credentials} for the given authentication scope.
     *
     * Any previous credentials for the given scope will be overwritten. See
     * {@link CredentialsProvider#setCredentials(AuthScope, Credentials)}. This method, once invoked, will replace any
     * credentials provider set on {@link HttpClientBuilder} when {@link #build()} method is called to build the client.
     *
     * @since 2.8
     */
    public Builder setCredentials(AuthScope authscope, Credentials credentials) {
      checkNotNull(authscope);
      credentialsProvider.setCredentials(authscope, credentials);
      credentialsProviderAltered = true;
      return this;
    }

    @VisibleForTesting
    public CredentialsProvider getCredentialsProvider() {
      return credentialsProvider;
    }

    public ConnectionConfig.Builder getConnectionConfigBuilder() {
      return connectionConfigBuilder;
    }

    public SocketConfig.Builder getSocketConfigBuilder() {
      return socketConfigBuilder;
    }

    public RequestConfig.Builder getRequestConfigBuilder() {
      return requestConfigBuilder;
    }

    // User-Agent is read-only in HttpClientBuilder so we expose this for r/w here to allow for customization

    public String getUserAgent() {
      return userAgent;
    }

    public void setUserAgent(final String userAgent) {
      this.userAgent = userAgent;
    }

    /**
     * Builds the {@link HttpClient} from current state of this builder.
     *
     * Once client is built and returned, it is immutable and thread safe (unless explicitly configured with non-thread
     * safe client connection manager). This instance might be re-used to create multiple clients, as the configuration
     * state once client is built, is detached from it.
     */
    public HttpClient build() {
      httpClientBuilder.setDefaultConnectionConfig(connectionConfigBuilder.build());
      httpClientBuilder.setDefaultSocketConfig(socketConfigBuilder.build());
      httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
      if (credentialsProviderAltered) {
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
      }

      if (userAgent != null) {
        httpClientBuilder.setUserAgent(userAgent);
      }

      return httpClientBuilder.build();
    }
  }
}
