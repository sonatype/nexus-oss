/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite;

import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

/**
 * Support for Nexus-serving-HTTPS integration tests.
 *
 * @since 2.11.1
 */
public abstract class NexusHttpsITSupport
    extends NexusCoreITSupport
{
  protected static final String DEFAULT_SESSION_COOKIE_NAME = "NXSESSIONID";

  /**
   * Configure Nexus with HTTPS support enabled.
   */
  @Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"), withHttps());
  }

  /**
   * Make sure Nexus is responding on the secure base URL before continuing
   */
  @Before
  public void waitForSecureNexus() throws Exception {
    waitFor(responseFrom(nexusSecureUrl));
  }

  /**
   * @return Client that can use preemptive auth and self-signed certificates
   */
  protected HttpClientBuilder clientBuilder() throws Exception {
    HttpClientBuilder builder = HttpClients.custom();
    builder.setDefaultRequestConfig(requestConfig());
    doUseCredentials(builder);
    builder.setSSLSocketFactory(sslSocketFactory());
    return builder;
  }

  protected void doUseCredentials(final HttpClientBuilder builder)
  {
    builder.setDefaultCredentialsProvider(credentialsProvider());
  }

  /**
   * @return Policy for handling cookies on client side
   */
  protected RequestConfig requestConfig() {
    return RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build();
  }

  /**
   * @return Provider of credentials for preemptive auth
   */
  protected CredentialsProvider credentialsProvider() {
    String hostname = nexusUrl.getHost();
    AuthScope scope = new AuthScope(hostname, -1);
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(scope, credentials());
    return credentialsProvider;
  }

  /**
   * @return Credentials to be used in preemptive auth
   */
  protected Credentials credentials() {
    return new UsernamePasswordCredentials("admin", "admin123");
  }

  /**
   * @return SSL socket factory that accepts self-signed certificates from any host
   */
  protected SSLConnectionSocketFactory sslSocketFactory() throws Exception {
    SSLContext context = SSLContexts.custom().loadTrustMaterial(trustStore(), new TrustSelfSignedStrategy()).build();
    return new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
  }

  /**
   * @return Client trust store containing exported Nexus certificate
   */
  protected KeyStore trustStore() throws Exception {
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try (FileInputStream instream = new FileInputStream(resolveBaseFile("src/test/it-resources/ssl/client.jks"))) {
      trustStore.load(instream, "password".toCharArray());
    }
    return trustStore;
  }

  /**
   * @return Context with preemptive auth enabled for Nexus
   */
  protected HttpClientContext clientContext() {
    HttpClientContext context = HttpClientContext.create();
    context.setAuthCache(basicAuthCache());
    return context;
  }

  /**
   * @return Cache with preemptive auth enabled for Nexus
   */
  protected AuthCache basicAuthCache() {
    String hostname = nexusUrl.getHost();
    AuthCache authCache = new BasicAuthCache();
    HttpHost hostHttp = new HttpHost(hostname, nexusUrl.getPort(), "http");
    HttpHost hostHttps = new HttpHost(hostname, nexusSecureUrl.getPort(), "https");
    authCache.put(hostHttp, new BasicScheme());
    authCache.put(hostHttps, new BasicScheme());
    return authCache;
  }

  /**
   * @return our session cookie; {@code null} if it doesn't exist
   */
  @Nullable
  protected Cookie getSessionCookie(CookieStore cookieStore) {
    for (Cookie cookie : cookieStore.getCookies()) {
      if (DEFAULT_SESSION_COOKIE_NAME.equals(cookie.getName())) {
        return cookie;
      }
    }
    return null;
  }

  /**
   * @return the header containing our session cookie; {@code null} if it doesn't exist
   */
  @Nullable
  protected Header getSessionCookieHeader(@Nonnull Header[] headers) {
    for (Header header : headers) {
      if (header.getValue().startsWith(DEFAULT_SESSION_COOKIE_NAME + "=")) {
        return header;
      }
    }
    return null;
  }

  /**
   * @return CookieOrigin suitable for validating session cookies from the given base URL
   */
  protected CookieOrigin cookieOrigin(final URL url) {
    return new CookieOrigin(url.getHost(), url.getPort(), cookiePath(url), url.getProtocol().equals("https"));
  }

  /**
   * @return the expected cookie path value of our session cookie from the given base URL
   */
  protected String cookiePath(final URL url) {
    final String path = url.getPath();
    return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
  }
}
