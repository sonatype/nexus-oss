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
package org.sonatype.nexus.testsuite;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import static org.sonatype.sisu.bl.BundleConfiguration.RANDOM_PORT;

/**
 * Support for Nexus-serving-HTTPS integration tests.
 *
 * @since 2.11.1
 */
public abstract class NexusHttpsITSupport
    extends NexusRunningParametrizedITSupport
{

  private CookieStore cookieStore = new BasicCookieStore();

  public NexusHttpsITSupport(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Override
  protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration config) {
    return super.configureNexus(config).enableHttps(RANDOM_PORT, testData().resolveFile("keystore.jks"), "changeit");
  }

  protected String getSecureUrl() {
    return String.format("https://%s:%s/nexus/", nexus().getConfiguration().getHostName(), nexus().getSslPort());
  }

  protected KeyStore trustStore() throws Exception {
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try (FileInputStream instream = new FileInputStream(testData().resolveFile("truststore.jks"))) {
      trustStore.load(instream, "changeit".toCharArray());
    }
    return trustStore;
  }

  protected SSLConnectionSocketFactory sslSocketFactory() throws Exception {
    SSLContext context = SSLContexts.custom().loadTrustMaterial(trustStore(), new TrustSelfSignedStrategy()).build();
    return new SSLConnectionSocketFactory(context, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
  }

  protected Credentials credentials() {
    return new UsernamePasswordCredentials("admin", "admin123");
  }

  protected CredentialsProvider credentialsProvider() {
    String hostname = nexus().getConfiguration().getHostName();
    AuthScope scope = new AuthScope(hostname, nexus().getSslPort(), "https");
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(scope, credentials());
    return credentialsProvider;
  }

  protected HttpClientBuilder clientBuilder() throws Exception {
    HttpClientBuilder builder = HttpClients.custom();
    builder.setDefaultCookieStore(cookieStore);
    builder.setDefaultCredentialsProvider(credentialsProvider());
    builder.setSSLSocketFactory(sslSocketFactory());
    return builder;
  }

  protected AuthCache basicAuthCache() {
    String hostname = nexus().getConfiguration().getHostName();
    HttpHost host = new HttpHost(hostname, nexus().getSslPort(), "https");
    AuthCache authCache = new BasicAuthCache();
    authCache.put(host, new BasicScheme());
    return authCache;
  }

  protected HttpClientContext clientContext() {
    HttpClientContext context = HttpClientContext.create();
    context.setAuthCache(basicAuthCache());
    return context;
  }

  @Nullable
  protected Cookie getSessionCookie() {
    for (Cookie cookie : cookieStore.getCookies()) {
      if ("JSESSIONID".equals(cookie.getName())) {
        return cookie;
      }
    }
    return null;
  }
}
